/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 9/18/20 5:48 PM
 */

package com.spikingacacia.spikyletabuyer.explore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.spikingacacia.spikyletabuyer.BuildConfig;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.shop.ShopA;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class ExploreActivity extends AppCompatActivity implements
        RestaurantsFragment.OnListFragmentInteractionListener
{
    private String TAG = "explore_a";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        setTitle("Pre-Order");
        Fragment fragment = RestaurantsFragment.newInstance(1);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.base,fragment,"restaurant").commit();
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        //getCurrentLocation();
    }

    public void gotoRestaurant(Restaurants item)
    {
        //if the location of the hotel is kenya then we ask for mpesa payment
        boolean has_payment = false;
            /*if( item.getmCode().contentEquals("") ||  item.getmCode().contentEquals("null") || item.getmCode().contentEquals("NULL"))
                has_payment = false;*/
        if(item.getCountryCode().contentEquals("KE"))
            has_payment = true;
        Intent intent=new Intent(this, ShopA.class);
        intent.putExtra("which",2);
        intent.putExtra("seller_names", item.getNames());
        intent.putExtra("seller_email",item.getEmail());
        intent.putExtra("order_radius",item.getRadius());
        intent.putExtra("buyer_distance",item.getDistance());
        intent.putExtra("number_of_tables",item.getNumberOfTables());
        intent.putExtra("table_number",item.getTableNumber());
        intent.putExtra("has_payment", has_payment);
        intent.putExtra("m_code", has_payment ? item.getmCode() : "");
        intent.putExtra("dining_options", item.getDiningOptions());
        //check if restaurant is within delivery radius
        boolean withinDeliveryRadius = false;
        if(item.getDistance()/1000<= (double) item.getDeliveryRadius())
            withinDeliveryRadius = true;
        intent.putExtra("within_delivery_radius",withinDeliveryRadius);
        Log.e("jkadsajdgsd"," delivery "+withinDeliveryRadius+" distance "+item.getDistance() +" radius "+item.getDeliveryRadius());

        startActivity(intent);
    }
/*
*** implementation of RestaurantsFragment.java
 */
    @Override
    public void onItemClicked(Restaurants item)
    {
        gotoRestaurant(item);
    }

}