/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 9/24/20 4:57 PM
 */

package com.spikingacacia.spikyletabuyer.database;

import java.io.Serializable;

public class DMenu implements Serializable
{
    private int id;
    private int categoryId;
    private int groupId;
    private String linkedItems;
    private String linkedItemsPrice;
    private String item;
    private String description;
    private String sizes;
    private String prices;
    private String imageType;
    private boolean available;
    private String dateAdded;
    private String datechanged;

    public DMenu(int id, int categoryId, int groupId, String linkedItems, String linkedItemsPrice, String item, String description,  String sizes, String prices, String imageType, boolean available, String dateAdded, String datechanged)
    {
        this.id = id;
        this.categoryId = categoryId;
        this.groupId = groupId;
        this.linkedItems = linkedItems;
        this.linkedItemsPrice = linkedItemsPrice;
        this.item = item;
        this.description = description;
        this.sizes = sizes;
        this.prices = prices;
        this.imageType = imageType;
        this.available = available;
        this.dateAdded = dateAdded;
        this.datechanged = datechanged;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(int categoryId)
    {
        this.categoryId = categoryId;
    }

    public int getGroupId()
    {
        return groupId;
    }

    public void setGroupId(int groupId)
    {
        this.groupId = groupId;
    }
    public String getLinkedItems()
    {
        return linkedItems;
    }

    public void setLinkedItems(String linkedItems)
    {
        this.linkedItems = linkedItems;
    }
    public String getLinkedItemsPrice()
    {
        return linkedItemsPrice;
    }

    public void setLinkedItemsPrice(String linkedItemsPrice)
    {
        this.linkedItemsPrice = linkedItemsPrice;
    }

    public String getItem()
    {
        return item;
    }

    public void setItem(String item)
    {
        this.item = item;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getSizes()
    {
        return sizes;
    }

    public void setSizes(String sizes)
    {
        this.sizes = sizes;
    }

    public String getPrices()
    {
        return prices;
    }

    public void setPrices(String prices)
    {
        this.prices = prices;
    }
    public String getImageType()
    {
        return imageType;
    }

    public void setImageType(String imageType)
    {
        this.imageType = imageType;
    }
    public boolean isAvailable()
    {
        return available;
    }

    public void setAvailable(boolean available)
    {
        this.available = available;
    }

    public String getDateAdded()
    {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded)
    {
        this.dateAdded = dateAdded;
    }

    public String getDatechanged()
    {
        return datechanged;
    }

    public void setDatechanged(String datechanged)
    {
        this.datechanged = datechanged;
    }







}
