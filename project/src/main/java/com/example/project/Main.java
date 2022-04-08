package com.example.project;

import com.example.project.UIAndSecurity.LoginPage;

public class Main {
    public static void main(String[] args) {
        // by default, we will show login page to the end user
        LoginPage loginPage = new LoginPage();
        loginPage.loginMenu();
        // to stop the server, we exit with code 0
        System.exit(0);
    }
}
