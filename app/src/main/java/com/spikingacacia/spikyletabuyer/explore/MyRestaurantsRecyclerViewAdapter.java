/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 8/19/20 12:57 PM
 */

package com.spikingacacia.spikyletabuyer.explore;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.orders.MyOrderRecyclerViewAdapter;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.explore.RestaurantsFragment.*;


public class MyRestaurantsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final OnListFragmentInteractionListener mListener;
    private List<Restaurants> mValues;
    private List<Restaurants> itemsCopy;
    private Context context;

    public MyRestaurantsRecyclerViewAdapter(OnListFragmentInteractionListener listener, Context context)
    {
        mValues = new LinkedList<>();
        mListener = listener;
        itemsCopy=new ArrayList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_explore_restaurants, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }

        /*View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_explore_restaurants, parent, false);
        return new ViewHolder(view);*/
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
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final ImageView imageView;
        public final TextView tNames;
        public final TextView tDistance;
        public final TextView tLocality;
        public final ImageView unavailable;
        public final TextView tClosingTime;
        public Restaurants mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            imageView =  view.findViewById(R.id.image);
            tNames = view.findViewById(R.id.names);
            tDistance = view.findViewById(R.id.distance);
            tLocality = view.findViewById(R.id.locality);
            unavailable = view.findViewById(R.id.unavailable);
            tClosingTime = view.findViewById(R.id.closing_time);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + tNames.getText() + "'";
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
    public void listUpdated(List<Restaurants> newitems)
    {
        mValues.clear();
        itemsCopy.clear();
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
    public void listAddItems(List<Restaurants> newitems)
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
        holder.mItem = mValues.get(position);
        holder.tNames.setText(mValues.get(position).getNames());
        Double distance = mValues.get(position).getDistance();
        String s_distance = distance<1000? String.format("%.0f m",distance) : String.format("%.0f km",distance/1000);
        holder.tDistance.setText(s_distance);
        String locality = mValues.get(position).getLocality();
        holder.tLocality.setText(locality.contentEquals("null") || locality.contentEquals("")?"":locality);

        String url= LoginA.base_url+"src/sellers_pics/"+ mValues.get(position).getId()+'_'+mValues.get(position).getImage_type();
        Glide.with(context).load(url).into(holder.imageView);
        if(!holder.mItem.isOpened())
        {
            holder.unavailable.setVisibility(View.VISIBLE);
            holder.mView.setEnabled(false);
            holder.tClosingTime.setText("Closed at "+holder.mItem.getClosingTime());
        }

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener!=null)
                    mListener.onItemClicked(holder.mItem);
            }
        });
    }


}