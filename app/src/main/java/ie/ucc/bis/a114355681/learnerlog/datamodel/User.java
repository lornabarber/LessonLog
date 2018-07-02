package ie.ucc.bis.a114355681.learnerlog.datamodel;

/**
 * Created by Lorna on 30/10/2017.
 */


//This class holds the user information attributes

public class User {

    private String name;
    private String address;
    private String phone_num;
    private String type;
    private String email;
    private String uid;

    public User(){

    }

    public User(String address, String name, String phone_num, String type) {
        this.address = address;
        this.name = name;
        this.phone_num = phone_num;
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
