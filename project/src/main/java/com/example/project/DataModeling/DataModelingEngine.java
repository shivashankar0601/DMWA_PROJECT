package com.example.project.DataModeling;

import com.example.project.DataExport.ExportEngine;
import com.example.project.LogManager.LogManager;
import com.example.project.DistributedDatabaseLayer.Requester;
import com.example.project.UIAndSecurity.UserCredentials;
import com.example.project.Utilities.Utils;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataModelingEngine {
    private BufferedReader input = null;
    private UserCredentials currentUser = null;
    private String path = null;
    
    LogManager logManager = new LogManager();

    public DataModelingEngine(BufferedReader input, UserCredentials currentUser, String path) {
        this.input = input;
        this.currentUser = currentUser;
        this.path = path;
    }

    List<String> dbs = null;

    private String allAvailableTables = null;

    public void begin() {
        String ipt = null;
        try {
            dbs = ExportEngine.getAllAvailableDBs();

            if (dbs.size() == 0) {
                System.err.println("no databases available to modeling");
                return;
            }

            do {
                System.out.println("\nAvailable databases for data modeling :");

                for (String s : dbs) {
                    System.out.println(s);
                }

                System.out.print("Enter the name of the database to be modeled (press 0 to exit):");

                ipt = input.readLine();

                if (ipt.equalsIgnoreCase("0"))
                    break;

                if (dbs.contains(ipt.trim())) {

                    allAvailableTables = ExportEngine.getAllAvailableTables(ipt, true);
                    if (allAvailableTables == null || allAvailableTables.length() == 0) {
                        System.err.println("no tables in the database for modeling");
                        try {
                            TimeUnit.SECONDS.sleep(1);
                            break;
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                            logManager.writeCrashReportsToEventLogs(e.getMessage());
                        }
                    } else {
                        if (startDataModeling(ipt.trim())) {
                            File cPath = new File(".");
                            System.out.println(ipt.trim() + " Database modeled successfully and saved to " + cPath.getCanonicalPath() + "\\" + ipt.trim() + "_model.txt");
                        } else {
                            // write the error messages to logs
                            System.err.println("There was an error modeling " + ipt.trim() + " database, for more information on the problem check logs");
                        }

                    }

                } else {
                    System.err.println("invalid option, try again ! ");
                    TimeUnit.SECONDS.sleep(1);
                }

            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            logManager.writeCrashReportsToEventLogs(e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean startDataModeling(String dbName) throws IOException {
        boolean result = false;
        try {
            String response = allAvailableTables;
            List<String> tables = Arrays.asList(response.split(Utils.delimiter));
            FileWriter dataModel = new FileWriter(dbName + "_model.txt");
            dataModel.write("Database:" + dbName + "\n");
            dataModel.write("------------------------------------------------------------------\n\n\n");
            for (String s : tables) {
                if (!s.equalsIgnoreCase("metadata.tsv"))
                    dataModel.write(getTableStructure(s, dbName) + "\n");
            }
            dataModel.flush();
            dataModel.close();
            result = true;
        } catch (Exception e) {

        }
        return result;
    }

    public static String getTableStructure(String s, String dbName) {
        try {
            if (ExportEngine.isLocalTable(s, dbName)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Table:" + s + "\n");
                sb.append("------------------------------------------------------------------\n");
                sb.append(String.format("%20s %20s %20s\r\n", "Column name", "Data type", "Column length"));
                BufferedReader br = new BufferedReader(new FileReader(Utils.resourcePath + dbName + "/" + s + ".tsv"));
                String line = br.readLine();// we ignore the structure
                line = br.readLine();// we ignore the second line as well, since we do not have anything to do with the column counts
                if ((line = br.readLine()) != null) {
                    String[] columnNames = line.split(Utils.delimiter);
                    for (String cols : columnNames) {
                        String[] col = cols.split(" ");
                        if (!col[0].startsWith("primary") && !col[0].startsWith("foreign"))
                            sb.append(String.format("%20s %20s %20s\r\n", col[0], col[1], col.length == 3 ? col[2] : "")); // you can add the length of the data type here
                    }
                    String format = String.format("------------------------------------------------------------------\n%20s %20s \r\n", "key", "Column");
                    String keys = format;
                    String cardinality = "";
                    String tFormat = String.format("%15s %15s        %15s %15s\r\n", "table", "column", "table", "column");
                    String cFormat = "%15s %15s (n) -> %15s %15s (1)\r\n";
                    for (String cols : columnNames) {
                        String[] col = cols.split(" ");
                        if (col[0].startsWith("primary") || col[0].startsWith("foreign")) {
                            keys += String.format("%20s %20s \r\n", col[0] + " " + col[1], col[2]);
                            if (col.length == 6) // foreign key (column_name) references table_name (column_name)
                                cardinality += String.format(cFormat, s, col[2], col[4], col[5]);

                        }
                    }
                    if (!keys.equalsIgnoreCase(format))
                        sb.append(keys);
                    if (cardinality.length() > 0) {
                        sb.append("------------------------------------------------------------------\n");
                        sb.append(tFormat + "" + cardinality);
                    }

                }
                sb.append("------------------------------------------------------------------\n");
                return sb.toString();
            } else {
                // request vm to get the data
                return Requester.getInstance().requestVMTableStructure(dbName, s);
            }
        } catch (Exception e) {
            // should write the error to log
            System.err.println(e.getLocalizedMessage());
        }
        return "";
    }
}