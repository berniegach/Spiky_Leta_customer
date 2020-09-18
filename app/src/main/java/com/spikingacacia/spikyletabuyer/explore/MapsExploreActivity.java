package com.spikingacacia.spikyletabuyer.explore;

import android.Manifest;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class MapsExploreActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
            ActivityCompat.OnRequestPermissionsResultCallback
{
    private static final int PERMISSION_REQUEST_INTERNET=2;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private LatLng latMyPos;
    private FusedLocationProviderClient mFusedLocationClient ;
    private float maxZoomLevel;
    private int who;
    private String location;
    private String TAG="explore_a";
    private Marker myMarker;
    private View mapView;
    private String url_get_restaurants=base_url+"get_near_restaurants.php";
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private JSONParser jsonParser;
    public static  List<Restaurants> restaurantsList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_explore);
        setTitle("Explore");

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        //get intent
        Intent intent=getIntent();
        who=intent.getIntExtra("who",0);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
        mapView=mapFragment.getView();
        jsonParser=new JSONParser();
        restaurantsList =new LinkedList<>();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        maxZoomLevel=googleMap.getMaxZoomLevel();
        if(maxZoomLevel>=15)
            maxZoomLevel=15;
        //Log.d("loc",String.format("min: %f, max: %f",googleMap.getMinZoomLevel(),googleMap.getMaxZoomLevel()));
        ////
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        enableMyLocation();
        //set the mylocation button position
        if(mapView!=null && mapView.findViewById(Integer.parseInt("1"))!=null)
        {
            //get the button view
            View locationButton=((View)mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

            //place it on bottom right
            RelativeLayout.LayoutParams layoutParams=(RelativeLayout.LayoutParams)locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,0);
            layoutParams.setMargins(50,100,0,0);
        }
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
        {
            @Override
            public void onInfoWindowClick(Marker marker)
            {
                final double latitude=  marker.getPosition().latitude;
                final double longitude= marker.getPosition().longitude;
                final String title = marker.getTitle();
                for(int c=0; c<restaurantsList.size(); c++)
                {
                    Restaurants restaurants = MapsExploreActivity.restaurantsList.get(c);
                    if(latitude==restaurants.getLatitude() && longitude == restaurants.getLongitude() && title.contentEquals(restaurants.getNames()))
                        gotoRestaurant(restaurants);

                }
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.explore_menu, menu);

        // Get the SearchView and set the searchable configuration
        /*SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getBaseContext(),SearchableActivity.class)));*/

        //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
       /* searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {

                startActivity(new Intent(MapsExploreActivity.this, SearchableActivity.class)
                        .putExtra(SearchManager.QUERY, query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                return false;
            }
        });*/
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.search_m:
                //start search dialog
                super.onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_search)
        {
            super.onSearchRequested();
            return true;
        }*/



        //return super.onOptionsItemSelected(item);
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
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(new LatLng(latitude, longitude))      // Sets the center of the map to location user
                                        .zoom(17)                   // Sets the zoom
                                        //.bearing(90)                // Sets the orientation of the camera to east
                                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                        .build();                   // Creates a CameraPosition from the builder
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                Thread thread=new Thread()
                                {
                                    @Override
                                    public void run()
                                    {
                                        //get addresses
                                        Geocoder geocoder=new Geocoder(MapsExploreActivity.this, Locale.getDefault());
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
                                thread.start();

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




    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)       != PackageManager.PERMISSION_GRANTED)
        {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_INTERNET);
        }
        else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Using my Location", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location)
    {
        //
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
        {
            return;
        }

        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else
        {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
    /**
     * Checks if the result contains a {@link PackageManager#PERMISSION_GRANTED} result for a
     * permission from a runtime permissions request.
     *
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }
    /**
     * A dialog that displays a permission denied message.
     */
    public static class PermissionDeniedDialog extends DialogFragment
    {

        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

        private boolean mFinishActivity = false;

        /**
         * Creates a new instance of this dialog and optionally finishes the calling Activity
         * when the 'Ok' button is clicked.
         */
        public static PermissionDeniedDialog newInstance(boolean finishActivity) {
            Bundle arguments = new Bundle();
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);

            PermissionDeniedDialog dialog = new PermissionDeniedDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mFinishActivity = getArguments().getBoolean(ARGUMENT_FINISH_ACTIVITY);

            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.location_permission_denied)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (mFinishActivity) {
                Toast.makeText(getActivity(), R.string.permission_required_toast,
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
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
        startActivity(intent);
    }
    private void showRestaurants()
    {
        //add markers
        Iterator<Restaurants> iterator= restaurantsList.iterator();
        while(iterator.hasNext())
        {
            Restaurants restaurants = iterator.next();
            int id= restaurants.getId();
            final String names= restaurants.getNames();
            double distance= restaurants.getDistance();
            final double latitude= restaurants.getLatitude();
            final double longitude= restaurants.getLongitude();
            //boolean visible_online = restaurants.get
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude,longitude))
                            .title(names)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                }
            });
        }
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
                RestaurantsData.setRestaurantsData();
            }
            else
            {
                Toast.makeText(getBaseContext(),"Error getting restaurants",Toast.LENGTH_SHORT).show();
            }
        }
    }



}
