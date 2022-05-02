package contacttools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AppGlobalSettings {

    public static int numberOfColumns;
    public static String[] columnHeaders;
    public static String mySQLServerURL;
    public static String mySQLServerTable;
    public static String mySQLServerAttendeesList;
    public static String mySQLServerUser;
    public static String mySQLServerPassword;
    public static String workingDirectory;
    public static String inputFile1;
    public static String inputFile2;
    public static String outputFile;
    public static String outputFile1;
    public static boolean isExcelCSV;
    public static char excelCSVSeparator;
    public static char excelCSVEol;
    public static char excelCSVEol2;
    public static char csvSeparator;
    public static char csvEol;

    AppGlobalSettings(){
        numberOfColumns = 17;
        columnHeaders= new String[17];
        columnHeaders[0] = "id";
        columnHeaders[1] = "secondnameeng";
        columnHeaders[2] = "firstnameeng";
        columnHeaders[3] = "fullnameeng";
        columnHeaders[4] = "secondnamerus";
        columnHeaders[5] = "nameandmiddlename";
        columnHeaders[6] = "ending";
        columnHeaders[7] = "country";
        columnHeaders[8] = "company";
        columnHeaders[9] = "fileas";
        columnHeaders[10] = "position";
        columnHeaders[11] = "businessphone";
        columnHeaders[12] = "email";
        columnHeaders[13] = "businessfax";
        columnHeaders[14] = "mobilephone";
        columnHeaders[15] = "categories";
        columnHeaders[16] = "opnid";
        mySQLServerURL = "jdbc:mysql://192.168.1.54:3306/partner_contact_list?useSSL=false";
        mySQLServerTable = "partner_mailing_list";
        mySQLServerAttendeesList = "s01_attendees_list";
        mySQLServerUser = "root";
        mySQLServerPassword = "Xke07inz!";
        workingDirectory = "D:\\Users\\Vasily\\OneDrive\\Partner Mailing Database Project\\";
        inputFile1 = "Add to Partners Mailing Database.csv";
        inputFile2 = "";
        outputFile = "Partners Mailing Database Export.csv";
        outputFile1 = "New Contacts.csv";
        isExcelCSV = true;
        excelCSVSeparator = ';';
        excelCSVEol = 0x0D;
        excelCSVEol2 = 0x0A;
        csvSeparator = ',';
        csvEol = 0x0A;
    }

    void ReadFromCNFfile() {

        char chars[];
        String fileContent;
        // Old style of str[] definition
        // String str[] = null;
        ArrayList<String> str = new ArrayList<>();

        boolean iStrEqualitySign = false;
        int iStrEqualitySignPosition = 0;

        File file = new File("appsettings.cfg");

        try (FileReader fr = new FileReader(file)) {
            chars = new char[(int) file.length()];
            fr.read(chars);
            fr.close();

            // delete spaces, tabs, eols, etc.

            fileContent = new String(chars);

            int startPosition = 0;

            for(int k = 0; k < fileContent.length(); k++) {
                if(fileContent.charAt(k) == '\n'){
                    str.add((fileContent.substring(startPosition, k-1)).trim());
                    startPosition = k+1;
                }
            }
            str.add((fileContent.substring(startPosition)).trim());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Parse the rows and read the application settings

        int iStr = 0;

        while(iStr < str.size()) {
            if(str.get(iStr).charAt(0) == ';') {
                iStr++;
                continue;
            }

            if(str.get(iStr).equals("")){
                iStr++;
                continue;
            }

            // Looking for '=' sign
            for(int i = 0; i < str.get(iStr).length(); i++) {
                if(str.get(iStr).charAt(i) == '=') {
                    iStrEqualitySign = true;
                    iStrEqualitySignPosition = i;
                    break;
                }
            }

            if(!iStrEqualitySign){
                System.out.println("Error: There is no \"=\" sign in configuration file string \n\"" + str.get(iStr) + "\", row #" + iStr);
                return;
            } else
                iStrEqualitySign = false;

            String str2 = str.get(iStr).substring(0, iStrEqualitySignPosition-1);
            str2 = str2.stripTrailing();
            switch (str2) {
                case "number_of_columns":
                    str.set(iStr, (str.get(iStr).substring(iStrEqualitySignPosition+1)).strip());
                    try {
                        numberOfColumns = Integer.parseInt(str.get(iStr));
                    } catch (NumberFormatException e) {
                        System.out.println("Integer format error in configuration file, setting \'number_of_columns\', row #" + iStr);
                        return;
                    }
                    iStr++;
                    break;
                case "column_headers":
                    // Number of strings in the headers data
                    int jStr = 0;
                    boolean openingCurlyBracketFound = false,
                            closingCurlyBracketFound = false;
                    // Looking for opening '{'
                    for(int j = 0; j < str.get(iStr).length(); j++) {
                        if(str.get(iStr).charAt(j) == '{') {
                            openingCurlyBracketFound = true;
                            str.set(iStr, str.get(iStr).substring(j+1));
                            break;
                        }
                    }
                    if(!openingCurlyBracketFound) {
                        System.out.println("Error in column_headers definition statement in row #" + iStr);
                        return;
                    }
                    // Looking for closing '}'
                    for(int k = iStr; k < str.size(); k++) {
                        for(int j = 0; j < str.get(k).length(); j++) {
                            if(str.get(k).charAt(j) == '}') {
                                closingCurlyBracketFound = true;
                                str.set(k, str.get(k).substring(0, j));
                                jStr = k;
                                k = str.size();
                                break;
                            }
                        }
                    }

                    if(!closingCurlyBracketFound) {
                        System.out.println("Error in column_headers definition statement in row #" + iStr + " or below");
                        return;
                    }
                    // Concatenate all rows in one, delete all spaces and parse it
                    String headersStr = "";
                    for(int k = iStr; k <= jStr; k++)
                        headersStr += str.get(k);

                    headersStr = headersStr.replaceAll("\\s+","");
                    int[] commasPositionArray = new int[numberOfColumns-1];
                    int j = 0;
                    for(int k = 0; k < headersStr.length(); k++)
                        if(headersStr.charAt(k) == ',') {
                            commasPositionArray[j] = k;
                            j++;
                        }
                    if(j != numberOfColumns-1) {
                        System.out.println("Error in column_headers definition statement in row #" + iStr + "or below");
                        return;
                    }

                    AppGlobalSettings.columnHeaders[0] = headersStr.substring(0, commasPositionArray[0]);
                    for(int k = 1; k < (numberOfColumns-1); k++){
                        AppGlobalSettings.columnHeaders[k] = headersStr.substring(commasPositionArray[k-1]+1, commasPositionArray[k]);
                    }

                    iStr = jStr+1;
                    break;
                case "mysql_server_url":
                    str.set(iStr, (str.get(iStr).substring(iStrEqualitySignPosition+1)).trim());
                    AppGlobalSettings.mySQLServerURL = str.get(iStr);
                    iStr++;
                    break;
                case "partner_mailing_list":
                    str.set(iStr, (str.get(iStr).substring(iStrEqualitySignPosition+1)).trim());
                    AppGlobalSettings.mySQLServerTable = str.get(iStr);
                    iStr++;
                    break;
                case "attendees_list":
                    str.set(iStr, str.get(iStr).substring(1+iStrEqualitySignPosition).trim());
                    AppGlobalSettings.mySQLServerAttendeesList = str.get(iStr);
                    iStr++;
                    break;
                case "mysql_server_user":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    AppGlobalSettings.mySQLServerUser = str.get(iStr);
                    iStr++;
                    break;
                case "mysql_server_password":
                    str.set(iStr,str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    AppGlobalSettings.mySQLServerPassword = str.get(iStr);
                    iStr++;
                    break;
                case "working_directory":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    AppGlobalSettings.workingDirectory = str.get(iStr);
                    iStr++;
                    break;
                case "input_file_1":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    AppGlobalSettings.inputFile1 = str.get(iStr);
                    iStr++;
                    break;
                case "input_file_2":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    AppGlobalSettings.inputFile2 = str.get(iStr);
                    iStr++;
                    break;
                case "output_file":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    AppGlobalSettings.outputFile = str.get(iStr);
                    iStr++;
                    break;
                case "output_file_1":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    AppGlobalSettings.outputFile1 = str.get(iStr);
                    iStr++;
                    break;
                case "is_excel_csv":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    int isExcel;
                    try {
                        isExcel = Integer.parseInt(str.get(iStr));
                    } catch (NumberFormatException e) {
                        System.out.println("Integer format error in configuration file, setting \'is_excel_csv\', row #" + iStr);
                        return;
                    }
                    AppGlobalSettings.isExcelCSV = isExcel == 1;
                    iStr++;
                    break;
                case "excel_csv_separator":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    AppGlobalSettings.excelCSVSeparator = str.get(iStr).charAt(0);
                    iStr++;
                    break;
                case "excel_csv_eol":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    int eCSVeol;
                    try {
                        eCSVeol = Integer.parseInt(str.get(iStr),16);
                    } catch (NumberFormatException e) {
                        System.out.println("Integer format error in configuration file, setting \'excel_csv_eol\', row #" + iStr);
                        return;
                    }
                    AppGlobalSettings.excelCSVEol = (char)eCSVeol;
                    iStr++;
                    break;
                case "excel_csv_eol1":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    try {
                        eCSVeol = Integer.parseInt(str.get(iStr),16);
                    } catch (NumberFormatException e) {
                        System.out.println("Integer format error in configuration file, setting \'excel_csv_eol1\', row #" + iStr);
                        return;
                    }
                    AppGlobalSettings.excelCSVEol = (char)eCSVeol;
                    iStr++;
                    break;
                case "csv_separator":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    AppGlobalSettings.csvSeparator = str.get(iStr).charAt(0);
                    iStr++;
                    break;
                case "csv_eol":
                    str.set(iStr, str.get(iStr).substring(iStrEqualitySignPosition+1).trim());
                    try {
                        eCSVeol = Integer.parseInt(str.get(iStr),16);
                    } catch (NumberFormatException e) {
                        System.out.println("Integer format error in configuration file, setting \'csv_eol\', row #" + iStr);
                        return;
                    }
                    AppGlobalSettings.csvEol = (char)eCSVeol;
                    iStr++;
                    break;
                default:
                    System.out.println("Error: Wrong parameter in configuration file, setting " + str.get(iStr) + ", row #" + iStr);
                    return;
            }
        }
    }

    void Print(){
        System.out.println("Current Application Global Settings:");
        System.out.println("numberOfColumns = " + numberOfColumns);
        System.out.print("columnHeaders = ");
        for(int i = 0; i < numberOfColumns-1; i++)
            System.out.print(columnHeaders[i] + ",");
        System.out.print(columnHeaders[numberOfColumns-1]);
        System.out.println(';');
        System.out.println("mySQLServerURL = " + mySQLServerURL);
        System.out.println("mySQLServerTable = " + mySQLServerTable);
        System.out.println("mySQLServerUser = " + mySQLServerUser);
        System.out.println("mySQLServerPassword = " + mySQLServerPassword);
        System.out.println("workingDirectory = " + workingDirectory);
        System.out.println("inputFile1 = " + inputFile1);
        System.out.println("inputFile2 = " + inputFile2);
        System.out.println("outputFile = " + outputFile);
        System.out.println("isExcelCSV = " + isExcelCSV);
        System.out.println("excelCSVSeparator = " + excelCSVSeparator);
        System.out.println("excelCSVEol = " + excelCSVEol);
        System.out.println("excelCSVEol2 = " + excelCSVEol2);
        System.out.println("csvSeparator = " + csvSeparator);
        System.out.println("csvEol = " + csvEol);
    }
}
