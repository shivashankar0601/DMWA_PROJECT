import java.io.*;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class LoginPage {

    private UserCredentials currentUser = null;
    private final String userProfilesFilePath = "./resources/User_Profile.txt";

    public void loginMenu(){
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            Map<String, UserCredentials> userCredentials = getCredentialsFromUserProfilesFile(userProfilesFilePath);

            int option = 0;

            System.out.println("Welcome to Distributed Database Management System");
            do {
                System.out.print("Kindly select one of the below options\n1. Login\n2. Register\nkindly enter your selection (enter 0 to quit): ");
                option = Integer.parseInt(input.readLine());
                switch(option){
                    case 0:
                        // by default we do not do anything as the user want's to quit
                        break;
                    case 1:
                        authenticateUser(userCredentials, input);
                        break;
                    case 2:
                        createNewUser(userCredentials,input,userProfilesFilePath);
                        userCredentials = getCredentialsFromUserProfilesFile(userProfilesFilePath); // updating credentials with most recent data
                        break;
                    default:
                        System.out.println("Invalid option selected, Try again");
                        option = -1;
                        break;
                }
            } while (option != 0);
        }
        catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
    }


    private Map<String,UserCredentials> getCredentialsFromUserProfilesFile(String s) {
        Map<String, UserCredentials> credentials = new HashMap<String, UserCredentials>();
        try {
            BufferedReader userProfile = new BufferedReader(new FileReader(s));
            String line = null;
            while ((line = userProfile.readLine() )!= null) {
                String [] vals = line.split(Utils.delemiter);
                if(vals.length==8){
                    credentials.put(vals[0], new UserCredentials(vals[0],vals[1],vals[2],vals[3],vals[4],vals[5],vals[6],vals[7]));
                }
            }
        }catch (FileNotFoundException ex) {
            System.out.println(ex.getLocalizedMessage());
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
        return credentials;
    }

    private void createNewUser(Map<String, UserCredentials> credentials, BufferedReader input, String path) {
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while(true) {
                System.out.print("Kindly enter username : ");
                line = Utils.hashWithMD5(input.readLine());
                if(credentials.containsKey(line)){
                    System.out.println("Username already in use, kindly try a different one");
                }
                else
                    break;
            }
            sb.append(line.trim()+Utils.delemiter);
            System.out.print("Kindly enter password : ");
            line = Utils.hashWithMD5(input.readLine());
            sb.append(line.trim()+Utils.delemiter);
            System.out.print("Kindly enter your 1st security question : ");
            line = input.readLine();
            sb.append(line.trim()+Utils.delemiter);
            System.out.print("Kindly enter your answer : ");
            line = input.readLine();
            sb.append(line.trim()+Utils.delemiter);
            System.out.print("Kindly enter your 2nd security question : ");
            line = input.readLine();
            sb.append(line.trim()+Utils.delemiter);
            System.out.print("Kindly enter your answer : ");
            line = input.readLine();
            sb.append(line.trim()+Utils.delemiter);
            System.out.print("Kindly enter your 3rd security question : ");
            line = input.readLine();
            sb.append(line.trim()+Utils.delemiter);
            System.out.print("Kindly enter your answer : ");
            line = input.readLine();
            sb.append(line.trim());
            FileWriter fileWriter = new FileWriter(path,true);
            fileWriter.append((credentials.size()>0?"\n":"")+sb.toString());
            fileWriter.close();
            System.out.println("User created successfully");
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private void authenticateUser(Map<String, UserCredentials> credentials, BufferedReader input) {
        String username = null;
        String password = null;
        boolean isUserValidated = false;
        try {
            do {
                System.out.print("Kindly enter username : ");
                username = Utils.hashWithMD5(input.readLine());
                System.out.print("Kindly enter password : ");
                password = Utils.hashWithMD5(input.readLine());
                if (credentials.containsKey(username)) {
                    if (credentials.get(username).getPassword().equalsIgnoreCase(password)) {
                        isUserValidated = true;
                        this.currentUser = credentials.get(username);
                        System.out.println("user validated successfully");
                    } else
                        System.out.println("Invalid password, Try again");
                } else
                    System.out.println("Invalid credentials, Try again");

            } while (!isUserValidated);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
        Menu menu = new Menu();
        menu.displayMenu(input);

    }


}
