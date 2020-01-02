package com.spikingacacia.spikyletabuyer.restaurants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.shop.ShopA;

public class SRRestaurantsA extends AppCompatActivity
    implements SRRestaurantsF.OnListFragmentInteractionListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_srrestaurants);
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Restaurants");
        Fragment fragment= SRRestaurantsF.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"restaurants");
        transaction.commit();
    }
    /**
     * implementation of SRRestaurants.java
     * */
    @Override
    public void onItemClicked(SRRestaurantsC.RestaurantItem item)
    {
        Intent intent=new Intent(this, ShopA.class);
         intent.putExtra("seller_id",item.id);
         intent.putExtra("order_radius",item.radius);
         intent.putExtra("buyer_distance",item.distance);
        intent.putExtra("number_of_tables",item.numberOfTables);
        startActivity(intent);
    }
}
