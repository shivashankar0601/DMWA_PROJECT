package com.example.project.QueryManager;

import com.example.project.Utilities.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.project.DistributedDatabaseLayer.Requester;

public class TableProcessor {

    private String path;
    public TableProcessor(String path) {
        this.path = path;
    }

    public static void performOperation(String query) {
        // take this method as starting point and start processing the query, after the DistributedDatabaseLayer is completed, we will have the api to be hit
        query = query.toLowerCase();
        String insertIntoRegx = "(insert\\sinto\\s[)(0-9a-zA-Z_\\s,'\"]+)[;]?"; //check if the insert query is in correct format
        String createTableRegx = "^(create\\stable\\s[)(0-9a-zA-Z_\\s,]+)[;]?$";
        if (query.matches(insertIntoRegx)) {
            insertIntoQuery(query,"local");
        }
        else if (query.matches(createTableRegx)) {
            createTableQuery(query);
        }
        else {
            System.out.println("Query is not correct");
        }
    }

    private static String insertIntoQuery(String query, String flag) {
        String tableName;
        List < String > insertValues = new ArrayList < > ();
        query = query.replaceAll("%20", " ");
        String insertIntoPattern = "((?<=(insert\\sinto\\s))[\\w\\d_]+(?=\\s+))|((?<=\\()([\\w\\d_,'\\w'\"\\w\"]+)+(?=\\)))"; //pattern to fetch table name and values to insert
        Pattern re = Pattern.compile(insertIntoPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = re.matcher(query);
        while (m.find()) {
            insertValues.add(m.group(0));
        }
        int columnCount = insertValues.get(1).split(",").length; //fetching column count
        tableName = insertValues.get(0); //fetching table name

        if (tableName != null) {
            if (checkIfTableExists(tableName)) {
                if (validateAndInsert(tableName, columnCount, insertValues)) {
                    if (flag.equals("local")) {
                        System.out.println("inserted successfully");
                    } else {
                        return "inserted successfully";
                    }
                }
            } else {
                if (flag.equals("remote")) {
                    return "table does not exist";
                } else {
                    Requester requester = Requester.getInstance();
                    String vmList = requester.requestVMDBCheck(Utils.currentDbName);
                    if (vmList.split("~").length > 1) {
                        requester.requestVMSetCurrentDbName(Utils.currentDbName);
                        String response = requester.requestVMInsertIntoQuery(query.replaceAll(" ", "%20"), "remote");
                        System.out.println(response);
                    } else {
                        System.out.println("table does not exist");
                    }
                }
            }
        } else {
            System.err.println("query is incorrect");
        }
        return "";
    }

    private static void setCurrentDbName(String currentDbName) {
        Utils.currentDbName = currentDbName;
    }

    private static Boolean validateAndInsert(String tableName, int columnCount, List < String > arr) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Utils.resourcePath + Utils.currentDbName + "/" + tableName + ".tsv"));
            String firstLine = br.readLine();
            String[] firstLineSplit = firstLine.split(Utils.delimiter);
            if (columnCount == Integer.parseInt(firstLineSplit[1])) {
                PrintWriter out = new PrintWriter(new FileWriter(Utils.resourcePath + Utils.currentDbName + "/" + tableName + ".tsv", true));
                for (int i = 1; i < arr.size(); i++) {
                    out.append("\n");
                    out.append(arr.get(i).replaceAll(",", "~").replaceAll("['\"]", ""));
                }
                out.close();
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException e) {
            System.err.println(Utils.dbMetadataNotFound);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean checkIfTableExists(String tableName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Utils.resourcePath + Utils.currentDbName + "/" + "metadata.tsv"));
            String line;
            while ((line = br.readLine()) != null && line.length() > 0) {
                String[] tableNames = line.split(Utils.delimiter);
                for (int i = 0; i < tableNames.length; i++) {
                    if (tableNames[i].equalsIgnoreCase(tableName))
                        return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(Utils.dbMetadataNotFound);
            // at least an empty metadata should be made available by the time application is created
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void createTableQuery(String query){
        String tablePath;
        String tableName;
        String output = "";
        String columnsAndValues;

        try {
            //please remove currentDbName variable added just for testing
            tablePath = Utils.currentDbName+ "/"+(query.split("\s")[2]) + ".tsv";
            Pattern p = Pattern.compile("(.+?)\\s+(.+?)\\s+(.+?)\\s+(.*)");
            Matcher m = p.matcher(query);
            if (m.find()) {
                tableName = m.group(3);
                if(!checkIfTableExists(tableName)) {

                    if (m.group(4).contains(";")) {
                        if (m.group(4).startsWith("(")) {
                            columnsAndValues = m.group(4).substring(1, m.group(4).indexOf(';') - 1);
                        } else {
                            columnsAndValues = m.group(4).substring(0, m.group(4).indexOf(';'));
                        }
                    }
                    else {
                        if (m.group(4).startsWith("(")) {
                            columnsAndValues = m.group(4).substring(1, m.group(4).length() - 1);
                        } else {
                            columnsAndValues = m.group(4).substring(0, m.group(4).length());
                        }
                    }
                    String[] columnInfo = columnsAndValues.split(",");
                    int columnCount = columnInfo.length;
                    int notColumnCount = 0;
                    for (String column : columnInfo) {
                        if (column.toLowerCase().contains("foreign key") || column.toLowerCase().contains("primary key")) {
                            notColumnCount++;
                        }
                        output += column.replaceAll("[()]", " ").replaceAll("  ", " ").trim() + "~";
                    }
                    columnCount = columnCount - notColumnCount;
                    output = "columnCount~" + columnCount + "\n" + output;

                    output = output.substring(0, output.length() - 1);
                    FileWriter fileWriter = new FileWriter(Utils.resourcePath+tablePath, true);
                    fileWriter.append(output);
                    fileWriter.close();
                    FileWriter fw = new FileWriter(Utils.resourcePath+Utils.currentDbName+"/metadata.tsv", true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw);
                    out.print(tableName+"~");
                    out.close();
                    bw.close();
                    fw.close();
                    System.out.println("Table created");

                }
                else{
                    System.out.println("Table already exists!");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
