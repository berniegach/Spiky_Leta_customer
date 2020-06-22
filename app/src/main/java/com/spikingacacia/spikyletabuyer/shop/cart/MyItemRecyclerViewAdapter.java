package com.spikingacacia.spikyletabuyer.shop.cart;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.spikingacacia.spikyletabuyer.AppController;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.shop.ShopA;
import com.spikingacacia.spikyletabuyer.shop.cart.CartContent.CartItem;


import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;


public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>
{
    private final List<CartItem> mValues;
    private final CartFragment.OnListFragmentInteractionListener mListener;
    private TextView textViewTotal;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public MyItemRecyclerViewAdapter(List<CartItem> items, CartFragment.OnListFragmentInteractionListener listener, TextView textView)
    {
        mValues = items;
        mListener = listener;
        textViewTotal = textView;
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position)
    {
        String image_url= base_url+"src/items_pics/";
        final int count= mValues.get(position).count;
        holder.mItem = mValues.get(position);
        holder.mPriceView.setText("Ksh. "+Integer.toString(mValues.get(position).price.intValue()));
        holder.mItemView.setText(mValues.get(position).name);
        holder.mCountView.setText(Integer.toString(count));
        holder.mSizeView.setText(mValues.get(position).size);

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        holder.mPlusView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int count2= count + 1;
                holder.mItem.count=count2;
                ShopA.tempTotal+=holder.mItem.price;
                new CountTask(count2,  Integer.parseInt(mValues.get(position).id), position).execute((Void)null);
            }
        });
        holder.mMinusView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int count2= count - 1;
                holder.mItem.count=count2;
                ShopA.tempTotal-=holder.mItem.price;
               new CountTask(count2,  Integer.parseInt(mValues.get(position).id), position).execute((Void)null);
            }
        });
        // thumbnail image
        String url=image_url+String.valueOf(mValues.get(position).inventoryId)+'_'+String.valueOf(mValues.get(position).imageType);
        holder.mImageView.setImageUrl(url, imageLoader);

    }

    @Override
    public int getItemCount()
    {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final NetworkImageView mImageView;
        public final TextView mPriceView;
        public final TextView mItemView;
        public final TextView mSizeView;
        public final ImageButton mPlusView;
        public final TextView mCountView;
        public final ImageButton mMinusView;
        public CartItem mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.image);
            mPriceView = view.findViewById(R.id.price);
            mItemView = view.findViewById(R.id.item);
            mSizeView = view.findViewById(R.id.size);
            mPlusView = view.findViewById(R.id.plus);
            mCountView = view.findViewById(R.id.count);
            mMinusView = view.findViewById(R.id.minus);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + mItemView.getText() + "'";
        }
    }

    private class CountTask extends AsyncTask<Void, Void, Boolean>
    {
        private int count;
        private int id;
        private int position;
        public CountTask(int count, int id, int position)
        {
            this.count=count;
            this.id=id;
            this.position = position;
        }
        @Override
        protected Boolean doInBackground(Void... voids)
        {
            if(count==0)
            {
                mValues.remove(position);
                ShopA.tempCartLinkedHashMap.remove(id);
            }
            else
            {
                ShopA.tempCartLinkedHashMap.put(id, count);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean successful)
        {
            if(count==0)
                notifyDataSetChanged();
            else
            {
                notifyItemChanged(position);
                textViewTotal.setText("Ksh. "+ ShopA.tempTotal.intValue());
            }
            mListener.totalItemsChanged();
        }
    }
}
