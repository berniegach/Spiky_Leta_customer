package com.spikingacacia.spikyletabuyer.database;

public class Orders
{
    private int id;
    private String waiterEmail;
    private int itemId;
    private int orderNumber;
    private int orderStatus;
    private String dateAdded;
    private String dateChanged;
    private String item;
    private double price;
    private int sellerId;
    private String sellerImageType;
    private String sellerNames;
    private String waiterNames;
    private int orderFormat;
    public int tableNumber;

    public Orders(int id, String waiterEmail, int itemId, int orderNumber, int orderStatus, String dateAdded, String dateChanged, String item, double price, int sellerId, String sellerImageType, String sellerNames, String waiterNames, int orderFormat, int tableNumber)
    {
        this.id = id;
        this.waiterEmail = waiterEmail;
        this.itemId = itemId;
        this.orderNumber = orderNumber;
        this.orderStatus = orderStatus;
        this.dateAdded = dateAdded;
        this.dateChanged = dateChanged;
        this.item = item;
        this.price = price;
        this.sellerId = sellerId;
        this.sellerImageType = sellerImageType;
        this.sellerNames = sellerNames;
        this.waiterNames = waiterNames;
        this.orderFormat = orderFormat;
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

    public String getItem()
    {
        return item;
    }

    public void setItem(String item)
    {
        this.item = item;
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





}