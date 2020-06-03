package com.spikingacacia.spikyletabuyer.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import static com.spikingacacia.spikyletabuyer.LoginA.mGoogleSignInClient;


public class MainActivity extends AppCompatActivity implements
        FindStoreFragment.OnListFragmentInteractionListener,
        ReceiptsFragment.OnListFragmentInteractionListener
{
    private static final int PERMISSION_REQUEST_INTERNET=2;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public static List<Store> storesList;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    public static LinkedHashMap<Integer, ShoppingList> shoppingListLinkedHashMap;
    private ProgressBar progressBar;
    private View mainFragment;
    public static List<Orders> ordersList;
    public static LinkedHashMap<String,Orders> uniqueOrderLinkedHashMap;
    private List<MpesaRequests> mpesaRequestsList;
    private String TAG="MainA";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home)
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
        storesList = new ArrayList<>();
        mpesaRequestsList = new ArrayList<>();
        start_background_tasks();
        final Handler handler=new Handler();
        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                //if something changed so something
            }
        };
        Thread thread=new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    while(true)
                    {
                        sleep(60000);
                        new MpesaGetPayStatus().execute((Void)null);
                        handler.post(runnable);
                    }
                }
                catch (InterruptedException e)
                {
                    Log.e(TAG,"error sleeping "+e.getMessage());
                }
            }
        };
        thread.start();
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

        //noinspection SimplifiableIfStatement
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
            getCurrentLocation();
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
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
                                Geocoder geocoder=new Geocoder(MainActivity.this, Locale.getDefault());
                                List<Address> addresses;
                                try
                                {
                                    addresses=geocoder.getFromLocation(latitude,longitude,10);
                                    Log.d("LOCATIONS: ", "lat: "+latitude+" long: "+longitude);
                                    new NearShopsTask(String.valueOf(latitude),String.valueOf(longitude),addresses.get(0).getLocality()).execute((Void)null);
                                    for(int c=0; c<addresses.size(); c+=1)
                                        Log.d("loc: ",addresses.get(c).getLocality()+"\n");
                                }
                                catch (IOException e)
                                {
                                    Snackbar.make(getWindow().getDecorView().getRootView(),"Error getting your location.\nPlease try again.", Snackbar.LENGTH_SHORT).show();
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
    private void showStores()
    {
        if(storesList.size()==0)
        {
            Snackbar.make(getWindow().getDecorView().getRootView(), "No Stores near you", Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            getCurrentLocation();
                        }
                    });
            return;
        }
        Intent intent=new Intent(MainActivity.this, NearStoresActivity.class);
        //prevent this activity from flickering as we call the next one
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
/*
* implementation of FindStoreFragment.java
* */
    @Override
    public void onUseMyLocationClicked()
    {
        //get the users location
        //show progressbar
        showProgress(true);
        getCurrentLocation();
    }

    @Override
    public void onEnterLocationClicked()
    {

    }

    /*
     * implementation of ReceiptsFragment.java
     * */
    @Override
    public void onListFragmentInteraction(Orders order, String totalPrice)
    {
        //find the orders that have the same date, order number and order status
        String[] date_pieces=order.getDate_added().split(" ");
        String unique_name=date_pieces[0]+":"+order.getOrder_number()+":"+order.getOrder_status();
        Intent intent = new Intent(MainActivity.this, ReceiptActivity.class);
        intent.putExtra("unique_name",unique_name);
        intent.putExtra("total_price",totalPrice);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
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

    private class NearShopsTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_stores=base_url+"find_near_stores.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        final private String latitude;
        final private String longitude;
        final private String location;

        public NearShopsTask(String latitude, String longitude, String location)
        {
            Log.d("nearshops","getting longitude: "+longitude+" latitude: "+latitude);
            //this.country=country;
            this.latitude=latitude;
            this.longitude=longitude;
            this.location=location;
            jsonParser = new JSONParser();
            storesList.clear();
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
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_stores,"POST",info);
            //Log.d("JSONLOCATION", jsonObject.toString());
            try
            {
                JSONArray restArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    restArrayList=jsonObject.getJSONArray("stores");
                    restArrayList=restArrayList.getJSONArray(0);
                    if(restArrayList.length()==0)
                        return false;
                    for(int count=0; count<restArrayList.length(); count+=1)
                    {
                        JSONObject jsonObject_restaurants=restArrayList.getJSONObject(count);
                        int id=jsonObject_restaurants.getInt("id");
                        String email=jsonObject_restaurants.getString("email");
                        String names=jsonObject_restaurants.getString("names");
                        String image_type=jsonObject_restaurants.getString("image_type");
                        double distance=jsonObject_restaurants.getDouble("distance");
                        double latitude=jsonObject_restaurants.getDouble("latitude");
                        double longitude=jsonObject_restaurants.getDouble("longitude");
                        String locality=jsonObject_restaurants.getString("locality");
                        String country_code=jsonObject_restaurants.getString("country_code");
                        String m_code=jsonObject_restaurants.getString("m_code");

                        Store store = new Store(id,email,names,image_type,distance,latitude,longitude,locality,country_code, m_code);
                        storesList.add(store);
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
                showStores();
            }
            else
            {
                Toast.makeText(getBaseContext(),"Error getting stores",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateOrderTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update=base_url+"update_order.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        final private String orderNumber;
        private final String dateAdded;
        private final String orderStatus;
        private final String storeEmail;

        public  UpdateOrderTask(String storeEmail, String orderNumber, String dateAdded, String orderStatus)
        {
            this.storeEmail = storeEmail;
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            this.orderStatus = orderStatus; // order status for unpaid order is -1, delete is 0 and for a succesful order is 1
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("store_email",storeEmail));
            info.add(new BasicNameValuePair("user_email",account.getEmail()));
            info.add(new BasicNameValuePair("assistant_email", "null"));
            info.add(new BasicNameValuePair("order_number",orderNumber));
            info.add(new BasicNameValuePair("status",orderStatus));
            info.add(new BasicNameValuePair("date_added",dateAdded));

            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_update,"POST",info);
            try
            {
                int success=jsonObject.getInt(TAG_SUCCESS);
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
            if (successful )
            {
            }
            else
            {
                //Snackbar.make(fab,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
    }
    private class MpesaCheckPayStatus extends AsyncTask<Void, Void, Boolean>
    {
        String businessShortCode;
        String password;
        String timestamp;
        String checkoutRequestID;
        private String orderNumber;
        private String dateAdded;
        private String storeEmail;
        private boolean orderPaymentCancelled;
        public MpesaCheckPayStatus(String storeEmail,String businessShortCode, String password, String timestamp, String checkoutRequestID, String orderNumber, String dateAdded)
        {
            this.businessShortCode = businessShortCode;
            this.password = password;
            this.timestamp = timestamp;
            this.checkoutRequestID = checkoutRequestID;
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            this.storeEmail = storeEmail;
        }

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            try
            {
                JSONObject jsonObject = Mpesa.STKPushTransactionStatus(businessShortCode,password,timestamp,checkoutRequestID);

                Log.d(TAG,"JSON"+jsonObject.toString());
                String  ResponseCode = jsonObject.getString("ResponseCode");
                if(ResponseCode.contentEquals("0"))
                {
                    String ResponseDescription = jsonObject.getString("ResponseDescription");
                    String MerchantRequestID = jsonObject.getString("MerchantRequestID");
                    String ResultCode = jsonObject.getString("ResultCode");
                    String ResultDesc = jsonObject.getString("ResultDesc");
                    if(ResultCode.contentEquals("1032"))
                        return false;// the result was cancelled by the user
                    else if(ResultCode.contentEquals("1037"))
                        return false;// DS timeout
                    else if(ResultCode.contentEquals("0"))
                        return true; // paid

                    return false;
                }
            }
            catch (IOException | JSONException e)
            {
                Log.e(TAG," "+e.getMessage());
                e.printStackTrace();
                return false;
            }

            return false;
        }
        @Override
        protected void onPostExecute(final Boolean successful)
        {

            if (successful)
            {
                //check the status
                new UpdateOrderTask(storeEmail,orderNumber, dateAdded,"-1").execute((Void)null);
            }

        }
    }
    private class MpesaGetPayStatus extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_status=base_url+"get_m_requests.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        public MpesaGetPayStatus()
        {
            jsonParser = new JSONParser();
        }

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("user_email",account.getEmail()));

            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_status,"POST",info);
            try
            {
                int success=jsonObject.getInt(TAG_SUCCESS);
                JSONArray jsonArray=null;
                if(success==1)
                {
                    jsonArray = jsonObject.getJSONArray("items");
                    //Log.d(TAG,jsonArray.toString());
                    for (int count = 0; count < jsonArray.length(); count += 1)
                    {
                        JSONObject jsonObjectItems = jsonArray.getJSONObject(count);
                        int id = jsonObjectItems.getInt("id");
                        String store_email = jsonObjectItems.getString("store_email");
                        String order_number = jsonObjectItems.getString("order_number");
                        String date_added = jsonObjectItems.getString("date_added");
                        String business_shortcode = jsonObjectItems.getString("business_shortcode");
                        String password = jsonObjectItems.getString("password");
                        String timestamp = jsonObjectItems.getString("timestamp");
                        String chequeout_request_id = jsonObjectItems.getString("chequeout_request_id");

                        MpesaRequests mpesaRequests = new MpesaRequests(id,store_email,order_number,date_added,business_shortcode,password,timestamp,chequeout_request_id);
                        mpesaRequestsList.add(mpesaRequests);
                    }
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    //Log.e(TAG_MESSAGE,""+message);
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
                //we have the mpesa ststaus now
                //proceed to check the mpesa status
                for(int c=0; c<mpesaRequestsList.size(); c++)
                {
                    MpesaRequests mpesaRequests = mpesaRequestsList.get(c);
                    new MpesaCheckPayStatus(mpesaRequests.getStore_email(),mpesaRequests.getBusiness_shortcode(),mpesaRequests.getPassword(),mpesaRequests.getTimestamp(),
                            mpesaRequests.getChequeout_request_id(), mpesaRequests.getOrder_number(), mpesaRequests.getDate_added()).execute((Void)null);
                }

            }

        }
    }
}
