/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 9/22/20 3:27 PM
 */

package com.spikingacacia.spikyletabuyer.orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Orders;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.main.orders_list.OrdersListFragment.*;

public class MyOrderRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<Orders> mValues;
    private List<Orders> itemsCopy;
    private final OnListFragmentInteractionListener mListener;
    private Context context;
    private int whichPager = 1;

    public MyOrderRecyclerViewAdapter(OnListFragmentInteractionListener listener, Context context, int whichPager)
    {
        mValues = new LinkedList<>();
        mListener = listener;
        itemsCopy=new ArrayList<>();
        this.context = context;
        this.whichPager = whichPager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_orders, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof ViewHolder)
        {
            populateItemRows((ViewHolder) holder, position);
        }
        else if (holder instanceof LoadingViewHolder)
        {
            showLoadingView((LoadingViewHolder) holder, position);
        }
    }

    @Override
    public int getItemCount()
    {
        return mValues == null ? 0 : mValues.size();
    }
    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return mValues.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }
    public void filter(String text)
    {
        mValues.clear();
        if(text.isEmpty())
            mValues.addAll(itemsCopy);
        else
        {
            text=text.toLowerCase();
            for(Orders orderItem:itemsCopy)
            {
                if(orderItem.getSellerNames().toLowerCase().contains(text))
                    mValues.add(orderItem);
            }
        }
        notifyDataSetChanged();
    }
    public void filter(int status)
    {
        mValues.clear();
        if(status==0)
            mValues.addAll(itemsCopy);
        else if(status == 1)//show the unfinished
        {
            for(Orders orderItem:itemsCopy)
            {
                if(orderItem.getOrderStatus()!=5)
                    mValues.add(orderItem);
            }
        }
        else if( status == 2)//show finished
        {
            for(Orders orderItem:itemsCopy)
            {
                if(orderItem.getOrderStatus()==5)
                    mValues.add(orderItem);
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final ImageView image;
        public final TextView mOrderView;
        public final TextView mTableView;
        public final TextView mUsernameView;
        public final TextView mDateView;
        public final TextView mOrderStatus;
        public Orders mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            image = view.findViewById(R.id.image);
            mOrderView = (TextView) view.findViewById(R.id.order_number);
            mTableView = (TextView) view.findViewById(R.id.table_number);
            mUsernameView = (TextView) view.findViewById(R.id.username);
            mDateView = (TextView) view.findViewById(R.id.date);
            mOrderStatus = view.findViewById(R.id.status);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + mUsernameView.getText() + "'";
        }
    }
    public  class LoadingViewHolder extends RecyclerView.ViewHolder
    {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView)
        {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

    }

    public void listUpdated(List<Orders> newitems)
    {
        mValues.clear();
        mValues.addAll(newitems);
        itemsCopy.addAll(newitems);
        notifyDataSetChanged();
    }
    public void listAddProgressBar()
    {
        mValues.add(null);
        notifyDataSetChanged();
    }
    public void listRemoveProgressBar()
    {
        mValues.remove(mValues.size()-1);
        notifyDataSetChanged();
    }
    public void listAddItems(List<Orders> newitems)
    {
        mValues.addAll(newitems);
        itemsCopy.addAll(newitems);
        notifyDataSetChanged();
    }
    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed

    }
    private void populateItemRows(final ViewHolder holder, int position)
    {
        String image_url= LoginA.base_url+"src/sellers_pics/";
        holder.mItem = mValues.get(position);
        holder.mOrderView.setText("Order "+mValues.get(position).getOrderNumber());
        int table_number = mValues.get(position).getTableNumber();
        holder.mTableView.setText(table_number == -1 ? "Pre - Order" : "Table "+table_number);
        holder.mUsernameView.setText(mValues.get(position).getSellerNames());
        //format the date
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        PrettyTime p = new PrettyTime();
        try
        {
            holder.mDateView.setText(p.format(format.parse(mValues.get(position).getDateAddedLocal())));
        } catch (ParseException e)
        {
            // e.printStackTrace();
        }
        String status;
        switch(mValues.get(position).getOrderStatus())
        {
            case -3:
                status = "Unpaid";
                break;
            case -2:
                status = "Processing payment";
                break;
            case -1:
                status = "Paid & Pending";
                break;
            case 1:
                status = "UnPaid & Pending";
                break;
            case 2:
                status = "Pending payment";
                break;
            case 3:
                status = "In progress";
                break;
            case 4:
                status = "Delivery";
                break;
            case 5:
                status = "Finished";
                break;
            default:
                status = "";
        }
        holder.mOrderStatus.setText(status);

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    String date_added_1=holder.mItem.getDateAdded();
                    String[] date_pieces=date_added_1.split(" ");
                    String unique_name=date_pieces[0]+":"+holder.mItem.getOrderNumber();
                    LinkedHashMap<Integer, Orders> ordersLinkedHashMap = new LinkedHashMap<>();
                    List<Orders> ordersList = new LinkedList<>();
                    if(whichPager == 1)
                        ordersLinkedHashMap = ordersLinkedHashMapPending;
                    else if(whichPager == 2)
                        ordersLinkedHashMap = ordersLinkedHashMapFinished;
                    for (LinkedHashMap.Entry<Integer, Orders> set : ordersLinkedHashMap.entrySet())
                    {
                        Orders orders = set.getValue();
                        int order_number = orders.getOrderNumber();
                        String date_added = orders.getDateAdded();
                        String[] date = date_added.split(" ");
                        if (!(date[0] + ":" + order_number).contentEquals(unique_name))
                            continue;
                        ordersList.add(orders);
                    }

                    mListener.onOrderClicked( ordersList);
                }
            }
        });
        String url=image_url+String.valueOf(mValues.get(position).getSellerId())+'_'+String.valueOf(mValues.get(position).getSellerImageType());
        Glide.with(context).load(url).into(holder.image);
    }
}
