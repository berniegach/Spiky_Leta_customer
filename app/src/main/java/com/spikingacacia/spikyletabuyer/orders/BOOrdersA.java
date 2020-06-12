package com.spikingacacia.spikyletabuyer.orders;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;


public class BOOrdersA extends AppCompatActivity
    implements BOOverviewF.OnFragmentInteractionListener, BOOrderF.OnListFragmentInteractionListener
{
    private String fragmentWhich="overview";
    private int buyerId;
    private int orderId;
    private int orderNumber;
    private String dateAdded;
    private int mWhichOrder=0;
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private String TAG="SOOrdersA";
    private JSONParser jsonParser;
    Preferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_boorders);
        jsonParser=new JSONParser();
        //set actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Orders");

        //set the first base fragment
        Fragment fragment=BOOverviewF.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"");
        transaction.commit();
        //fragment manager
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
        {
            @Override
            public void onBackStackChanged()
            {
                int count=getSupportFragmentManager().getBackStackEntryCount();
                if(count==0)
                    setTitle("Orders");
                else if(count==1)
                    setTitle(fragmentWhich);
            }
        });
    }
    /**
     * implementation of BOOverviewF.java*/
    @Override
    public void onChoiceClicked(int which, int format)
    {
        mWhichOrder=which;
        switch(which)
        {
            case 0:
                fragmentWhich="Current";
                break;
            case 1:
                fragmentWhich="Pending";
                break;
            case 2:
                fragmentWhich= format==1?"In Progress":"Payment";
                break;
            case 3:
                fragmentWhich= format==1?"Delivery":"In Progress";
                break;
            case 4:
                fragmentWhich= format==1?"Payment":"Delivery";
                break;
            case 5:
                fragmentWhich="Finished";
        }
        setTitle(fragmentWhich);
        Fragment fragment=BOOrderF.newInstance(1,which);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,fragmentWhich);
        transaction.addToBackStack(fragmentWhich);
        transaction.commit();
    }
    /**
     * implementation of BOOrderF.java
     * */
    public void onListFragmentInteraction(BOOrderC.OrderItem item)
    {
        String date_added=item.dateAdded;
        String[] date_pieces=date_added.split(" ");
        String unique_name=date_pieces[0]+":"+item.orderNumber;
        setTitle("Order");
        Fragment fragment=BOOrdersF.newInstance(unique_name);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"order");
        transaction.addToBackStack("order");
        transaction.commit();
    }

}
