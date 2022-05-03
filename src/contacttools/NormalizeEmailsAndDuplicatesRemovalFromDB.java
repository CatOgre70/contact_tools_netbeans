package contacttools;

import static contacttools.CommonTools.printSQLException;
import java.sql.*;
import java.util.ArrayList;

public class NormalizeEmailsAndDuplicatesRemovalFromDB {

    public static void main(String[] args){

        // Initialize Global Application Settings and read them from CNF file
        AppGlobalSettings globalSettings = new AppGlobalSettings();
        globalSettings.ReadFromCNFfile();

        ArrayList<ContactItem> ciA = new ArrayList<>();
        ContactItem[] ciUpdateArray;

        // Read existing partner contact data from MySQL database
        ContactItem[] ciArray = ContactItem.readCIArrayFromDB(globalSettings);

        // Normalize emails in the ciArray and put the normalized into ciUpdateArray
        for (ContactItem ciArray1 : ciArray) {
            String initialEmail = ciArray1.getValueByHeader("email");
            ciArray1.normalizeEmail();
            if (!initialEmail.equals(ciArray1.getValueByHeader("email"))) {
                ContactItem newCI = new ContactItem();
                newCI.copy(ciArray1);
                ciA.add(newCI);
            }
        }

        if(!ciA.isEmpty()){
            ciUpdateArray = new ContactItem[ciA.size()];
            ciA.toArray(ciUpdateArray);
            ContactItem.updateCIArrayToDB(ciUpdateArray);
        }else{
            System.out.println("All emails are already normalized, nothing to do");
        }

        // Check for dublicates in the DB and remove them

        // Read existing partner contact data from MySQL database
        ciArray = ContactItem.readCIArrayFromDB(globalSettings);
        boolean isDublicatedFound = false;

        // Open mySQL Connection and read Items
        try (Connection connection = DriverManager
                .getConnection(AppGlobalSettings.mySQLServerURL, AppGlobalSettings.mySQLServerUser,
                        AppGlobalSettings.mySQLServerPassword);
        ){

            for(int i = 0; i < ciArray.length; i++){
                String email = ciArray[i].getValueByHeader("email");
                ciA = new ArrayList<>();
                ArrayList<Integer> ciI = new ArrayList<>();
                for(int j = i+1; j < ciArray.length; j++){
                    if(email.equals(ciArray[j].getValueByHeader("email"))){
                        ContactItem newCI = new ContactItem();
                        newCI.copy(ciArray[j]);
                        ciA.add(newCI);
                        ciI.add(j);
                    }
                }
                if(!ciA.isEmpty()){
                    isDublicatedFound = true;
                    ciUpdateArray = new ContactItem[ciA.size()];
                    ciA.toArray(ciUpdateArray);

                    // Prepare and change foreign keys in the RegAttDB
                    for (ContactItem ciUpdateArray1 : ciUpdateArray) {
                        // Array of Registration and Attendance Items
                        ArrayList<RegAttDB> raDBA = new ArrayList<>();
                        // Construct the query for MySQL
                        String Query = "select * from attend_reg_status where fk_id = " + ciUpdateArray1.getId();
                        // Create a statement using connection object
                        Statement stmt = connection.createStatement();
                        // Execute the query or update query
                        ResultSet rs = stmt.executeQuery(Query);
                        // Step 4: Process the ResultSet object.
                        while (rs.next()) {
                            RegAttDB raDBItem = new RegAttDB();
                            for(int k = 0; k < 5; k++) // 5 - number of RegAttDB columns
                                raDBItem.setValueByIndex(k, rs.getInt(RegAttDB.getHeaderByIndex(k)));
                            raDBA.add(raDBItem);
                        }
                        RegAttDB[] raUpdateDB = new RegAttDB[raDBA.size()];
                        raDBA.toArray(raUpdateDB);
                        for (RegAttDB raUpdateDB1 : raUpdateDB) {
                            raUpdateDB1.setValueByIndex(2, ciArray[i].getId()); // Update fk_id field by first id represented in DB
                        }
                        RegAttDB.updateRADBToMySQL(raUpdateDB);
                    }
                    // Delete dublicated contacts from SQL DB
                    ContactItem.deleteCIArrayFromDB(ciUpdateArray);

                    // Delete dublicated contacts from ciArray
                    for(int j = ciI.size()-1; j >= 0; j--){
                        ContactItem.deleteArrayRow(ciArray, ciI.get(j));
                    }

                }
            }

            connection.close();

        } catch (SQLException e){
            printSQLException(e);
        }

        if(!isDublicatedFound){
            System.out.println("Dublicated records not found, nothing to do");
        }

        // Export results to Output1 file
        ContactItem.exportCIArrayToCSVfile(AppGlobalSettings.workingDirectory
                + AppGlobalSettings.outputFile, ciArray);
    }
}
