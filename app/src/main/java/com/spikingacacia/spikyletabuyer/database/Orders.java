/*
 * Created by Benard Gachanja on 09/10/19 4:20 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 9/23/20 3:10 PM
 */

package com.spikingacacia.spikyletabuyer.database;

import java.io.Serializable;

public class Orders implements Serializable
{
    private int id;
    private String waiterEmail;
    private int itemId;
    private int orderNumber;
    private int orderStatus;
    private String urlCodeStartDelivery;
    private String urlCodeEndDelivery;
    private String dateAdded;
    private String dateChanged;
    private String dateAddedLocal;
    private String item;
    private String size;
    private double price;
    private int sellerId;
    private String sellerEmail;
    private String sellerImageType;
    private String sellerNames;
    private String waiterNames;
    private int orderFormat;
    public int tableNumber;
    private int preOrder;
    private String collectTime;
    private int paymentType;
    private int orderType;
    private String mpesaMessage;

    public Orders(int id, String waiterEmail, int itemId, int orderNumber, int orderStatus, String urlCodeStartDelivery, String urlCodeEndDelivery, String dateAdded, String dateChanged, String dateAddedLocal, String item, String size, double price, int sellerId, String sellerEmail, String sellerImageType,
                  String sellerNames, String waiterNames, int orderFormat, int tableNumber, int preOrder, String collectTime, int paymentType, int order_type, String mpesaMessage)
    {
        this.id = id;
        this.waiterEmail = waiterEmail;
        this.itemId = itemId;
        this.orderNumber = orderNumber;
        this.orderStatus = orderStatus;
        this.urlCodeStartDelivery = urlCodeStartDelivery;
        this.urlCodeEndDelivery = urlCodeEndDelivery;
        this.dateAdded = dateAdded;
        this.dateChanged = dateChanged;
        this.dateAddedLocal = dateAddedLocal;
        this.item = item;
        this.size = size;
        this.price = price;
        this.sellerId = sellerId;
        this.sellerEmail = sellerEmail;
        this.sellerImageType = sellerImageType;
        this.sellerNames = sellerNames;
        this.waiterNames = waiterNames;
        this.orderFormat = orderFormat;
        this.tableNumber = tableNumber;
        this.preOrder = preOrder;
        this.collectTime = collectTime;
        this.paymentType = paymentType;
        this.orderType = order_type;
        this.mpesaMessage = mpesaMessage;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getWaiterEmail()
    {
        return waiterEmail;
    }

    public void setWaiterEmail(String waiterEmail)
    {
        this.waiterEmail = waiterEmail;
    }

    public int getItemId()
    {
        return itemId;
    }

    public void setItemId(int itemId)
    {
        this.itemId = itemId;
    }

    public int getOrderNumber()
    {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber)
    {
        this.orderNumber = orderNumber;
    }

    public int getOrderStatus()
    {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus)
    {
        this.orderStatus = orderStatus;
    }
    public String getUrlCodeStartDelivery()
    {
        return urlCodeStartDelivery;
    }

    public void setUrlCodeStartDelivery(String urlCodeStartDelivery)
    {
        this.urlCodeStartDelivery = urlCodeStartDelivery;
    }

    public String getUrlCodeEndDelivery()
    {
        return urlCodeEndDelivery;
    }

    public void setUrlCodeEndDelivery(String urlCodeEndDelivery)
    {
        this.urlCodeEndDelivery = urlCodeEndDelivery;
    }

    public String getDateAdded()
    {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded)
    {
        this.dateAdded = dateAdded;
    }

    public String getDateChanged()
    {
        return dateChanged;
    }

    public void setDateChanged(String dateChanged)
    {
        this.dateChanged = dateChanged;
    }
    public String getDateAddedLocal()
    {
        return dateAddedLocal;
    }

    public void setDateAddedLocal(String dateAddedLocal)
    {
        this.dateAddedLocal = dateAddedLocal;
    }

    public String getItem()
    {
        return item;
    }

    public void setItem(String item)
    {
        this.item = item;
    }

    public String getSize()
    {
        return size;
    }

    public void setSize(String size)
    {
        this.size = size;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public int getSellerId()
    {
        return sellerId;
    }

    public void setSellerId(int sellerId)
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
    public String getSellerImageType()
    {
        return sellerImageType;
    }

    public void setSellerImageType(String sellerImageType)
    {
        this.sellerImageType = sellerImageType;
    }

    public String getSellerNames()
    {
        return sellerNames;
    }

    public void setSellerNames(String sellerNames)
    {
        this.sellerNames = sellerNames;
    }

    public String getWaiterNames()
    {
        return waiterNames;
    }

    public void setWaiterNames(String waiterNames)
    {
        this.waiterNames = waiterNames;
    }

    public int getOrderFormat()
    {
        return orderFormat;
    }

    public void setOrderFormat(int orderFormat)
    {
        this.orderFormat = orderFormat;
    }

    public int getTableNumber()
    {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber)
    {
        this.tableNumber = tableNumber;
    }

    public int getPreOrder()
    {
        return preOrder;
    }

    public void setPreOrder(int preOrder)
    {
        this.preOrder = preOrder;
    }

    public String getCollectTime()
    {
        return collectTime;
    }

    public void setCollectTime(String collectTime)
    {
        this.collectTime = collectTime;
    }
    public int getPaymentType()
    {
        return paymentType;
    }

    public void setPaymentType(int paymentType)
    {
        this.paymentType = paymentType;
    }
    public int getOrderType()
    {
        return orderType;
    }

    public void setOrderType(int orderType)
    {
        this.orderType = orderType;
    }
    public String getMpesaMessage()
    {
        return mpesaMessage;
    }

    public void setMpesaMessage(String mpesaMessage)
    {
        this.mpesaMessage = mpesaMessage;
    }
}
