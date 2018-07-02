package ie.ucc.bis.a114355681.learnerlog.datamodel;

/**
 * Created by Lorna on 31/10/2017.
 */

//This class holds the booking information to be inserted/retrieved from the database

public class Booking {

    private String lessonType;
    private String date;
    private String time;
    private String location;
    private String status;
    private String userID;
    private String name;
    private String lessonNum;

    //empty constructor
    public Booking(){

    }

    //loaded constructor
    public Booking(String lessonType, String date, String time, String location, String status, String userID, String name, String lessonNum) {
        this.lessonType = lessonType;
        this.date = date;
        this.time = time;
        this.location = location;
        this.status = status;
        this.userID = userID;
        this.name = name;
        this.lessonNum = lessonNum;
    }

    //getter and setters

    public String getLessonNum() {
        return lessonNum;
    }

    public void setLessonNum(String lessonNum) {
        this.lessonNum = lessonNum;
    }

    public String getLessonType() {
        return lessonType;
    }

    public void setLessonType(String lessonType) {
        this.lessonType = lessonType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
