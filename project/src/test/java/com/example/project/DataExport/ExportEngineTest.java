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
}