package com.example.project.UIAndSecurity;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.example.project.Utilities.Utils;

// remaining items from login page
// should write logic to check if the user_profile file exists, if it does not we will create one and then append the information
// we should implement login such as GCKEY with security questions that are randomly picked

public class LoginPage {

    private UserCredentials currentUser = null;

    public void loginMenu(){
        try {
            // loading all the configuration parameters needed from configuration.tsv
            try {
                Utils.loadConfiguration();
                System.out.println("configuration loaded successfully");
            }
            catch(Exception e){
                // if there was an error configuring the db, then there is no point in moving forward with those errors, so we exit
                e.printStackTrace();
                System.exit(1);
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            Map<String, UserCredentials> userCredentials = getCredentialsFromUserProfilesFile(Utils.userProfiles);

            int option = 0;

            System.out.println("Welcome to Distributed Database Management System on "+Utils.currentDevice);
            do {
                System.out.print("Kindly select one of the below options\n1. Login\n2. Register\nkindly enter your selection (enter 0 to exit): ");
                option = Integer.parseInt(input.readLine());
                switch(option){
                    case 0:
                        // by default we do not do anything as the user want's to quit
                        break;
                    case 1:
                        authenticateUser(userCredentials, input);
                        break;
                    case 2:
                        createNewUser(userCredentials,input,Utils.userProfiles);
                        userCredentials = getCredentialsFromUserProfilesFile(Utils.userProfiles); // updating credentials with most recent data
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
                String [] vals = line.split(Utils.delimiter);
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
        String user = null;
        String line = null;
        try {// username and password
            while(true) {
                System.out.print("Kindly enter username : ");
                user = input.readLine();
                line = Utils.hashWithMD5(user);
                if(credentials.containsKey(line)){
                    System.out.println("Username already in use, kindly try a different one");
                }
                else
                    break;
            }
            sb.append(line.trim()+ Utils.delimiter);
            System.out.print("Kindly enter password : ");
            line = Utils.hashWithMD5(input.readLine());
            sb.append(line.trim()+ Utils.delimiter);
            System.out.print("Kindly enter your 1st security question : ");
            line = input.readLine();
            sb.append(line.trim()+ Utils.delimiter);
            System.out.print("Kindly enter your answer : ");
            line = input.readLine();
            sb.append(line.trim()+ Utils.delimiter);
            System.out.print("Kindly enter your 2nd security question : ");
            line = input.readLine();
            sb.append(line.trim()+ Utils.delimiter);
            System.out.print("Kindly enter your answer : ");
            line = input.readLine();
            sb.append(line.trim()+ Utils.delimiter);
            System.out.print("Kindly enter your 3rd security question : ");
            line = input.readLine();
            sb.append(line.trim()+ Utils.delimiter);
            System.out.print("Kindly enter your answer : ");
            line = input.readLine();
            sb.append(line.trim());
            FileWriter fileWriter = new FileWriter(path,true);
            fileWriter.append((credentials.size()>0?"\n":"")+sb.toString());
            fileWriter.close();
            if(Utils.createDirectory(Utils.resourcePath,user))
                System.out.println("User "+user+" created successfully");
            else
                throw new Exception("user directory creation error");
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private void authenticateUser(Map<String, UserCredentials> credentials, BufferedReader input) {
        String username = null;
        String password = null;
        String user = null;
        boolean isUserValidated = false;
        // implement
        try { // have to check the security questions when they try to reset the password
            do {
                System.out.print("Kindly enter username : ");
                user = input.readLine();
                if(user.equalsIgnoreCase("0"))
                    break;
                username = Utils.hashWithMD5(user);
                System.out.print("Kindly enter password : ");
                password = Utils.hashWithMD5(input.readLine());

                if (credentials.containsKey(username)) {
                    if (credentials.get(username).getPassword().equalsIgnoreCase(password)) {

                        for(int j = 0;j<3; j++) {

                            // security questions just like GCKEY
                            String q[] = credentials.get(username).getRandomQuestion().split(Utils.delimiter);
                            System.out.println(q[0] + " ?");
                            if (!input.readLine().equalsIgnoreCase(q[1])) {
                                System.err.println("wrong answer for security question, kindly try again");
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                    if(j==2)
                                        return;
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                                continue;
                            }
                            else {
                                isUserValidated = true;
                                this.currentUser = credentials.get(username);
                                System.out.println("user validated successfully");
                                navigateToMenu(input, currentUser, username);
                                break;
                            }
                        }

                    } else
                        System.out.println("Invalid password, Try again");
                }
                else {
                    System.out.println("Invalid credentials, Try again");
                    System.err.println("enter 0 as username to exit:");
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }

            } while (!isUserValidated);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

    }

    private void navigateToMenu(BufferedReader input, UserCredentials currentUser, String user){
        if(currentUser!=null) {
            Menu menu = new Menu(input, currentUser, Utils.resourcePath + user + "/");
            menu.displayMenu();
        }
    }



}
