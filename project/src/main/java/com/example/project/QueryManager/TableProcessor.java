package com.example.project.QueryManager;

import com.example.project.Utilities.Utils;

import java.io.*;
import java.util.*;
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
        } else if(query.contains("update")){
            updateQuery(query,"local");
        } else if(query.contains("delete")){
            deleteQuery(query,"local");
        }else {
            System.out.println("Query is not correct");
        }
    }

    private static String deleteQuery(String query, String flag) {
        try {
            String[] deleteQueryArray = query.split(" ");
            String queryTableName = deleteQueryArray[2];

            String afterWhere = query.substring(query.indexOf("where") + 6, query.indexOf(";") == -1 ? query.length() : query.length() - 1);

            // Variable for conditions after where Clause
            String[] condition = afterWhere.replace("\"","").split("=");

            BufferedReader br = new BufferedReader(
                    new FileReader(Utils.resourcePath + Utils.currentDbName + "/metadata.tsv"));
            String metadata = "";
            String st = "";
            while ((st = br.readLine()) != null) {
                metadata += st;
            }
            String[] metaDataTables = metadata.split("~");

            // If Metadata contains the table which is in query.
            if (checkIfTableExists(queryTableName)) {
                BufferedReader br2 = new BufferedReader(
                        new FileReader(Utils.resourcePath + Utils.currentDbName + "/" +
                                queryTableName+ ".tsv"));
                ArrayList<String> tabledata = new ArrayList<String>();
                String st2 = "";

                // Adding data to arraylist line by line.
                while ((st2 = br2.readLine()) != null) {
                    tabledata.add(st2);
                }
                br2.close();
                Integer columnCount = 0;
                ArrayList<String[]> columnParameter = new ArrayList();
                ArrayList<String []> valueList = new ArrayList();
                ArrayList<LinkedHashMap> table=new ArrayList<>();
                for (int counter = 0; counter < tabledata.size(); counter++) {
                    if (counter == 0) {
                        // Helpful with further processing
                        columnCount = Integer.parseInt(tabledata.get(counter).split("~")[1]);
                    } else if (counter == 1) {
                        // Adding the column names row to separate ArrayList.
                        String[] localSplitCol = tabledata.get(counter).split("~");
                        for (int i = 0; i < localSplitCol.length; i++) {
                            String[] localColumnParameter = localSplitCol[i].split(" ");
                            columnParameter.add(localColumnParameter);
                        }
                    } else {
                        // Adding all the values of data to ArrayList
                        valueList.add(tabledata.get(counter).split("~"));
                    }
                }

                // Creating a separate variable only for columns names
                ArrayList<String> columns = new ArrayList<String>();
                int j = 0;
                for (String[] i : columnParameter) {
                    if (j < columnCount) {
                        columns.add(i[0]);
                    }
                }

                if(!columns.contains(condition[0].toLowerCase())){
                    throw new Exception("Table "+queryTableName+" does not contain column named '"
                            +condition[0]+"'.");
                }

                // Adding all the values to HashMap to get key-value format between column name and value.
                for(int i=0;i<valueList.size();i++) {
                    LinkedHashMap<String,String> hm=new LinkedHashMap<>();
                    for(int m=0;m<valueList.get(i).length;m++){
                        hm.put(columnParameter.get(m)[0].toLowerCase(), valueList.get(i)[m].toString());
                    }
                    table.add(hm);
                }
                // Helpful for analytics
                int affectedRows= 0;

                for(int i=0;i< table.size();i++){
                    // If query conditions are fulfilled, removing the row
                    if(table.get(i).containsKey(condition[0]) && table.get(i).containsValue(condition[1])){
                        table.remove(i);
                        affectedRows++;
                    }
                }

                //  Writing to a file
                try {
                    FileWriter file = new FileWriter(Utils.resourcePath+Utils.currentDbName+"/"+queryTableName.toLowerCase()+".tsv", false);
                    PrintWriter writer=new PrintWriter(file);
                    writer.append(tabledata.get(0));
                    writer.append("\n");
                    writer.append(tabledata.get(1));
                    String final_str="";
                    int val;

                    for(int i=0;i< table.size();i++){
                        Iterator<String> iterhm=table.get(i).keySet().iterator();
                        val=0;
                        String local_str="";
                        writer.append("\n");
                        while (iterhm.hasNext()) {
                            String key=iterhm.next();
                            String value=table.get(i).get(key).toString();
                            if(val==0){
                                local_str+=value;
                                final_str+=value;
                                val++;
                            } else {
                                local_str+="~"+value;
                                final_str+="~"+value;
                            }
                        }
                        writer.append(local_str);
                        final_str+="\n";
                    }
                    writer.close();
                    if(flag.equals("local")){
                        System.out.println(affectedRows+" rows affected");
                    } else
                        return affectedRows+" rows affected";

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if(flag.equals("remote")){
                    return "Table '" + queryTableName + "' does not exist in " + Utils.currentDbName;
                } else {
                    // Call Another VM
                }
            }
        } catch(Exception e){
            System.out.println("Exception: "+e);
        }
        return "";
    }

    private static String updateQuery(String query, String flag) {
        try {
            String[] updateQueryArray = query.split(" ");
            String queryTableName = updateQueryArray[1];

            // Contains the list of [column,new-value] that needs to be updated
            ArrayList<ArrayList> keyVal = new ArrayList();
            String keyValQuery = query.substring(query.indexOf("set ") + 4, query.indexOf(" where "));
            String afterWhere = query.substring(query.indexOf("where") + 6, query.indexOf(";") == -1 ? query.length() : query.length() - 1);

            // Variable for conditions after where Clause
            String[] condition = afterWhere.replace("\"","").split("=");
            String[] KV = keyValQuery.split(",");

            // Parsing and adding [column,new-value] to list
            for (int i = 0; i < KV.length; i++) {
                ArrayList<String> newl = new ArrayList();
                newl.add(KV[i].substring(0, KV[i].indexOf("=")).trim());
                newl.add(KV[i].substring(KV[i].indexOf("=") + 1, KV[i].length()).trim().replace("\"",""));
                keyVal.add(newl);
            }

            if (checkIfTableExists(queryTableName)) {
                BufferedReader br2 = new BufferedReader(
                        new FileReader(Utils.resourcePath + Utils.currentDbName + "/" +
                                queryTableName + ".tsv"));
                ArrayList<String> tabledata = new ArrayList<String>();
                String st2 = "";

                // Adding data to arraylist line by line.
                while ((st2 = br2.readLine()) != null) {
                    tabledata.add(st2);
                }
                br2.close();
                Integer columnCount = 0;
                ArrayList<String[]> columnParameter = new ArrayList();
                ArrayList<String []> valueList = new ArrayList();
                ArrayList<LinkedHashMap> table=new ArrayList<>();
                for (int counter = 0; counter < tabledata.size(); counter++) {
                    if (counter == 0) {
                        // Helpful with further processing
                        columnCount = Integer.parseInt(tabledata.get(counter).split("~")[1]);
                    } else if (counter == 1) {
                        // Adding the column names row to separate ArrayList.
                        String[] localSplitCol = tabledata.get(counter).split("~");
                        for (int i = 0; i < localSplitCol.length; i++) {
                            String[] localColumnParameter = localSplitCol[i].split(" ");
                            columnParameter.add(localColumnParameter);
                        }
                    } else {
                        // Adding all the values of data to ArrayList
                        valueList.add(tabledata.get(counter).split("~"));
                    }
                }

                // Creating a separate variable only for columns names
                ArrayList<String> columns = new ArrayList<String>();
                int j = 0;
                for (String[] i : columnParameter) {
                    if (j < columnCount) {
                        columns.add(i[0]);
                    }
                }

                // Iterating to see whether columns specified in query are present in table or not.
                Iterator<ArrayList> iter = keyVal.iterator();
                while (iter.hasNext()) {
                    if (!columns.contains(iter.next().get(0).toString())) {
                        throw new Exception("Table "+queryTableName+" does not contain column named '"
                                +iter.next().get(0).toString()+"'.");
                    }
                }
                if(!columns.contains(condition[0].toLowerCase())){
                    throw new Exception("Table "+queryTableName+" does not contain column named '"
                            +condition[0]+"'.");
                }

                // Adding all the values to HashMap to get key-value format between column name and value.
                for(int i=0;i<valueList.size();i++) {
                    LinkedHashMap<String,String> hm=new LinkedHashMap<>();
                    for(int m=0;m<valueList.get(i).length;m++){
                        hm.put(columnParameter.get(m)[0].toLowerCase(), valueList.get(i)[m].toString());
                    }
                    table.add(hm);
                }
                // Helpful for analytics
                int affectedRows= 0;

                for(int i=0;i< table.size();i++){
                    // If query conditions are fulfilled, replacing old data with new one
                    if(table.get(i).containsKey(condition[0]) && table.get(i).containsValue(condition[1])){
                        for(int iter1=0;iter1< keyVal.size();iter1++){
                            table.get(i).replace(keyVal.get(iter1).get(0),keyVal.get(iter1).get(1));
                        }
                        affectedRows++;
                    }
                }

                //  Writing to a file
                try {
                    FileWriter file = new FileWriter(Utils.resourcePath+Utils.currentDbName+"/"+queryTableName.toLowerCase()+".tsv", false);
                    PrintWriter writer=new PrintWriter(file);
                    writer.append(tabledata.get(0));
                    writer.append("\n");
                    writer.append(tabledata.get(1));
                    String final_str="";
                    int val;

                    for(int i=0;i< table.size();i++){
                        Iterator<String> iterhm=table.get(i).keySet().iterator();
                        val=0;
                        String local_str="";
                        writer.append("\n");
                        while (iterhm.hasNext()) {
                            String key=iterhm.next();
                            String value=table.get(i).get(key).toString();
                            if(val==0){
                                local_str+=value;
                                final_str+=value;
                                val++;
                            } else {
                                local_str+="~"+value;
                                final_str+="~"+value;
                            }
                        }
                        writer.append(local_str);
                        final_str+="\n";
                    }
                    writer.close();
                    if(flag.equals("local")){
                        System.out.println(affectedRows+" rows affected");
                    } else
                        return affectedRows+" rows affected";

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if(flag.equals("remote")){
                    return "Table '" + queryTableName + "' does not exist in " + Utils.currentDbName;
                } else {
                    // Call Another VM
                }
            }
        } catch(Exception e){
            System.out.println("Exception: "+e);
        }
        return "";
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
