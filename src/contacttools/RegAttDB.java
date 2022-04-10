/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contacttools;

import static contacttools.CommonTools.*;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author vdemin
 */
public class RegAttDB {
    private static String headers[] = { "attendance_id", "fk_event_id", 
            "fk_id", "registration_status", "attendance_status" };
    private int regAttDB[];
    
    RegAttDB(){
        this.regAttDB = new int[5];
        for(int i = 0; i < 5; i++) this.regAttDB[i] = 0;
    }
    
    RegAttDB(int[] values){
        this.regAttDB = new int[5];
        if(values.length != 5) {
            System.out.println("Error in class RegAttDB constructor argument: shoud be int array of length 5!");
            return;
        }
        System.arraycopy(this.regAttDB, 0, values, 0, 5);
    }
    
    RegAttDB(int a, int b, int c, int d, int e){
        this.regAttDB = new int[5];
        this.regAttDB[0] = a;
        this.regAttDB[1] = b;
        this.regAttDB[2] = c;
        this.regAttDB[3] = d;
        this.regAttDB[4] = e;
    }
    
    public static String getHeaderByIndex(int i){
        if((i >= 0) & i < headers.length) {
            return headers[i];
        } else {
            System.out.println("Error in RegAttDB class static method getHeaderByIndex: wrong function argument (index)");
            return null;
        }
    }
    
    public int getValueByIndex(int i) {
        if( (i < 0) | (i >= 5)){
            System.out.println("Error in RegAttDB.getValueByIndex function argument: shoud be 1 <= i <= 5!");
            return 0;
        }
        return this.regAttDB[i];
    }
    
    public void setValueByIndex(int i, int v) {
        this.regAttDB[i] = v;
    }
    
    public int getValueByHeader(String h){
        for(int i = 0; i < 5; i++)
            if(RegAttDB.headers[i].compareTo(h) == 0) return this.regAttDB[i];
        System.out.println("Error in RegAttDB.getValueByHeader method: no such header");
        return -1;
    }
    
    public void setValueByHeader(String h, int v){
        boolean found = false;
        for(int i = 0; i < 5; i++)
            if(RegAttDB.headers[i].compareTo(h) == 0) {
                this.regAttDB[i] = v;
                found = true;
            }
        if(!found)
            System.out.println("Error in RegAttDB.setValueByHeader method: no such header");
    }
    
    public static RegAttDB[] readRADBfromMySQL(AppGlobalSettings globalSettings){
        
        // Array of Registration and Attendance Items
        ArrayList<RegAttDB> raDBA = new ArrayList<>();
        
        // Construct the query for MySQL
        String Query = "select ";
        int n = 5; // Number of columns in attend_reg_status DB table
        
        for(int i = 0; i < n-1; i++)
            Query = Query + RegAttDB.headers[i] + ", ";
        Query = Query + RegAttDB.headers[n-1]
                + " from attend_reg_status";
        
        // Open mySQL Connection and read Items
        try (Connection connection = DriverManager
            .getConnection(globalSettings.mySQLServerURL, globalSettings.mySQLServerUser, 
                    globalSettings.mySQLServerPassword);

            // Step 2:Create a statement using connection object
            Statement stmt = connection.createStatement();

            // Step 3: Execute the query or update query
            ResultSet rs = stmt.executeQuery(Query)) {

            // Step 4: Process the ResultSet object.
            
            while (rs.next()) {
                RegAttDB raDBItem = new RegAttDB();
                for(int i = 0; i < n; i++)
                    raDBItem.setValueByIndex(i, rs.getInt(RegAttDB.headers[i]));
                raDBA.add(raDBItem);
            }
            
        } catch (SQLException e) {
            printSQLException(e);
        }
        
        RegAttDB[] raDB = new RegAttDB[raDBA.size()];
        raDBA.toArray(raDB);
        return raDB;
        
    }
    
    public static void writeRADBToMySQL(RegAttDB[] raInDB, AppGlobalSettings globalSettings){
        if(raInDB == null){
            System.out.println("There aren\'t new registration data in the input file. Nothing to do");
            return;
        }
        
        String QUERY = "insert into attend_reg_status (";
        
        int n = 5; // Number of columns in the RegAttDB
        for(int i = 0; i < n-1; i++)
            QUERY = QUERY + RegAttDB.headers[i] + ", ";
        QUERY = QUERY + RegAttDB.headers[n-1] + ") " + "values (";
        for(int i = 0; i < n-1; i++)
            QUERY = QUERY + "?,";
        QUERY = QUERY +"?)";
        
        // System.out.println("QUERY1= " + QUERY);
        
        try {
            // Create MySQL database connection
            Connection conn = DriverManager
            .getConnection(globalSettings.mySQLServerURL, 
                    globalSettings.mySQLServerUser, 
                    globalSettings.mySQLServerPassword);

            
            for(int k = 0; k < raInDB.length; k++){
                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(QUERY);
                for(int i = 0; i < 5; i++)
                    preparedStmt.setInt(i+1,raInDB[k].getValueByIndex(i));
                // execute the preparedstatement
                preparedStmt.execute();
            }
            
            conn.close();
           
        } catch (SQLException e) {
            printSQLException(e);
        }
    }
    
    public static void updateRADBToMySQL(RegAttDB[] raUpdDB, AppGlobalSettings globalSettings){
        if(raUpdDB == null){
            System.out.println("There aren\'t changed registration data in the input file. Nothing to do");
            return;
        }
        
        String QUERY = "update attend_reg_status set ";
        
        int n = 5; // Number of columns in the RegAttDB
        for(int i = 0; i < n-1; i++)
            QUERY = QUERY + RegAttDB.headers[i] + " = ?, ";
        QUERY = QUERY + RegAttDB.headers[n-1] + " = ? where " + RegAttDB.headers[0] + " = ?;";
                
        System.out.println("QUERY1= " + QUERY);
        
        try {
            // Create MySQL database connection
            Connection conn = DriverManager
            .getConnection(globalSettings.mySQLServerURL, 
                    globalSettings.mySQLServerUser, 
                    globalSettings.mySQLServerPassword);

            
            for(int k = 0; k < raUpdDB.length; k++){
                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(QUERY);
                for(int i = 0; i < 5; i++)
                    preparedStmt.setInt(i+1,raUpdDB[k].getValueByIndex(i));
                preparedStmt.setInt(6, raUpdDB[k].getValueByIndex(0));
                // execute the preparedstatement
                preparedStmt.executeUpdate();
            }
            
            conn.close();
           
        } catch (SQLException e) {
            printSQLException(e);
        }
    }
    
    public void print(){
        String str = "";
        
        for(int i = 0; i <5 ; i++)
            str = str + this.regAttDB[i] + ", ";
        
        System.out.print(str);
    }
    
    public void println(){
        String str = "";
        
        for(int i = 0; i <5 ; i++)
            str = str + this.regAttDB[i] + ", ";
        
        System.out.println(str);
    }
    
}
