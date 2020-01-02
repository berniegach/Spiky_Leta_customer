package com.spikingacacia.spikyletabuyer.database;

public class BMessages
{
    private int id;
    private int classes;
    private String message;
    private String dateAdded;
    public BMessages()
    {
    }

    public BMessages(int id, int classes, String message, String dateAdded)
    {
        this.id = id;
        this.classes = classes;
        this.message = message;
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

    public int getClasses()
    {
        return classes;
    }

    public void setClasses(int classes)
    {
        this.classes = classes;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
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
