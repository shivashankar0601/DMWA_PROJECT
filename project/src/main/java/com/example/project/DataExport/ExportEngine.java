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

import static com.example.project.QueryManager.DatabaseProcessor.checkIsLocalDB;

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
                    if(tables==null || tables.length()==0){
                        // which means no tables in the db, what should i do
                        System.err.println("no tables in the database to export");
                        try {
                            TimeUnit.SECONDS.sleep(1);
                            break;
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                    else{
                        // perform the data export operation

                        if(createExportFile(Arrays.asList(tables.split(Utils.delimiter)), ipt.trim())){
                            System.out.println(ipt.trim() + " Database exported successfully to {some path}");
                        }
                        else{
                            System.err.println(ipt.trim() + " Database export failed");
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

            for(String table : tables){
                String [] data = readTableData(table, dbName);
            }












        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }

    private String[] readTableData(String table, String dbName) {
        String [] data = new String[2]; // create statement and the data in one line

        if(DatabaseProcessor.checkIsLocalDB(dbName)){
            try {
                BufferedReader br = new BufferedReader(new FileReader(Utils.resourcePath + dbName));
                // for create query
                int cols = Integer.parseInt(br.readLine().split(Utils.delimiter)[1]);

                String columns = br.readLine();
                String keys = prepareKeys(columns.split(Utils.delimiter),cols);
                columns = prepareColumns(columns.split(Utils.delimiter), cols);




                data[0] = String.format("create table %s ( %s ) %s;",table,columns,keys);
                String line = null;
                // for all values
                while((line=br.readLine())!=null){

                }
            }
            catch(Exception e){

            }
        }



        return data;
    }

    public String prepareKeys(String[] split, int cols) {
        return "";
    }


    public String prepareColumns(String[] cols, int count) {
        StringBuilder sb = new StringBuilder();
        int i = count;
        // we extract only those many number of columns, we will not worry about primary key and foreign key stuff
        for(String col : cols){
            if(i-- == 0)
                break;
            String info [] = col.split(" ");
            if(info.length==3){
                sb.append(col.replace(info[2],"("+info[2]+"), "));
            }
            else{
                sb.append(col+", ");
            }
        }
        return sb.toString().substring(0,sb.length()-2);
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
                tname = tname.substring(0,tname.indexOf(".tsv"));
                if(tname.equalsIgnoreCase("metadata"))
                    continue;
                tables.add(tname);
                //System.out.println(tname);
            }
        }

        String response = StringUtils.join(tables,Utils.delimiter.charAt(0));

        if(shouldRequestVM){
            // ask vm to give the tables available over there
            String res = Requester.getInstance().requestVMAllTables(dbName,!shouldRequestVM);
            // returning empty string from server is causing null pointer exception in html response so returning none
            if(res!=null && res.length()>0) {
                response = response.length()>0? response.concat(Utils.delimiter+res):res;
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
