package ma.ensaf.veryempty.models;

import java.io.Serializable;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class Users implements Serializable {
    private int id;
    private String name;
    private String image;
    private String location;
    private String phoneNumber;
    private String bloodGroup;
    private String lastDonatedDate;

    public Users() {
    }

    public Users(int id, String name, String image, String location, String phoneNumber, String bloodGroup) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.bloodGroup = bloodGroup;
    }

    public Users(int id, String name, String image, String location, String phoneNumber, String bloodGroup, String lastDonatedDate) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.bloodGroup = bloodGroup;
        this.lastDonatedDate = lastDonatedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getLastDonatedDate() {
        return lastDonatedDate;
    }

    public void setLastDonatedDate(String lastDonatedDate) {
        this.lastDonatedDate = lastDonatedDate;
    }
}

