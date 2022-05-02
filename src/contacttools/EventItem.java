package contacttools;

import static contacttools.CommonTools.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class EventItem {

    int eventID;
    String marketingCode;
    String eventName;
    String eventShortName;
    String eventRubric;
    String eventTopic;
    Date eventDate;
    String eventType;
    String eventPlatform;
    String eventOwner;

    EventItem(){
        eventID = 0;
        marketingCode = "";
        eventName = "";
        eventShortName = "";
        eventRubric = "";
        eventTopic = "";
        eventDate = new Date();
        eventType = "";
        eventPlatform = "";
        eventOwner = "";
    }

    public static EventItem[] ReadEventListFromDB() {

        final String QUERY = "select event_id, marketing_code, event_name, event_short_name, "
                + "event_rubric, event_topic, event_date, event_type, event_platform, "
                + "event_owner from event_list";

        // ArrayList of Contact Item Strings
        ArrayList<EventItem> eiA = new ArrayList<>();

        // Open mySQL Connection and read Items
        try (Connection connection = DriverManager
                .getConnection(AppGlobalSettings.mySQLServerURL, AppGlobalSettings.mySQLServerUser,
                        AppGlobalSettings.mySQLServerPassword);

             // Step 2:Create a statement using connection object
             Statement stmt = connection.createStatement();

             // Step 3: Execute the query or update query
             ResultSet rs = stmt.executeQuery(QUERY)) {

            // Step 4: Process the ResultSet object.
            while (rs.next()) {

                EventItem eiItem = new EventItem();


                eiItem.eventID = rs.getInt("event_id");
                eiItem.marketingCode = rs.getString("marketing_code");
                eiItem.eventName = rs.getString("event_name");
                eiItem.eventShortName = rs.getString("event_short_name");
                eiItem.eventRubric = rs.getString("event_rubric");
                eiItem.eventTopic = rs.getString("event_topic");
                eiItem.eventDate = rs.getDate("event_date");
                eiItem.eventType = rs.getString("event_type");
                eiItem.eventPlatform = rs.getString("event_platform");
                eiItem.eventOwner = rs.getString("event_owner");

                eiA.add(eiItem);
            }

        } catch (SQLException e) {
            printSQLException(e);
        }

        EventItem[] eiArray = new EventItem[eiA.size()];
        eiA.toArray(eiArray);
        return eiArray;
    }

    int getEventID(){
        return this.eventID;
    }

    String getEventDataByName(String s){
        switch (s) {
            case "marketingCode":
                return this.marketingCode;
            case "eventName":
                return this.eventName;
            case "eventShortName":
                return this.eventShortName;
            case "eventRubric":
                return this.eventRubric;
            case "eventTopic":
                return this.eventTopic;
            case "eventType":
                return this.eventType;
            case "eventPlatform":
                return this.eventPlatform;
            case "eventOwner":
                return this.eventOwner;
            default:
                System.out.println("Error with function argument: such field don't exist in the EventItem Class");
                return "";
        }
    }

    String getEventDataByIndex(int i){
        switch (i) {
            case 1:
                return this.marketingCode;
            case 2:
                return this.eventName;
            case 3:
                return this.eventShortName;
            case 4:
                return this.eventRubric;
            case 5:
                return this.eventTopic;
            case 7:
                return this.eventType;
            case 8:
                return this.eventPlatform;
            case 9:
                return this.eventOwner;
            default:
                System.out.println("Error with function argument: wrong index of the eventItem Class");
                return "";
        }
    }

    Date getEventDate(){
        return this.eventDate;
    }

    public void print(){
        String str = "" + this.eventID + ", "
                + this.marketingCode + ", "
                + this.eventName + ", "
                + this.eventShortName + ", "
                + this.eventRubric + ", "
                + this.eventTopic + ", "
                + this.eventDate + ", "
                + this.eventType + ", "
                + this.eventPlatform + ", "
                + this.eventOwner;

        System.out.print(str);
    }

    public void println(){
        String str = "" + this.eventID + ", "
                + this.marketingCode + ", "
                + this.eventName + ", "
                + this.eventShortName + ", "
                + this.eventRubric + ", "
                + this.eventTopic + ", "
                + this.eventDate + ", "
                + this.eventType + ", "
                + this.eventPlatform + ", "
                + this.eventOwner;

        System.out.println(str);
    }

}
