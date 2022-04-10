package com.example.project.DataExport;

import com.example.project.DistributedDatabaseLayer.Requester;
import com.example.project.QueryManager.DatabaseProcessor;
import com.example.project.UIAndSecurity.UserCredentials;
import com.example.project.Utilities.Utils;
import org.apache.tomcat.util.buf.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExportEngine {
    private UserCredentials user = null;
    private BufferedReader input = null;
    private String path = null;

    public ExportEngine(BufferedReader input, UserCredentials currentUser, String path) {
        this.user = currentUser;
        this.input = input;
        this.path = path;
    }

    List<String> dbs = null;

    // write the whole logic of exporting taking this method as a starting point
    public void begin() {
        String ipt = null;
        try {
            dbs = getAllAvailableDBs();

            if (dbs.size() == 0) {
                System.err.println("no databases available to export");
                return;
            }

            do {
                System.out.println("\nAvailable databases for data export :");

                for (String s : dbs) {
                    System.out.println(s);
                }

                System.out.print("Enter the name of the database to be exported (press 0 to exit):");

                ipt = input.readLine();

                if (ipt.equalsIgnoreCase("0"))
                    break;

                if (dbs.contains(ipt.trim())) {

                    String tables = getAllAvailableTables(ipt, true);
                    if (tables == null || tables.length() == 0) {
                        // which means no tables in the db, what should i do
                        System.err.println("no tables in the database to export");
                        try {
                            TimeUnit.SECONDS.sleep(1);
                            break;
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    } else {
                        // perform the data export operation

                        if (createExportFile(Arrays.asList(tables.split(Utils.delimiter)), ipt.trim())) {
                            File cPath = new File(".");
//                            System.out.println(cPath.getCanonicalPath());
                            System.out.println(ipt.trim() + " Database exported successfully to " + cPath.getCanonicalPath() + "\\" + ipt.trim() + "_exported_data.sql");
                        } else {
                            // should write to logs that there was an error
                            System.err.println(ipt.trim() + " Database export failed, for more information on the problem check logs");
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
            e.printStackTrace();
        }


    }

    private boolean createExportFile(List<String> tables, String dbName) {
        boolean status = false;
        try {
            FileWriter export = new FileWriter(dbName + "_exported_data.sql");

            for (String table : tables) {
                String[] data = readTableData(table, dbName);
                export.append(data[0] + "\n");
                export.append(data[1] + "\n");
            }

            export.flush();
            export.close();
            status = true;

        } catch (IOException e) {
            e.printStackTrace();
            status = false;
        }

        return status;
    }

    public static String[] readTableData(String table, String dbName) {
        String[] data = new String[2]; // create statement and the data in one line

        if (DatabaseProcessor.checkIsLocalDB(dbName) && isLocalTable(table, dbName)) {
            try {
                String path = Utils.resourcePath + dbName + "/" + table + ".tsv";
                BufferedReader br = new BufferedReader(new FileReader(path));
                // for create query

                data[0] = br.readLine(); // check with others if they are ok with this

                br.readLine();
                br.readLine();
                String line = null;
                // for all values
                StringBuilder sb = new StringBuilder();


                sb.append("insert into " + table + " values ");
                while ((line = br.readLine()) != null) {
                    sb.append("(");
                    if (line.contains(Utils.delimiter)) {

                        String splits[] = line.split("~");
                        for (int i = 0; i < splits.length; i++) {
                            try {
                                sb.append(Integer.parseInt(splits[i]));
                            } catch (Exception e) {
                                if (!splits[i].contains("'"))
                                    sb.append("'" + splits[i] + "'");
                                else
                                    sb.append(splits[i]);
                            }
                            if (i != splits.length - 1)
                                sb.append(",");
                        }
                    } else {
                        sb.append(line);
                    }
                    sb.append("),");
                }
                // when there is no data in the tables, i mean no rows in the table (empty table)
                data[1] = sb.toString().equalsIgnoreCase("insert into " + table + " values ") ? "" : sb.toString().substring(0, sb.length() - 1) + ";";
            } catch (Exception e) {

            }
        } else {
            data = Requester.getInstance().requestVMWholeTable(table, dbName);
        }


        return data;
    }

    public static boolean isLocalTable(String table, String dbName) {
        boolean res = false;
        File file = new File(Utils.resourcePath + dbName + "/" + table + ".tsv");
        if (file.exists())
            res = true;
        return res;
    }

    public static String getAllAvailableTables(String dbName, boolean shouldRequestVM) {
        ArrayList<String> tables = new ArrayList<String>();

        // check if the db exists locally as we are not storing the information from previous requests
        String path = Utils.resourcePath + dbName;
        File f = new File(path);
        if (f.exists()) { // db exists locally
            path = path + "/";
            File tbls[] = f.listFiles();
            for (File table : tbls) {
                String tname = table.getName();
                tname = tname.substring(0, tname.indexOf(".tsv"));
                if (tname.equalsIgnoreCase("metadata"))
                    continue;
                tables.add(tname);
                //System.out.println(tname);
            }
        }

        String response = StringUtils.join(tables, Utils.delimiter.charAt(0));

        if (shouldRequestVM) {
            // ask vm to give the tables available over there
            String res = Requester.getInstance().requestVMAllTables(dbName, !shouldRequestVM);
            // returning empty string from server is causing null pointer exception in html response so returning none
            if (res != null && res.length() > 0) {
                response = response.length() > 0 ? response.concat(Utils.delimiter + res) : res;
                return response;
            }
        }
        // if it is a request from other vm, we will just send them the local information
        return response;
    }

    public static List<String> getAllAvailableDBs() {

        if (Utils.gddExists) {

            ArrayList<String> dbs = new ArrayList<String>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(Utils.resourcePath + "gdd.tsv"));
                String line = null;
                while ((line = br.readLine()) != null && line.trim().length() != 0) {
                    String db = line.split(Utils.delimiter)[0];
                    // if there are two databases with the same name, we will show only one as we will have same database over two Vm's
                    if (!dbs.contains(db))
                        dbs.add(db);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dbs;
        } else {
            // request vm to get the list of dbs
            String res[] = (Requester.getInstance().requestVMForDBs()).split(Utils.delimiter);
            return Arrays.asList(res);
        }
    }
}
