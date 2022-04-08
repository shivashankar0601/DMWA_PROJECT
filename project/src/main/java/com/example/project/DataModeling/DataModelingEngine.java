package com.example.project.DataModeling;

import com.example.project.UIAndSecurity.UserCredentials;

import java.io.BufferedReader;

public class DataModelingEngine {
    private BufferedReader input = null;
    private UserCredentials currentUser = null;
    private String path = null;
    public DataModelingEngine(BufferedReader input, UserCredentials currentUser, String path){
        this.input = input;
        this.currentUser = currentUser;
        this.path = path;
    }
    public void begin() {
        // you can ask user input from here and start the modeling and show either on console or write to a file. its up to you
    }
}
