package com.spikingacacia.spikyletabuyer.restaurants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.shop.ShopA;

public class SRRestaurantsA extends AppCompatActivity
    implements SRRestaurantsF.OnListFragmentInteractionListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_srrestaurants);
        setTitle("Restaurants");
        Fragment fragment= SRRestaurantsF.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"restaurants");
        transaction.commit();
    }
    /**
     * implementation of SRRestaurants.java
     * */
    @Override
    public void onItemClicked(Restaurants item)
    {
        Intent intent=new Intent(this, ShopA.class);
         intent.putExtra("seller_email",item.getEmail());
         intent.putExtra("order_radius",item.getRadius());
         intent.putExtra("buyer_distance",item.getDistance());
        intent.putExtra("number_of_tables",item.getNumberOfTables());
        intent.putExtra("table_number",item.getTableNumber());
        startActivity(intent);
    }
}
