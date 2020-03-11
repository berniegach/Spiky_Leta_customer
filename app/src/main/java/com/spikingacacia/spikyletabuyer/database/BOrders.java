package com.spikingacacia.spikyletabuyer.database;

public class BOrders
{
    private int id;
    private int itemId;
    private int orderNumber;
    private int orderStatus;
    private String orderName;
    private double price;
    private int orderFormat;
    public int tableNumber;
    public String restaurantName;
    public String waiter_names;
    private String dateAdded;

    public BOrders(int id, int itemId, int orderNumber, int orderStatus, String orderName, double price, int orderFormat, int tableNumber,
                   String restaurantName, String waiter_names, String dateAdded)
    {
        this.id = id;
        this.itemId = itemId;
        this.orderNumber = orderNumber;
        this.orderStatus = orderStatus;
        this.orderName = orderName;
        this.price = price;
        this.orderFormat=orderFormat;
        this.tableNumber=tableNumber;
        this.restaurantName=restaurantName;
        this.waiter_names=waiter_names;
        this.dateAdded = dateAdded;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
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

    public String getOrderName()
    {
        return orderName;
    }

    public void setOrderName(String orderName)
    {
        this.orderName = orderName;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
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

    public String getRestaurantName()
    {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName)
    {
        this.restaurantName = restaurantName;
    }

    public String getWaiter_names()
    {
        return waiter_names;
    }

    public void setWaiter_names(String waiter_names)
    {
        this.waiter_names = waiter_names;
    }


    public String getDateAdded()
    {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded)
    {
        this.dateAdded = dateAdded;
    }


}
