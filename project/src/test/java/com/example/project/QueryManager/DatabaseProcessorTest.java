package com.example.project.QueryManager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseProcessorTest {

    DatabaseProcessor dp = new DatabaseProcessor();

    @Test
    void checkDBFromGDD() {
        assertEquals("vm1~vm2",DatabaseProcessor.checkDBFromGDD("shiva"));
    }
//
//    @Test
//    void createDatabaseWithSemicolon() {
//        assertEquals("shiva",dp.performOperation("create database shiva;"));
//    }
//
//    @Test
//    void createDatabaseWithOutSemicolon(){
//        assertEquals("dmwa",dp.performOperation("create database dmwa"));
//    }
//
//    @Test
//    void createDatabaseWithUnderscore(){
//        assertEquals("dmwa_4",dp.performOperation("create database dmwa_4"));
//    }
//
//    @Test
//    void createDatabaseWithInvalidCharacter(){
//        assertEquals(null,dp.performOperation("create database dmwa$4"));
//    }
//    @Test
//    void useDatabaseWithSemicolon(){
//        assertEquals("./resources/shiva/shiva",dp.performOperation("use shiva;"));
//    }
//
//    @Test
//    void useDatabaseWithoutSemicolon(){
//        assertEquals("./resources/shiva/shiva",dp.performOperation("use shiva"));
//    }
//
//    @Test
//    void useDatabaseWithUnderscore(){
//        assertEquals("./resources/shiva/dmwa_4",dp.performOperation("use dmwa_4"));
//    }
//
//
//    @Test
//    void useDatabaseWithInvalidCharacter(){
//        assertEquals(null,dp.performOperation("use $hiva;"));
//    }
//




}