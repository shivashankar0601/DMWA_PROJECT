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

        if (query.matches(insertIntoRegx)) {
            insertIntoQuery(query,"local");
        } else {
            System.out.println("Query is not correct");
        }
    }

    private static String insertIntoQuery(String query, String flag) {
        String tableName;
        List < String > insertValues = new ArrayList < > ();
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
                    if(flag.equals("local")) {
                        System.out.println("inserted successfully");
                    } else {
                        return "inserted successfully";
                    }
                }
            } else {
                if(flag.equals("remote")) {
                    return "table does not exist";
                }
                else {
                    System.out.println("call vm2");
                    //check GDD
                    //if present:
                        //requestVMSetCurrentDbName(Utils.currentDbName)
                        //String response = requestVMInsertIntoQuery(query, "remote");
                        //System.out.println(response);
                    //else:
                        //System.out.println("table does not exist");
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
}
