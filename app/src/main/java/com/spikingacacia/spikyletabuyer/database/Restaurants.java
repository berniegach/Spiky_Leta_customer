/*
 * Created by Benard Gachanja on 09/10/19 4:20 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 8/14/20 4:41 PM
 */

package com.spikingacacia.spikyletabuyer.database;

import android.util.Log;

public class Restaurants implements Comparable< Restaurants>
{
    private int id;
    private String email;
    private String names;
    private double distance;
    private double latitude;
    private double longitude;
    private String locality;
    private String countryCode;
    private int radius;
    private int numberOfTables;
    private String image_type;
    private int tableNumber;
    private String mCode;
    private String diningOptions;
    private String openingTime;
    private String closingTime;
    private boolean opened;



    public Restaurants(int id, String email, String names, double distance, double latitude, double longitude, String locality, String countryCode, int radius, int numberOfTables, String image_type,
                       int tableNumber,  String mCode, String diningOptions, String openingTime, String closingTime, boolean opened)
    {
        this.id = id;
        this.email = email;
        this.names = names;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locality = locality;
        this.countryCode = countryCode;
        this.radius=radius;
        this.numberOfTables=numberOfTables;
        this.image_type = image_type;
        this.tableNumber = tableNumber;
        this.mCode = mCode;
        this.diningOptions = diningOptions;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.opened = opened;
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

    public String getCountryCode()
    {
        return countryCode;
    }

    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
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
    public String getmCode()
    {
        return mCode;
    }

    public void setmCode(String mCode)
    {
        this.mCode = mCode;
    }
    public String getDiningOptions()
    {
        return diningOptions;
    }

    public void setDiningOptions(String diningOptions)
    {
        this.diningOptions = diningOptions;
    }

    @Override
    public int compareTo(Restaurants o)
    {
        return Double.compare(distance, o.distance);
    }

    public String getOpeningTime()
    {
        return openingTime;
    }

    public void setOpeningTime(String openingTime)
    {
        this.openingTime = openingTime;
    }

    public String getClosingTime()
    {
        return closingTime;
    }

    public void setClosingTime(String closingTime)
    {
        this.closingTime = closingTime;
    }

    public boolean isOpened()
    {
        return opened;
    }

    public void setOpened(boolean opened)
    {
        this.opened = opened;
    }
}
