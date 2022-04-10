package com.example.project.QueryManager;

import com.example.project.LogManager.LogManager;
import com.example.project.Utilities.Utils;

import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.project.DistributedDatabaseLayer.Requester;

import static java.lang.Thread.sleep;

public class TableProcessor {

    private String path;
    private static String query;

    private static LogManager logManager = new LogManager();

    public TableProcessor(String path) {
        this.path = path;
    }

    public Boolean performOperation(String query, Boolean isTransaction) {
        this.query = query;
        // take this method as starting point and start processing the query, after the DistributedDatabaseLayer is completed, we will have the api to be hit
        query = query.toLowerCase();
        String insertIntoRegx = "(insert\\sinto\\s[)(0-9a-zA-Z_\\s,'\"]+)[;]?"; //check if the insert query is in correct format
        String createTableRegx = "^(create\\stable\\s[)(0-9a-zA-Z_\\s,]+)[;]?$";
        String selectTableReg = "^((select)\\s([*]?|[0-9a-zA-Z_ ,]+)\\s(from)\\s([0-9a-zA-Z _'=]+)(\\s(where)\\s([0-9a-zA-Z _'\\\\\"=]+))?)[;]?$";
        String updateTableRegx = "^(update\\s[a-zA-Z][0-9A-Za-z_]+\\sset\\s[a-zA-Z0-9\\\"'\\s$&+,:;=?@#|'<>.^()%!-]+\\swhere\\s[a-zA-Z0-9\\\"'\\s]+=[a-zA-Z0-9\\\"'\\s_|!@#$%^&*)(-.,:]+)[;]?$";
        String deleteTableRegx = "^(delete from\\s[a-zA-Z][0-9A-Za-z_]+\\swhere\\s[a-zA-Z][a-zA-Z0-9]+=[a-zA-Z0-9\\s_\\\"\\'\\-$#@!]+)[;]?$";
        String flag = "";
        if (Utils.isVMRequest) {
            flag = "remote";
        } else {
            flag = "local";
        }

        if (query.matches(insertIntoRegx)) {
            String isValid = insertIntoQuery(query,flag,isTransaction);
            if(isValid.equals("invalid")) {
                return false;
            }
        }
        else if (query.matches(createTableRegx)) {
            createTableQuery(query);
        }
        else if(query.matches(updateTableRegx)) {
            String isValid=updateQuery(query,flag,isTransaction);
            if(isValid.equals("invalid")) {
                return false;
            }
        }
        else if(query.matches(deleteTableRegx)) {
            String isValid=deleteQuery(query,flag,isTransaction);
            if(isValid.equals("invalid")) {
                return false;
            }
        }
        else if (query.matches(selectTableReg)) {
            selectQuery(query,flag);
        }
        else {
            System.out.println("Query is not correct");
        }
        return true;
    }

    public static String insertIntoQuery(String query, String flag, Boolean isTransaction) {
        String tableName;
        List < String > insertValues = new ArrayList < > ();
        query = query.replaceAll("%20", " ");
        String insertIntoPattern = "((?<=(insert\\sinto\\s))[\\w\\d_]+(?=\\s+))|((?<=\\()([\\w\\d_,'\\w\\s'\"\\w\\s\"]+)+(?=\\)))"; //pattern to fetch table name and values to insert
        Pattern re = Pattern.compile(insertIntoPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = re.matcher(query);
        while (m.find()) {
            insertValues.add(m.group(0));
        }
        int columnCount = insertValues.get(1).split(",").length; //fetching column count
        tableName = insertValues.get(0); //fetching table name

        if (tableName != null) {
            if (checkIfTableExists(tableName)) {

                while(Utils.tableLocked.contains(tableName)) {
                    try {
                        System.out.println("Waiting for the lock to get released...");
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Utils.tableLocked.add(tableName);

                if (validateValues(tableName, columnCount, insertValues, isTransaction)) {
                    if (flag.equals("local") && !isTransaction) {
                        System.out.println("inserted successfully");
                        if(Utils.tableLocked.contains(tableName))
                            Utils.tableLocked.remove(tableName);
                    } else if(flag.equals("remote") && !isTransaction){
                        if(Utils.tableLocked.contains(tableName))
                            Utils.tableLocked.remove(tableName);
                        return "success";
                    } else {
                        return "inserted successfully";
                    }
                }  else {
                    System.err.println("Column length is incorrect");
                    if(Utils.tableLocked.contains(tableName))
                        Utils.tableLocked.remove(tableName);
                    return "invalid";
                }
            } else {
                if (flag.equals("remote")) {
                    return "table does not exist";
                } else {
                    Requester requester = Requester.getInstance();
                    String vmList = requester.requestVMDBCheck(Utils.currentDbName);
                    if (vmList.split(Utils.delimiter).length > 1 || !vmList.equals(Utils.currentDevice)) {
                        requester.requestVMSetCurrentDbName(Utils.currentDbName);
                        String response = requester.requestVMInsertQuery(query.replaceAll(" ", "%20"), "remote", isTransaction);
                        if(response.equals("invalid")) {
                            return "invalid";
                        } else if(!response.equals("invalid") && response.equals("inserted successfully")){
                            Utils.transQueryList.add(query);
                        }else {
                            System.out.println(response);
                        }
                    } else {
                        System.out.println("table does not exist");
                        return "invalid";
                    }
                }
            }
        } else {
            System.err.println("query is incorrect");
            return "invalid";
        }
        return "";
    }

    public static void setCurrentDbName(String currentDbName) {
        Utils.currentDbName = currentDbName;
    }

    public static Boolean validateValues(String tableName, int columnCount, List<String> arr, Boolean isTransaction) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Utils.resourcePath + Utils.currentDbName + "/" + tableName + ".tsv"));
            br.readLine();// since i added the query in the first line, i am reading an empty line to not cause any discrepancies
            String firstLine = br.readLine();
            String[] firstLineSplit = firstLine.split(Utils.delimiter);
            if (columnCount == Integer.parseInt(firstLineSplit[1])) {
                if (!isTransaction) {
                    insertValues(tableName, arr);
                    logManager.writeEventLog("insert", tableName);
                } else {
                    Utils.transQueryList.add(query);
                }
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException e) {
            logManager.writeCrashReportsToEventLogs(e.getMessage());
            System.err.println(Utils.dbMetadataNotFound);
        } catch (IOException e) {
            logManager.writeCrashReportsToEventLogs(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static void insertValues(String tableName, List<String> arr) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(Utils.resourcePath + Utils.currentDbName + "/" + tableName + ".tsv", true));
        for (int i = 1; i < arr.size(); i++) {
            out.append("\n");
            out.append(arr.get(i).replaceAll(",", "~").replaceAll("['\"]", ""));
        }
        out.close();
        BufferedReader br3 = new BufferedReader(
                new FileReader(Utils.resourcePath + "tableAnalysis.tsv"));
        ArrayList<String> tableAnalysis = new ArrayList<String>();
        String st3 = "";

        // Adding data to arraylist line by line.
        while ((st3 = br3.readLine()) != null) {
            tableAnalysis.add(st3);
        }
        br3.close();

        FileWriter fw1=new FileWriter(Utils.resourcePath+"tableAnalysis.tsv", false);
        BufferedWriter bw1 = new BufferedWriter(fw1);
        PrintWriter out1 = new PrintWriter(bw1);

        String writeAnalysis="";
        for(int i=0;i<tableAnalysis.size();i++){
            writeAnalysis="";
            String[] findAndUpdate=tableAnalysis.get(i).split(Utils.delimiter);
            if(findAndUpdate[0].equals(Utils.currentDbName) && findAndUpdate[1].equals(tableName)){
                Integer insertCount=Integer.parseInt(findAndUpdate[3])+1;
                writeAnalysis+=findAndUpdate[0]+"~"+findAndUpdate[1]+"~insert~"+insertCount+"~update~"
                        +findAndUpdate[5]+"~delete~"+findAndUpdate[7];
                out1.println(writeAnalysis);
            } else {
                writeAnalysis+=tableAnalysis.get(i);
                out1.println(writeAnalysis);
            }
        }
        out1.close();
        bw1.close();
        fw1.close();
    }

    public static Boolean checkIfTableExists(String tableName) {
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
            //System.err.println(Utils.dbMetadataNotFound);
            logManager.writeCrashReportsToEventLogs(e.getMessage());
            // at least an empty metadata should be made available by the time application is created
            return false;
        } catch (IOException e) {
            logManager.writeCrashReportsToEventLogs(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static void createTableQuery(String query){
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
                    output = query+"\ncolumnCount~" + columnCount + "\n" + output;

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
                    FileWriter fw1=new FileWriter(Utils.resourcePath+"tableAnalysis.tsv", true);
                    BufferedWriter bw1 = new BufferedWriter(fw1);
                    PrintWriter out1 = new PrintWriter(bw1);
                    String insertAnalysis=Utils.currentDbName+"~"+ tableName+"~insert~0~update~0~delete~0";
                    out1.println(insertAnalysis);
                    out1.close();
                    bw1.close();
                    fw1.close();
                    System.out.println("Table created");
                    logManager.writeEventLog("create table", tableName);
                }
                else{
                    System.out.println("Table already exists!");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String deleteQuery(String query, String flag, Boolean isTransaction) {
        try {
            query = query.replaceAll("%20", " ");
            String[] deleteQueryArray = query.split(" ");
            String queryTableName = deleteQueryArray[2];

            String afterWhere = query.substring(query.indexOf("where") + 6, query.indexOf(";") == -1 ? query.length() : query.length() - 1);

            // Variable for conditions after where Clause
            String[] condition = afterWhere.replace("\"","").replace("'","").split("=");

            // If Metadata contains the table which is in query.
            if (checkIfTableExists(queryTableName)) {
                BufferedReader br2 = new BufferedReader(
                        new FileReader(Utils.resourcePath + Utils.currentDbName + "/" +
                                queryTableName+ ".tsv"));
                ArrayList<String> tabledata = new ArrayList<String>();
                String st2 = "";

                br2.readLine();// ignore the very first line, so i am reading and ignoring it

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

                if(!columns.contains(condition[0].trim().toLowerCase())){
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
                    if(table.get(i).containsKey(condition[0].trim()) && table.get(i).containsValue(condition[1].trim())){
                        table.remove(i);
                        affectedRows++;
                    }
                }
                if(isTransaction && flag.equals("local")){
                    Utils.transQueryList.add(query);
                } else if(isTransaction && flag.equals("remote")){
                    return "deleted successfully";
                } else {
                //  Writing to a file
                try {
                    FileWriter file = new FileWriter(Utils.resourcePath+Utils.currentDbName+"/"+queryTableName.toLowerCase()+".tsv", false);
                    PrintWriter writer=new PrintWriter(file);
                    writer.append(tabledata.get(0));
                    writer.append("\n");
                    writer.append(tabledata.get(1));
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
                                val++;
                            } else {
                                local_str+="~"+value;
                            }
                        }
                        writer.append(local_str);
                    }
                    writer.close();

                    BufferedReader br3 = new BufferedReader(
                            new FileReader(Utils.resourcePath + "tableAnalysis.tsv"));
                    ArrayList<String> tableAnalysis = new ArrayList<String>();
                    String st3 = "";

                    // Adding data to arraylist line by line.
                    while ((st3 = br3.readLine()) != null) {
                        tableAnalysis.add(st3);
                    }
                    br3.close();

                    FileWriter fw1=new FileWriter(Utils.resourcePath+"tableAnalysis.tsv", false);
                    BufferedWriter bw1 = new BufferedWriter(fw1);
                    PrintWriter out1 = new PrintWriter(bw1);

                    String writeAnalysis="";
                    for(int i=0;i<tableAnalysis.size();i++){
                        writeAnalysis="";
                        String[] findAndUpdate=tableAnalysis.get(i).split(Utils.delimiter);
                        if(findAndUpdate[0].equals(Utils.currentDbName) && findAndUpdate[1].equals(queryTableName)){
                            Integer deleteCount=Integer.parseInt(findAndUpdate[7])+1;
                            writeAnalysis+=findAndUpdate[0]+"~"+findAndUpdate[1]+"~insert~"+findAndUpdate[3]+"~update~"
                                    +findAndUpdate[5]+"~delete~"+deleteCount;
                            out1.println(writeAnalysis);
                        } else {
                            writeAnalysis+=tableAnalysis.get(i);
                            out1.println(writeAnalysis);
                        }
                    }
                    out1.close();
                    bw1.close();
                    fw1.close();

                    logManager.writeEventLog("delete", queryTableName);
                    if(flag.equals("local")){
                        System.out.println(affectedRows+" rows affected");
                    } else
                        return affectedRows+" rows affected";
                } catch (Exception e) {
                    logManager.writeCrashReportsToEventLogs(e.getMessage());
                    e.printStackTrace();
                }
                }
            } else {
                if(flag.equals("remote")){
                    return "Table '" + queryTableName + "' does not exist in " + Utils.currentDbName;
                } else {
                    // Call Another VM
                    Requester requester = Requester.getInstance();
                    String vmList = requester.requestVMDBCheck(Utils.currentDbName);
                    if (vmList.split("~").length > 1 || !vmList.equals(Utils.currentDevice)) {
                        requester.requestVMSetCurrentDbName(Utils.currentDbName);
                        String response = requester.requestVMDeleteQuery(query.replaceAll(" ", "%20"), "remote", isTransaction);
                        System.out.println(response);
                        if(response.equals("invalid")){
                            return "invalid";
                        } else if(!response.equals("invalid") && response.equals("deleted successfully")){
                            Utils.transQueryList.add(query);
                        }
                        return response;
                    } else {
                        if(isTransaction){
                            return "invalid";
                        }
                        System.out.println("Table does not exist");
                    }
                }
            }
        } catch(Exception e){
            logManager.writeCrashReportsToEventLogs(e.getMessage());
            System.out.println("Exception: "+e);
            if(isTransaction){
                return "invalid";
            }
            return "Exception: "+e;
        }
        return "";
    }

    public static String updateQuery(String query, String flag, Boolean isTransaction) {
        try {
            query = query.replaceAll("%20", " ");
            String[] updateQueryArray = query.split(" ");
            String queryTableName = updateQueryArray[1];

            // Contains the list of [column,new-value] that needs to be updated
            ArrayList<ArrayList> keyVal = new ArrayList();
            String keyValQuery = query.substring(query.indexOf("set ") + 4, query.indexOf(" where "));
            String afterWhere = query.substring(query.indexOf("where") + 6, query.indexOf(";") == -1 ? query.length() : query.length() - 1);

            // Variable for conditions after where Clause
            String[] condition = afterWhere.replace("\"","").replace("'","").split("=");
            String[] KV = keyValQuery.split(",");

            // Parsing and adding [column,new-value] to list
            for (int i = 0; i < KV.length; i++) {
                ArrayList<String> newl = new ArrayList();
                newl.add(KV[i].substring(0, KV[i].indexOf("=")).trim());
                newl.add(KV[i].substring(KV[i].indexOf("=") + 1, KV[i].length()).trim().replace("\"","").replace("'",""));
                keyVal.add(newl);
            }

            if (checkIfTableExists(queryTableName)) {
                BufferedReader br2 = new BufferedReader(
                        new FileReader(Utils.resourcePath + Utils.currentDbName + "/" +
                                queryTableName + ".tsv"));
                ArrayList<String> tabledata = new ArrayList<String>();
                String st2 = "";

                br2.readLine();// reading a line to ignore the structure of the query

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
                if(!columns.contains(condition[0].trim().toLowerCase())){
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
                    if(table.get(i).containsKey(condition[0].trim()) && table.get(i).containsValue(condition[1].trim())){
                        for(int iter1=0;iter1< keyVal.size();iter1++){
                            table.get(i).replace(keyVal.get(iter1).get(0),keyVal.get(iter1).get(1));
                        }
                        affectedRows++;
                    }
                }

                if(isTransaction && flag.equals("local")) {
                    Utils.transQueryList.add(query);
                } else if (isTransaction && flag.equals("remote")){
                    return "updated successfully";
                } else {
                //  Writing to a file
                try {
                    FileWriter file = new FileWriter(Utils.resourcePath+Utils.currentDbName+"/"+queryTableName.toLowerCase()+".tsv", false);
                    PrintWriter writer=new PrintWriter(file);
                    writer.append(tabledata.get(0));
                    writer.append("\n");
                    writer.append(tabledata.get(1));

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
                                val++;
                            } else {
                                local_str+="~"+value;
                            }
                        }
                        writer.append(local_str);
                    }
                    writer.close();

                    BufferedReader br3 = new BufferedReader(
                            new FileReader(Utils.resourcePath + "tableAnalysis.tsv"));
                    ArrayList<String> tableAnalysis = new ArrayList<String>();
                    String st3 = "";

                    // Adding data to arraylist line by line.
                    while ((st3 = br3.readLine()) != null) {
                        tableAnalysis.add(st3);
                    }
                    br3.close();

                    FileWriter fw1=new FileWriter(Utils.resourcePath+"tableAnalysis.tsv", false);
                    BufferedWriter bw1 = new BufferedWriter(fw1);
                    PrintWriter out1 = new PrintWriter(bw1);

                    String writeAnalysis="";
                    for(int i=0;i<tableAnalysis.size();i++){
                        writeAnalysis="";
                        String[] findAndUpdate=tableAnalysis.get(i).split(Utils.delimiter);
                        if(findAndUpdate[0].equals(Utils.currentDbName) && findAndUpdate[1].equals(queryTableName)){
                            Integer updateCount=Integer.parseInt(findAndUpdate[5])+1;
                            writeAnalysis+=findAndUpdate[0]+"~"+findAndUpdate[1]+"~insert~"+findAndUpdate[3]+"~update~"
                            +updateCount+"~delete~"+findAndUpdate[7];
                            out1.println(writeAnalysis);
                        } else {
                            writeAnalysis+=tableAnalysis.get(i);
                            out1.println(writeAnalysis);
                        }
                    }
                    out1.close();
                    bw1.close();
                    fw1.close();
                    logManager.writeEventLog("update", queryTableName);

                    if(flag.equals("local")){
                        System.out.println(affectedRows+" rows affected");
                    } else
                        return affectedRows+" rows affected";

                } catch (Exception e) {
                    logManager.writeCrashReportsToEventLogs(e.getMessage());
                    e.printStackTrace();
                }}
            } else {
                if(flag.equals("remote")){
                    return "Table '" + queryTableName + "' does not exist in " + Utils.currentDbName;
                } else {
                    // Call Another VM
                    Requester requester = Requester.getInstance();
                    String vmList = requester.requestVMDBCheck(Utils.currentDbName);
                    if (vmList.split("~").length > 1 || !vmList.equals(Utils.currentDevice)) {
                        requester.requestVMSetCurrentDbName(Utils.currentDbName);
                        String response = requester.requestVMUpdateQuery(query.replaceAll(" ", "%20"), "remote", isTransaction);
                        System.out.println(response);
                        if(response.equals("invalid")){
                            return "invalid";
                        } else if(!response.equals("invalid") && response.equals("updated successfully")){
                            Utils.transQueryList.add(query);
                        }
                        return response;
                    } else {
                        if(isTransaction){
                            return "invalid";
                        }
                        System.out.println("Table does not exist");
                    }
                }
            }
        } catch(Exception e){
            logManager.writeCrashReportsToEventLogs(e.getMessage());
            System.out.println("Exception: "+e);
            if(isTransaction){
                return "invalid";
            }
            return "Exception: "+e;
        }
        return "";
    }

    //selectQuery extracts the table name, the columns asked by user to be displayed and the condition that should be satisfied for the data displayed
    public static String selectQuery(String query, String flag) {
        String processedQuery = "";
        String tableName = "";
        String[] columnDetails = new String[2];
        String output = "";
        ArrayList<String> columnNameList = new ArrayList<String>();
        // to get the name of table, list of column names requested by user and to get the where condition below steps are performed
        try {
            //for each query the substring containing everything other than the select will be considered while if there is a semicolon at the end it will also be removed
            if (query.contains(";")) {
                processedQuery = query.substring(7, query.indexOf(';'));
            } else {
                processedQuery = query.substring(7, query.length());
            }
            //after keeping only the essential part if a query starts with * and does not contain where condition the tablename can directly be obtained from the processedQuery substring but if the where condition is present it can be split through where to obtain the table name and = to obtain the condition details
            if (processedQuery.startsWith("*")) {
                columnNameList = null;
                processedQuery = processedQuery.substring(7, processedQuery.length());
                if (processedQuery.contains("where")) {
                    String[] tableDetails = processedQuery.split("where");
                    tableName = tableDetails[0].trim();
                    columnDetails = tableDetails[1].split("=");
                } else {
                    tableName = processedQuery.trim();
                    columnDetails = null;
                }
                output = selectReadTable(tableName,columnNameList,columnDetails);
            }
            //if query starts with list of column names the names are stored in arraylist
            else {
                if (processedQuery.contains("where")) {
                    String[] tableDetails = processedQuery.split("where");
                    String[] colDetail = tableDetails[0].split("from");
                    columnDetails = tableDetails[1].replaceAll("\"", " ").split("=");
                    String[] columnList = colDetail[0].split(",");
                    tableName = colDetail[1].trim();
                    for (String colName : columnList) {
                        columnNameList.add(colName.trim());
                    }
                    output = selectReadTable(tableName,columnNameList,columnDetails);
                } else {
                    String[] tableDetails = processedQuery.split("from");
                    tableName = tableDetails[1].trim();
                    String[] columnList = tableDetails[0].split(",");
                    for (String colName : columnList) {
                        columnNameList.add(colName.trim());
                    }
                    columnDetails = null;
                    output = selectReadTable(tableName,columnNameList,columnDetails);
                }
            }
            // once all the values are obtained the selectReadMTable method is called from respective conditions to obtain the required data from table
            // this output is stored in the "output" String

            // if tableName is not null then only further steps will be considered
            if (tableName != null) {
                //checkIfTableExists method is called to check whether the table exist in the current VM.
                if (checkIfTableExists(tableName)) {
                    //if table exist and the request i.e flag is set for local VM, invalid output is obtained for incorrect column names otherwise the output will be printed.
                    if (flag.equals("local")) {
                        if(output.equals("invalid")) {
                            System.out.println("Column name is incorrect");
                        } else {
                            System.out.println("output: \n" + output);
                        }
                        return "";
                    }
                    // for remote VM the output will be returned to the method to print into other VM
                    else {
                        return output;
                    }
                }
                // if the table does not exist
                else {
                    // and the request is from remote so it means that it has already been checked in the other VM so we can say that table does not exist.
                    if (flag.equals("remote")) {
                        return "table does not exist";
                    }
                    // if the request is local a request will be made to the other VM through requester
                    else {
                        Requester requester = Requester.getInstance();
                        String vmList = requester.requestVMDBCheck(Utils.currentDbName);
                        //the request will be sent to other VM only if the currentDevice is the requester otherwise we can say table does not exist in both VM
                        if (vmList.split(Utils.delimiter).length > 1 || !vmList.equals(Utils.currentDevice)) {
                            requester.requestVMSetCurrentDbName(Utils.currentDbName);
                            String response = requester.requestVMSelectQuery(query.replaceAll(" ", "%20"), "remote");
                            //if the response obtained from the other VM is invalid the string invalid is returned so that while implementing transaction we can use the invalid response to return false for validating the select query
                            if(response.equals("invalid")) {
                                return "invalid";
                            }
                            // otherwise the response is printed
                            else {
                                System.out.println(response);
                            }

                        }
                        else {
                            System.out.println("table does not exist");
                        }
                    }
                }
            } else {
                System.err.println("query is incorrect");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(output.equals("invalid") && flag.equals("remote")) {
            System.out.println("Column name is incorrect");
        } else {
            System.out.println("output: \n" + output);
        }
        return "";
    }

    //the extracted information from the selectQuery is passed to this method where the existing data is processed and the required value is returned
    public static String selectReadTable(String tableName, ArrayList<String> columnList, String[] keyValuePair) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(Utils.resourcePath+Utils.currentDbName+"/"+tableName+".tsv"));
        String line;
        String str = "";
        br.readLine(); // you can ignore the first line as its the structure of the table
        br.readLine();
        ArrayList<Integer> index = new ArrayList<>();
        String colName = br.readLine();
        String[] columnNames = colName.split(Utils.delimiter);
        int whereIndex = -1;
        //All the column names are obtained from the existing data to match
        for(int j =0;j<columnNames.length;j++) {
            //if the columnList is not null the whole list is iterated to compare the column name to store the index of the required column
            if(columnList!=null) {
                for (int i = 0; i < columnList.size(); i++) {
                    if(colName.contains(columnList.get(i))) {
                        if (columnList.get(i).equals(columnNames[j].split(" ")[0])) {
                            index.add(j);
                        }
                    }
                    //if the columnName string does not contain the column name requested by the user we can say that the column is not present in the data hence return invalid
                    else {
                        return "invalid";
                    }
                }
            }
            //columnList is null it means all the columns details are to be printed hence the index of the column added in where condition is stored
            if(keyValuePair!=null && columnNames[j].split(" ")[0].equals(keyValuePair[0].trim())) {
                whereIndex = j;
            }
        }
        //in case of all the column details are requested the index of where condition column is used to split and compare the value to each rows and when the condition satisfies the row data is appended to output string
        if(columnList==null) {
            while ((line = br.readLine()) != null && line.length() > 0) {
                String [] lineSplits = line.split(Utils.delimiter);
                if(whereIndex>-1) {
                    if(lineSplits[whereIndex].equals(keyValuePair[1].trim())) {
                        str += line + "\n";
                    }
                } else {
                    str += line + "\n";
                }
            }
            return str;
        }
        //but if the user wants only the requested columns in case of a where condition below logic will be used where the index stored in the above loop will be used to split the data rows and append only the specific column data to the output
        else {
            if(index.size()>0) {
                while ((line = br.readLine()) != null && line.length() > 0) {
                    String[] lineSplits = line.split(Utils.delimiter);
                    for (int i = 0; i < index.size(); i++) {
                        //if where condition index is set then data for when the condition satisfies only will be appended
                        if (whereIndex > -1) {
                            if (lineSplits[whereIndex].equals(keyValuePair[1].trim())) {
                                str += lineSplits[index.get(i)] + Utils.delimiter;
                            }
                        }
                        // otherwise all the row data will be appended
                        else {
                            str += lineSplits[index.get(i)] + Utils.delimiter;
                        }
                    }
                    if (str != "") {
                        str = str.substring(0, str.length() - 1);
                        str += "\n";
                    }
                }
                br.close();
                return str;
            } else {
                return "invalid";
            }
        }
    }

}
