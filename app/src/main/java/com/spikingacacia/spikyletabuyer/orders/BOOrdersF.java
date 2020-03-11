package com.spikingacacia.spikyletabuyer.orders;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.database.BOrders;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class BOOrdersF extends Fragment
{

    private static final String ARG_ORDER_DATE = "order_date";
    private String orderDate;
    Preferences preferences;


    public static BOOrdersF newInstance(String date)
    {
        BOOrdersF fragment = new BOOrdersF();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ORDER_DATE,date);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            orderDate= getArguments().getString(ARG_ORDER_DATE);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.f_booorders, container, false);
        //preference
        preferences=new Preferences(getContext());
        if(!preferences.isDark_theme_enabled())
        {
            root.findViewById(R.id.sec_main).setBackgroundColor(getResources().getColor(R.color.secondary_background_light));
        }
        ProgressBar progressBar=root.findViewById(R.id.progress);
        LinearLayout l_base=root.findViewById(R.id.orders_base);
        TextView t_date=root.findViewById(R.id.date);
        TextView t_status=root.findViewById(R.id.status);
        TextView t_table=root.findViewById(R.id.table);
        TextView t_seller=root.findViewById(R.id.seller);
        TextView t_waiter=root.findViewById(R.id.waiter);

        int count=0;
        double total_price=0.0;
        String date_to_show="";
        Iterator iterator= LoginA.bOrdersList.entrySet().iterator();
        while (iterator.hasNext())
        {
            LinkedHashMap.Entry<Integer, BOrders>set=(LinkedHashMap.Entry<Integer, BOrders>) iterator.next();
            BOrders bOrders=set.getValue();
            int itemId=bOrders.getItemId();
            int order_number=bOrders.getOrderNumber();
            int orderStatus=bOrders.getOrderStatus();
            String orderName=bOrders.getOrderName();
            orderName=orderName.replace("_"," ");
            double price=bOrders.getPrice();
            int order_format=bOrders.getOrderFormat();
            int table=bOrders.getTableNumber();
            String restaurant=bOrders.getRestaurantName();
            String waiter=bOrders.getWaiter_names();
            String dateAdded=bOrders.getDateAdded();
            String[] date=dateAdded.split(" ");
            if(!(date[0]+":"+order_number).contentEquals(orderDate))
                continue;
            if(count==0)
            {
                progressBar.setProgress(orderStatus);
                t_table.setText("Table "+table);
                t_seller.setText(restaurant);
                t_waiter.setText(waiter);
                String[] status_1=new String[]{"Pending","In progress","Delivery","Payment","Finished"};
                String[] status_2=new String[]{"Pending","Payment","In Progress","Delivery","Finished"};
                t_status.setText(order_format==1? status_1[orderStatus-1]: status_2[orderStatus-1]);
            }
            //add the layouts
            //main layout
            LinearLayout l_main=new LinearLayout(getContext());
            l_main.setWeightSum(10);
            l_main.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            l_main.setOrientation(LinearLayout.HORIZONTAL);
            l_main.setPadding(2,2,2,2);
            //textView for count
            TextView t_count=new TextView(getContext());
            t_count.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1));
            t_count.setText(String.valueOf(count+1));
            t_count.setGravity(Gravity.START);
            //textview for names
            TextView t_names=new TextView(getContext());
            t_names.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,6));
            t_names.setText(orderName);
            t_names.setGravity(Gravity.CENTER);
            //textview for price
            TextView t_price=new TextView(getContext());
            t_price.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,3));
            t_price.setText(String.valueOf(price));
            t_price.setGravity(Gravity.END);
            //add the layouts
            l_main.addView(t_count);
            l_main.addView(t_names);
            l_main.addView(t_price);
            l_base.addView(l_main);
            count+=1;
            total_price+=price;
            date_to_show=dateAdded;
        }
        //textview for total price
        TextView t_total=new TextView(getContext());
        t_total.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        t_total.setGravity(Gravity.END);
        t_total.setText("Total: "+String.valueOf(total_price));
        l_base.addView(t_total);
        //set date text
        t_date.setText(date_to_show);
        return root;
    }
}