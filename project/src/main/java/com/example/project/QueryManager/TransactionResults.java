package com.example.project.QueryManager;

import java.util.List;

public class TransactionResults {
    public String tableName = null;
    public int columnCount = 0;
    public List< String > insertValues = null;

    public TransactionResults(String tableName, int columnCount, List< String > insertValues) {
        this.tableName = tableName;
        this.columnCount = columnCount;
        this.insertValues = insertValues;
    }
}
