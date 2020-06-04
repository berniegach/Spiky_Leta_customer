package com.spikingacacia.spikyletabuyer.database;

public class ServerAccount
{
    private int id;
    private String email;
    private String password;
    private String username;
    private String location;
    private String imageType;
    private String dateadded;
    private String datechanged;

    public ServerAccount(){}

    public ServerAccount(int id, String email, String password, String username, String location, String imageType, String dateadded, String datechanged) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.location = location;
        this.imageType = imageType;
        this.dateadded = dateadded;
        this.datechanged = datechanged;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageType()
    {
        return imageType;
    }

    public void setImageType(String imageType)
    {
        this.imageType = imageType;
    }
    public String getDateadded() {
        return dateadded;
    }

    public void setDateadded(String dateadded) {
        this.dateadded = dateadded;
    }

    public String getDatechanged() {
        return datechanged;
    }

    public void setDatechanged(String datechanged) {
        this.datechanged = datechanged;
    }

}