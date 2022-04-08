package com.example.project.DataExport;

import com.example.project.UIAndSecurity.UserCredentials;

import java.io.BufferedReader;

public class ExportEngine {
    private UserCredentials user = null;
    private BufferedReader input = null;
    private String path = null;

    public ExportEngine(BufferedReader input, UserCredentials currentUser, String path) {
        this.user=currentUser;
        this.input = input;
        this.path = path;
    }

    public void begin() {
        // write the whole logic of exporting taking this method as a starting point
    }
}
