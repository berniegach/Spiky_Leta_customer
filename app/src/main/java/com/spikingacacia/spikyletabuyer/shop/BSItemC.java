package com.spikingacacia.spikyletabuyer.shop;

import com.spikingacacia.spikyletabuyer.database.SItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.spikingacacia.spikyletabuyer.shop.ShopA.bItemsList;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class BSItemC
{
    /**
     * An array of items.
     */
    public final List<InventoryItem> ITEMS = new ArrayList<InventoryItem>();
    public final Map<String, InventoryItem> ITEM_MAP = new HashMap<String, InventoryItem>();

    public BSItemC(int categoryId, int groupId)
    {
        int pos=1;
        Iterator iterator= bItemsList.entrySet().iterator();
        while(iterator.hasNext())
        {
            LinkedHashMap.Entry<Integer, SItems>set=(LinkedHashMap.Entry<Integer, SItems>) iterator.next();
            int id=set.getKey();
            SItems sItems=set.getValue();
            int category=sItems.getCategory();
            int group=sItems.getGroup();
            String item=sItems.getItem();
            String description=sItems.getDescription();
            double selling_price=sItems.getSellingPrice();
            int available=sItems.getAvailable();
            String date_added=sItems.getDateadded();
            String date_changed=sItems.getDatechanged();
            if(groupId==group && category==categoryId)
            {
                addItem(createItem(pos,id,category,group,item,description,selling_price,available,date_added,date_changed));
                pos+=1;
            }
        }
    }



    private  void addItem(InventoryItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.position, item);
    }

    public InventoryItem createItem(int position, int id, int category, int group, String item, String description, double sellingPrice, int available, String dateadded, String datechanged) {
        return new InventoryItem(String.valueOf(position),  id, category, group, item, description, sellingPrice, available, dateadded,  datechanged);
    }


    public  class InventoryItem
    {
        public final String position;
        public final int id;
        public final int category;
        public final int group;
        public final String item;
        public final String description;
        public final double sellingPrice;
        public final int available;
        public final String dateadded;
        public final String datechanged;

        public InventoryItem(String position, int id, int category, int group, String item, String description, double sellingPrice, int available, String dateadded, String datechanged) {
            this.position=position;
            this.id = id;
            this.category = category;
            this.group=group;
            this.item=item;
            this.description = description;
            this.sellingPrice=sellingPrice;
            this.available=available;
            this.dateadded = dateadded;
            this.datechanged = datechanged;
        }

        @Override
        public String toString() {
            return item;
        }
    }
}
