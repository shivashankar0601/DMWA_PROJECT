package com.example.project.DataExport;

import com.example.project.Utilities.Utils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportEngineTest {
    ExportEngine e = new ExportEngine(null,null,null);
    @Test
    void begin() {
    }

    @Test
    void getAllAvailableTables() {
        String lst = e.getAllAvailableTables("shiva",false);
        System.out.println("succeeded");
    }

    @Test
    void prepareColumns() {
//        String [] lst = {"name varchar 100","id int"};
//        assertEquals("name varchar (100), id int",e.prepareColumns(lst,2));

        String [] lst = {"name varchar 100","id int","orderid int","primary key orderid","foreign key personid references persons personid"};
        assertEquals("name varchar (100), id int, orderid int",e.prepareColumns(lst,3));


    }
}