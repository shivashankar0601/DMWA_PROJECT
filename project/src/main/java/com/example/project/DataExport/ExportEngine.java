package com.example.project.DataExport;

import com.example.project.DistributedDatabaseLayer.Requester;
import com.example.project.UIAndSecurity.UserCredentials;
import com.example.project.Utilities.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExportEngine {
    private UserCredentials user = null;
    private BufferedReader input = null;
    private String path = null;

    public ExportEngine(BufferedReader input, UserCredentials currentUser, String path) {
        this.user = currentUser;
        this.input = input;
        this.path = path;
    }

    List<String> dbs = null;

    // write the whole logic of exporting taking this method as a starting point
    public void begin() {
        String ipt = null;
        try {
            dbs = getAllAvailableDBs();
            System.out.println("Available databases for data export :");

            if (dbs.size() == 0) {
                System.err.println("no databases available to export");
                return;
            }

            for (String s : dbs) {
                System.out.println(s);
            }

            do {

                System.out.print("Enter the name of the database to be exported (press 0 to exit):");

                ipt = input.readLine();

                if(ipt.equalsIgnoreCase("0"))
                    break;

                if(dbs.contains(ipt.trim())){
                    // perform the data export operation
                    System.out.println(ipt.trim()+" Database exported successfully to {some path}");
                }
                else{

                    System.err.println("invalid option, try again ! ");
                    TimeUnit.SECONDS.sleep(1);
                }

            }while(true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public static List<String> getAllAvailableDBs() {

        if (Utils.gddExists) {

            ArrayList<String> dbs = new ArrayList<String>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(Utils.resourcePath + "gdd.tsv"));
                String line = null;
                while ((line = br.readLine()) != null && line.trim().length() != 0) {
                    String db = line.split(Utils.delimiter)[0];
                    // if there are two databases with the same name, we will show only one as we will have same database over two Vm's
                    if (!dbs.contains(db))
                        dbs.add(db);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dbs;
        } else {
            // request vm to get the list of dbs
            String res[] = (Requester.getInstance().requestVMForDBs()).split(Utils.delimiter);
            return Arrays.asList(res);
        }
    }
}
