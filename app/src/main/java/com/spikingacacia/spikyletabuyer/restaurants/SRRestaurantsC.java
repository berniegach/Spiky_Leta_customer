package com.spikingacacia.spikyletabuyer.restaurants;

import com.spikingacacia.spikyletabuyer.BMenuA;
import com.spikingacacia.spikyletabuyer.database.BRestaurants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class SRRestaurantsC
{
    /**
     * An array of items.
     */
    public final List<RestaurantItem> ITEMS = new ArrayList<RestaurantItem>();
    public final Map<String, RestaurantItem> ITEM_MAP = new HashMap<String, RestaurantItem>();

    public SRRestaurantsC()
    {
        int pos=1;
        Iterator<BRestaurants> iterator= BMenuA.bRestaurantsList.iterator();
        while(iterator.hasNext())
        {
            BRestaurants bRestaurants = iterator.next();
            int id=bRestaurants.getId();
            String names=bRestaurants.getNames();
            double distance=bRestaurants.getDistance();
            int radius=bRestaurants.getRadius();
            int tables=bRestaurants.getNumberOfTables();

            addItem(createItem(pos,id,names, distance,radius, tables));
            pos+=1;
        }
    }



    private  void addItem(RestaurantItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.position, item);
    }

    public RestaurantItem createItem(int position, int id, String names, double distance,int radius, int numberOfTables) {
        return new RestaurantItem(String.valueOf(position),  id, names,  distance, radius, numberOfTables);
    }


    public  class RestaurantItem
    {
        public final String position;
        public final int id;
        public final String names;
        public final double distance;
        public final int radius;
        public final int numberOfTables;

        public RestaurantItem(String position, int id, String names, double distance, int radius, int numberOfTables) {
            this.position=position;
            this.id = id;
            this.names=names;
            this.distance=distance;
            this.radius=radius;
            this.numberOfTables=numberOfTables;
        }

        @Override
        public String toString() {
            return names;
        }
    }
}
