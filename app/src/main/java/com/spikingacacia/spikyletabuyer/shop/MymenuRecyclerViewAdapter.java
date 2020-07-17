package com.spikingacacia.spikyletabuyer.shop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.spikingacacia.spikyletabuyer.AppController;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.shop.menuFragment.OnListFragmentInteractionListener;


import java.util.LinkedList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;


public class MymenuRecyclerViewAdapter extends RecyclerView.Adapter<MymenuRecyclerViewAdapter.ViewHolder>
{
    private List<DMenu> mValues;
    private List<DMenu>itemsCopy;
    private final OnListFragmentInteractionListener mListener;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private Context context;
    private FragmentManager fragmentManager;
    private static int lastImageFaded = -1;

    public MymenuRecyclerViewAdapter(OnListFragmentInteractionListener listener, Context context, FragmentManager fragmentManager)
    {
        mListener = listener;
        mValues = new LinkedList<>();
        itemsCopy = new LinkedList<>();
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        String image_url= base_url+"src/items_pics/";
        holder.mItem = mValues.get(position);
        holder.mItemView.setText(mValues.get(position).getItem());
        holder.mDescriptionView.setText(mValues.get(position).getDescription());
        String[] sizes = mValues.get(position).getSizes().split(":");
        String[] prices = mValues.get(position).getPrices().split(":");
        String sizePrice="";
        for(int c=0; c<sizes.length; c++)
        {
            sizePrice+=" "+sizes[c]+" @ "+prices[c];
        }
        holder.mPriceView.setText(sizePrice);
        holder.mAddButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onMenuItemInteraction(holder.mItem);
            }
        });

        // image
        String url=image_url+String.valueOf(mValues.get(position).getId())+'_'+String.valueOf(mValues.get(position).getImageType());
        holder.image.setImageUrl(url, imageLoader);
        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(holder.image.getImageAlpha()==20)
                {
                    holder.image.setImageAlpha(255);
                   // holder.mItemView.setAlpha((float)0.4);
                    holder.mDescriptionView.setAlpha((float)0.0);
                }
                else
                {
                    holder.image.setImageAlpha(20);
                    //holder.mItemView.setAlpha((float)1.0);
                    holder.mDescriptionView.setAlpha((float)1.0);
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
        public final NetworkImageView image;
        public final TextView mItemView;
        public final TextView mDescriptionView;
        public final TextView mPriceView;
        public final ImageButton mAddButton;
        public DMenu mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            image = view.findViewById(R.id.image);
            mItemView = (TextView) view.findViewById(R.id.item);
            mDescriptionView = view.findViewById(R.id.description);
            mPriceView = (TextView) view.findViewById(R.id.price);
            mAddButton = view.findViewById(R.id.add);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + mItemView.getText() + "'";
        }
    }
    public void filter(String text)
    {
        mValues.clear();
        if(text.isEmpty())
            mValues.addAll(itemsCopy);
        else
        {
            text=text.toLowerCase();
            for(DMenu item:itemsCopy)
            {
                if(item.getItem().toLowerCase().contains(text))
                    mValues.add(item);
            }
        }
        notifyDataSetChanged();
    }
    public void listUpdated(List<DMenu> newitems)
    {
        mValues.clear();
        mValues.addAll(newitems);
        itemsCopy.addAll(newitems);
        notifyDataSetChanged();
    }
}