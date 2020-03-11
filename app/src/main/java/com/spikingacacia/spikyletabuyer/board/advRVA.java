package com.spikingacacia.spikyletabuyer.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.board.advF.OnListFragmentInteractionListener;
import com.spikingacacia.spikyletabuyer.board.AdsC.AdItem;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class advRVA extends RecyclerView.Adapter<advRVA.ViewHolder>
{

    private final List<AdsC.AdItem> mValues;
    private final advF.OnListFragmentInteractionListener mListener;
    Preferences preferences;
    Context mContext;


    public advRVA(List<AdItem> items, OnListFragmentInteractionListener listener, Context context)
    {
        mValues = items;
        mListener = listener;
        mContext=context;
        preferences=new Preferences(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.f_adv, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).title);
        holder.mImageView.setImageBitmap(mValues.get(position).bitmap);
        holder.mImageSellerView.setImageBitmap(mValues.get(position).bitmap_seller);
        holder.mSellerView.setText(mValues.get(position).seller_name);
        holder.mViewsView.setText(mValues.get(position).views+" views");
        holder.mLikesView.setText(mValues.get(position).likes+ " likes");
        holder.mCommentsView.setText(mValues.get(position).comments +" comments");
        //format the date
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        PrettyTime p = new PrettyTime();
        try
        {
            holder.mDateView.setText(p.format(format.parse(mValues.get(position).date)));
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        if(!preferences.isDark_theme_enabled())
        {
            holder.mView.setBackgroundColor(mContext.getResources().getColor(R.color.secondary_background_light));
        }
        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onAdClicked(holder.mItem);
                }
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
        public final TextView mTitleView;
        public final ImageView mImageView;
        public final ImageView mImageSellerView;
        public final TextView mSellerView;
        public final TextView mViewsView;
        public final TextView mLikesView;
        public final TextView mCommentsView;
        public final TextView mDateView;
        public AdItem mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mImageView = (ImageView) view.findViewById(R.id.image);
            mImageSellerView = (ImageView) view.findViewById(R.id.image_seller);
            mSellerView = (TextView) view.findViewById(R.id.seller);
            mViewsView = (TextView) view.findViewById(R.id.views);
            mLikesView = (TextView) view.findViewById(R.id.likes);
            mCommentsView = (TextView) view.findViewById(R.id.comments);
            mDateView = (TextView) view.findViewById(R.id.date);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }

    public void add_ads(int id, String title, Bitmap bitmap, Bitmap bitmap_seller, String seller_name, String content, int views, int likes, int comments, String date)
    {
        for(int c=0; c<mValues.size(); c++)
            if(mValues.get(c).id.contentEquals(String.valueOf(id)))
                return;
        AdsC content1=new AdsC();
        AdItem dummyItem=content1.createItem(String.valueOf(id), title, bitmap, bitmap_seller, seller_name, content, String.valueOf(views), String.valueOf(likes), String.valueOf(comments), date);
        mValues.add(dummyItem);
        Collections.sort(mValues, new Comparator<AdItem>()
        {
            @Override
            public int compare(AdItem o1, AdItem o2)
            {
                return Integer.parseInt(o2.id)-Integer.parseInt(o1.id);
            }
        });
        notifyDataSetChanged();
    }
    public void clearData() {
        mValues.clear(); // clear list
        notifyDataSetChanged(); // let your adapter know about the changes and reload view.
    }


}
