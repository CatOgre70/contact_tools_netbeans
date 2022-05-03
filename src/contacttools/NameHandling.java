package contacttools;

import java.util.ArrayList;

public class NameHandling {

    public static void main(String[] args) {

        String[] TransLitTable = {"A", "B", "V", "G", "D", "E", "Zh", "Z", "I",
                "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T",
                "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "Y",
                "", "E", "Yu", "Ya",
                "a", "b", "v", "g", "d", "e", "zh", "z",
                "i", "y", "k", "l", "m", "n", "o", "p", "r", "s",
                "t", "u", "f", "h", "ts", "ch", "sh", "sch", "",
                "y", "", "e", "yu", "ya"};

        // Initialize Global Application Settings and read them from CNF file
        AppGlobalSettings globalSettings = new AppGlobalSettings();
        globalSettings.ReadFromCNFfile();

        // Read existing partner contact data from MySQL database
        ContactItem[] ciArray = ContactItem.readCIArrayFromDB(globalSettings);
        ArrayList<ContactItem> ciA = new ArrayList<>();
        boolean isFound = false;

        for (ContactItem ciArray1 : ciArray) {
            if (ciArray1.getValueByIndex(0).equals("")) {
                isFound = true;
                String secondNameEng, firstNameEng, fullNameEng;
                String secondNameRus = ciArray1.getValueByIndex(3);
                String nameAndMiddlename = ciArray1.getValueByIndex(4);
                StringBuilder secondNameEngSB = new StringBuilder();
                for (int j = 0; j < secondNameRus.length(); j++) {
                    int charCode = secondNameRus.charAt(j);
                    if ((charCode >= 0x0410) & (charCode <= 0x044F)) {
                        secondNameEngSB.append(TransLitTable[charCode-0x0410]);
                    } else if (charCode == 0x0401) {
                        secondNameEngSB.append("Yo");
                    } else if (charCode == 0x0451) {
                        secondNameEngSB.append("yo");
                    } else if (charCode == 32) {
                    } else {
                        System.out.println("Error: wrong letter " + (char)charCode + " code of "
                                + charCode + " in the Russian second name of");
                        ciArray1.println();
                        return;
                    }
                }
                secondNameEng = secondNameEngSB.toString();
                StringBuilder firstNameEngSB = new StringBuilder();
                for (int j = 0; j < nameAndMiddlename.length(); j++) {
                    int charCode = nameAndMiddlename.charAt(j);
                    if ((charCode >= 0x0410) & (charCode <= 0x044F)) {
                        firstNameEngSB.append(TransLitTable[charCode-0x0410]);
                    } else if (charCode == 0x0401) {
                        firstNameEngSB.append("Yo");
                    } else if (charCode == 0x0451) {
                        firstNameEngSB.append("yo");
                    } else if (charCode == 32) {
                    } else {
                        System.out.println("Error: wrong letter " + (char)charCode + " code of "
                                + charCode + " in the Russian first name of");
                        ciArray1.println();
                        return;
                    }
                }
                firstNameEng = firstNameEngSB.toString();
                fullNameEng = firstNameEng + " " + secondNameEng;
                ContactItem ci = new ContactItem();
                ci.setId(ciArray1.getId());
                ci.setValueByIndex(0, secondNameEng);
                ci.setValueByIndex(1, firstNameEng);
                ci.setValueByIndex(2, fullNameEng);
                for (int j = 3; j < AppGlobalSettings.numberOfColumns-1; j++) {
                    ci.setValueByIndex(j, ciArray1.getValueByIndex(j));
                }
                // ci.println();

                ciA.add(ci);
            }
        }

        if(!isFound){
            System.out.println("There are no any records with empty English names in the database, nothing to do");
            return;
        }

        ContactItem[] ciUpdateArray = new ContactItem[ciA.size()];
        ciA.toArray(ciUpdateArray);

        ContactItem.updateCIArrayToDB(ciUpdateArray);

    }

}

