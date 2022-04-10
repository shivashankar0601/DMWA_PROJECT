package com.example.project.DataModeling;

        import com.example.project.DataExport.ExportEngine;
        import com.example.project.UIAndSecurity.UserCredentials;
        import com.example.project.Utilities.Utils;

        import java.io.*;
        import java.util.Arrays;
        import java.util.List;
        import java.util.concurrent.TimeUnit;

public class DataModelingEngine {
    private BufferedReader input = null;
    private UserCredentials currentUser = null;
    private String path = null;
    public DataModelingEngine(BufferedReader input, UserCredentials currentUser, String path){
        this.input = input;
        this.currentUser = currentUser;
        this.path = path;
    }

    List<String> dbs = null;

    public void begin(){
        String ipt = null;
        try {
            dbs = ExportEngine.getAllAvailableDBs();

            if (dbs.size() == 0) {
                System.err.println("no databases available to modeling");
                return;
            }

            do {
                System.out.println("\nAvailable databases for data modeling :");

                for (String s : dbs) {
                    System.out.println(s);
                }

                System.out.print("Enter the name of the database to be modeled (press 0 to exit):");

                ipt = input.readLine();

                if (ipt.equalsIgnoreCase("0"))
                    break;

                if (dbs.contains(ipt.trim())) {

                    String tables = ExportEngine.getAllAvailableTables(ipt, true);
                    if(tables==null || tables.length()==0){
                        System.err.println("no tables in the database for modeling");
                        try {
                            TimeUnit.SECONDS.sleep(1);
                            break;
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                    else{
                        // perform the data export operation

//                        if(createExportFile(Arrays.asList(tables.split(Utils.delimiter)), ipt.trim())){
//                            File cPath = new File(".");
//                            System.out.println(cPath.getCanonicalPath());
//                            System.out.println(ipt.trim() + " Database exported successfully to "+cPath.getCanonicalPath()+"\\"+ipt.trim()+"_exported_data.sql");
//                        }
//                        else{
//                            System.err.println(ipt.trim() + " Database export failed");
//                        }

                    }

                } else {
                    System.err.println("invalid option, try again ! ");
                    TimeUnit.SECONDS.sleep(1);
                }

            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void working() throws IOException {






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