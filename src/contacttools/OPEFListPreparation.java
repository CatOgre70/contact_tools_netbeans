package contacttools;

import java.util.ArrayList;

public class OPEFListPreparation {

    public static void main(String[] args) {

        // Initialize Global Application Settings and read them from CNF file
        AppGlobalSettings globalSettings = new AppGlobalSettings();
        globalSettings.ReadFromCNFfile();

        // Read OPEF consolidated list from PAMs
        RegistrationAttendance[] OPEFConsolidatedList =
                RegistrationAttendance.readRAArrayFromCSVFile(
                        AppGlobalSettings.workingDirectory + "OPEF input.csv");

        // Read existing partner contact data from MySQL database
        ContactItem[] ciArray = ContactItem.readCIArrayFromDB(globalSettings);

        ArrayList<ContactItem> addList = new ArrayList<>();

        for (ContactItem ciArray1 : ciArray) {
            String s1 = ciArray1.getValueByHeader("email");
            s1 = s1.toLowerCase();
            boolean found = false;
            for (RegistrationAttendance OPEFConsolidatedList1 : OPEFConsolidatedList) {
                String s2 = OPEFConsolidatedList1.getValueByHeader("email");
                s2 = s2.toLowerCase();
                if(s1.equals(s2))
                    found = true;
            }
            if (!found) {
                addList.add(ciArray1);
            }
        }

        ContactItem[] additionalList = new ContactItem[addList.size()];
        addList.toArray(additionalList);

        ContactItem.exportCIArrayToCSVfile(AppGlobalSettings.workingDirectory +
                "OPEF additional list.csv", additionalList);

    }

}
