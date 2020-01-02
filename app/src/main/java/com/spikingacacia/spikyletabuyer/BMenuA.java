package com.spikingacacia.spikyletabuyer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.MapsExploreA;
import com.spikingacacia.spikyletabuyer.messages.BMMessageListActivity;
import com.spikingacacia.spikyletabuyer.orders.BOOrdersA;
import com.spikingacacia.spikyletabuyer.restaurants.SRRestaurantsA;
import com.spikingacacia.spikyletabuyer.database.BRestaurants;

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

public class BMenuA extends AppCompatActivity
implements BMenuF.OnFragmentInteractionListener
{
    private static final int PERMISSION_REQUEST_INTERNET=2;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String url_get_restaurants=base_url+"get_near_restaurants.php";
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private String TAG="BMenuA";
    private JSONParser jsonParser;
    public static  List<BRestaurants>bRestaurantsList;
    private TextView tWho;
    private boolean runRate=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_bmenu);
        //set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout=findViewById(R.id.collapsingToolbar);
        final Typeface tf= ResourcesCompat.getFont(this,R.font.amita);
        collapsingToolbarLayout.setCollapsedTitleTypeface(tf);
        collapsingToolbarLayout.setExpandedTitleTypeface(tf);
        setSupportActionBar(toolbar);
        setTitle("Menu");

        Fragment fragment=BMenuF.newInstance("","");
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"menu");
        transaction.commit();
        //
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        jsonParser=new JSONParser();
        bRestaurantsList=new LinkedList<>();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        //set the welcome text
        //we set it in onResume to factor in the possibility of the username changing in the settings
        tWho=findViewById(R.id.who);
        try
        {
            if(LoginA.buyerAccount.getUsername().length()<2 || LoginA.buyerAccount.getUsername().contentEquals("null"))
            {
                tWho.setText("Please go to settings and set your name...");
            }
            else
                tWho.setText(LoginA.buyerAccount.getUsername());
        }
        catch (Exception e)
        {
            tWho.setText("Please go to settings and set your name...");
        }
        if(runRate)
        {
            AppRater.app_launched(this);
            runRate=false;
        }
    }
    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
        new AlertDialog.Builder(BMenuA.this)
                .setTitle("Quit")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finishAffinity();
                        //Intent intent=new Intent(Intent.ACTION_MAIN);
                        // intent.addCategory(Intent.CATEGORY_HOME);
                        // startActivity(intent);
                    }
                }).create().show();
    }
    /**implementation of UMenuFragment.java**/
    @Override
    public void onMenuClicked(int id)
    {
        if(id==1)
        {
            //restaurant
            new AlertDialog.Builder(BMenuA.this)
                    .setItems(new String[]{"Scan QR code", "Location"}, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            if(i==0)
                            {
                                //scan the QR code to access the restaurant
                            }
                            else if(i==1)
                            {
                                Toast.makeText(getBaseContext(),"Please wait",Toast.LENGTH_SHORT).show();
                                //get the users location
                               getCurrentLocation();
                            }
                        }
                    }).create().show();
            //Intent intent=new Intent(this, UPProfileA.class);
            // intent.putExtra("NOTHING","nothing");
            //startActivity(intent);
        }
        else if(id==2)
        {
            //orders
            Intent intent=new Intent(this, BOOrdersA.class);
            startActivity(intent);
        }
        else if(id==3)
        {
            //explore
            Intent intent=new Intent(this, MapsExploreA.class);
            startActivity(intent);
        }
        else if(id==4)
        {
            //notifications
            Intent intent=new Intent(this, BMMessageListActivity.class);
            startActivity(intent);
        }
        else if(id==5)
        {
            //settings
            Intent intent=new Intent(this, BSettingsA.class);
            // intent.putExtra("NOTHING","nothing");
            startActivity(intent);
        }
    }
    @Override
    public void onLogOut()
    {
        new AlertDialog.Builder(BMenuA.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SharedPreferences loginPreferences=getBaseContext().getSharedPreferences("loginPrefs",MODE_PRIVATE);
                        SharedPreferences.Editor loginPreferencesEditor =loginPreferences.edit();
                        loginPreferencesEditor.putBoolean("rememberme",false);
                        loginPreferencesEditor.commit();
                        finishAffinity();
                        //Intent intent=new Intent(Intent.ACTION_MAIN);
                        //intent.addCategory(Intent.CATEGORY_HOME);
                        // startActivity(intent);
                    }
                }).create().show();
    }
    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == PERMISSION_REQUEST_INTERNET && resultCode == RESULT_OK )
        {
            getCurrentLocation();
        }
    }
    private void getCurrentLocation()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //get the users location
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>()
                    {
                        @Override
                        public void onSuccess(Location location)
                        {
                            //Get last known location. In some rare situations this can be null
                            if(location!=null)
                            {
                                double latitude=location.getLatitude();
                                double longitude=location.getLongitude();
                                //get addresses
                                Geocoder geocoder=new Geocoder(BMenuA.this, Locale.getDefault());
                                List<Address> addresses;
                                try
                                {
                                    addresses=geocoder.getFromLocation(latitude,longitude,10);
                                    new RestaurantsTask(String.valueOf(latitude),String.valueOf(longitude),addresses.get(0).getLocality()).execute((Void)null);
                                    for(int c=0; c<addresses.size(); c+=1)
                                        Log.d("loc: ",addresses.get(c).getLocality()+"\n");
                                }
                                catch (IOException e)
                                {
                                    Snackbar.make(tWho,"Error getting your location.\nPlease try again.",Snackbar.LENGTH_SHORT).show();
                                    Log.e("address",""+e.getMessage());
                                }
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
        if(bRestaurantsList.size()==0)
        {
            Snackbar.make(tWho,"No restaurants near you.",Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent intent=new Intent(this, SRRestaurantsA.class);
        startActivity(intent);
    }


    /**
     * Following code will all personnel tasks info from boss tasks table.
     * The returned columns are id, titles, descriptions, startings, endings, repetitions, locations, positions, geofence dateadded, datechanged.
     * Arguments are:
     * id==boss id.
     * Returns are:
     * tasks rows
     * success==1 successful get
     * success==0 for missing certificates info
     * success==0 for id argument missing
     **/
    private class RestaurantsTask extends AsyncTask<Void, Void, Boolean>
    {
        //final private String country;
        final private String latitude;
        final private String longitude;
        final private String location;

        public RestaurantsTask( String latitude, String longitude, String location)
        {
            //this.country=country;
            this.latitude=latitude;
            this.longitude=longitude;
            this.location=location;
        }
        @Override
        protected void onPreExecute()
        {

            Log.d("CRESTAUNRANTS: ","starting....");
            if(!bRestaurantsList.isEmpty())
                bRestaurantsList.clear();
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
            info.add(new BasicNameValuePair("which","1"));
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
                        String names=jsonObject_restaurants.getString("username");
                        double distance=jsonObject_restaurants.getDouble("distance");
                        double latitude=jsonObject_restaurants.getDouble("latitude");
                        double longitude=jsonObject_restaurants.getDouble("longitude");
                        String locality=jsonObject_restaurants.getString("locality");
                        String country=jsonObject_restaurants.getString("country");
                        int order_radius=jsonObject_restaurants.getInt("order_radius");
                        int tables = jsonObject_restaurants.getInt("number_of_tables");

                        BRestaurants bRestaurants=new BRestaurants(id,names,distance,latitude,longitude,locality,country,order_radius, tables);
                        bRestaurantsList.add(bRestaurants);
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
            }
            else
            {
                Toast.makeText(getBaseContext(),"Error getting restaurants",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
