package com.spikingacacia.spikyletabuyer.database;

public class Restaurants
{
    private int id;
    private String email;
    private String names;
    private double distance;
    private double latitude;
    private double longitude;
    private String locality;
    private int radius;
    private int numberOfTables;
    private String image_type;
    private int tableNumber;


    public Restaurants(int id, String email, String names, double distance, double latitude, double longitude, String locality, int radius, int numberOfTables, String image_type, int tableNumber)
    {
        this.id = id;
        this.email = email;
        this.names = names;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locality = locality;
        this.radius=radius;
        this.numberOfTables=numberOfTables;
        this.image_type = image_type;
        this.tableNumber = tableNumber;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getNames()
    {
        return names;
    }

    public void setNames(String names)
    {
        this.names = names;
    }

    public double getDistance()
    {
        return distance;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }
    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public String getLocality()
    {
        return locality;
    }

    public void setLocality(String locality)
    {
        this.locality = locality;
    }

    public int getRadius()
    {
        return radius;
    }

    public void setRadius(int radius)
    {
        this.radius = radius;
    }
    public int getNumberOfTables()
    {
        return numberOfTables;
    }

    public void setNumberOfTables(int numberOfTables)
    {
        this.numberOfTables = numberOfTables;
    }
    public String getImage_type()
    {
        return image_type;
    }

    public void setImage_type(String image_type)
    {
        this.image_type = image_type;
    }
    public int getTableNumber()
    {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber)
    {
        this.tableNumber = tableNumber;
    }

}