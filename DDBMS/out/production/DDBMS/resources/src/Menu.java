import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class Menu {
    public void displayMenu(BufferedReader input){
        try {
            int option = 0;
            System.out.println("Welcome to Distributed Database Management System");
            do {
                System.out.println("Kindly select one of the below options");
                System.out.println("1. Write Queries\n2. Export\n3. Data Model\n4. Analytics");
                System.out.print("Enter your choice (enter 0 to exit): ");
                option = Integer.parseInt(input.readLine());
                switch (option) {
                    case 0:
                        // by default we donot do anything as the user want's to quit
                        break;
                    case 1:
                        break;
                    case 2:
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
