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
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.CommonHelper;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.shop.BSCategoryC.CategoryItem;
import com.spikingacacia.spikyletabuyer.shop.BSCategoryF.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.shop.ShopA.sellerId;

/**
 * {@link RecyclerView.Adapter} that can display a {@link CategoryItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BSCategoryRecyclerViewAdapter extends RecyclerView.Adapter<BSCategoryRecyclerViewAdapter.ViewHolder>
{
    private final List<CategoryItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private JSONParser jsonParser;
    private List<CategoryItem>itemsCopy;
    private final Context mContext;
    private static Context mContextStatic;
    private static ImageView imageView;
    private static int mCategoryIdForPhoto;
    Preferences preferences;

    public BSCategoryRecyclerViewAdapter(List<CategoryItem> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        itemsCopy=new ArrayList<>();
        itemsCopy.addAll(items);
        mListener = listener;
        mContext=context;
        mContextStatic=context;
        jsonParser=new JSONParser();
        //preference
        preferences=new Preferences(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.f_bicategory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position)
    {
        String cat=mValues.get(position).category;
        String des=mValues.get(position).description;
        cat=cat.replace("_"," ");
        des=des.replace("_"," ");
        holder.mItem = mValues.get(position);
        holder.mPositionView.setText(mValues.get(position).position);
        holder.mCategoryView.setText(cat);
        holder.mDescriptionView.setText(des);
        if(!preferences.isDark_theme_enabled())
        {
            holder.mView.setBackgroundColor(mContext.getResources().getColor(R.color.secondary_background_light));
        }
        //get the category photo
        String url= LoginA.base_url+"src/sellers/"+String.format("%s/pics/c_%d", CommonHelper.makeName(sellerId), mValues.get(position).id)+".jpg";
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
            for(CategoryItem item:itemsCopy)
            {
                if(item.category.toLowerCase().contains(text))
                    mValues.add(item);
            }
        }
        notifyDataSetChanged();
    }
    public void notifyChange(int position,int id, String category, String description, String dateadded, String datechanged)
    {
        BSCategoryC content=new BSCategoryC();
        mValues.add(content.createItem(position, id,category, description, dateadded,datechanged));
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mPositionView;
        public final ImageView mImageView;
        public final TextView mCategoryView;
        public final TextView mDescriptionView;
        public CategoryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPositionView = (TextView) view.findViewById(R.id.position);
            mImageView = (ImageView) view.findViewById(R.id.image);
            mCategoryView = (TextView) view.findViewById(R.id.category);
            mDescriptionView = (TextView) view.findViewById(R.id.description);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mCategoryView.getText() + "'";
        }
    }
}
