package ma.ensaf.veryempty.models;

import java.io.Serializable;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class CUsers implements Serializable {
    private int id;
    private String name;
    private int image;
    private String location;
    private String phoneNumber;
    private String bloodGroup;
    private String lastDonatedDate;

    public CUsers() {
    }

    public CUsers(int id, String name, int image, String location, String phoneNumber, String bloodGroup) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.bloodGroup = bloodGroup;
    }

    public CUsers(int id, String name, int image, String location, String phoneNumber, String bloodGroup, String lastDonatedDate) {
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
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