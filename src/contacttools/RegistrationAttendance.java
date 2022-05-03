package contacttools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RegistrationAttendance extends ContactItem {

    private int regID;
    private int eventID;
    private int registration;
    private int attendance;

    RegistrationAttendance(){
        super();
        regID = 0;
        eventID = 0;
        registration = 0;
        attendance = 0;
    }

    RegistrationAttendance(int rID, int evID, int i, String[] s, int reg, int att){
        super(i, s);
        regID = rID;
        eventID = evID;
        registration = reg;
        attendance = att;
    }

    RegistrationAttendance(int rID, int evID, ContactItem ci, int reg, int att){
        super();
        int n = AppGlobalSettings.numberOfColumns - 1;
        regID = rID;
        eventID = evID;
        registration = reg;
        attendance = att;
        this.setId(ci.getId());
        for(int i = 0; i < n; i++){
            this.setValueByIndex(i, ci.getValueByIndex(i));
        }
    }

    public int getRegID(){
        return this.regID;
    }

    public void setRegID(int i){
        this.regID = i;
    }

    public int getEventID(){
        return this.eventID;
    }

    public void setEventID(int i){
        this.eventID = i;
    }

    public int getRegistration(){
        return this.registration;
    }

    public void setRegistration(int i){
        if( (i != 0) & (i != 1) ) {
            System.out.println("Error in Registration data: registration field should be 0 or 1");
            return;
        }
        this.registration = i;
    }

    public int getAttendance(){
        return this.attendance;
    }

    public void setAttendance(int i){
        if( (i != 0) & (i != 1) ) {
            System.out.println("Error in Attendance data: attendance field should be 0 or 1");
            return;
        }
        this.attendance = i;
    }

    @Override
    public void println(){
        StringBuilder str = new StringBuilder(this.regID);
        for(int j = 0; j < AppGlobalSettings.numberOfColumns-1; j++) {
            str.append(" ");
            str.append(this.getValueByIndex(j));
        }
        str.append(this.registration);
        str.append(this.attendance);
        System.out.println("id = " + this.getId() + "," + str);
    }

    @Override
    public void print(){
        StringBuilder str = new StringBuilder(this.regID);
        for(int j = 0; j < AppGlobalSettings.numberOfColumns-1; j++) {
            str.append(" "); str.append(this.getValueByIndex(j));
        }
        str.append(this.registration);
        str.append(this.attendance);
        System.out.print("id = " + this.getId() + "," + str);
    }

    public static RegistrationAttendance[] readRAArrayFromCSVFile(String csvFile){

        // Read new contacts and registration/attendance data from Input1 file

        ArrayList<RegistrationAttendance> raIA = new ArrayList<>();
        String str;
        String fileContent;
        char[] chars;
        int n = AppGlobalSettings.numberOfColumns + 3;

        File file = new File(csvFile);

        try (FileReader fr = new FileReader(file)) {
            chars = new char[(int) file.length()];
            fr.read(chars);

            fileContent = new String(chars);

            int iCI = 0;
            int startPosition = 0;

            for(int k = 0; k < fileContent.length(); k++) {
                if(fileContent.charAt(k) == 0x0D){
                    k++; // Excel has two eol characters
                    RegistrationAttendance raItem = new RegistrationAttendance();
                    // raInputArray = RegistrationAttendance.increaseRAArray(raInputArray);

                    // Parsing current string from Input1 file
                    str = fileContent.substring(startPosition, k-1);
                    int[] semicolonPosition = new int[n - 1];
                    int count = 0;
                    for(int m = 0; m < str.length(); m++)
                        if(str.charAt(m) == AppGlobalSettings.excelCSVSeparator) {
                            semicolonPosition[count] = m;
                            count++;
                        }
                    if(count != (n-1)){
                        System.out.println("Error in data format, row #" + iCI);
                        return null;
                    }

                    raItem.setId(0); // id field should be empty in Input1 file
                    raItem.setRegID(0); // regID field is not represented in Input1 file

                    if(iCI != 0) {

                        try {
                            raItem.setEventID(Integer.parseInt(str.substring(0, semicolonPosition[0])));
                        } catch (NumberFormatException e){
                            System.out.println("Error in Input1 file format, string #" + iCI + "position #1 (event_id)");
                            return null;
                        }

                        try {
                            raItem.setAttendance(Integer.parseInt(str.substring(semicolonPosition[n-2]+1)));
                        } catch(NumberFormatException e){
                            System.out.println("Error in Input1 file format, string #" + iCI + "position #20 (attendance)");
                            return null;
                        }

                        try {
                            raItem.setRegistration(
                                    Integer.parseInt(str.substring(semicolonPosition[n-3]+1, semicolonPosition[n-2])));
                        } catch(NumberFormatException e){
                            System.out.println("Error in Input1 file format, string #" + iCI + "position #19 (registration)");
                            return null;
                        }
                    } else {
                        // Process first string with Input1 file headers
                        raItem.setEventID(0);
                        raItem.setAttendance(0);
                        raItem.setRegistration(0);
                    }

                    for(int m = 0; m < n - 4; m++) {
                        raItem.setValueByIndex(m,
                                str.substring(semicolonPosition[m+1]+1, semicolonPosition[m+2]));
                    }

                    if(iCI > 0) raItem.normalizeEmail();

                    // Add raItem to raIA ArrayList
                    raIA.add(raItem);

                    startPosition = k+1;
                    iCI++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete first row (headers) from input array
        raIA.remove(0);

        // Copy all Registration and Attendance info in the array and return it
        if(!raIA.isEmpty()) {
            RegistrationAttendance[] raInputArray = new RegistrationAttendance[raIA.size()];
            raIA.toArray(raInputArray);
            return raInputArray;
        } else {
            throw new RuntimeException("There is no any data in the registration and attendance CSV file. " +
                    "Program terminated");
        }

    }

    public static void copyCIfromRA(ContactItem contItem, RegistrationAttendance regAtt){
        contItem.setId(regAtt.getId());
        for(int i = 0; i < AppGlobalSettings.numberOfColumns - 1; i++){
            contItem.setValueByIndex(i, regAtt.getValueByIndex(i));
        }
    }

    public static void exportRAArrayToCSVfile(String csvFileName, RegistrationAttendance[] regAtt){

        try (FileWriter fw = new FileWriter(csvFileName)) {

            StringBuilder str = new StringBuilder();
            // char separator = AppGlobalSettings.excelCSVSeparator;
            char separator = '\t';
            //char eoString = AppGlobalSettings.excelCSVEol2;
            char eoString = '\n';

            for (int j = 0; j < regAtt.length; j++) {
                str.append(regAtt[j].getEventID());
                str.append(separator);
                str.append(regAtt[j].getId());
                str.append(separator);
                for(int i = 0; i < AppGlobalSettings.numberOfColumns-1; i++){
                    str.append(regAtt[j].getValueByIndex(i));
                    str.append(separator);
                }
                str.append(regAtt[j].getRegistration());
                str.append(separator);
                if(j == regAtt.length - 1) {
                    str.append(regAtt[j].getAttendance());
                } else {
                    str.append(regAtt[j].getAttendance());
                    str.append(eoString);
                }
                fw.write(str.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
