package com.spikingacacia.spikyletabuyer.restaurants;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.spikingacacia.spikyletabuyer.AppController;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.restaurants.SRRestaurantsF.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

public class SRRestaurantsRecyclerViewAdapter extends RecyclerView.Adapter<SRRestaurantsRecyclerViewAdapter.ViewHolder>
{
    private final List<Restaurants> mValues;
    private final OnListFragmentInteractionListener mListener;
    private List<Restaurants>itemsCopy;
    private final Context mContext;

    public SRRestaurantsRecyclerViewAdapter( OnListFragmentInteractionListener listener, Context context) {
        mValues = new ArrayList<>();
        itemsCopy=new ArrayList<>();
        mValues.addAll(MainActivity.restaurantsList);
        itemsCopy.addAll(MainActivity.restaurantsList);
        mListener = listener;
        mContext=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.f_srrestaurants, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position)
    {
        String names=mValues.get(position).getNames();
        names=names.replace("_"," ");
        holder.mItem = mValues.get(position);
        holder.mNamesView.setText(names);
        holder.mDistanceView.setText(String.format("%.0f meters away",mValues.get(position).getDistance()));

        //get the category photo
        String url= LoginA.base_url+"src/sellers_pics/"+ mValues.get(position).getId()+'_'+mValues.get(position).getImage_type();


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onItemClicked(holder.mItem);
                }
            }
        });
        Glide.with(mContext).load(url).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
    public void filter(String text)
    {
        mValues.clear();
        if(text.isEmpty())
            mValues.addAll(itemsCopy);
        else
        {
            text=text.toLowerCase();
            for(Restaurants item:itemsCopy)
            {
                if(item.getNames().toLowerCase().contains(text))
                    mValues.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mPositionView;
        public final ImageView mImageView;
        public final TextView mNamesView;
        public final TextView mDistanceView;
        public Restaurants mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPositionView = (TextView) view.findViewById(R.id.position);
            mImageView = view.findViewById(R.id.image);
            mNamesView = (TextView) view.findViewById(R.id.names);
            mDistanceView = (TextView) view.findViewById(R.id.distance);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNamesView.getText() + "'";
        }
    }
}
