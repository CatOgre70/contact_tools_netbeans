package contacttools;

import static contacttools.CommonTools.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class ContactItem {

    private final String[] headers;
    private int id;
    private final String[] values;

    ContactItem(){
        int n = AppGlobalSettings.numberOfColumns;
        headers = new String[n];
        System.arraycopy(AppGlobalSettings.columnHeaders, 0, headers, 0, n);
        id = 0;
        values = new String[n-1];
    }

    ContactItem(int i, String[] s){
        id = i;
        int n = AppGlobalSettings.numberOfColumns;
        headers = new String[n];
        System.arraycopy(AppGlobalSettings.columnHeaders, 0, headers, 0, n);
        values = new String[n-1];
        System.arraycopy(s, 0, values, 0, n-1);
    }

    int getId(){
        return id;
    }

    String getValueByIndex(int i){
        return values[i];
    }

    String getValueByHeader(String s){
        int i = 0;
        for(int j = 1; j < AppGlobalSettings.numberOfColumns; j++)
            if(headers[j].equals(s)) {
                i = j;
                break;
            }
        if(i == 0) {
            System.out.println("Error: Header not found!");
            return null;
        }
        return values[i-1];
    }

    void setId(int i){
        id = i;
    }

    void setValueByIndex(int i, String s){
        values[i] = s;
    }

    void setValueByHeader(String h, String s){
        int i = 0;
        for(int j = 1; j < AppGlobalSettings.numberOfColumns; j++)
            if(headers[j].equals(h)) {
                i = j;
                break;
            }
        if(i == 0) {
            System.out.println("Error: Header not found!");
            return;
        }
        values[i-1] = s;
    }

    void copy(ContactItem ci){
        this.setId(ci.getId());
        for(int i = 0; i < AppGlobalSettings.numberOfColumns-1; i++){
            this.setValueByIndex(i, ci.getValueByIndex(i));
        }
    }

    public void println(){
        StringBuilder str = new StringBuilder();
        for(int j = 0; j < AppGlobalSettings.numberOfColumns-1; j++) {
            str.append(" ");
            str.append(this.values[j]);
        }
        System.out.println("id = " + this.id + "," + str);
    }

    public void print() {
        StringBuilder str = new StringBuilder();
        for (int j = 0; j < AppGlobalSettings.numberOfColumns - 1; j++) {
            str.append(" ");
            str.append(this.values[j]);
        }
        System.out.print("id = " + this.id + "," + str);
    }

    public static ContactItem[] increaseCIArray(ContactItem[] ciArray){
        if (ciArray == null) {
            ciArray = new ContactItem[1];
            ciArray[0] = new ContactItem();
        } else {
            ContactItem[] ciArrayNew = new ContactItem[ciArray.length+1];
            System.arraycopy(ciArray, 0, ciArrayNew, 0, ciArray.length);
            ciArrayNew[ciArray.length] = new ContactItem();
            ciArray = ciArrayNew;
        }
        return ciArray;
    }

    public static ContactItem[] deleteArrayFirstRow(ContactItem[] ciArray){
        if(ciArray.length == 1){
            System.out.println("Error with ciArray: only one row is in it");
            return null;
        } else {
            ContactItem[] ciArrayNew = new ContactItem[ciArray.length-1];
            System.arraycopy(ciArray, 1, ciArrayNew, 0, ciArray.length-1);
            ciArray = ciArrayNew;
            return ciArray;
        }
    }

    public static ContactItem[] deleteArrayRow(ContactItem[] ciArray, int i){

        if(ciArray.length == 1){
            System.out.println("Error in ciArray: only one row is in it");
            return null;
        } else {
            ContactItem[] ciArrayNew = new ContactItem[ciArray.length-1];
            System.arraycopy(ciArray, 0, ciArrayNew, 0, i);
            for(int m = i+1; m < ciArray.length; m++)
                ciArrayNew[m-1] = ciArray[m];
            ciArray = ciArrayNew;
            return ciArray;
        }
    }

    public void normalizeEmail(){

        String email = this.getValueByHeader("email");
        // Preliminar processing
        if("".equals(email)){
            System.out.println("Warning: wrong email format in Input1 file, line:");
            this.println();
            return;
        }
        email = email.strip();
        if("".equals(email)){
            System.out.println("Warning: wrong email format in Input1 file, line:");
            this.println();
            return;
        }
        email = email.toLowerCase();
        int emailLength = email.length();

        // Looking for '@' character
        boolean isFound = false;
        int commercialAtPosition = -1;
        for(int i = 0; i < emailLength; i++){
            if(email.charAt(i) == '@') {
                if(!isFound){
                    isFound = true;
                    commercialAtPosition = i;
                }else{
                    System.out.println("Warning: wrong email format in Input1 file, line:");
                    this.println();
                }
            }
        }

        if(!isFound | (commercialAtPosition == 0)){
            System.out.println("Warning: wrong email format in Input1 file, line:");
            this.println();
            return;
        }

        // Looking for last delimiter before '@'
        int lastDelimiterPosition = -1;
        for(int i = 0; i < commercialAtPosition; i++){
            char ch = email.charAt(i);
            if( (ch == ' ') | (ch == ',') | (ch == '\"') | (ch == '>') | (ch == '<')) {
                lastDelimiterPosition = i;
            }
        }

        if(lastDelimiterPosition == (commercialAtPosition-1)){
            System.out.println("Warning: wrong email format in Input1 file, line:");
            this.println();
            return;
        }

        // Looking for first delimiter after '@'
        int firstDelimiterPosition = emailLength;
        for(int i = commercialAtPosition + 1; i < emailLength; i++){
            char ch = email.charAt(i);
            if( (ch == ' ') | (ch == ',') | (ch == '\"') | (ch == '>') | (ch == '<')) {
                firstDelimiterPosition = i;
                break;
            }
        }

        if(firstDelimiterPosition == (commercialAtPosition+1)){
            System.out.println("Warning: wrong email format in Input1 file, line:");
            this.println();
            return;
        }

        // Substitute e-mail address from the email String, from lastDelimiterPosition
        // to firstDelimiterPosition
        if(lastDelimiterPosition == -1){
            if(firstDelimiterPosition != emailLength)
                email = email.substring(0,firstDelimiterPosition);
        } else {
            if(firstDelimiterPosition == emailLength)
                email = email.substring(lastDelimiterPosition+1);
            else
                email = email.substring(lastDelimiterPosition+1,firstDelimiterPosition);
        }
        // System.out.println("Processed email field: " + email);
        this.setValueByHeader("email", email);
    }

    public static ContactItem[] readCIArrayFromDB(AppGlobalSettings globalSettings){

        // ArrayList of Contact Item Strings
        ArrayList<ContactItem> ciA = new ArrayList<>();

        // Construct the query for MySQL
        String Query = "select ";
        int n = AppGlobalSettings.numberOfColumns;
        StringBuilder str = new StringBuilder();

        for(int i = 0; i < n-1; i++) {
            str.append(AppGlobalSettings.columnHeaders[i]);
            str.append(", ");
        }
        str.append(AppGlobalSettings.columnHeaders[n-1]);
        str.append(" from ");
        str.append(AppGlobalSettings.mySQLServerTable);
        Query = Query + str;

        // Open mySQL Connection and read Items
        try (Connection connection = DriverManager
                .getConnection(AppGlobalSettings.mySQLServerURL, AppGlobalSettings.mySQLServerUser,
                        AppGlobalSettings.mySQLServerPassword);

             // Step 2:Create a statement using connection object
             Statement stmt = connection.createStatement();

             // Step 3: Execute the query or update query
             ResultSet rs = stmt.executeQuery(Query)) {

            // Step 4: Process the ResultSet object.
            while (rs.next()) {

                ContactItem ciItem = new ContactItem();
                // ciArray = ContactItem.increaseCIArray(ciArray);

                ciItem.setId(rs.getInt(AppGlobalSettings.columnHeaders[0]));

                for(int i = 0; i < AppGlobalSettings.numberOfColumns-1; i++) {
                    ciItem.setValueByIndex(i, rs.getString(AppGlobalSettings.columnHeaders[i+1]));
                    if(ciItem.getValueByIndex(i) == null){
                        ciItem.setValueByIndex(i, "");
                    }
                }
                ciA.add(ciItem);
            }

        } catch (SQLException e) {
            printSQLException(e);
        }

        // Create ciArray and copy ArrayList ciA to ciArray
        ContactItem[] ciArray = new ContactItem[ciA.size()];
        ciA.toArray(ciArray);
        return ciArray;
    }

    public static void writeCIArrayToDB(ContactItem[] ciArray, AppGlobalSettings globalSettings){

        // Export ContactsItems Array in the MySQL database
        if(ciArray == null){
            System.out.println("There aren't new contacts in the input file. Nothing to do");
            return;
        }

        String QUERY = "insert into " + AppGlobalSettings.mySQLServerTable + " (";
        StringBuilder str = new StringBuilder();

        int n = AppGlobalSettings.numberOfColumns;
        for(int i = 0; i < n-1; i++) {
            str.append(AppGlobalSettings.columnHeaders[i]);
            str.append(", ");
        }
        str.append(AppGlobalSettings.columnHeaders[n-1]);
        str.append(") " + "values (");
        for(int i = 0; i < n-1; i++)
            str.append("?,");
        str.append("?)");
        QUERY = QUERY + str;

        // System.out.println("QUERY1= " + QUERY1);

        try {
            // Create MySQL database connection
            Connection conn = DriverManager
                    .getConnection(AppGlobalSettings.mySQLServerURL,
                            AppGlobalSettings.mySQLServerUser,
                            AppGlobalSettings.mySQLServerPassword);


            for(ContactItem ciArray1 : ciArray) {
                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(QUERY);
                preparedStmt.setInt(1, ciArray1.getId());
                for (int m = 0; m < n - 1; m++) {
                    preparedStmt.setString(m+2, ciArray1.getValueByIndex(m));
                }
                // execute the preparedstatement
                preparedStmt.execute();
            }

            conn.close();

        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public static void updateCIArrayToDB(ContactItem[] ciArray){

        // Update ContactsItems Array in the MySQL database
        if(ciArray == null){
            System.out.println("There aren't updated contacts in the input file. Nothing to do");
            return;
        }

        String QUERY2 = "update " + AppGlobalSettings.mySQLServerTable + " set ";

        int n = AppGlobalSettings.numberOfColumns;
        for(int i = 0; i < n-1; i++)
            QUERY2 = QUERY2 + ciArray[0].headers[i] + " = ?, ";
        QUERY2 = QUERY2 + ciArray[0].headers[n-1] + " = ? where " + ciArray[0].headers[0] + " = ?;";

        System.out.println("QUERY2 = " + QUERY2);

        try {
            // Create MySQL database connection
            Connection conn = DriverManager
                    .getConnection(AppGlobalSettings.mySQLServerURL,
                            AppGlobalSettings.mySQLServerUser,
                            AppGlobalSettings.mySQLServerPassword);


            for (ContactItem ciArray1 : ciArray) {
                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(QUERY2);
                preparedStmt.setInt(1, ciArray1.getId());
                for (int i = 1; i < n; i++) {
                    preparedStmt.setString(i+1, ciArray1.getValueByIndex(i-1));
                }
                preparedStmt.setInt(n+1, ciArray1.getId());
                // execute the preparedstatement
                preparedStmt.executeUpdate();
            }

            conn.close();

        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public static void deleteCIArrayFromDB(ContactItem[] ciArray){
        // Delete ContactsItems Array from the MySQL database
        if(ciArray == null){
            System.out.println("There aren\'t contacts to delete in the input file. Nothing to do");
            return;
        }

        String QUERY3 = "delete from " + AppGlobalSettings.mySQLServerTable + " where id = ?";

        System.out.println("QUERY3 = " + QUERY3);

        try {
            // Create MySQL database connection
            Connection conn = DriverManager
                    .getConnection(AppGlobalSettings.mySQLServerURL,
                            AppGlobalSettings.mySQLServerUser,
                            AppGlobalSettings.mySQLServerPassword);

            for(int k = 0; k < ciArray.length; k++){
                // create the mysql insert preparedstatement
                PreparedStatement preparedStmt = conn.prepareStatement(QUERY3);
                preparedStmt.setInt(1,ciArray[k].getId());
                // execute the preparedstatement
                preparedStmt.executeUpdate();
            }

            conn.close();

        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public static void exportCIArrayToCSVfile(String csvFileName, ContactItem[] ciArray){

        try (FileWriter fw = new FileWriter(csvFileName)) {

            String str;
            // char separator = AppGlobalSettings.excelCSVSeparator;
            char separator = '\t';
            //char eoString = AppGlobalSettings.excelCSVEol2;
            char eoString = '\n';

            for (int j = 0; j < ciArray.length - 1; j++) {
                str = Integer.toString(ciArray[j].getId()) + separator;
                for(int i = 0; i < AppGlobalSettings.numberOfColumns-2; i++){
                    str = str + ciArray[j].getValueByIndex(i) + separator;
                }
                str = str + ciArray[j].getValueByIndex(AppGlobalSettings.numberOfColumns-2)
                        + eoString;
                fw.write(str);
            }

            str = Integer.toString(ciArray[ciArray.length-1].getId()) + separator;
            for(int i = 0; i < AppGlobalSettings.numberOfColumns-2; i++){
                str = str + ciArray[ciArray.length-1].getValueByIndex(i) + separator;
            }
            str = str + ciArray[ciArray.length-1].getValueByIndex(AppGlobalSettings.numberOfColumns-2);
            fw.write(str);

            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

