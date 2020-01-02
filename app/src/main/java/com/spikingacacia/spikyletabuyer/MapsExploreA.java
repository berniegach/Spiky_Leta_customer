package com.spikingacacia.spikyletabuyer;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.spikingacacia.spikyletabuyer.database.BRestaurants;

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

public class MapsExploreA extends FragmentActivity implements
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
    private String TAG="MapsA";
    private Marker myMarker;
    private View mapView;
    private String url_get_restaurants=base_url+"get_near_restaurants.php";
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private JSONParser jsonParser;
    public static  List<BRestaurants>bRestaurantsList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_maps_explore);
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
        bRestaurantsList=new LinkedList<>();
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
        Log.d("loc",String.format("min: %f, max: %f",googleMap.getMinZoomLevel(),googleMap.getMaxZoomLevel()));
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
                                if(location!=null)
                                {
                                    double latitude=location.getLatitude();
                                    double longitude=location.getLongitude();
                                    //get addresses
                                    Geocoder geocoder=new Geocoder(MapsExploreA.this, Locale.getDefault());
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
                                        Log.e("address",""+e.getMessage());
                                    }
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
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (isPermissionGranted(permissions, grantResults,  Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
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
    private void showRestaurants()
    {
        //add markers
        Iterator<BRestaurants> iterator= bRestaurantsList.iterator();
        while(iterator.hasNext())
        {
            BRestaurants bRestaurants = iterator.next();
            int id=bRestaurants.getId();
            String names=bRestaurants.getNames();
            double distance=bRestaurants.getDistance();
            double latitude=bRestaurants.getLatitude();
            double longitude=bRestaurants.getLongitude();
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude,longitude))
                    .title(names)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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
                        String names=jsonObject_restaurants.getString("username");
                        double distance=jsonObject_restaurants.getDouble("distance");
                        double latitude=jsonObject_restaurants.getDouble("latitude");
                        double longitude=jsonObject_restaurants.getDouble("longitude");
                        String locality=jsonObject_restaurants.getString("locality");
                        String country=jsonObject_restaurants.getString("country");
                        int order_radius=jsonObject_restaurants.getInt("order_radius");
                        int number_of_tables=jsonObject_restaurants.getInt("number_of_tables");

                        BRestaurants bRestaurants=new BRestaurants(id,names,distance,latitude,longitude,locality,country, order_radius, number_of_tables);
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
