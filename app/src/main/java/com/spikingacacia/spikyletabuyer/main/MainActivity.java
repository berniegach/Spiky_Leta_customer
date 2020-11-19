/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 10/11/20 7:45 PM
 */

package com.spikingacacia.spikyletabuyer.main;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.provider.FontRequest;
import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.NavigationIconClickListener;
import com.spikingacacia.spikyletabuyer.database.ServerAccount;
import com.spikingacacia.spikyletabuyer.explore.ExploreActivity;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.SettingsActivity;
import com.spikingacacia.spikyletabuyer.barcode.BarcodeCaptureActivity;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.main.messages.MessagesActivity;
import com.spikingacacia.spikyletabuyer.main.order.OrderSearchFragment;
import com.spikingacacia.spikyletabuyer.main.orders_list.OrdersListFragment;
import com.spikingacacia.spikyletabuyer.main.tasty.TastyBoardActivity;
import com.spikingacacia.spikyletabuyer.orders.OrdersActivity;
import com.spikingacacia.spikyletabuyer.shop.ShopA;
import com.spikingacacia.spikyletabuyer.util.MyFirebaseMessagingService;
import com.spikingacacia.spikyletabuyer.wallet.WalletActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.LoginA.mGoogleSignInClient;


public class MainActivity extends AppCompatActivity implements
        OrderSearchFragment.OnFragmentInteractionListener,
        OrdersListFragment.OnListFragmentInteractionListener
{
    /** Change this to {@code false} when you want to use the downloadable Emoji font. */
    private static final boolean USE_BUNDLED_EMOJI = true;
    private static final int PERMISSION_REQUEST_INTERNET = 2;
    private int REQUEST_CHECK_SETTINGS = 3;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ProgressBar progressBar;
    private View mainFragment;
    private String TAG = "MainA";
    private static boolean useQrCode = false;
    ActivityResultLauncher<Intent> mGetBarcode = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                    Intent intent = result.getData();
                    try
                    {
                        Barcode barcode = intent.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        barcodeReceived(barcode);
                    } catch (NullPointerException excpetion)
                    {
                        Log.e(TAG, "no barcode");
                        // TODO: remove this its only for testing
                        //onCorrectScan();
                    }

                }
            });
    public static String myLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        initEmojiCompat();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_order, R.id.navigation_orders)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        progressBar = findViewById(R.id.progress);
        mainFragment = findViewById(R.id.nav_host_fragment);
        mainFragment.setBackgroundColor(Color.WHITE);
        myToolbar.setNavigationIcon(R.drawable.ic_baseline_menu_w);
        myToolbar.setNavigationOnClickListener(new NavigationIconClickListener(this, mainFragment, new AccelerateDecelerateInterpolator(),
                getBaseContext().getResources().getDrawable(R.drawable.ic_baseline_menu_w), getBaseContext().getResources().getDrawable( R.drawable.ic_baseline_menu_open_w)));
        setMenuOnclickListeners();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        checkFirebaseToken();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    saveCurrentLocation(location);
                }
            }
        };

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (checkIfLocationEnabled())
        {
            createLocationRequest();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
    private void setMenuOnclickListeners()
    {
        MaterialButton b_wallet = findViewById(R.id.action_wallet);
        MaterialButton b_messages = findViewById(R.id.action_messages);
        MaterialButton b_tasty_board = findViewById(R.id.action_tasty_board);
        MaterialButton b_sign_out = findViewById(R.id.action_sign_out);
        MaterialButton b_settings = findViewById(R.id.action_settings);

        b_wallet.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, WalletActivity.class);
                //prevent this activity from flickering as we call the next one
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        b_messages.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
                //prevent this activity from flickering as we call the next one
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        b_tasty_board.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, TastyBoardActivity.class);
                //prevent this activity from flickering as we call the next one
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        b_sign_out.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Log.d(TAG, "gmail signed out");
                        finish();
                    }
                });
            }
        });
        b_settings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
                //prevent this activity from flickering as we call the next one
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

    }


    // This callback is called only when there is a saved instance that is previously saved by using
// onSaveInstanceState(). We restore some state in onCreate(), while we can optionally restore
// other state here, possibly usable after onStart() has completed.
// The savedInstanceState Bundle is same as the one used in onCreate().
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        LoginA.setServerAccount((ServerAccount) savedInstanceState.getSerializable(LoginA.SAVE_INSTANCE_SERVER_ACCOUNT));
        //Log.d(TAG,"main_a has been recreated therefoew we restore server account");
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putSerializable(LoginA.SAVE_INSTANCE_SERVER_ACCOUNT, LoginA.getServerAccount());
        //Log.d(TAG,"main_a is been destroyed threfore we call onsaved instance");
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_tasty_board)
        {
            Intent intent = new Intent(MainActivity.this, TastyBoardActivity.class);
            //prevent this activity from flickering as we call the next one
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else if (id == R.id.action_messages)
        {
            Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
            //prevent this activity from flickering as we call the next one
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else if (id == R.id.action_wallet)
        {
            Intent intent = new Intent(MainActivity.this, WalletActivity.class);
            //prevent this activity from flickering as we call the next one
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else if (id == R.id.action_settings)
        {
            //proceedToSettings();
            return true;
        } else if (id == R.id.action_sign_out)
        {
            mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    Log.d(TAG, "gmail signed out");
                    finish();
                }
            });
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {

        int stack = getSupportFragmentManager().getBackStackEntryCount();
        if (stack > 0)
            getSupportFragmentManager().popBackStack();
        else
        {
            new AlertDialog.Builder(MainActivity.this)
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
                        }
                    }).create().show();
        }

    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PERMISSION_REQUEST_INTERNET && resultCode == RESULT_OK)
        {
            //getCurrentLocation(null);
            createLocationRequest();
        }
        else if( requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK)
            createLocationRequest();
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void barcodeReceived(Barcode barcode)
    {
        showProgress(true);
        try
        {
            String[] loc = myLocation.split(":");
            if(useQrCode)
                new RestaurantQRTask(String.valueOf(loc[0]),String.valueOf(loc[1]),"null",barcode.displayValue).execute((Void)null);

        }
        catch (Exception e)
        {
            Toast.makeText(getBaseContext(),"Location unavailable",Toast.LENGTH_SHORT);
        }
    }

    protected void createLocationRequest()
    {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>()
        {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse)
            {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_INTERNET);
                    return;
                }
                else
                {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                            locationCallback,
                            Looper.getMainLooper());
                }
            }
        });

        task.addOnFailureListener(this, new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }
    private boolean checkIfLocationEnabled()
    {
        LocationManager lm = (LocationManager)getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        //the device may use gps which has high accuracy or network which has low accuracy
        //if gps is enabled its well and fine that means we can get the accurate location
        //if gps is not available but internet is we can use coarse location which is better than the user not been able to access the loaction at all
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage("Location is not enabled")
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton("Cancel",null)
                            .show();
        }
        if(!network_enabled)
        {
            new AlertDialog.Builder(this)
                    .setMessage("Internet is not enabled")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            ;//startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .show();
        }
        if(!isNetworkAvailable())
        {
            new AlertDialog.Builder(this)
                    .setMessage("No internet")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            ;//startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .show();
            network_enabled = false;
        }
        return gps_enabled || network_enabled;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void checkFirebaseToken()
    {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        if(!LoginA.getServerAccount().getmFirebaseTokenId().contentEquals(token))
                            new MyFirebaseMessagingService.UpdateTokenTask(token).execute((Void)null);
                    }
                });
    }
    private void  saveCurrentLocation(Location location)
    {
        //Get last known location. In some rare situations this can be null
        if(location!=null)
        {
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();

            myLocation = String.valueOf(latitude) + ":" + String.valueOf(longitude) + ":" + "null";
            Thread thread_location = new Thread()
            {
                @Override
                public void run()
                {
                    //get addresses
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses;
                    try
                    {
                        addresses = geocoder.getFromLocation(latitude, longitude, 10);
                        myLocation = String.valueOf(latitude) + ":" + String.valueOf(longitude) + ":" + addresses.get(0).getCountryCode();
                    } catch (IOException e)
                    {
                        Log.e(TAG, "" + e.getMessage());
                    }
                }
            };
            thread_location.start();
        }
    }
    private void showScannedRestaurants(Restaurants item)
    {

        //if the location of the hotel is kenya then we ask for mpesa payment
        boolean has_payment = false;
            /*if( item.getmCode().contentEquals("") ||  item.getmCode().contentEquals("null") || item.getmCode().contentEquals("NULL"))
                has_payment = false;*/
        if(item.getCountryCode().contentEquals("KE"))
            has_payment = true;
        Intent intent=new Intent(this, ShopA.class);
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
        startActivity(intent);


    }



    void showProgress(boolean show)
    {
        progressBar.setVisibility(show? View.VISIBLE : View.GONE);
        mainFragment.setVisibility( show? View.INVISIBLE :View.VISIBLE);
    }

    /*
     * implementation of OrderSearchFragment.java
     * */
    @Override
    public void onFindRestaurantMenuClicked(int id)
    {
        if(checkIfLocationEnabled())
        {
            if(myLocation.contentEquals(""))
                Toast.makeText(getBaseContext(),"Please wait",Toast.LENGTH_SHORT).show();
            else
            {
                if(id==1)
                {
                    //scan the QR code to access the restaurant
                    //scan the item and remove it from the list of unscanned items
                    Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    mGetBarcode.launch(intent);
                    useQrCode = true;
                }
                else if( id == 2)
                {
                    Intent intent = new Intent(this, ExploreActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);

                }
            }
        }


    }


/*
implementation of OrdersFragment.java
 */
    @Override
    public void onOrderClicked(List<Orders> ordersList)
    {
        Intent intent = new Intent(this, OrdersActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("orders", (Serializable) ordersList);
        startActivity(intent);
    }


    private void initEmojiCompat() {
        final EmojiCompat.Config config;
        if (USE_BUNDLED_EMOJI) {
            // Use the bundled font for EmojiCompat
            config = new BundledEmojiCompatConfig(getApplicationContext());
        }
        else
            {
            // Use a downloadable font for EmojiCompat
            final FontRequest fontRequest = new FontRequest(
                    "com.google.android.gms.fonts",
                    "com.google.android.gms",
                    "Noto Color Emoji Compat",
                    R.array.com_google_android_gms_fonts_certs);
            config = new FontRequestEmojiCompatConfig(getApplicationContext(), fontRequest);
        }

        config.setReplaceAll(true)
                .registerInitCallback(new EmojiCompat.InitCallback() {
                    @Override
                    public void onInitialized() {
                        Log.d(TAG, "EmojiCompat initialized");
                    }

                    @Override
                    public void onFailed(@Nullable Throwable throwable) {
                        Log.e(TAG, "EmojiCompat initialization failed", throwable);
                    }
                });

        EmojiCompat.init(config);
    }

    private class RestaurantQRTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_restaurants_qr=base_url+"get_restaurant_from_qr_code_1.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        //final private String country;
        final private String latitude;
        final private String longitude;
        final private String location;
        final private String barcode;
        private  int success;
        private Restaurants restaurant;


        public RestaurantQRTask( String latitude, String longitude, String location, String barcode)
        {
            this.latitude=latitude;
            this.longitude=longitude;
            this.location=location;
            this.barcode = barcode;
            jsonParser=new JSONParser();
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
            info.add(new BasicNameValuePair("url_code",barcode));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_restaurants_qr,"POST",info);
            Log.d(TAG,"json:"+jsonObject.toString());
            try
            {
                success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    JSONArray accountArray=jsonObject.getJSONArray("restaurants");
                    JSONObject jsonObject_restaurants=accountArray.getJSONObject(0);

                    int id=jsonObject_restaurants.getInt("id");
                    String email = jsonObject_restaurants.getString("email");
                    String names=jsonObject_restaurants.getString("username");
                    double distance=jsonObject_restaurants.getDouble("distance");
                    double latitude=jsonObject_restaurants.getDouble("latitude");
                    double longitude=jsonObject_restaurants.getDouble("longitude");
                    String locality=jsonObject_restaurants.getString("locality");
                    String country_code = jsonObject_restaurants.getString("country_code");
                    int order_radius=jsonObject_restaurants.getInt("order_radius");
                    int tables = jsonObject_restaurants.getInt("number_of_tables");
                    String image_type=jsonObject_restaurants.getString("image_type");
                    int table_number = jsonObject_restaurants.getInt("table_number");
                    String m_code = jsonObject_restaurants.getString("m_code");
                    String dining_options = jsonObject_restaurants.getString("dining_options");
                    String opening_time = jsonObject_restaurants.getString("opening_time");
                    String closing_time = jsonObject_restaurants.getString("closing_time");
                    boolean opened = jsonObject_restaurants.getBoolean("opened");
                    int delivery_radius = jsonObject_restaurants.getInt("delivery_radius");

                    restaurant =new Restaurants(id, email, names,distance,latitude,longitude,locality,country_code, order_radius,
                            tables, image_type, table_number, m_code, dining_options, opening_time, closing_time, opened, delivery_radius);
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

            showProgress(false);
            useQrCode = false;
            if (successful)
            {
                showScannedRestaurants( restaurant);

            }
            else
            {
                String message;
                if(barcode.contentEquals("https://play.google.com/store/apps/details?id=com.spikingacacia.spikyletabuyer"))
                    message = "Wrong QR code\nScan the bottom QR code";
                else if(success ==-3)
                    message = "You are too far away from the restaurant";
                else
                    message = "Error getting restaurants";
                Toast.makeText(getBaseContext(),message,Toast.LENGTH_SHORT).show();
            }
        }
    }

}
