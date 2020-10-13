/*
 * Created by Benard Gachanja on 11/07/20 10:13 AM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 7/11/20 11:24 AM
 */

package com.spikingacacia.spikyletabuyer.database;

public class MpesaRequests
{
    private int id ;
    private String seller_email;
    private String order_number;
    private String date_added;
    private String business_shortcode;
    private String password;
    private String timestamp;
    private String chequeout_request_id;

    public MpesaRequests(int id, String seller_email, String order_number, String date_added, String business_shortcode, String password, String timestamp, String chequeout_request_id)
    {
        this.id = id;
        this.seller_email = seller_email;
        this.order_number = order_number;
        this.date_added = date_added;
        this.business_shortcode = business_shortcode;
        this.password = password;
        this.timestamp = timestamp;
        this.chequeout_request_id = chequeout_request_id;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getSeller_email()
    {
        return seller_email;
    }

    public void setSeller_email(String seller_email)
    {
        this.seller_email = seller_email;
    }

    public String getOrder_number()
    {
        return order_number;
    }

    public void setOrder_number(String order_number)
    {
        this.order_number = order_number;
    }

    public String getDate_added()
    {
        return date_added;
    }

    public void setDate_added(String date_added)
    {
        this.date_added = date_added;
    }

    public String getBusiness_shortcode()
    {
        return business_shortcode;
    }

    public void setBusiness_shortcode(String business_shortcode)
    {
        this.business_shortcode = business_shortcode;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getChequeout_request_id()
    {
        return chequeout_request_id;
    }

    public void setChequeout_request_id(String chequeout_request_id)
    {
        this.chequeout_request_id = chequeout_request_id;
    }





}
