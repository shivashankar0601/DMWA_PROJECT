package com.example.project.QueryManager;

import com.example.project.LogManager.LogManager;
import com.example.project.UIAndSecurity.UserCredentials;
import com.example.project.Utilities.Utils;
import java.io.BufferedReader;
import java.io.IOException;

public class QueryProcessor {
    private UserCredentials user;
    private BufferedReader input;
    private String path;

    private LogManager logManager = new LogManager();

    public QueryProcessor(BufferedReader input, UserCredentials currentUser, String path) {
        this.user = currentUser;
        this.input = input;
        this.path = path;
    }

    public void begin() {
        try {
            int option = 0;
            System.out.println("Welcome to Query Processor");
            do {
                System.out.print("Query : ");
                String query = input.readLine();
                // log the query and current timestamp
                logManager.writeQueryLog(query, this.user.getName());
                long startTime = System.currentTimeMillis();
                long stopTime = 0;
                String diff = "";
                switch (queryParser(query)) {
                    case 0: // for both create and use database queries, we should return zero from query processor
                        DatabaseProcessor dbp = new DatabaseProcessor();
                        String response = dbp.performOperation(query);
                        if (response != null || response != "") {
                            Utils.currentDbName = response;
                        }
                        stopTime = System.currentTimeMillis();
                        diff = (stopTime - startTime) + "";
                        logManager.writeGeneralLog(diff, this.user.getName());
                        break;
                    case 1: // any query related to tables goes here
                        TableProcessor tp = new TableProcessor(path);
                        if (Utils.currentDbName != null) {
                            tp.performOperation(query,false);
                        } else {
                            System.err.println("No database used.");
                        }
                        stopTime = System.currentTimeMillis();
                        diff = (stopTime - startTime) + "";
                        logManager.writeGeneralLog(diff, this.user.getName());
                        break;
                    default:
                        break;
                    case 2: // for transaction queries
                        System.out.println("Write your queries in transaction");
                        // read transaction queries until end transaction is encountered
                        do {
                            System.out.print("Transaction Query : ");
                            String transactionQuery = input.readLine();

                            if (queryParser(transactionQuery) == 0) {
                                DatabaseProcessor dbp2 = new DatabaseProcessor();
                                response = dbp2.performOperation(transactionQuery);
                                if (response != null || response != "") {
                                    Utils.currentDbName = response;
                                }
                            } else if (queryParser(transactionQuery) == 1) {
                                TableProcessor tp2 = new TableProcessor(path);
                                if(!tp2.performOperation(transactionQuery, true)) {
                                    break;
                                }
                            }
                            if(queryParser(transactionQuery) == 3) {
                                for(int i=0;i<Utils.transQueryList.size();i++)
                                {
                                    String transQuery = Utils.transQueryList.get(i);
                                    if (queryParser(transQuery) == 1) {
                                        TableProcessor tp2 = new TableProcessor(path);
                                        tp2.performOperation(transQuery, false);
                                    }
                                }
                                System.out.println("Transaction Completed Successfully");
                                break;
                            } else if (transactionQuery.toLowerCase().contains("rollback")) {
                                System.err.println("Transaction rolled back");
                                break;
                            }
                        } while(true);
                        Utils.transQueryList.clear();
                        break;
                }
            } while (option != 0);
        } catch (IOException e) {
            logManager.writeCrashReportsToEventLogs(e.getMessage());
            System.out.println(e.getLocalizedMessage());
        }
    }

    public int queryParser(String query) {
        if (query.toLowerCase().contains("table") || query.toLowerCase().contains("insert") || query.toLowerCase().contains("update") || query.toLowerCase().contains("delete") || query.toLowerCase().contains("select"))
            return 1;
        else if (query.toLowerCase().contains("start transaction") )
            return 2;
        else if (query.toLowerCase().contains("commit"))
            return 3;
        else
            return 0;
    }
}