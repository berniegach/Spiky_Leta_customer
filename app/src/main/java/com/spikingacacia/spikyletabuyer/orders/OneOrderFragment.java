package com.spikingacacia.spikyletabuyer.orders;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.main.orders_list.OrdersFragment;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class OneOrderFragment extends Fragment
{

    private static final String ARG_ORDER = "order";
    private static final String ARG_FORMAT = "order_format";
    private static final String ARG_ORDER_STATUS = "order_status";
    private String mOrder;
    private int mOrderFormat;
    private int mOrderStatus;
    private OnFragmentInteractionListener mListener;


    public static OneOrderFragment newInstance(String order, int format, int station)
    {
        OneOrderFragment fragment = new OneOrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER, order);
        args.putInt(ARG_FORMAT, format);
        args.putInt(ARG_ORDER_STATUS, station);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mOrder = getArguments().getString(ARG_ORDER);
            mOrderFormat = getArguments().getInt(ARG_FORMAT);
            mOrderStatus = getArguments().getInt(ARG_ORDER_STATUS);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_one_orders, container, false);
        ProgressBar progressBar=view.findViewById(R.id.progress);
        LinearLayout l_base=view.findViewById(R.id.orders_base);
        TextView t_date=view.findViewById(R.id.date);
        TextView t_username=view.findViewById(R.id.username);
        TextView t_table=view.findViewById(R.id.table);
        TextView t_waiter=view.findViewById(R.id.waiter);
        TextView t_status = view.findViewById(R.id.status);
        //set the buttons listeners
        Button b_pay=view.findViewById(R.id.pay);
        b_pay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener!=null)
                    mListener.onPay();
            }
        });

        String[] status_strings_1 = new String[]{"pending","in progress","delivery","payment","finished"};
        String[] status_strings_2 = new String[]{"pending","payment","in progress","delivery","finished"};
        t_status.setText(mOrderFormat==1?status_strings_1[mOrderStatus-1]:status_strings_2[mOrderStatus-1]);
        //show the respective buttons and change their labels accordingly
        //for pending the buttons remain as they are
        if(mOrderFormat==1)
        {
            //pending--in progress--delivery--payment--finished
            if(mOrderStatus == 4)
                b_pay.setVisibility(View.VISIBLE);

        }
        else
        {
            //pending--payment--in progress--delivery--finished
            if(mOrderStatus == 2)
                b_pay.setVisibility(View.VISIBLE);
        }
        String username="";
        int table=0;
        int count=0;
        double total_price=0.0;
        String date_to_show="";
        String waiter="";
        Iterator iterator= OrdersFragment.ordersLinkedHashMap.entrySet().iterator();
        while (iterator.hasNext())
        {
            LinkedHashMap.Entry<Integer, Orders>set=(LinkedHashMap.Entry<Integer, Orders>) iterator.next();
            Orders orders =set.getValue();
            int itemId= orders.getItemId();
            int order_number= orders.getOrderNumber();
            int orderStatus= orders.getOrderStatus();
            String orderName= orders.getItem();
            orderName=orderName.replace("_"," ");
            double price= orders.getPrice();
            String dateAdded= orders.getDateAdded();
            String[] date=dateAdded.split(" ");
            if(!(date[0]+":"+order_number).contentEquals(mOrder))
                continue;
            username= orders.getSellerNames();
            waiter= orders.getWaiterNames();
            table= orders.getTableNumber();
            if(count==0)
            {
                progressBar.setProgress(orderStatus);
            }
            //add the layouts
            //cardview
            View layout = inflater.inflate(R.layout.order_cardview_layout,null);
            TextView t_count = layout.findViewById(R.id.count);
            TextView t_item = layout.findViewById(R.id.item);
            TextView t_price = layout.findViewById(R.id.price);

            t_count.setText(String.valueOf(count+1));
            t_item.setText(orderName);
            t_price.setText(String.valueOf(price));
            l_base.addView(layout);


            count+=1;
            total_price+=price;
            date_to_show=dateAdded;
        }
        ((TextView)view.findViewById(R.id.total)).setText("Total "+String.valueOf(total_price));
        // l_base.addView(t_total);
        //set date text
        t_date.setText(date_to_show);
        t_username.setText(username);
        t_table.setText("Table "+table);
        t_waiter.setText(waiter);
        return view;
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }
    public interface OnFragmentInteractionListener
    {
        void onPay();
    }
}