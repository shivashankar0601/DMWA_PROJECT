package QueryManager;

import Utilities.Utils;

public class DatabaseProcessor {
    private String path = null;
    private String createDatabaseRegx = "^(create\\sdatabase\\s[0-9a-zA-Z_]+)[;]?$";
    private String useDatabaseRegx = "^(use\\s[0-9a-zA-Z_]+)[;]?$";

    public DatabaseProcessor(String path) {
        this.path = path;
    }

    public String performOperation(String query) {
        query = query.toLowerCase();
        String selectedDB = null;
        if(query.matches(createDatabaseRegx)) {
            selectedDB = (query.split("\s")[2]);
            if (selectedDB.contains(";")) // if there is a ; at the end, we should remove it (; is a optional parameter)
                selectedDB = selectedDB.substring(0, selectedDB.indexOf(';'));
            Utils.createUserDirectory(path,selectedDB);
            return path; // since the database is created but not selected we return "" string
        }
        else if(query.matches(useDatabaseRegx)) {
            selectedDB = (query.split("\s")[1]);
            if (selectedDB.contains(";")) // if there is a ; at the end, we should remove it (; is a optional parameter)
                selectedDB = selectedDB.substring(0, selectedDB.indexOf(';'));
            selectedDB = path+selectedDB;
        }
        return selectedDB;
    }



}
