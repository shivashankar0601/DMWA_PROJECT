package com.example.project.DistributedDatabaseLayer;

import com.example.project.QueryManager.DatabaseProcessor;
import com.example.project.Utilities.Utils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class Responder {

    // / gdd?name=dbName
    @RequestMapping(value = "/gdd", params="checkDB", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String checkIfDatabaseExists(@RequestParam(value="checkDB", defaultValue = "") String dbName){
        return DatabaseProcessor.checkDBFromGDD(dbName);
    }

    @RequestMapping(value = "/gdd", params="addDB", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean addDBToGDD(@RequestParam(value="addDB", defaultValue = "") String dbName, @RequestParam(value="vm", defaultValue = "") String vm){
        return DatabaseProcessor.addDBToGDD(dbName,vm);
        //return false;
    }
    @RequestMapping(value = "/utils", params="setCDBN", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateCurrentDBName(@RequestParam(value="setCDBN", defaultValue = "") String dbName){
        if(dbName != null && dbName.length()>0)
            Utils.currentDbName = dbName;
        return Utils.currentDbName;
    }


}
