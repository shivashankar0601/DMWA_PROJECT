package com.example.project.Analytics;

import com.example.project.LogManager.LogManager;
import com.example.project.Utilities.Utils;

import java.io.*;
import java.util.ArrayList;

public class fetchAnalysis {
    private static String query;

    LogManager logManager = new LogManager();

    public void performAnalysis(String query){
        this.query=query.toLowerCase();
        String countUpdateRegx="^(count\\supdate\\s[)(0-9a-zA-Z_]+)[;]?$";
        String countInsertRegx="^(count\\sinsert\\s[)(0-9a-zA-Z_]+)[;]?$";
        String countDeleteRegx="^(count\\sdelete\\s[)(0-9a-zA-Z_]+)[;]?$";
        String countQueriesRegx="^(count\\squeries)?$";
        if(query.matches(countUpdateRegx)){
            updateAnalysisByDB(query);
        } else if(query.matches(countInsertRegx)){
            insertAnalysisByDB(query);
        } else if(query.matches(countDeleteRegx)){
            deleteAnalysisByDB(query);
        }else if(query.matches(countQueriesRegx)){
            countQueries(query);
        }else {
            System.out.println("Invalid query for analysis");
        }
    }

    private void countQueries(String query) {
        try{
            BufferedReader br3 = new BufferedReader(
                    new FileReader(Utils.resourcePath + "queryLogs.tsv"));
            ArrayList<String> queryLogs = new ArrayList<String>();
            String st3 = "";

            // Adding data to arraylist line by line.
            while ((st3 = br3.readLine()) != null) {
                queryLogs.add(st3);
            }
            br3.close();
            ArrayList<String> tempResultData = new ArrayList<>();
            ArrayList<String> str=new ArrayList<>();
            ArrayList<String> db=new ArrayList<>();
            ArrayList<String> device=new ArrayList<>();
            for(int i=0;i<queryLogs.size();i++){
                System.out.println(queryLogs.get(i));
                System.out.println(queryLogs.get(i).split(Utils.delimiter));
                System.out.println(queryLogs.get(i).split(Utils.delimiter)[2]);
                System.out.println(queryLogs.get(i).split(Utils.delimiter)[2].split(":"));
                System.out.println(queryLogs.get(i).split(Utils.delimiter)[2].split(":")[1]);
                if(!queryLogs.get(i).split(Utils.delimiter)[2].split(":")[1].equals("null")){
                    String local_str=queryLogs.get(i).split(Utils.delimiter)[1].split(":")[1]+"~"+
                            queryLogs.get(i).split(Utils.delimiter)[2].split(":")[1]+"~"+
                            queryLogs.get(i).split(Utils.delimiter)[3].split(":")[1];
                    str.add(local_str);
                }
            }
            ArrayList<String> str2=new ArrayList<>();
            for(int i=0;i< str.size();i++){
                if(i==0){
                    str2.add(str.get(i)+"~1");
                } else
                    str2=checkIfExistsInStr(str2, str.get(i));
            }
            FileWriter fw1=new FileWriter(Utils.resourcePath+"countQueries.tsv", false);
            BufferedWriter bw1 = new BufferedWriter(fw1);
            PrintWriter out1 = new PrintWriter(bw1);
            for(int i=0;i< str2.size();i++){
                String[] ls=str2.get(i).split("~");
//                user SDEY submitted 10 queries for DB1 running on Virtual Machine 1
                System.out.println("user "+ls[0]+" submitted "+ls[3]+
                        " queries for "+ls[1]+" running on "+ls[2]);
                out1.println("user "+ls[0]+" submitted "+ls[3]+
                        " queries for "+ls[1]+" running on "+ls[2]);
            }
            out1.close();
            bw1.close();
            fw1.close();
        } catch (Exception e){
            System.out.println(e);
        }
    }

    private ArrayList<String> checkIfExistsInStr(ArrayList<String> s2,String s) {
        boolean flag=false;
        for(int m=0;m<s2.size();m++){
            if(s2.get(m).indexOf(s)>-1){
                int index=s2.get(m).lastIndexOf(Utils.delimiter);
                String[] newstr={
                        s2.get(m).substring(0,index),
                        s2.get(m).substring(index)
                };
                int countQ=Integer.parseInt(newstr[1].replace("~",""))+1;
                s2.set(m,newstr[0]+Utils.delimiter+countQ);
                flag=true;
            }
        }
        if(!flag){
            s2.add(s+Utils.delimiter+"1");
        }
        return s2;
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
            logManager.writeCrashReportsToEventLogs(e.getMessage());
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
            logManager.writeCrashReportsToEventLogs(e.getMessage());
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
            logManager.writeCrashReportsToEventLogs(e.getMessage());
            System.out.println(e);
        }
    }
}
