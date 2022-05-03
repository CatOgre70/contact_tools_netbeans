package contacttools;

import java.util.ArrayList;

public class RAInputProcess {

    public static void main(String[] args) {

        // Initialize Global Application Settings and read them from CNF file
        AppGlobalSettings globalSettings = new AppGlobalSettings();
        globalSettings.ReadFromCNFfile();

        // Read input registration and attendance data from Input1 file
        RegistrationAttendance[] raArray =
                RegistrationAttendance.readRAArrayFromCSVFile(
                        AppGlobalSettings.workingDirectory +
                                AppGlobalSettings.inputFile1);

        // Read existing partner contact data from MySQL database
        ContactItem[] ciArray = ContactItem.readCIArrayFromDB(globalSettings);

        // Read existing event list from MySQL database
        EventItem[] eventArray = EventItem.ReadEventListFromDB();

        // Read existing registration and attendance data from MySQL database
        RegAttDB[] raDB = RegAttDB.readRADBfromMySQL(globalSettings);

        // Define free index set for ciArray: nextIDAfterMax & availableIDsArray[]
        int nextIDAfterMax = 0;
        int[] availableIDsArray = null;

        if(ciArray.length > 0){

            for (ContactItem contactItem : ciArray)
                if (contactItem.getId() > nextIDAfterMax) {
                    nextIDAfterMax = contactItem.getId();
                }
            nextIDAfterMax++;

            if( (nextIDAfterMax-1) > ciArray.length){
                availableIDsArray = new int[nextIDAfterMax - ciArray.length - 1];
                int iID = 0;

                for(int j = 1; j < nextIDAfterMax; j++){
                    boolean jIDExist = false;
                    for (ContactItem ciArray1 : ciArray) {
                        if (ciArray1.getId() == j) {
                            jIDExist = true;
                            break;
                        }
                    }
                    if(!jIDExist) {
                        availableIDsArray[iID] = j;
                        iID++;
                    }
                }

            }

        } else
            nextIDAfterMax = 1;

        // Define free index set for raDB: nextRAAfterMax and availableRAIndexArray[]
        // Set ID index field for raArray
        int nextRAAfterMax = 0;
        int[] availableRAIndexArray = null;

        if(raDB.length > 0){

            for (RegAttDB regAttDB : raDB)
                if (regAttDB.getValueByIndex(0) > nextRAAfterMax) {
                    nextRAAfterMax = regAttDB.getValueByIndex(0);
                }
            nextRAAfterMax++;

            if( (nextRAAfterMax-1) > raDB.length){
                availableRAIndexArray = new int[nextRAAfterMax - raDB.length - 1];

                int iID = 0;

                for(int j = 1; j < nextRAAfterMax; j++){
                    boolean jRAExist = false;
                    for (RegAttDB raDB1 : raDB) {
                        if (raDB1.getValueByIndex(0) == j) {
                            jRAExist = true;
                        }
                    }
                    if(!jRAExist) {
                        availableRAIndexArray[iID] = j;
                        iID++;
                    }
                }
            }

        } else
            nextRAAfterMax = 1;

        //Find new contacts in raArray, copy them to separate ciInputArray

        ContactItem[] ciInputArray = null;
        ArrayList<ContactItem> ciInArr = new ArrayList<>();

        ArrayList<Integer> newCIindexes = new ArrayList<>();

        if(ciArray.length > 0){
            for(int i = 0; i < raArray.length; i++) {
                String s1 = raArray[i].getValueByHeader("email");
                s1 = s1.toLowerCase();
                boolean found = false;
                for (ContactItem ciArray1 : ciArray) {
                    String s2 = ciArray1.getValueByHeader("email");
                    s2 = s2.toLowerCase();
                    if (s1.equals(s2)) {
                        found = true;
                        raArray[i].setId(ciArray1.getId());
                    }
                }
                if(!found) {
                    ContactItem ciItem = new ContactItem();
                    newCIindexes.add(i);
                    RegistrationAttendance.copyCIfromRA(ciItem, raArray[i]);
                    ciInArr.add(ciItem);
                }
            }
        }

        if(!ciInArr.isEmpty()){
            ciInputArray = new ContactItem[ciInArr.size()];
            ciInArr.toArray(ciInputArray);
        }

        // Assign IDs to new contacts

        if(ciInputArray != null) {
            if(availableIDsArray != null) {

                if(availableIDsArray.length < ciInputArray.length) {
                    for(int i = 0; i < availableIDsArray.length; i++) {
                        ciInputArray[i].setId(availableIDsArray[i]);
                        raArray[newCIindexes.get(i)].setId(availableIDsArray[i]);
                    }
                    int nextID = nextIDAfterMax;
                    for(int i = availableIDsArray.length; i < ciInputArray.length; i++) {
                        ciInputArray[i].setId(nextID);
                        raArray[newCIindexes.get(i)].setId(nextID);
                        nextID++;
                    }
                } else {
                    for(int i = 0; i < ciInputArray.length; i++) {
                        ciInputArray[i].setId(availableIDsArray[i]);
                        raArray[newCIindexes.get(i)].setId(availableIDsArray[i]);
                    }
                }

            } else {

                int nextID = nextIDAfterMax;
                for(int i = 0; i < ciInputArray.length; i++) {
                    ciInputArray[i].setId(nextID);
                    raArray[newCIindexes.get(i)].setId(nextID);
                    nextID++;
                }

            }
        }

        if(ciInputArray != null) {
            int count = 0;
            for(int i = 0; i < ciInputArray.length; i++)
                count += raArray[newCIindexes.get(i)].getAttendance();
            System.out.println("New partner's employees participated: " + count);
            System.out.println("New partner's employees registered: " + ciInputArray.length);
        } else
            System.out.println("New partner's employees registered & participated: 0");

        // Write new contact items to MySQL database
        ContactItem.writeCIArrayToDB(ciInputArray, globalSettings);

        // Add existing and new contacts to ciNewArray
        ContactItem[] ciNewArray;

        if(ciInputArray != null) {
            ciNewArray = new ContactItem[ciArray.length + ciInputArray.length];
            System.arraycopy(ciArray, 0, ciNewArray, 0, ciArray.length);
            System.arraycopy(ciInputArray, 0, ciNewArray, ciArray.length, ciInputArray.length);
            ContactItem.exportCIArrayToCSVfile(AppGlobalSettings.workingDirectory +
                    AppGlobalSettings.outputFile, ciNewArray);
        } else {
            ciNewArray = ciArray;
        }

        // Export existing and new (all) contacts to csv file
        ContactItem.exportCIArrayToCSVfile(AppGlobalSettings.workingDirectory +
                AppGlobalSettings.outputFile, ciNewArray);

        // Analyze new registration and attendance data, add new records to the raInputArray
        // add changed records to the raUpdateArray
        ArrayList<RegistrationAttendance> raInArr = new ArrayList<>();
        ArrayList<RegistrationAttendance> raUpdArr = new ArrayList<>();
        ArrayList<Integer> newRAindexes = new ArrayList<>();

        for(int i = 0; i < raArray.length; i++) {
            if(raArray[i].getEventID() == 0) continue;
            boolean raFound = false;
            for (RegAttDB regAttDB : raDB) {
                if ((raArray[i].getEventID() == regAttDB.getValueByHeader("fk_event_id")) & (raArray[i].getId() == regAttDB.getValueByHeader("fk_id"))) {
                    raFound = true;
                    raArray[i].setRegID(regAttDB.getValueByHeader("attendance_id"));
                    if (raArray[i].getRegistration() != 0) {
                        if (raArray[i].getAttendance() != regAttDB.getValueByHeader("attendance_status")) {
                            raArray[i].setAttendance(regAttDB.getValueByHeader("attendance_status"));
                            raUpdArr.add(raArray[i]);
                        }
                    } else {
                        raArray[i].setRegistration(1);
                        raUpdArr.add(raArray[i]);
                    }
                    break;
                }
            }
            if(!raFound) {
                newRAindexes.add(i);
                raArray[i].setRegistration(1);
                raInArr.add(raArray[i]);
            }
        }

        RegistrationAttendance[] raInputArray = null;
        if(!raInArr.isEmpty()) {
            raInputArray = new RegistrationAttendance[raInArr.size()];
            raInArr.toArray(raInputArray);
        }

        RegistrationAttendance[] raUpdateArray = null;
        if(!raUpdArr.isEmpty()) {
            raUpdateArray = new RegistrationAttendance[raUpdArr.size()];
            raUpdArr.toArray(raUpdateArray);
        }

        // Assign Attendance IDs to new Registration Attendance records
        if(raInputArray != null) {
            if(availableRAIndexArray != null) {

                if(availableRAIndexArray.length < raInputArray.length) {
                    for(int i = 0; i < availableRAIndexArray.length; i++) {
                        raInputArray[i].setRegID(availableRAIndexArray[i]);
                        raArray[newRAindexes.get(i)].setRegID(availableRAIndexArray[i]);
                    }
                    int nextID = nextRAAfterMax;
                    for(int i = availableRAIndexArray.length; i < raInputArray.length; i++) {
                        raInputArray[i].setRegID(nextID);
                        raArray[newRAindexes.get(i)].setRegID(nextID);
                        nextID++;
                    }
                } else {
                    for(int i = 0; i < raInputArray.length; i++) {
                        raInputArray[i].setRegID(availableRAIndexArray[i]);
                        raArray[newRAindexes.get(i)].setRegID(availableRAIndexArray[i]);
                    }
                }

            } else {

                int nextID = nextRAAfterMax;
                for(int i = 0; i < raInputArray.length; i++) {
                    raInputArray[i].setRegID(nextID);
                    raArray[newRAindexes.get(i)].setRegID(nextID);
                    nextID++;
                }
            }
        }

        // Write registration and attendance data to MySQL database
        if(raInputArray != null){
            RegAttDB[] raInDB = new RegAttDB[raInputArray.length];
            for(int i = 0; i < raInputArray.length; i++){
                raInDB[i] = new RegAttDB();
                raInDB[i].setValueByIndex(0, raInputArray[i].getRegID());
                raInDB[i].setValueByIndex(1, raInputArray[i].getEventID());
                raInDB[i].setValueByIndex(2, raInputArray[i].getId());
                raInDB[i].setValueByIndex(3, raInputArray[i].getRegistration());
                raInDB[i].setValueByIndex(4, raInputArray[i].getAttendance());
            }
            RegAttDB.writeRADBToMySQL(raInDB);
        } else {
            System.out.println("There aren't new registration and attendance data in the input file. Nothing to do");
        }

        // Update registration and attendance data to MySQL database
        if(raUpdateArray != null){
            RegAttDB[] raUpdDB = new RegAttDB[raUpdateArray.length];
            for(int i = 0; i < raUpdateArray.length; i++){
                raUpdDB[i] = new RegAttDB();
                raUpdDB[i].setValueByIndex(0, raUpdateArray[i].getRegID());
                raUpdDB[i].setValueByIndex(1, raUpdateArray[i].getEventID());
                raUpdDB[i].setValueByIndex(2, raUpdateArray[i].getId());
                raUpdDB[i].setValueByIndex(3, raUpdateArray[i].getRegistration());
                raUpdDB[i].setValueByIndex(4, raUpdateArray[i].getAttendance());
            }
            RegAttDB.updateRADBToMySQL(raUpdDB);
        } else {
            System.out.println("There aren't updated registration and attendance data in the input file. Nothing to do");
        }

        // Read final registration and attendance data from MySQL database
        raDB = RegAttDB.readRADBfromMySQL(globalSettings);

        // Export new contacts registration and attendance information into CSV file

        if(ciInputArray != null) {
            // Create RegistrationAttendance array with new contacts for export
            RegistrationAttendance[] raNewArray = new RegistrationAttendance[ciInputArray.length];
            for(int i = 0; i < ciInputArray.length; i++){
                // Search in raDB for contact registration and attendance data
                int regAttIndex = -1;
                for(int j = 0; j < raDB.length; j++) {
                    if(raDB[j].getValueByIndex(2) == ciInputArray[i].getId()) {
                        regAttIndex = j;
                    }
                }
                if(regAttIndex == -1) {
                    System.out.println("Warning: no registration & attendance info connected with this contact");
                    ciInputArray[i].println();
                    raNewArray[i] = new RegistrationAttendance(0, 0, ciInputArray[i], 0, 0);
                } else
                    raNewArray[i] = new RegistrationAttendance(raDB[regAttIndex].getValueByIndex(0), raDB[regAttIndex].getValueByIndex(1),
                            ciInputArray[i], raDB[regAttIndex].getValueByIndex(3), raDB[regAttIndex].getValueByIndex(4));
            }

            // Export RegistrationAttendance array to the CSV file
            RegistrationAttendance.exportRAArrayToCSVfile(AppGlobalSettings.workingDirectory +
                    AppGlobalSettings.outputFile1, raNewArray);

        } else {
            System.out.println("Error: ciInputArray is empty, no any new contacts for export, nothing to do");
        }

        // To build final report table from EventList, ciNewArray, ciInputArray and raDB

        int[] PartnersRegistered = new int[eventArray.length];
        int[] PartnersParticipated = new int[eventArray.length];

        for(int i = 0; i < eventArray.length; i++){
            PartnersRegistered[i] = 0;
            PartnersParticipated[i] = 0;
            int eventID = eventArray[i].getEventID();
            for (RegAttDB regAttDB : raDB) {
                if (regAttDB.getValueByHeader("fk_event_id") == eventID) {
                    PartnersRegistered[i] += regAttDB.getValueByHeader("registration_status");
                    PartnersParticipated[i] += regAttDB.getValueByHeader("attendance_status");
                }
            }
            System.out.println("EventID:\t" + eventID + "\tPartners Registered:\t" +
                    PartnersRegistered[i] + "\tPartners Attended:\t" + PartnersParticipated[i]);
        }

        // Calculate number of unique attendees

        int[] PartnersAttendeeList = new int[ciNewArray.length];
        for (RegAttDB regAttDB : raDB)
            if (regAttDB.getValueByHeader("attendance_status") == 1) {
                int ciID = regAttDB.getValueByHeader("fk_id");
                for (int i = 0; i < ciNewArray.length; i++) {
                    if (ciNewArray[i].getId() == ciID) {
                        PartnersAttendeeList[i]++;
                        break;
                    }
                }
            }

        int AttendeeCounter = 0;
        for (int k : PartnersAttendeeList)
            if (k > 0)
                AttendeeCounter++;

        System.out.println("Total number of unique attendees: " + AttendeeCounter);

        // Looking for partners who visited maximum number of events

        int maxEventsVisited = 0;
        int lastIndex = -1;
        for(int i = 0; i < PartnersAttendeeList.length; i++)
            if(PartnersAttendeeList[i] > maxEventsVisited) {
                maxEventsVisited = PartnersAttendeeList[i];
                lastIndex = i;
            }

        int maxEventsVisitedCounter = 0;
        for (int j : PartnersAttendeeList)
            if (j == maxEventsVisited)
                maxEventsVisitedCounter++;

        System.out.println("Maximum number of events, visited by single person: " + maxEventsVisited);
        System.out.println("Number of persons with maximum visited events: " + maxEventsVisitedCounter);
        System.out.println("Last Index = " + lastIndex);
        ciNewArray[lastIndex].println();
    }
}
