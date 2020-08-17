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
import com.spikingacacia.spikyletabuyer.shop.ShopA;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class ExploreActivity extends AppCompatActivity implements
        RestaurantsFragment.OnListFragmentInteractionListener
{
    private FusedLocationProviderClient mFusedLocationClient ;
    private final int PERMISSION_REQUEST_INTERNET=3;
    private String TAG = "explore_a";
    public static  List<Restaurants> restaurantsList;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
        restaurantsList = new LinkedList<>();
        setTitle("Pre-Order");
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
        getCurrentLocation();
    }

    private void getCurrentLocation()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //get the users location
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>()
                    {
                        @Override
                        public void onSuccess(Location location)
                        {
                            //Get last known location. In some rare situations this can be null
                            if(location!=null)
                            {
                                //Get last known location. In some rare situations this can be null
                                final double latitude=location.getLatitude();
                                final double longitude=location.getLongitude();
                                new RestaurantsTask(String.valueOf(latitude),String.valueOf(longitude),"null").execute((Void)null);

                                /*Thread thread=new Thread()
                                {
                                    @Override
                                    public void run()
                                    {
                                        //get addresses
                                        Geocoder geocoder=new Geocoder(ExploreActivity.this, Locale.getDefault());
                                        List<Address> addresses;
                                        try
                                        {
                                            addresses=geocoder.getFromLocation(latitude,longitude,10);
                                            new RestaurantsTask(String.valueOf(latitude),String.valueOf(longitude),addresses.get(0).getCountryCode()).execute((Void)null);
                                            for(int c=0; c<addresses.size(); c+=1)
                                                Log.d("loc: ",addresses.get(c).getLocality()+"\n");
                                        }
                                        catch (IOException e)
                                        {
                                            Log.e(TAG,""+e.getMessage());
                                        }
                                    }
                                };
                                thread.start();*/

                            }

                        }
                    });
        }
        //request the permission
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_INTERNET);
        }
    }
    private void showRestaurants()
    {
        ((ProgressBar)findViewById(R.id.progress)).setVisibility(View.GONE);
        Fragment fragment = RestaurantsFragment.newInstance(1);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
        .add(R.id.base,fragment,"restaurant").commitAllowingStateLoss();
        //when calling commit() it would cause IllegalStateException so i changed to commitAllowingStateLoss()
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
        intent.putExtra("seller_email",item.getEmail());
        intent.putExtra("order_radius",item.getRadius());
        intent.putExtra("buyer_distance",item.getDistance());
        intent.putExtra("number_of_tables",item.getNumberOfTables());
        intent.putExtra("table_number",item.getTableNumber());
        intent.putExtra("has_payment", has_payment);
        intent.putExtra("m_code", has_payment ? item.getmCode() : "");
        intent.putExtra("dining_options", item.getDiningOptions());
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

    private class RestaurantsTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_restaurants=base_url+"get_near_restaurants.php";
        private JSONParser jsonParser;
        //final private String country;
        final private String latitude;
        final private String longitude;
        final private String location;
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";

        public RestaurantsTask( String latitude, String longitude, String location)
        {
            jsonParser = new JSONParser();
            //this.country=country;
            this.latitude=latitude;
            this.longitude=longitude;
            this.location=location;
        }
        @Override
        protected void onPreExecute()
        {

            Log.d("CRESTAUNRANTS: ","starting....");
            if(!restaurantsList.isEmpty())
                restaurantsList.clear();
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            //info.add(new BasicNameValuePair("country",country));
            info.add(new BasicNameValuePair("latitude",latitude));
            info.add(new BasicNameValuePair("longitude",longitude));
            info.add(new BasicNameValuePair("location",location));
            info.add(new BasicNameValuePair("which","2"));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_restaurants,"POST",info);
            Log.d("cTasks",""+jsonObject.toString());
            try
            {
                JSONArray restArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    restArrayList=jsonObject.getJSONArray("restaurants");
                    restArrayList=restArrayList.getJSONArray(0);
                    for(int count=0; count<restArrayList.length(); count+=1)
                    {
                        JSONObject jsonObject_restaurants=restArrayList.getJSONObject(count);
                        int id=jsonObject_restaurants.getInt("id");
                        if (!BuildConfig.DEBUG && id == 10)
                        {
                            // do not show the testing restaurant which is spiky leta
                            continue;
                        }
                        String email = jsonObject_restaurants.getString("email");
                        String names=jsonObject_restaurants.getString("username");
                        double distance=jsonObject_restaurants.getDouble("distance");
                        double latitude=jsonObject_restaurants.getDouble("latitude");
                        double longitude=jsonObject_restaurants.getDouble("longitude");
                        String locality=jsonObject_restaurants.getString("locality");
                        String country_code = jsonObject_restaurants.getString("country_code");
                        int order_radius=jsonObject_restaurants.getInt("order_radius");
                        int number_of_tables=jsonObject_restaurants.getInt("number_of_tables");
                        String image_type=jsonObject_restaurants.getString("image_type");
                        int table_number = jsonObject_restaurants.getInt("table_number");
                        String m_code = jsonObject_restaurants.getString("m_code");
                        String dining_options = jsonObject_restaurants.getString("dining_options");


                        Restaurants restaurants =new Restaurants(id,email,names,distance,latitude,longitude,locality,country_code, order_radius, number_of_tables, image_type, table_number, m_code, dining_options);
                        restaurantsList.add(restaurants);
                    }
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    Log.e(TAG_MESSAGE,""+message);
                    return false;
                }
            }
            catch (JSONException e)
            {
                Log.e("JSON",""+e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean successful)
        {

            if (successful)
            {
                showRestaurants();
                //RestaurantsData.setRestaurantsData();
            }
            else
            {
                Toast.makeText(getBaseContext(),"Error getting restaurants",Toast.LENGTH_SHORT).show();
            }
        }
    }
}