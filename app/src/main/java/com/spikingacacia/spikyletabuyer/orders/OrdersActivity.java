package com.spikingacacia.spikyletabuyer.orders;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.main.orders_list.OrdersFragment;


public class OrdersActivity extends AppCompatActivity
    implements  OneOrderFragment.OnFragmentInteractionListener
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
        setContentView(R.layout.activity_orders);
       String unique_order_name = getIntent().getStringExtra("unique_order_name");
       int order_format = getIntent().getIntExtra("order_format",1);
       int order_status = getIntent().getIntExtra("order_status",-1);
       String seller_names = getIntent().getStringExtra("seller_names");
       setTitle(seller_names);

        Fragment fragment= OneOrderFragment.newInstance(unique_order_name, order_format, order_status);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.base,fragment,"order");
        transaction.commit();
    }


    @Override
    public void onPay()
    {

    }
}
