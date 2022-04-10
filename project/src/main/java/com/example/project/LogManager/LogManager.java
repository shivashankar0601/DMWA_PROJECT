package com.example.project.LogManager;

import com.example.project.DataExport.ExportEngine;
import com.example.project.Utilities.Utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class LogManager {
    // save the log file path for general logs, event logs and query logs
    private static String generalLogPath = Utils.resourcePath + "Logs/generalLogs";
    private static String eventLogPath = Utils.resourcePath + "Logs/eventLogs";
    private static String queryLogPath = Utils.resourcePath + "Logs/queryLogs";

    ExportEngine engine = new ExportEngine(null, null, null);

    public int getNumberOfTables(List<String> availableDatabases) {
        int count = 0;
        for (String database : availableDatabases) {
            String allAvailableTables = engine.getAllAvailableTables(database, true);
            count += allAvailableTables.split("~").length;
        }
        return count;
    }

    public void writeGeneralLog(String queryExecutionTime, String userName) throws IOException {
        // write to generalLogPath file
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;

        List<String> availableDatabases = engine.getAllAvailableDBs();

        int numberOfTables = getNumberOfTables(availableDatabases);
        String databaseState = "numberOfDatabases:" + availableDatabases.size() + "~" + "numberOfTables:" + numberOfTables;

        try {
            File file = new File(generalLogPath + ".tsv");
            if (file.exists()) {
                fw = new FileWriter(generalLogPath + ".tsv", true);
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(bw);
                pw.print("queryExecutionTime:" + queryExecutionTime + "~"
                        + "databaseState:" + databaseState + "~"
                        + "userName:" + userName + "~"
                        + "deviceName:" + Utils.currentDevice + "~"
                        + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime()))
                        + "\n");
            } else {
                fw = new FileWriter(generalLogPath + ".tsv");
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(bw);
                pw.print("queryExecutionTime:" + queryExecutionTime + "~"
                        + "databaseState:" + databaseState + "~"
                        + "userName:" + userName + "~"
                        + "deviceName:" + Utils.currentDevice + "~"
                        + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime()))
                        + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pw.close();
            bw.close();
            fw.close();
        }
    }

    public void writeEventLog(String queryType, String tableName) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            File file = new File(eventLogPath + ".tsv");
            if (file.exists()) {
                fw = new FileWriter(eventLogPath + ".tsv", true);
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(bw);
                pw.print("queryType:" + queryType + "~"
                        + "databaseName:" + Utils.currentDbName + "~"
                        + "tableName:" + tableName + "~"
                        + "deviceName:" + Utils.currentDevice + "~"
                        + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime()))
                        + "\n");
                pw.close();
            } else {
                if (Utils.createDirectory(Utils.resourcePath, "Logs") && Utils.createFile(eventLogPath, "")) {
                    fw = new FileWriter(eventLogPath + ".tsv", true);
                    bw = new BufferedWriter(fw);
                    pw = new PrintWriter(bw);
                    pw.print("queryType:" + queryType + "~"
                            + "databaseName:" + Utils.currentDbName + "~"
                            + "tableName:" + tableName + "~"
                            + "deviceName:" + Utils.currentDevice + "~"
                            + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime()))
                            + "\n");
                    pw.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeQueryLog(String query, String userName) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            // check if the file exists with path queryLogPath
            File file = new File(queryLogPath + ".tsv");
            if (file.exists()) {
                fw = new FileWriter(queryLogPath + ".tsv", true);
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(bw);
                pw.print("query:" + query + "~"
                        + "userName:" + userName + "~"
                        + "databaseName:" + Utils.currentDbName + "~"
                        + "deviceName:" + Utils.currentDevice + "~"
                        + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime()))
                        + "\n");
                pw.close();
            } else {
                if (Utils.createDirectory(Utils.resourcePath, "Logs") && Utils.createFile(queryLogPath, "")) {
                    fw = new FileWriter(queryLogPath + ".tsv", true);
                    bw = new BufferedWriter(fw);
                    pw = new PrintWriter(bw);
                    pw.print("query:" + query + "~"
                            + "userName:" + userName + "~"
                            + "databaseName:" + Utils.currentDbName + "~"
                            + "deviceName:" + Utils.currentDevice + "~"
                            + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime()))
                            + "\n");
//                pw.print("\n" + query + "~" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime())));
                    pw.close();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeCrashReportsToEventLogs(String message) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            File file = new File(eventLogPath + ".tsv");
            if (file.exists()) {
                fw = new FileWriter(eventLogPath + ".tsv", true);
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(bw);
                pw.print("crashReport:" + message + "~"
                        + "deviceName:" + Utils.currentDevice + "~"
                        + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime()))
                        + "\n");
                pw.close();
            } else {
                if (Utils.createDirectory(Utils.resourcePath, "Logs") && Utils.createFile(eventLogPath, "")) {
                    fw = new FileWriter(eventLogPath + ".tsv", true);
                    bw = new BufferedWriter(fw);
                    pw = new PrintWriter(bw);
                    pw.print("crashReport:" + message + "~"
                            + "deviceName:" + Utils.currentDevice + "~"
                            + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime()))
                            + "\n");
                    pw.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
