package com.example.project.UIAndSecurity;

import com.example.project.Analytics.AnalysisEngine;
import com.example.project.DataExport.ExportEngine;
import com.example.project.DataModeling.DataModelingEngine;
import com.example.project.LogManager.LogManager;
import com.example.project.QueryManager.QueryProcessor;
import com.example.project.Utilities.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Menu {
    private BufferedReader input = null;
    private UserCredentials currentUser = null;
    private String path = null;

    public Menu(BufferedReader input, UserCredentials currentUser, String path) {
        this.input = input;
        this.currentUser = currentUser;
        this.path = path;
    }

    public void displayMenu() {

        int option = 0;
        System.out.println("Welcome to Distributed Database Management System");
        do {
            try {
                System.out.println("\nKindly select one of the below options");
                System.out.println("1. Write Queries\n2. Export\n3. Data Model\n4. Analytics");
                System.out.print("Enter your choice (enter 0 to logout): ");
                try {
                    option = Integer.parseInt(input.readLine());
                } catch (Exception e) {
                    System.err.println("wrong input received, try again");
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        continue;
                    } catch (InterruptedException ie) {
                        LogManager.writeCrashReportsToEventLogs(e.getLocalizedMessage());
                        //ie.printStackTrace();
                    }
                }
                switch (option) {
                    case 0:
                        // by default we donot do anything as the user want's to quit
                        currentUser = null;
                        System.out.println("Thank you, you have been successfully logged out");
                        break;
                    case 1:
                        QueryProcessor qp = new QueryProcessor(input, currentUser, path);
                        qp.begin();
                        break;
                    case 2:
                        ExportEngine exportEngine = new ExportEngine(input, currentUser, path);
                        exportEngine.begin();
                        break;
                    case 3:
                        DataModelingEngine dataModelingEngine = new DataModelingEngine(input, currentUser, path);
                        dataModelingEngine.begin();
                        break;
                    case 4:
                        AnalysisEngine analysisEngine = new AnalysisEngine(input, currentUser, path);
                        analysisEngine.begin();
                        break;
                    default:
                        System.out.println("Invalid option selected, Try again");
                        option = -1;
                        break;
                }

            } catch (Exception e) {
                LogManager.writeCrashReportsToEventLogs(e.getLocalizedMessage());
//                System.out.println(e.getLocalizedMessage());
            }

        } while (option != 0);

    }

}
