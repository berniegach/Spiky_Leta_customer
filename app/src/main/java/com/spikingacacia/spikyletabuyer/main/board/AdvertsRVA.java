package com.spikingacacia.spikyletabuyer.main.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.spikingacacia.spikyletabuyer.AppController;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Adverts;
import com.spikingacacia.spikyletabuyer.main.board.AdvertsFragment.OnListFragmentInteractionListener;
import com.spikingacacia.spikyletabuyer.main.board.AdsC.AdItem;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class AdvertsRVA extends RecyclerView.Adapter<AdvertsRVA.ViewHolder>
{

    private final List<Adverts> mValues;
    private final AdvertsFragment.OnListFragmentInteractionListener mListener;
    Context mContext;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();


    public AdvertsRVA(OnListFragmentInteractionListener listener, Context context)
    {
        mValues = new ArrayList<>();
        mListener = listener;
        mContext=context;
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_adverts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        String ad_image_url= base_url+"src/ads/";
        String seller_image_url = base_url+"src/sellers/";
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).getTitle());
        holder.mSellerView.setText(mValues.get(position).getSeller_name());
        holder.mViewsView.setText(mValues.get(position).getViews()+" views");
        holder.mLikesView.setText(mValues.get(position).getLikes()+ " likes");
        holder.mCommentsView.setText(mValues.get(position).getComments() +" comments");
        //format the date
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        PrettyTime p = new PrettyTime();
        try
        {
            holder.mDateView.setText(p.format(format.parse(mValues.get(position).getDate())));
        } catch (ParseException e)
        {
            e.printStackTrace();
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
        // ad image
        String url_post_image=ad_image_url+String.valueOf(holder.mItem.getId())+String.valueOf(holder.mItem.getImageType());
        holder.mImageView.setImageUrl(url_post_image, imageLoader);
        //seller image
        String url_seller_image= seller_image_url+String.valueOf(holder.mItem.getSeller_id())+"/pics/prof_pic"+holder.mItem.getSellerImageType();
        holder.mImageSellerView.setImageUrl(url_seller_image,imageLoader);
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
        public final NetworkImageView mImageView;
        public final NetworkImageView mImageSellerView;
        public final TextView mSellerView;
        public final TextView mViewsView;
        public final TextView mLikesView;
        public final TextView mCommentsView;
        public final TextView mDateView;
        public Adverts mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mImageView =  view.findViewById(R.id.image);
            mImageSellerView = view.findViewById(R.id.image_seller);
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

    public void listUpdated(List<Adverts> newitems)
    {
        mValues.clear();
        mValues.addAll(newitems);
        notifyDataSetChanged();
    }


}
