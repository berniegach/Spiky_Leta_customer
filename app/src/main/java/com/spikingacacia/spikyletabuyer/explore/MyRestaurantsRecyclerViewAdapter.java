package com.spikingacacia.spikyletabuyer.explore;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Restaurants;

import java.util.List;

import static com.spikingacacia.spikyletabuyer.explore.RestaurantsFragment.*;


public class MyRestaurantsRecyclerViewAdapter extends RecyclerView.Adapter<MyRestaurantsRecyclerViewAdapter.ViewHolder>
{
    private final OnListFragmentInteractionListener mListener;
    private final List<Restaurants> mValues;
    private Context context;

    public MyRestaurantsRecyclerViewAdapter(OnListFragmentInteractionListener listener, Context context)
    {
        mValues = ExploreActivity.restaurantsList;
        mListener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_explore_restaurants, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
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

    @Override
    public int getItemCount()
    {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final ImageView imageView;
        public final TextView tNames;
        public final TextView tDistance;
        public final TextView tLocality;
        public Restaurants mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            imageView =  view.findViewById(R.id.image);
            tNames = view.findViewById(R.id.names);
            tDistance = view.findViewById(R.id.distance);
            tLocality = view.findViewById(R.id.locality);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + tNames.getText() + "'";
        }
    }
}