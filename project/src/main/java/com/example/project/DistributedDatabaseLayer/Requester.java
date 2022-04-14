package com.example.project.DistributedDatabaseLayer;

import com.example.project.LogManager.LogManager;
import com.example.project.Utilities.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class Requester {

    private static Requester requester = null;

    public Requester() {
    }

    public static Requester getInstance() {
        if (requester == null)
            requester = new Requester();
        return requester;
    }

    // this method will request for dbcheck, if db exists in GDD it will return the VM name or ip for now
    //http://localhost:8080/gdd?checkDB=test
    public String requestVMDBCheck(String dbName) {
        return requestVM("gdd?checkDB=" + dbName);
    }

    //http://localhost:8080/gdd?addDB=test&vm=vm1
    public boolean requestVMAddDB(String dbName, String vmName) {
        return Boolean.parseBoolean(requestVM("gdd?addDB=" + dbName + "&vm=" + vmName));
    }

    public String requestVMInsertQuery(String query, String flag, Boolean isTransaction) {
        return requestVM("query?insert=" + query + "&flag=" + flag + "&isTransaction=" + isTransaction);
    }

    public String requestVMUpdateQuery(String query, String flag, Boolean isTransaction) {
        return requestVM("query?update=" + query + "&flag=" + flag + "&isTransaction=" + isTransaction);
    }

    public String requestVMDeleteQuery(String query, String flag, Boolean isTransaction) {
        return requestVM("query?delete=" + query + "&flag=" + flag + "&isTransaction=" + isTransaction);
    }

    public String requestVMSelectQuery(String query, String flag) {
        return requestVM("query?select=" + query + "&flag=" + flag);
    }

    public String requestVMSetCurrentDbName(String currentDbName) {
        return requestVM("utils?setCDBN=" + Utils.currentDbName);
    }

    public String requestVMForDBs() {
        return requestVM("gdd?list=all");
    }

    public String requestVMAllTables(String dbName, boolean isVmRequest) {
        return requestVM("tables?db=" + dbName + "&vm=" + isVmRequest);
    }

    public String[] requestVMWholeTable(String tableName, String dbName) {
        return requestVM("readTable?table=" + tableName + "&db=" + dbName).split(Utils.delimiter);
    }

    public String requestVMTableStructure(String dbName, String table) {
        return requestVM("readStructure?table=" + table + "&db=" + dbName);
    }

    public List<String> requestVmGetColumns(String tableName, String dbName) {
        return Arrays.asList(requestVM("getColumns?table="+tableName+"&db="+dbName).split(Utils.delimiter));
    }

    // parameter is a method name with all the parameters
    private String requestVM(String mwp) {

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Utils.ip + mwp))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            }

        } catch (IOException e) {
//            e.printStackTrace();
            LogManager.writeCrashReportsToEventLogs(e.getLocalizedMessage());
        } catch (InterruptedException e) {
            LogManager.writeCrashReportsToEventLogs(e.getLocalizedMessage());
//            e.printStackTrace();
        }
        // if an error occurred, then we will return null instead of the response object
        return null;
    }

}
