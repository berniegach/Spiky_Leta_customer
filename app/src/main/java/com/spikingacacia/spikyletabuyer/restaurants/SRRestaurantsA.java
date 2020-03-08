package com.spikingacacia.spikyletabuyer.restaurants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.shop.ShopA;

public class SRRestaurantsA extends AppCompatActivity
    implements SRRestaurantsF.OnListFragmentInteractionListener
{
    Preferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_srrestaurants);
        //preference
        preferences=new Preferences(getBaseContext());
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Restaurants");
        Fragment fragment= SRRestaurantsF.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"restaurants");
        transaction.commit();
        if(!preferences.isDark_theme_enabled())
        {
            setTheme(R.style.AppThemeLight_NoActionBarLight);
            toolbar.setTitleTextColor(getResources().getColor(R.color.text_light));
            toolbar.setPopupTheme(R.style.AppThemeLight_PopupOverlayLight);
            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
            appBarLayout.getContext().setTheme(R.style.AppThemeLight_AppBarOverlayLight);
            appBarLayout.setBackgroundColor(getResources().getColor(R.color.main_background_light));
            findViewById(R.id.main).setBackgroundColor(getResources().getColor(R.color.main_background_light));
        }
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
