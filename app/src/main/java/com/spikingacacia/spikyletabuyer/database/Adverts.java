package com.spikingacacia.spikyletabuyer.database;

import android.graphics.Bitmap;

public class Adverts
{
    private  int id;
    private String imageType;
    private String sellerImageType;
    private int seller_id;
    private  String seller_name;
    private  String title;
    private  String content;
    private  int views;
    private  int likes;
    private  int comments;
    private  String date;

    public Adverts(int id, String imageType, String sellerImageType, int seller_id, String seller_name, String title, String content, int views, int likes, int comments, String date)
    {
        this.id = id;
        this.imageType = imageType;
        this.sellerImageType = sellerImageType;
        this.seller_id = seller_id;
        this.seller_name = seller_name;
        this.title = title;
        this.content = content;
        this.views = views;
        this.likes = likes;
        this.comments = comments;
        this.date = date;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getImageType()
    {
        return imageType;
    }

    public void setImageType(String imageType)
    {
        this.imageType = imageType;
    }

    public String getSellerImageType()
    {
        return sellerImageType;
    }

    public void setSellerImageType(String sellerImageType)
    {
        this.sellerImageType = sellerImageType;
    }
    public int getSeller_id()
    {
        return seller_id;
    }

    public void setSeller_id(int seller_id)
    {
        this.seller_id = seller_id;
    }
    public String getSeller_name()
    {
        return seller_name;
    }

    public void setSeller_name(String seller_name)
    {
        this.seller_name = seller_name;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
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

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }


}
