package com.example.project.DistributedDatabaseLayer;

import com.example.project.DataExport.ExportEngine;
import com.example.project.QueryManager.DatabaseProcessor;
import com.example.project.QueryManager.TableProcessor;
import com.example.project.Utilities.Utils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Responder {

    // / gdd?name=dbName
    @RequestMapping(value = "/gdd", params = "checkDB", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String checkIfDatabaseExists(@RequestParam(value = "checkDB", defaultValue = "") String dbName) {
        return DatabaseProcessor.checkDBFromGDD(dbName);
    }

    @RequestMapping(value = "/gdd", params = "addDB", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean addDBToGDD(@RequestParam(value = "addDB", defaultValue = "") String dbName, @RequestParam(value = "vm", defaultValue = "") String vm) {
        return DatabaseProcessor.addDBToGDD(dbName, vm);
        //return false;
    }

    @RequestMapping(value = "/gdd", params = "list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllDBsFromGdd(@RequestParam(value = "list", defaultValue = "") String all) {
        String res = "";
        List<String> lst = ExportEngine.getAllAvailableDBs();
        StringBuilder sb = new StringBuilder();
        if (lst.size() > 0) {
            for (String s : lst) {
                sb.append(s);
                sb.append(Utils.delimiter);
            }
            res = sb.toString();
            return res.substring(0, res.length() - 1);
        } else
            return res;
    }

    @RequestMapping(value = "/utils", params = "setCDBN", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateCurrentDBName(@RequestParam(value = "setCDBN", defaultValue = "") String dbName) {
        if (dbName != null && dbName.length() > 0)
            Utils.currentDbName = dbName;
        return Utils.currentDbName;
    }

    @RequestMapping(value = "/query", params = "insert", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String performInsertQuery(@RequestParam(value = "insert", defaultValue = "") String query, @RequestParam(value = "flag", defaultValue = "") String flag, @RequestParam(value = "isTransaction", defaultValue = "") Boolean isTransaction) {
        return TableProcessor.insertIntoQuery(query, flag, isTransaction);
    }

    @RequestMapping(value = "/query", params = "update", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String performUpdateQuery(@RequestParam(value = "update", defaultValue = "") String query, @RequestParam(value = "flag", defaultValue = "") String flag, @RequestParam(value = "isTransaction", defaultValue = "") Boolean isTransaction) {
        return TableProcessor.updateQuery(query, flag, isTransaction);
    }


    @RequestMapping(value = "/query", params = "delete", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String performDeleteQuery(@RequestParam(value = "delete", defaultValue = "") String query, @RequestParam(value = "flag", defaultValue = "") String flag, @RequestParam(value = "isTransaction", defaultValue = "") Boolean isTransaction) {
        return TableProcessor.deleteQuery(query, flag, isTransaction);
    }

    @RequestMapping(value = "/query", params = "select", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String performSelectQuery(@RequestParam(value = "select", defaultValue = "") String query, @RequestParam(value = "flag", defaultValue = "") String flag) {
        return TableProcessor.selectQuery(query, flag);
    }



    @RequestMapping(value = "/tables", params = "db", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllTablesFromDB(@RequestParam(value = "db", defaultValue = "") String dbName, @RequestParam(value = "vm", defaultValue = "") boolean shouldRequestVM) {
        return ExportEngine.getAllAvailableTables(dbName, shouldRequestVM);
    }




}
