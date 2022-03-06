package QueryManager;

import UIAndSecurity.UserCredentials;

import java.io.BufferedReader;
import java.io.IOException;

public class QueryProcessor {
    private UserCredentials user = null;
    private BufferedReader input = null;
    private String path = null;
    public QueryProcessor(BufferedReader input, UserCredentials currentUser, String path) {
        this.user=currentUser;
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
                switch(queryParser(query)){
                    case 0: // for both create and use database queries, we should return zero from query processor
                        DatabaseProcessor dbp = new DatabaseProcessor(path);
                        dbp.performOperation(query);
                        break;
                    case 1:// any query related to tables goes here
                        TableProcessor tp = new TableProcessor(path);
                        tp.performOperation(query);
                        break;
                    default:
                        break;
                }

            } while (option != 0);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public int queryParser(String query){
        int response = 0;


        return response;
    }


}
