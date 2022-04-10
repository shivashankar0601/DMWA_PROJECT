package com.example.project.LogManager;

import com.example.project.Utilities.Utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogManager {
    // save the log file path for general logs, event logs and query logs
    private static String generalLogPath = Utils.resourcePath + "Logs/generalLogs";
    private String eventLogPath = Utils.resourcePath + "Logs/eventLogs";
    private String queryLogPath = Utils.resourcePath + "Logs/queryLogs";

    // save query execution time and state of the database to generalLogPath file
    public void writeGeneralLog(String queryExecutionTime, String databaseState) throws IOException {
        // write to generalLogPath file
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            File file = new File(generalLogPath + ".tsv");
            if (file.exists()) {
                fw = new FileWriter(generalLogPath + ".tsv", true);
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(bw);
                pw.println(queryExecutionTime + " " + databaseState);
                pw.print("queryExecutionTime:" + queryExecutionTime + "~"
                        + "databaseState:" + databaseState + "~"
                        + "deviceName:" + Utils.currentDevice + "~"
                        + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime())));
            } else {
                fw = new FileWriter(generalLogPath + ".tsv");
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(bw);
                pw.println("queryExecutionTime" + "\t" + "databaseState");
                pw.println(queryExecutionTime + "\t" + databaseState);
                pw.print("queryExecutionTime:" + queryExecutionTime + "~"
                        + "databaseState:" + databaseState + "~"
                        + "deviceName:" + Utils.currentDevice + "~"
                        + "time:" + (new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pw.close();
            bw.close();
            fw.close();
        }
    }

    // save changes in database, concurrent transactions, crash reports and other events to eventLogPath file
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
                if (Utils.createDirectory(Utils.resourcePath, "Logs") && Utils.createFile(queryLogPath, "")) {
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

    // save user queries and timestamp of query submission to queryLogPath file
    public void writeQueryLog(String query, String userName) {
        // write to queryLogPath file
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

    // implement function to create if file not exists


}
