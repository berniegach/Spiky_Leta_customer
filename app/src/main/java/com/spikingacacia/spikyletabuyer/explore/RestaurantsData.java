package com.spikingacacia.spikyletabuyer.explore;

import android.util.Log;

import com.spikingacacia.spikyletabuyer.database.Restaurants;

import java.util.ArrayList;
import java.util.List;

public class RestaurantsData
{
    private static List<String> restaurants;
    static {
        restaurants =  new ArrayList<String>();
        for(int c=0; c<MapsExploreActivity.restaurantsList.size(); c++)
        {
            Restaurants restaurants_info = MapsExploreActivity.restaurantsList.get(c);
            String name = restaurants_info.getNames();
            restaurants.add(name+":"+c);
        }
    }

    public static List<String> getRestaurants(){
        return restaurants;
    }

    public static List<String> filterData(String searchString){
        List<String> searchResults =  new ArrayList<String>();
        if(searchString != null){
            searchString = searchString.toLowerCase();

            for(String rec : restaurants){
                if(rec.toLowerCase().contains(searchString)){
                    searchResults.add(rec);
                }
            }
        }
        return searchResults;
    }
    public static void setRestaurantsData()
    {
        restaurants.clear();
        for(int c=0; c<MapsExploreActivity.restaurantsList.size(); c++)
        {
            Restaurants restaurants_info = MapsExploreActivity.restaurantsList.get(c);
            String name = restaurants_info.getNames();
            restaurants.add(name+":"+c);
        }
    }
}
