package com.spikingacacia.spikyletabuyer.main;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.provider.FontRequest;
import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.database.MpesaRequests;
import com.spikingacacia.spikyletabuyer.database.ServerAccount;
import com.spikingacacia.spikyletabuyer.database.TastyBoard;
import com.spikingacacia.spikyletabuyer.explore.ExploreActivity;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.SettingsActivity;
import com.spikingacacia.spikyletabuyer.barcode.BarcodeCaptureActivity;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.main.order.OrderSearchFragment;
import com.spikingacacia.spikyletabuyer.main.orders_list.OrdersFragment;
import com.spikingacacia.spikyletabuyer.main.tasty.TastyBoardActivity;
import com.spikingacacia.spikyletabuyer.main.tasty.TastyBoardFragment;
import com.spikingacacia.spikyletabuyer.orders.OrdersActivity;
import com.spikingacacia.spikyletabuyer.restaurants.SRRestaurantsA;
import com.spikingacacia.spikyletabuyer.shop.ShopA;
import com.spikingacacia.spikyletabuyer.util.Mpesa;
import com.spikingacacia.spikyletabuyer.util.MyFirebaseMessagingService;
import com.spikingacacia.spikyletabuyer.wallet.WalletActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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
         OrdersFragment.OnListFragmentInteractionListener, TastyBoardFragment.OnListFragmentInteractionListener
{
    /** Change this to {@code false} when you want to use the downloadable Emoji font. */
    private static final boolean USE_BUNDLED_EMOJI = true;
    private static final int PERMISSION_REQUEST_INTERNET=2;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ProgressBar progressBar;
    private View mainFragment;
    private String TAG="MainA";
    public static  List<Restaurants> restaurantsList;
    private static boolean useQrCode = false;
    private List<MpesaRequests> mpesaRequestsList;
    private boolean mpesaOrderUpdateInprogress;
    //private Thread thread;
    ActivityResultLauncher<Intent> mGetBarcode = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                    Intent intent = result.getData();
                    try
                    {
                        Barcode barcode = intent.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        barcodeReceived(barcode);
                    }
                    catch (NullPointerException excpetion)
                    {
                        Log.e(TAG,"no barcode");
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
        initEmojiCompat();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                 R.id.navigation_order,  R.id.navigation_tasty_board, R.id.navigation_orders, R.id.navigation_messages)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener()
        {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments)
            {
                // if(destination.getId() != R.id.navigation_home)
                //appBarLayout.setExpanded(false, true);
            }
        });
        progressBar = findViewById(R.id.progress);
        mainFragment = findViewById(R.id.nav_host_fragment);

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        start_background_tasks();
        checkIfLocationEnabled();
        getCurrentLocation();
        restaurantsList =new LinkedList<>();
        mpesaRequestsList = new ArrayList<>();
        checkFirebaseToken();

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
        outState.putSerializable(LoginA.SAVE_INSTANCE_SERVER_ACCOUNT,LoginA.getServerAccount());
        //Log.d(TAG,"main_a is been destroyed threfore we call onsaved instance");
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if( id == R.id.action_wallet)
        {
            Intent intent=new Intent(MainActivity.this, WalletActivity.class);
            //prevent this activity from flickering as we call the next one
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

        if (id == R.id.action_settings)
        {
            proceedToSettings();
            return true;
        }
        else if( id == R.id.action_sign_out)
        {
            mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    Log.d(TAG,"gmail signed out");
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
        if ( stack> 0)
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
        if (requestCode == PERMISSION_REQUEST_INTERNET && resultCode == RESULT_OK )
        {
            getCurrentLocation(null);
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    void barcodeReceived(Barcode barcode)
    {
        showProgress(true);
        getCurrentLocation(barcode);
    }
    private boolean checkIfLocationEnabled()
    {
        LocationManager lm = (LocationManager)getBaseContext().getSystemService(Context.LOCATION_SERVICE);
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
        return gps_enabled && network_enabled;
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
    private void getCurrentLocation(final Barcode barcode)
    {
        checkIfLocationEnabled();
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
                                //myLocation = String.valueOf(latitude)+":"+String.valueOf(longitude)+":"+"null";
                                if(useQrCode)
                                    new RestaurantQRTask(String.valueOf(latitude),String.valueOf(longitude),"null",barcode.displayValue).execute((Void)null);
                                else
                                    new RestaurantsTask(String.valueOf(latitude),String.valueOf(longitude),"null").execute((Void)null);
                                //get addresses
                               /* Geocoder geocoder=new Geocoder(MainActivity.this, Locale.getDefault());
                                List<Address> addresses;
                                try
                                {
                                    addresses=geocoder.getFromLocation(latitude,longitude,10);
                                    Log.d("LOCATIONS: ", "lat: "+latitude+" long: "+longitude);
                                    myLocation = String.valueOf(latitude)+":"+String.valueOf(longitude)+":"+addresses.get(0).getCountryCode();
                                    if(useQrCode)
                                        new RestaurantQRTask(String.valueOf(latitude),String.valueOf(longitude),addresses.get(0).getLocality(),barcode.displayValue).execute((Void)null);
                                    else
                                        new RestaurantsTask(String.valueOf(latitude),String.valueOf(longitude),addresses.get(0).getCountryCode()).execute((Void)null);
                                    for(int c=0; c<addresses.size(); c+=1)
                                        Log.d("loc: ",addresses.get(c).getLocality()+"\n");
                                }
                                catch (IOException e)
                                {
                                    showProgress(false);
                                    Snackbar.make(getWindow().getDecorView().getRootView(),"Error getting your location.\nPlease try again.", Snackbar.LENGTH_SHORT).show();
                                    Log.e("address",""+e.getMessage());
                                }*/
                            }

                        }
                    }).addOnFailureListener(this, new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    showProgress(false);
                    Log.e(TAG,"location failed");
                }
            });
        }
        //request the permission
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_INTERNET);
        }

    }
    private void getCurrentLocation()
    {
        checkIfLocationEnabled();
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
                                final double latitude=location.getLatitude();
                                final double longitude=location.getLongitude();

                                myLocation = String.valueOf(latitude)+":"+String.valueOf(longitude)+":"+"null";
                                Thread thread_location=new Thread()
                                {
                                    @Override
                                    public void run()
                                    {
                                        //get addresses
                                        Geocoder geocoder=new Geocoder(MainActivity.this, Locale.getDefault());
                                        List<Address> addresses;
                                        try
                                        {
                                            addresses=geocoder.getFromLocation(latitude,longitude,10);
                                            myLocation = String.valueOf(latitude)+":"+String.valueOf(longitude)+":"+addresses.get(0).getCountryCode();
                                            new UpdateLastKnownLocationTask().execute((Void)null);
                                            //if(addresses.get(0).getCountryCode().contentEquals("KE"))
                                                //thread.start();
                                        }
                                        catch (IOException e)
                                        {
                                            runOnUiThread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    showProgress(false);
                                                    //Snackbar.make(getWindow().getDecorView().getRootView(),"Error getting your location.\nPlease try again.", Snackbar.LENGTH_SHORT).show();
                                                }
                                            });

                                            Log.e(TAG,""+e.getMessage());
                                        }
                                    }
                                };
                                thread_location.start();

                            }

                        }
                    }).addOnFailureListener(this, new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    showProgress(false);
                    Log.e(TAG,"location failed");
                }
            });
        }
        //request the permission
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_INTERNET);
        }

    }
    private void showRestaurants(boolean scan ,Restaurants item)
    {
        Log.d(TAG," GOT THE RESTRAUNTS");

        if(restaurantsList.size()==0)
        {
            Log.d(TAG," GOT THE RESTRAUNTS 1");

            Snackbar.make(getWindow().getDecorView().getRootView(), "No restaurants near you.", Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            getCurrentLocation(null);
                        }
                    });
            return;
        }
        if(scan)
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
            startActivity(intent);
        }
        else
        {
            Intent intent=new Intent(this, SRRestaurantsA.class);
            //prevent this activity from flickering as we call the next one
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }


    }



    void showProgress(boolean show)
    {
        progressBar.setVisibility(show? View.VISIBLE : View.GONE);
        mainFragment.setVisibility( show? View.INVISIBLE :View.VISIBLE);
    }
    private void start_background_tasks()
    {
    }
    void proceedToSettings()
    {
        Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
        //prevent this activity from flickering as we call the next one
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
    /*
     * implementation of OrderSearchFragment.java
     * */
    @Override
    public void onFindRestaurantMenuClicked(int id)
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
        else if(id==2)
        {
            //get the users location
            showProgress(true);
            getCurrentLocation(null);
        }
        else if( id == 3)
        {
            //Intent intent = new Intent(this, MapsExploreActivity.class);
            if(checkIfLocationEnabled())
            {
                Intent intent = new Intent(this, ExploreActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }

        }
    }


/*
implementation of OrdersFragment.java
 */
    @Override
    public void onOrderClicked(Orders item)
    {
        String date_added=item.getDateAdded();
        String[] date_pieces=date_added.split(" ");
        String unique_name=date_pieces[0]+":"+item.getOrderNumber();
        Intent intent = new Intent(this, OrdersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("unique_order_name",unique_name);
        intent.putExtra("order_format",item.getOrderFormat());
        intent.putExtra("order_status",item.getOrderStatus());
        intent.putExtra("pre_order", item.getPreOrder());
        intent.putExtra("seller_names",item.getSellerNames());
        startActivity(intent);
    }

    /*
     * implementation of TastyBoardFragment.java
     */
    @Override
    public void onTastyBoardItemClicked(TastyBoard tastyBoard)
    {
        //if the location of the hotel is kenya then we ask for mpesa payment
        boolean has_payment = false;
            /*if( item.getmCode().contentEquals("") ||  item.getmCode().contentEquals("null") || item.getmCode().contentEquals("NULL"))
                has_payment = false;*/
        if(tastyBoard.getCountry().contentEquals("KE"))
            has_payment = true;
        Intent intent=new Intent(MainActivity.this, TastyBoardActivity.class);
        //prevent this activity from flickering as we call the next one
        intent.putExtra("tasty_board",tastyBoard);
        intent.putExtra("has_payment", has_payment);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
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

    private class RestaurantsTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_restaurants=base_url+"get_near_restaurants.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        //final private String country;
        final private String latitude;
        final private String longitude;
        final private String location;
        private JSONParser jsonParser;

        public RestaurantsTask( String latitude, String longitude, String location)
        {
            //this.country=country;
            this.latitude=latitude;
            this.longitude=longitude;
            this.location=location;
            jsonParser=new JSONParser();
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
            Log.d(TAG,info.toString());
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

                        Restaurants restaurants =new Restaurants(id, email, names,distance,latitude,longitude,locality,country_code,order_radius, tables, image_type, table_number, m_code, dining_options);
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
            showProgress(false);
            if (successful)
            {
                Collections.sort(restaurantsList, new Comparator<Restaurants>(){
                    public int compare(Restaurants obj1, Restaurants obj2) {
                        // ## Ascending order
                        //return obj1.firstName.compareToIgnoreCase(obj2.firstName); // To compare string values
                        // return Integer.valueOf(obj1.empId).compareTo(Integer.valueOf(obj2.empId)); // To compare integer values
                        return Double.compare(obj1.getDistance(), obj2.getDistance());

                        // ## Descending order
                        // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                        // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
                    }
                });
                showRestaurants(false, null);
            }
            else
            {
                Toast.makeText(getBaseContext(),"Error getting restaurants",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class RestaurantQRTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_restaurants_qr=base_url+"get_restaurant_from_qr_code.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        //final private String country;
        final private String latitude;
        final private String longitude;
        final private String location;
        final private String barcode;
        private  int success;


        public RestaurantQRTask( String latitude, String longitude, String location, String barcode)
        {
            this.latitude=latitude;
            this.longitude=longitude;
            this.location=location;
            this.barcode = barcode;
            jsonParser=new JSONParser();
            if(!restaurantsList.isEmpty())
                restaurantsList.clear();
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

                    Restaurants restaurants =new Restaurants(id, email, names,distance,latitude,longitude,locality,country_code, order_radius, tables, image_type, table_number, m_code, dining_options);
                    restaurantsList.add(restaurants);
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
                showRestaurants(true, restaurantsList.get(0));
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

    private class UpdateLastKnownLocationTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update_item = base_url+"update_buyer_last_location.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        private int success;
        UpdateLastKnownLocationTask()
        {
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //building parameters
            List<NameValuePair> info=new ArrayList<NameValuePair>();
            info.add(new BasicNameValuePair("email", LoginA.getServerAccount().getEmail()));
            info.add(new BasicNameValuePair("location",myLocation));
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_update_item,"POST",info);
            try
            {
                success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
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
            if(successful)
            {


            }
            else
            {
                Log.e(TAG, "updating last known location");
            }

        }
    }
}
