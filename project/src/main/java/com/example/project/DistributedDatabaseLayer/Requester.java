package com.example.project.DistributedDatabaseLayer;

import com.example.project.Utilities.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Requester {

    private static Requester requester = null;

    public Requester(){  }

    public static Requester getInstance(){
        if(requester==null)
            requester = new Requester();
        return requester;
    }

    // this method will request for dbcheck, if db exists in GDD it will return the VM name or ip for now
    //http://localhost:8080/gdd?checkDB=test
    public String requestVMDBCheck(String dbName){
        return requestVM("gdd?checkDB="+dbName);
    }

    //http://localhost:8080/gdd?addDB=test&vm=vm1
    public boolean requestVMAddDB(String dbName, String vmName){
        return Boolean.parseBoolean(requestVM("gdd?addDB="+dbName+"&vm="+vmName));
    }

    /*public String requestVMInsertIntoQuery (String query, String flag) {
    //String response =  insertIntoQuery(query,flag);
    }*/

    /*public void  requestVMSetCurrentDbName(String currentDbName) {
    //call setCurrentDbName(Utils.currentDbName);
    }*/



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
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // if an error occurred, then we will return null instead of the response object
        return null;
    }


}
