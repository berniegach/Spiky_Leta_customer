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
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.BMenuA;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.SettingsActivity;
import com.spikingacacia.spikyletabuyer.barcode.BarcodeCaptureActivity;
import com.spikingacacia.spikyletabuyer.database.Adverts;
import com.spikingacacia.spikyletabuyer.database.BRestaurants;
import com.spikingacacia.spikyletabuyer.main.board.AdvertsFragment;
import com.spikingacacia.spikyletabuyer.main.order.OrderFragment;
import com.spikingacacia.spikyletabuyer.restaurants.SRRestaurantsA;
import com.spikingacacia.spikyletabuyer.shop.ShopA;
import com.spikingacacia.spikyletabuyer.util.Mpesa;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
        OrderFragment.OnFragmentInteractionListener, AdvertsFragment.OnListFragmentInteractionListener
{
    private static final int PERMISSION_REQUEST_INTERNET=2;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ProgressBar progressBar;
    private View mainFragment;
    private String TAG="MainA";
    public static  List<BRestaurants>bRestaurantsList;
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
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_order)
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
                        //new MpesaGetPayStatus().execute((Void)null);
                        handler.post(runnable);
                    }
                }
                catch (InterruptedException e)
                {
                    Log.e(TAG,"error sleeping "+e.getMessage());
                }
            }
        };
        //thread.start();
        bRestaurantsList=new LinkedList<>();
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
    void barcodeReceived(Barcode barcode)
    {
        new RestaurantQRTask(barcode.displayValue).execute((Void)null);
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
                                    new RestaurantsTask(String.valueOf(latitude),String.valueOf(longitude),addresses.get(0).getLocality()).execute((Void)null);
                                    for(int c=0; c<addresses.size(); c+=1)
                                        Log.d("loc: ",addresses.get(c).getLocality()+"\n");
                                }
                                catch (IOException e)
                                {
                                    showProgress(false);
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
    private void showRestaurants()
    {
        if(bRestaurantsList.size()==0)
        {
            Snackbar.make(getWindow().getDecorView().getRootView(), "No restaurants near you.", Snackbar.LENGTH_LONG)
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
        Intent intent=new Intent(this, SRRestaurantsA.class);
        //prevent this activity from flickering as we call the next one
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void run_shop(String email, int radius, int distance, int numberOfTables)
    {
        Intent intent=new Intent(this, ShopA.class);
        intent.putExtra("seller_email",email);
        intent.putExtra("order_radius",radius);
        intent.putExtra("buyer_distance",distance);
        intent.putExtra("number_of_tables",numberOfTables);
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
    /*
     * implementation of OrderFragment.java
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
        }
        else if(id==2)
        {
            //get the users location
            showProgress(true);
            getCurrentLocation();
        }
    }

    @Override
    public void onAdClicked(Adverts item)
    {

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
                        String email = jsonObject_restaurants.getString("email");
                        String names=jsonObject_restaurants.getString("username");
                        double distance=jsonObject_restaurants.getDouble("distance");
                        double latitude=jsonObject_restaurants.getDouble("latitude");
                        double longitude=jsonObject_restaurants.getDouble("longitude");
                        String locality=jsonObject_restaurants.getString("locality");
                        int order_radius=jsonObject_restaurants.getInt("order_radius");
                        int tables = jsonObject_restaurants.getInt("number_of_tables");
                        String image_type=jsonObject_restaurants.getString("image_type");

                        BRestaurants bRestaurants=new BRestaurants(id, email, names,distance,latitude,longitude,locality,order_radius, tables, image_type);
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
            showProgress(false);
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
    private class RestaurantQRTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_restaurants_qr=base_url+"get_restaurant_from_qr_code.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        //final private String country;
        final private String email;
        String seller_email;
        String username;
        int online;
        int deliver;
        String country;
        String location;
        int order_radius;
        int order_format;
        int number_of_tables;

        public RestaurantQRTask( String email)
        {
            this.email=email;
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            //info.add(new BasicNameValuePair("country",country));
            info.add(new BasicNameValuePair("email",email));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_restaurants_qr,"POST",info);
            Log.d("cTasks",""+jsonObject.toString());
            try
            {
                JSONArray restArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    //seccesful
                    JSONArray accountArray=jsonObject.getJSONArray("account");
                    JSONObject accountObject=accountArray.getJSONObject(0);

                    seller_email = accountObject.getString("seller_email");
                    username = accountObject.getString("username");
                    online = accountObject.getInt("online");
                    deliver = accountObject.getInt("deliver");
                    country = accountObject.getString("country");
                    location = accountObject.getString("location");
                    order_radius = accountObject.getInt("order_radius");
                    order_format = accountObject.getInt("order_format");
                    number_of_tables = accountObject.getInt("number_of_tables");
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
                run_shop(seller_email,order_radius,5,number_of_tables);
            }
            else
            {
                Toast.makeText(getBaseContext(),"Error getting restaurant",Toast.LENGTH_SHORT).show();
            }
        }
    }
   /* private class UpdateOrderTask extends AsyncTask<Void, Void, Boolean>
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
    }*/
}
