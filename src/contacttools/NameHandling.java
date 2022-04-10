/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contacttools;

import java.util.ArrayList;

/**
 *
 * @author VDEMIN
 */
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
        
        for(int i = 0; i < ciArray.length; i++) {
            if(ciArray[i].getValueByIndex(0).equals("")){
                isFound = true;
                String secondNameEng = "", firstNameEng = "", fullNameEng = "", 
                        secondNameRus = ciArray[i].getValueByIndex(3), 
                        nameAndMiddlename = ciArray[i].getValueByIndex(4);
                for(int j = 0; j < secondNameRus.length(); j++) {
                    int charCode = (int) secondNameRus.charAt(j);
                    if((charCode >= 0x0410) & (charCode <= 0x044F))
                        secondNameEng += TransLitTable[charCode-0x0410];
                    else if(charCode == 0x0401) // Ё character
                        secondNameEng += "Yo";
                    else if(charCode == 0x0451) // ё character
                        secondNameEng += "yo";
                    else if(charCode == 32) // space character
                        continue;
                    else {
                        System.out.println("Error: wrong letter " + (char)charCode + " code of " 
                                + charCode + " in the Russian second name of");
                        ciArray[i].println();
                        return;
                    }
                }
                for(int j = 0; j < nameAndMiddlename.length(); j++) {
                    int charCode = (int) nameAndMiddlename.charAt(j);
                    if((charCode >= 0x0410) & (charCode <= 0x044F))
                        firstNameEng += TransLitTable[charCode-0x0410];
                    else if(charCode == 0x0401) // Ё character
                        firstNameEng += "Yo";
                    else if(charCode == 0x0451) // ё character
                        firstNameEng += "yo";
                    else if(charCode == 32) // space character
                        continue;
                    else {
                        System.out.println("Error: wrong letter " + (char)charCode + " code of " 
                                + charCode + " in the Russian first name of");
                        ciArray[i].println();
                        return;
                    }
                }
                
                fullNameEng = firstNameEng + " " + secondNameEng;
                
/*
                System.out.println("Сonverting from Russian to Transliteration result: " +
                        nameAndMiddlename + " " + secondNameRus + " -> " + 
                        firstNameEng + " " + secondNameEng + " (" + fullNameEng + ")");
 */             
                ContactItem ci = new ContactItem();
                ci.setId(ciArray[i].getId());
                ci.setValueByIndex(0, secondNameEng);
                ci.setValueByIndex(1, firstNameEng);
                ci.setValueByIndex(2, fullNameEng);
                for(int j = 3; j < AppGlobalSettings.numberOfColumns-1; j++) 
                    ci.setValueByIndex(j, ciArray[i].getValueByIndex(j));
                                
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
        
        ContactItem.updateCIArrayToDB(ciUpdateArray, globalSettings);
        
    }
    
}
