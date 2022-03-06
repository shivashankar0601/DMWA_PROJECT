package UIAndSecurity;

import DataExport.ExportEngine;
import QueryManager.QueryProcessor;

import java.io.BufferedReader;
import java.io.IOException;

public class Menu {
    private BufferedReader input = null;
    private UserCredentials currentUser = null;
    private String path = null;
    public Menu(BufferedReader input, UserCredentials currentUser, String path) {
        this.input = input;
        this.currentUser = currentUser;
        this.path = path;
    }

    public void displayMenu(){
        try {
            int option = 0;
            System.out.println("Welcome to Distributed Database Management System");
            do {
                System.out.println("Kindly select one of the below options");
                System.out.println("1. Write Queries\n2. Export\n3. Data Model\n4. Analytics");
                System.out.print("Enter your choice (enter 0 to logout): ");
                option = Integer.parseInt(input.readLine());
                switch (option) {
                    case 0:
                        // by default we donot do anything as the user want's to quit
                        currentUser = null;
                        System.out.println("Thank you, you have been successfully logged out");
                        break;
                    case 1:
                        QueryProcessor qp = new QueryProcessor(input,currentUser, path);
                        qp.begin();
                        break;
                    case 2:
                        ExportEngine exportEngine = new ExportEngine(input,currentUser, path);
                        exportEngine.begin();
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    default:
                        System.out.println("Invalid option selected, Try again");
                        option = -1;
                        break;
                }

            } while (option != 0);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
