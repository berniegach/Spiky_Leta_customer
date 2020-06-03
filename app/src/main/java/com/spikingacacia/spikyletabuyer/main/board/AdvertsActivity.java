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
        if(!preferences.isDark_theme_enabled())
        {
            setTheme(R.style.AppThemeLight);
            toolbar.setTitleTextColor(getResources().getColor(R.color.text_light));
            toolbar.setPopupTheme(R.style.AppThemeLight_PopupOverlayLight);
            AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
            appBarLayout.getContext().setTheme(R.style.AppThemeLight_AppBarOverlayLight);
            appBarLayout.setBackgroundColor(getResources().getColor(R.color.main_background_light));
            findViewById(R.id.main).setBackgroundColor(getResources().getColor(R.color.main_background_light));
        }

        Fragment fragment= AdvertsFragment.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"ads");
        transaction.commit();
    }

    @Override
    public void onAdClicked(AdsC.AdItem item)
    {
        Fragment fragment=AdOverviewF.newInstance(item.id,item.title, item.bitmap, item.bitmap_seller, item.seller_name,item.content, item.views,item.likes,item.comments,item.date);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"overview");
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
