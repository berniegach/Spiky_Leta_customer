package com.spikingacacia.spikyletabuyer.main.tasty;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.database.TastyBoard;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.restaurants.SRRestaurantsA;
import com.spikingacacia.spikyletabuyer.shop.ShopA;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class TastyBoardActivity extends AppCompatActivity implements
        TastyBoardOverviewFragment.OnListFragmentInteractionListener
{
    private TastyBoard tastyBoard;
    private String TAG = "tast_board_a";
    private ProgressBar progressBar;
    private View mainFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasty_board);

        setTitle("Tasty Board");
        //get the tasty board
        tastyBoard = (TastyBoard) getIntent().getSerializableExtra("tasty_board");

        progressBar = findViewById(R.id.progress);
        mainFragment = findViewById(R.id.base);

        Fragment fragment= TastyBoardOverviewFragment.newInstance(tastyBoard);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.base,fragment,"overview");
        transaction.commit();
    }
    /*
     * implementation of TastyBoardOverviewFragment.java
     */
    @Override
    public void onTastyBoardItemPreOrder(TastyBoard tastyBoard)
    {

    }

    @Override
    public void onGotoMenu()
    {
        showProgress(true);
        if(!MainActivity.myLocation.contentEquals(""))
        {
            String[] location = MainActivity.myLocation.split(":");
            new RestaurantTask(location[0], location[1]).execute((Void)null);
        }
    }
    void showProgress(boolean show)
    {
        progressBar.setVisibility(show? View.VISIBLE : View.GONE);
        mainFragment.setVisibility( show? View.INVISIBLE :View.VISIBLE);
    }
    private void showRestaurants(Restaurants item)
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
    private class RestaurantTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_restaurant=base_url+"get_restaurant_from_tasty_board.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        //final private String country;
        final private String latitude;
        final private String longitude;
        private  int success;
        private Restaurants restaurant;


        public RestaurantTask( String latitude, String longitude)
        {
            this.latitude=latitude;
            this.longitude=longitude;
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
            info.add(new BasicNameValuePair("location",""));
            info.add(new BasicNameValuePair("seller_email",tastyBoard.getSellerEmail()));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_restaurant,"POST",info);
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

                    restaurant =new Restaurants(id, email, names,distance,latitude,longitude,locality,country_code, order_radius, tables, image_type, table_number, m_code, dining_options);
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
                showRestaurants( restaurant);
            }
            else
            {
                Toast.makeText(getBaseContext(),"Error getting restaurant",Toast.LENGTH_SHORT).show();
            }
        }
    }



}