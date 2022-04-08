package com.example.project.QueryManager;

import com.example.project.DistributedDatabaseLayer.Requester;
import com.example.project.Utilities.Utils;

import java.io.*;

public class DatabaseProcessor {

    public String performOperation(String query) throws IOException {
        query = query.toLowerCase();
        String selectedDB;
        String createDatabaseRegx = "^(create\\sdatabase\\s[0-9a-zA-Z_]+)[;]?$";
        String useDatabaseRegx = "^(use\\s[0-9a-zA-Z_]+)[;]?$";

        if (query.matches(createDatabaseRegx)) {
            selectedDB = (query.split("\s")[2]);
            if (selectedDB.contains(";")) // if there is a ; at the end, we should remove it (; is an optional parameter)
                selectedDB = selectedDB.substring(0, selectedDB.indexOf(';'));
            if (!dbExists(selectedDB) && Utils.createDirectory(Utils.resourcePath, selectedDB)) {
                System.out.println("database " + selectedDB + " created successfully");
                // should update the gdd about the newly created db
                addNewDB(selectedDB);
                createTableMetadata(selectedDB);
                return ""; // since the database is created but not selected we return "" string
            } else {
                System.out.println("Database with name " + selectedDB + " already exists");
                return null;
            }
        } else if (query.matches(useDatabaseRegx)) {
            // check if DB exists in this vm or not, if it doesn't exist, then request for checking in the other vm

            selectedDB = (query.split("\s")[1]);
            if (selectedDB.contains(";")) // if there is a ; at the end, we should remove it (; is a optional parameter)
                selectedDB = selectedDB.substring(0, selectedDB.indexOf(';'));

            // check if you should make dbExists method return type string, i feel like you should
            if (dbExists(selectedDB)) {
                System.out.println("selected db " + selectedDB + " successfully from " + (Utils.isVMRequest ? "remote machine" : Utils.currentDevice));
                return selectedDB;
            } else {
                System.err.println("A database with the given name does not exists");
                return null;
            }
        }
        return null;
    }

    private void createTableMetadata(String selectedDB) throws IOException {
        FileWriter writer = new FileWriter(Utils.resourcePath + selectedDB + "/metadata.tsv");
        writer.write("");
        writer.close();
    }

    public boolean dbExists(String dbName) {
        if (Utils.gddExists) {
            String res = checkDBFromGDD(dbName);
            if (res != null && !res.equalsIgnoreCase(Utils.currentDevice)) {
                Utils.isVMRequest = true;
            } else if (res != null) {
                Utils.isVMRequest = false;
                return true;
            }
        } else {
            File f = new File(Utils.resourcePath + dbName);
            if (f.exists()) {
                Utils.isVMRequest = false;
                return true;
            }
            String res = Requester.getInstance().requestVMDBCheck(dbName);
            if (res != null && res.length() > 0) {
                Utils.isVMRequest = true;
                return true;
            }
        }
        return false;
    }

    public boolean addNewDB(String dbName) {
        if (Utils.gddExists) {
            return addDBToGDD(dbName, Utils.currentDevice);
        } else {
            return Requester.getInstance().requestVMAddDB(dbName, Utils.currentDevice);
        }
    }

    public static String checkDBFromGDD(String dbName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Utils.resourcePath + "gdd.tsv"));
            String line;
            String res = "";
            while ((line = br.readLine()) != null && line.length() > 0) {
                String[] splits = line.split(Utils.delimiter);
                if (splits[0].equalsIgnoreCase(dbName))
                    if(res.length()==0)
                        res = splits[1];
                    else
                        res = res + Utils.delimiter +splits[1] ;
            }
            if(res.length()>0)
                return res;
        } catch (FileNotFoundException e) {
            System.err.println(Utils.gddNotFound);
            // at least an empty gdd should be made available by the time application is created
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean addDBToGDD(String dbName, String vm) {
        try {
            FileWriter fileWriter = new FileWriter(Utils.resourcePath + "gdd.tsv", true);
            fileWriter.append(dbName + Utils.delimiter + vm + "\n");
            fileWriter.flush();
            fileWriter.close();
            return true;
        } catch (FileNotFoundException e) {
            System.err.println(Utils.gddNotFound);
            // at least an empty gdd should be made available by the time application is created
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}