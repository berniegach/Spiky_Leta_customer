package com.spikingacacia.spikyletabuyer.main.board;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Adverts;


import static com.spikingacacia.spikyletabuyer.LoginA.base_url;


public class AdvertsActivity extends AppCompatActivity implements AdvertsFragment.OnListFragmentInteractionListener
{
    private static final int PERMISSION_REQUEST_INTERNET=255;
    private String url_add_advert= base_url+"add_advert.php";
    private RecyclerView recyclerView;
    public String title;
    public String content;
    Preferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tasty Board");
        //preference
        preferences=new Preferences(getBaseContext());


        Fragment fragment= AdvertsFragment.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"ads");
        transaction.commit();
    }

    @Override
    public void onAdClicked(Adverts item)
    {
        /*Fragment fragment=AdOverviewF.newInstance(item.id,item.title, item.bitmap, item.bitmap_seller, item.seller_name,item.content, item.views,item.likes,item.comments,item.date);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"overview");
        transaction.addToBackStack(null);
        transaction.commit();*/
    }
}
