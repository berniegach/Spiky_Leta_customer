package com.spikingacacia.spikyletabuyer.shop;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.CommonHelper;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.shop.BSItemC.InventoryItem;
import com.spikingacacia.spikyletabuyer.shop.BSItemF.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.shop.ShopA.sellerId;

/**
 * {@link RecyclerView.Adapter} that can display a {@link InventoryItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BSItemRecyclerViewAdapter extends RecyclerView.Adapter<BSItemRecyclerViewAdapter.ViewHolder>
{
    private final List<InventoryItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private List<InventoryItem>itemsCopy;
    private final Context mContext;
    private final int mCategoryId;
    private final int mGroupId;

    public BSItemRecyclerViewAdapter(List<InventoryItem> items, OnListFragmentInteractionListener listener, Context context, int categoryId, int groupId) {
        mValues = items;
        itemsCopy=new ArrayList<>();
        itemsCopy.addAll(items);
        mListener = listener;
        mContext=context;
        mCategoryId=categoryId;
        mGroupId=groupId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.f_siitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position)
    {
        final int available=mValues.get(position).available;
        String item=mValues.get(position).item;
        String des=mValues.get(position).description;
        item=item.replace("_"," ");
        des=des.replace("_"," ");
        holder.mItem = mValues.get(position);
        holder.mPositionView.setText(mValues.get(position).position);
        holder.mItemView.setText(item);
        holder.mPriceView.setText(Double.toString(mValues.get(position).sellingPrice));
        holder.mDescriptionView.setText(des);
        //get the category photo
        String url= LoginA.base_url+"src/sellers/"+String.format("%s/pics/i_%d", CommonHelper.makeName(sellerId), mValues.get(position).id)+".jpg";
        ImageRequest request=new ImageRequest(
                url,
                new Response.Listener<Bitmap>()
                {
                    @Override
                    public void onResponse(Bitmap response)
                    {
                        holder.mImageView.setImageBitmap(response);
                        Log.d("volley","succesful");
                    }
                }, 0, 0, null,
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError e)
                    {
                        Log.e("voley",""+e.getMessage()+e.toString());
                    }
                });
        RequestQueue request2 = Volley.newRequestQueue(mContext);
        request2.add(request);

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
            for(InventoryItem item:itemsCopy)
            {
                if(item.item.toLowerCase().contains(text))
                    mValues.add(item);
            }
        }
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mPositionView;
        public final ImageView mImageView;
        public final TextView mItemView;
        public final TextView mPriceView;
        public final TextView mDescriptionView;
        public InventoryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPositionView = (TextView) view.findViewById(R.id.position);
            mImageView = (ImageView) view.findViewById(R.id.image);
            mItemView = (TextView) view.findViewById(R.id.item);
            mPriceView = (TextView) view.findViewById(R.id.price);
            mDescriptionView = (TextView) view.findViewById(R.id.description);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItemView.getText() + "'";
        }
    }
}
