package com.spikingacacia.spikyletabuyer.database;

import java.io.Serializable;

public class TastyBoard implements Serializable
{
    private int id;
    private String sellerId;
    private String sellerEmail;
    private String title;
    private String description;
    private int linkedItemId;
    private String sizeAndPrice;
    private String discountPrice;
    private String expiryDate;
    private String imageType;
    private int views;
    private int likes;
    private int comments;
    private int orders;
    private String dateAdded;
    //seller
    private String sellerNames;
    private String sellerImageType;
    private double distance;
    private String location;
    private String country;

    public TastyBoard(int id, String sellerId, String sellerEmail, String title, String description, int linkedItemId, String sizeAndPrice, String discountPrice, String expiryDate, String imageType,
                      int views, int likes, int comments, int orders, String dateAdded,
                      String sellerNames, String sellerImageType, double distance, String location, String country)
    {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerEmail = sellerEmail;
        this.title = title;
        this.description = description;
        this.linkedItemId = linkedItemId;
        this.sizeAndPrice = sizeAndPrice;
        this.discountPrice = discountPrice;
        this.expiryDate = expiryDate;
        this.imageType = imageType;
        this.views = views;
        this.likes = likes;
        this.comments = comments;
        this.orders = orders;
        this.dateAdded = dateAdded;
        this.sellerNames = sellerNames;
        this.sellerImageType = sellerImageType;
        this.distance = distance;
        this.location = location;
        this.country = country;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getSellerId()
    {
        return sellerId;
    }

    public void setSellerId(String sellerId)
    {
        this.sellerId = sellerId;
    }

    public String getSellerEmail()
    {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail)
    {
        this.sellerEmail = sellerEmail;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getLinkedItemId()
    {
        return linkedItemId;
    }

    public void setLinkedItemId(int linkedItemId)
    {
        this.linkedItemId = linkedItemId;
    }

    public String getSizeAndPrice()
    {
        return sizeAndPrice;
    }

    public void setSizeAndPrice(String sizeAndPrice)
    {
        this.sizeAndPrice = sizeAndPrice;
    }

    public String getDiscountPrice()
    {
        return discountPrice;
    }

    public void setDiscountPrice(String discountPrice)
    {
        this.discountPrice = discountPrice;
    }

    public String getExpiryDate()
    {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate)
    {
        this.expiryDate = expiryDate;
    }

    public String getImageType()
    {
        return imageType;
    }

    public void setImageType(String imageType)
    {
        this.imageType = imageType;
    }

    public int getViews()
    {
        return views;
    }

    public void setViews(int views)
    {
        this.views = views;
    }

    public int getLikes()
    {
        return likes;
    }

    public void setLikes(int likes)
    {
        this.likes = likes;
    }

    public int getComments()
    {
        return comments;
    }

    public void setComments(int comments)
    {
        this.comments = comments;
    }

    public int getOrders()
    {
        return orders;
    }

    public void setOrders(int orders)
    {
        this.orders = orders;
    }

    public String getDateAdded()
    {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded)
    {
        this.dateAdded = dateAdded;
    }

    public String getSellerNames()
    {
        return sellerNames;
    }

    public void setSellerNames(String sellerNames)
    {
        this.sellerNames = sellerNames;
    }

    public String getSellerImageType()
    {
        return sellerImageType;
    }

    public void setSellerImageType(String sellerImageType)
    {
        this.sellerImageType = sellerImageType;
    }

    public double getDistance()
    {
        return distance;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }
    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

}
