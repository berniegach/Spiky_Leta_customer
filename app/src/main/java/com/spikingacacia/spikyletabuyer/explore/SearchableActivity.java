package com.spikingacacia.spikyletabuyer.explore;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.spikingacacia.spikyletabuyer.AppController;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.shop.ShopA;

public class SearchableActivity extends AppCompatActivity
{
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private String TAG = "searchable_A";
    private ListView listView;
    private View layoutSingle;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        setTitle("Pre Order");
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        listView = findViewById(R.id.list);
        layoutSingle = findViewById(R.id.single_restaurant);

        // search
        handleSearch();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSearch();
    }
    private void handleSearch() {
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG,"INSEARCH SJDJS:"+query);

        }
        else if(Intent.ACTION_VIEW.equals(intent.getAction()))
        {
            String selectedSuggestionRowId =  intent.getDataString();
            //execution comes here when an item is selected from search suggestions
            //you can continue from here with user selected search item
            showRestaurant(Integer.parseInt(selectedSuggestionRowId));
        }
    }
    private void showRestaurant(int id)
    {
        listView.setVisibility(View.GONE);
        layoutSingle.setVisibility(View.VISIBLE);
        final Restaurants restaurants = MapsExploreActivity.restaurantsList.get(id);
        ((TextView)layoutSingle.findViewById(R.id.names)).setText(restaurants.getNames());
        Double distance = restaurants.getDistance();
        String s_distance = distance<1000? String.format("%.0f metres away",distance) : String.format("%.0f km away",restaurants.getDistance()/1000);
        ((TextView)layoutSingle.findViewById(R.id.distance)).setText(s_distance);
        ((Button)layoutSingle.findViewById(R.id.pre_order)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gotoRestaurant(restaurants);
            }
        });
        String url= LoginA.base_url+"src/sellers_pics/"+ restaurants.getId()+'_'+restaurants.getImage_type();
        //((ImageView)layoutSingle.findViewById(R.id.image)).setImageUrl(url,imageLoader);
        Glide.with(getBaseContext()).load(url).into((ImageView)layoutSingle.findViewById(R.id.image));
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
        intent.putExtra("dining_options", item.getDiningOptions());
        intent.putExtra("m_code", has_payment ? item.getmCode() : "");
        startActivity(intent);
    }
}