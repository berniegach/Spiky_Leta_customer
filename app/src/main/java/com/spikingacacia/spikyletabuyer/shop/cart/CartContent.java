package com.spikingacacia.spikyletabuyer.shop.cart;

import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.shop.ShopA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class CartContent
{
    public final List<CartItem> ITEMS = new ArrayList<CartItem>();
    public final Map<String, CartItem> ITEM_MAP = new HashMap<String, CartItem>();



    public CartContent()
    {
        Iterator iterator= ShopA.tempCartLinkedHashMap.entrySet().iterator();
        while(iterator.hasNext())
        {
            LinkedHashMap.Entry<String, Integer>set = (LinkedHashMap.Entry<String, Integer>) iterator.next();
            String id_size = set.getKey();
            String[] id_size_pieces = id_size.split(":");
            int id=Integer.parseInt(id_size_pieces[0]);
            int count=set.getValue();
            DMenu inv = ShopA.menuLinkedHashMap.get(id);
            int serverInvId = inv.getId();
            String name = inv.getItem();
            String imageType = inv.getImageType();

            int pos = ShopA.itemPriceSizeLinkedHashMap.get(id_size);
            String priceString = inv.getPrices();
            final String[] prices = priceString.split(":");
            String[] sizes = inv.getSizes().split(":");
            Double price = Double.parseDouble( prices[pos].contentEquals("null")?"0":prices[pos]);
            String size = sizes[pos];
            String description = inv.getDescription();

            addItem(createItem(id_size,serverInvId, name,price,size,description,imageType,count));
        }
    }
    private void addItem(CartItem item)
    {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private CartItem createItem(String id, int serverInvId, String name, Double price, String size, String description,  String imageType,int count)
    {
        return new CartItem(id, serverInvId, name, price, size, description, imageType, count);
    }

    public  class CartItem
    {
        public final String id;
        public final int inventoryId;
        public final String name;
        public final Double price;
        public final String size;
        public final String description;
        public final String imageType;
        public int count;

        public CartItem(String id, int serverInvId, String name, Double price, String size, String description, String imageType, int count)
        {
            this.id = id;
            inventoryId = serverInvId;
            this.name = name;
            this.price = price;
            this.size = size;
            this.description = description;
            this.imageType = imageType;
            this.count = count;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
