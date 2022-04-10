package com.example.project.Analytics;

import com.example.project.Utilities.Utils;

import java.io.*;
import java.util.ArrayList;

public class fetchAnalysis {
    private static String query;

    public void performAnalysis(String query){
        this.query=query.toLowerCase();
        String countUpdateRegx="^(count\\supdate\\s[)(0-9a-zA-Z_]+)[;]?$";
        String countInsertRegx="^(count\\sinsert\\s[)(0-9a-zA-Z_]+)[;]?$";
        String countDeleteRegx="^(count\\sdelete\\s[)(0-9a-zA-Z_]+)[;]?$";
        if(query.matches(countUpdateRegx)){
            updateAnalysisByDB(query);
        } else if(query.matches(countInsertRegx)){
            insertAnalysisByDB(query);
        } else if(query.matches(countDeleteRegx)){
            deleteAnalysisByDB(query);
        }else {
            System.out.println("Invalid query for analysis");
        }
    }

    private void updateAnalysisByDB(String query) {
        String[] querySplit=query.split(" ");
        String dbName=querySplit[2];
        try{
            BufferedReader br3 = new BufferedReader(
                    new FileReader(Utils.resourcePath + "tableAnalysis.tsv"));
            ArrayList<String> tableAnalysis = new ArrayList<String>();
            String st3 = "";

           // Adding data to arraylist line by line.
            while ((st3 = br3.readLine()) != null) {
                tableAnalysis.add(st3);
            }
            br3.close();
            ArrayList<String> resultData = new ArrayList<>();

            for(int i=0;i<tableAnalysis.size();i++){
                if(tableAnalysis.get(i).split(Utils.delimiter)[0].equals(dbName)){
                    resultData.add(tableAnalysis.get(i));
                }
            }
            FileWriter fw1=new FileWriter(Utils.resourcePath+"updateCountAnalysis.tsv", false);
            BufferedWriter bw1 = new BufferedWriter(fw1);
            PrintWriter out1 = new PrintWriter(bw1);
            for(int i=0;i<tableAnalysis.size();i++){
                String[] out=tableAnalysis.get(i).split(Utils.delimiter);
                System.out.println("Total "+out[5]+" Update operations are performed on "+out[1]);
                out1.println("Total "+out[5]+" Update operations are performed on "+out[1]);
            }
            out1.close();
            bw1.close();
            fw1.close();

        } catch (Exception e){
            System.out.println(e);
        }
    }

    private void insertAnalysisByDB(String query) {
        String[] querySplit=query.split(" ");
        String dbName=querySplit[2];
        try{
            BufferedReader br3 = new BufferedReader(
                    new FileReader(Utils.resourcePath + "tableAnalysis.tsv"));
            ArrayList<String> tableAnalysis = new ArrayList<String>();
            String st3 = "";

            // Adding data to arraylist line by line.
            while ((st3 = br3.readLine()) != null) {
                tableAnalysis.add(st3);
            }
            br3.close();
            ArrayList<String> resultData = new ArrayList<>();

            for(int i=0;i<tableAnalysis.size();i++){
                if(tableAnalysis.get(i).split(Utils.delimiter)[0].equals(dbName)){
                    resultData.add(tableAnalysis.get(i));
                }
            }
            FileWriter fw1=new FileWriter(Utils.resourcePath+"insertCountAnalysis.tsv", false);
            BufferedWriter bw1 = new BufferedWriter(fw1);
            PrintWriter out1 = new PrintWriter(bw1);

            for(int i=0;i<tableAnalysis.size();i++){
                String[] out=tableAnalysis.get(i).split(Utils.delimiter);
                System.out.println("Total "+out[3]+" Update operations are performed on "+out[1]);
                out1.println("Total "+out[3]+" Update operations are performed on "+out[1]);
            }
            out1.close();
            bw1.close();
            fw1.close();

        } catch (Exception e){
            System.out.println(e);
        }
    }
    private void deleteAnalysisByDB(String query) {
        String[] querySplit=query.split(" ");
        String dbName=querySplit[2];
        try{
            BufferedReader br3 = new BufferedReader(
                    new FileReader(Utils.resourcePath + "tableAnalysis.tsv"));
            ArrayList<String> tableAnalysis = new ArrayList<String>();
            String st3 = "";

            // Adding data to arraylist line by line.
            while ((st3 = br3.readLine()) != null) {;
                tableAnalysis.add(st3);
            }
            br3.close();
            ArrayList<String> resultData = new ArrayList<>();

            for(int i=0;i<tableAnalysis.size();i++){
                if(tableAnalysis.get(i).split(Utils.delimiter)[0].equals(dbName)){
                    resultData.add(tableAnalysis.get(i));
                }
            }
            FileWriter fw1=new FileWriter(Utils.resourcePath+"deleteCountAnalysis.tsv", false);
            BufferedWriter bw1 = new BufferedWriter(fw1);
            PrintWriter out1 = new PrintWriter(bw1);

            for(int i=0;i<tableAnalysis.size();i++){
                String[] out=tableAnalysis.get(i).split(Utils.delimiter);
                System.out.println("Total "+out[7]+" Delete operations are performed on "+out[1]);
                out1.println("Total "+out[7]+" Delete operations are performed on "+out[1]);
            }
            out1.close();
            bw1.close();
            fw1.close();

        } catch (Exception e){
            System.out.println(e);
        }
    }
}
