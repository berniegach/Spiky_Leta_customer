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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.barcode.BarcodeCaptureActivity;
import com.spikingacacia.spikyletabuyer.main.board.AdvertsActivity;
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

import com.google.android.gms.vision.barcode.Barcode;
import com.spikingacacia.spikyletabuyer.shop.ShopA;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class BMenuA extends AppCompatActivity
implements BMenuF.OnFragmentInteractionListener
{
    private static final int PERMISSION_REQUEST_INTERNET=2;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String TAG="BMenuA";
    private JSONParser jsonParser;

    private TextView tWho;
    private boolean runRate=true;
    Preferences preferences;
    private final static String default_notification_channel_id = "default";
    private static final int RC_BARCODE_CAPTURE = 9001;
    boolean autofocus=true;
    boolean use_flash=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_bmenu);
        //preference
        preferences=new Preferences(getBaseContext());
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
            if(LoginA.serverAccount.getUsername().length()<2 || LoginA.serverAccount.getUsername().contentEquals("null"))
            {
                tWho.setText("Please go to settings and set your name...");
            }
            else
                tWho.setText(LoginA.serverAccount.getUsername());
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
            //tasty boards
            Intent intent=new Intent(this, AdvertsActivity.class);
            startActivity(intent);
        }
        else if(id==5)
        {
            //notifications
            Intent intent=new Intent(this, BMMessageListActivity.class);
            startActivity(intent);
        }
        else if(id==6)
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
    public void play_notification()
    {
        Uri alarmSound =
                RingtoneManager. getDefaultUri (RingtoneManager. TYPE_NOTIFICATION );
        MediaPlayer mp = MediaPlayer. create (getBaseContext(), alarmSound);
        mp.start();
       /* NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(SMenuA.this, default_notification_channel_id )
                        .setSmallIcon(R.mipmap.ic_launcher )
                        .setContentTitle( "New Order" )
                        .setContentText( "a new order has arrived" ) ;
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context. NOTIFICATION_SERVICE );
        mNotificationManager.notify(( int ) System. currentTimeMillis () ,
                mBuilder.build());*/
    }




}
