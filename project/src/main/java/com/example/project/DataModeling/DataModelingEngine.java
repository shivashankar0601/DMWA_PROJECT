package com.example.project.DataModeling;

        import com.example.project.UIAndSecurity.UserCredentials;
        import com.example.project.Utilities.Utils;

        import java.io.*;
public class DataModelingEngine {
    private BufferedReader input = null;
    private UserCredentials currentUser = null;
    private String path = null;
    public DataModelingEngine(BufferedReader input, UserCredentials currentUser, String path){
        this.input = input;
        this.currentUser = currentUser;
        this.path = path;
    }
    public void begin() throws IOException {
        // you can ask user input from here and start the modeling and show either on console or write to a file. its up to you
        File dbfolder = new File(Utils.resourcePath + Utils.currentDbName);
        File dataModel = new File(Utils.resourcePath + "dataModel.tsv");
        dataModel.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(dataModel));
        writer.write("Database:" + Utils.currentDbName + "\n");
        for (File table : dbfolder.listFiles()) {
            if(!table.getName().equals("metadata.tsv")) {
                writer.write("----------------------------------------------\n");
                writer.write("Table:" + table.getName().replace(".tsv", "") + "\n");
                writer.write(String.format("%20s %20s \r\n", "Column name", "Data type"));
                BufferedReader br = new BufferedReader(new FileReader(table));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] columnNames = line.split("~");
                    if(!line.startsWith("columnCount")) {
                        for (String cols:columnNames) {
                            String[] col = cols.split(" ");
                            if(!col[0].startsWith("primary") && !col[0].startsWith("foreign"))
                                writer.write(String.format("%20s %20s \r\n", col[0], col[1]));
                        }
                        writer.write("****\n");
                        writer.write(String.format("%20s %20s \r\n", "key", "Column"));
                        for (String cols:columnNames) {
                            String[] col = cols.split(" ");
                            if(col[0].startsWith("primary") || col[0].startsWith("foreign"))
                                writer.write(String.format("%20s %20s \r\n", col[0], col[2]));
                        }
                        break;
                    }
                }
            }
        }
        writer.close();
    }
}