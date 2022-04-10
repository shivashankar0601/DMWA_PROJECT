package com.example.project.Analytics;

import com.example.project.QueryManager.DatabaseProcessor;
import com.example.project.QueryManager.TableProcessor;
import com.example.project.UIAndSecurity.UserCredentials;
import com.example.project.Utilities.Utils;

import java.io.BufferedReader;
import java.io.IOException;

public class AnalysisEngine {
    private UserCredentials user;
    private BufferedReader input;
    private String path;

    public AnalysisEngine(BufferedReader input, UserCredentials currentUser, String path) {
        this.user = currentUser;
        this.input = input;
        this.path = path;
    }

    public void begin(){
        try {
            int option = 0;
            System.out.println("Welcome to Analysis");
            do {
                System.out.print("Query : ");
                String query = input.readLine();
                switch (analysisParser(query)) {
                    case 0: // for both create and use database queries, we should return zero from query processor
                        System.out.println("Invalid Analysis Query... Try again with queries like count update [DBNAME]!");
                        break;
                    case 1: // any query related to tables goes here
                        fetchAnalysis fa = new fetchAnalysis();
                        fa.performAnalysis(query);
                        break;
                    default:
                        break;
                }
            } while (option != 0);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
    public int analysisParser(String query) {
        if (query.toLowerCase().contains("update") || query.toLowerCase().contains("queries") ||
                query.toLowerCase().contains("insert") || query.toLowerCase().contains("delete"))
            return 1;
        else
            return 0;
    }
}
