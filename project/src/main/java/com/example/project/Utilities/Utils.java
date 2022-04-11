package com.example.project.Utilities;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;

import com.example.project.LogManager.LogManager;

public class Utils {

    public static String currentDevice = null;
    public static String delimiter = "~";
    public static String resourcePath = "./resources/";
    public static String ip = null;
    public static String userProfiles = resourcePath + "users.tsv";
    public static boolean gddExists = false;
    public static String currentDbName = null;

    // just defined for future use
    public static boolean debugging = false;
    public static boolean isVMRequest = false;

    static LogManager logManager = new LogManager();

    // Error messages which are commonly used
    public static String gddNotFound = "Global Data Dictionary is not found";
    public static String dbMetadataNotFound = "Metadata file not found";

    //queryList for Transaction
    public static ArrayList<String> transQueryList = new ArrayList<>();
    public static ArrayList<String> tableLocked = new ArrayList<>();

    public static String hashWithMD5(String user_name) {
        try {
            BigInteger hashed_number = new BigInteger(1, ((MessageDigest.getInstance("MD5")).digest(user_name.getBytes())));
            String final_value = hashed_number.toString(16);
            while (final_value.length() < 32) {
                final_value = "0" + final_value;
            }
            return final_value;
        } catch (Exception e) {
            LogManager.writeCrashReportsToEventLogs(e.getLocalizedMessage());
            //System.out.println(e.getLocalizedMessage());;
//            e.printStackTrace();
        }
        return null;
    }

    public static boolean createDirectory(String path, String directory_name) {
        try {
            File f = new File(path + directory_name);
            f.mkdir();
            return true;
        } catch (Exception e) {
            LogManager.writeCrashReportsToEventLogs(e.getLocalizedMessage());
//            e.printStackTrace();
        }
        return false;
    }

    public static boolean createFile(String path, String name) {
        try {
            File f = new File(path + name + ".tsv"); // tilda separated value file
            f.createNewFile();
            return true;
        } catch (Exception e) {
            LogManager.writeCrashReportsToEventLogs(e.getLocalizedMessage());
//            e.printStackTrace();
        }
        return false;
    }

    // all the important parameters for the application are loaded beforehand for proper execution
    public static boolean loadConfiguration() throws IOException {
        try {

            File rPath = new File("./resources");
            if (!rPath.exists()) {
                rPath.mkdir();
                System.out.println("resources directory created successfully");
                FileWriter conf = new FileWriter(resourcePath + "configuration.tsv");
                conf.write("name~vm1\nvm~0.0.0.0\ngdd~true");//default configuration
                conf.flush();
                conf.close();
                System.out.println("successfully created configuration file");
            }

            BufferedReader br = new BufferedReader(new FileReader(resourcePath + "configuration.tsv"));
            String line = "";
            while ((line = br.readLine()) != null) {
                String splits[] = line.split(delimiter);
                if (splits[0].equalsIgnoreCase("name"))
                    currentDevice = splits[1];
                else if (splits[0].equalsIgnoreCase("vm"))
                    ip = String.format("http://%s:8080/", splits[1]);
                else if (splits[0].equalsIgnoreCase("gdd")) {
                    gddExists = Boolean.parseBoolean(splits[1]);

                    if (gddExists == true) { // creating gdd automatically if it is not created by the administrator
                        File f = new File(Utils.resourcePath + "gdd.tsv");
                        if (!f.exists()) {
                            FileWriter fileWriter = new FileWriter(Utils.resourcePath + "gdd.tsv");
                            fileWriter.append("");
                            fileWriter.flush();
                            fileWriter.close();
                        }
                    }
                }
            }
            // creating user profile if it doesn't exist
            File f = new File(Utils.resourcePath + "users.tsv");
            if (!f.exists()) {
                FileWriter fileWriter = new FileWriter(Utils.resourcePath + "users.tsv");
                fileWriter.append("");
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (Exception e) {
            LogManager.writeCrashReportsToEventLogs(e.getLocalizedMessage());
//            System.err.println(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

}