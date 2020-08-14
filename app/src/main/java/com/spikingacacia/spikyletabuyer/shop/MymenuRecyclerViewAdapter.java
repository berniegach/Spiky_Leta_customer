package com.spikingacacia.spikyletabuyer.shop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.spikingacacia.spikyletabuyer.AppController;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.shop.menuFragment.OnListFragmentInteractionListener;


import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
            String location = MainActivity.myLocation;
            String[] location_pieces = location.split(":");
            if(location_pieces.length==3)
                sizePrice+=" "+sizes[c]+" @ "+getCurrencyCode(location_pieces[2])+" "+prices[c];
            else
                sizePrice+=" "+sizes[c]+" @ "+prices[c];
        }
        holder.mPriceView.setText(sizePrice);
        holder.mAddButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //first choose accompaniment
                chooseLinkedFood(holder.mItem);
                //mListener.onMenuItemInteraction(holder.mItem);
            }
        });

        // image
        final String url=image_url+String.valueOf(mValues.get(position).getId())+'_'+String.valueOf(mValues.get(position).getImageType());
        Glide.with(context).load(url).into(holder.image);
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
    //to retrieve currency code
    private String getCurrencyCode(String countryCode) {
        return Currency.getInstance(new Locale("", countryCode)).getCurrencyCode();
    }

    //to retrieve currency symbol
    private String getCurrencySymbol(String countryCode) {
        return Currency.getInstance(new Locale("", countryCode)).getSymbol();
    }

    @Override
    public int getItemCount()
    {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final ImageView image;
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
    public void filterCategory(int category_id)
    {
        mValues.clear();
        if(category_id == 0)
            mValues.addAll(itemsCopy);
        else
        {
            for(DMenu item:itemsCopy)
            {
                if(item.getCategoryId() == category_id)
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
    private void chooseLinkedFood(final DMenu dMenu)
    {
        final List<DMenu> dMenuList = new ArrayList<>();
        String linked_foods = dMenu.getLinkedItems();
        String[] links = linked_foods.split(":");
        String[] items = new String[mValues.size()];
        final String[] ids = new String[mValues.size()];
        final boolean[] items_checked = new boolean[mValues.size()];

        if(links.length==1 && links[0].contentEquals("null"))
            links[0]="-1";
        else if(links.length==1 && links[0].contentEquals(""))
            links[0]="-1";
        int itemsCount = 0;
        for(int c=0; c<items.length; c++)
        {
            items[c] = mValues.get(c).getItem();
            ids[c] = String.valueOf(mValues.get(c).getId());
            //set the linked item to true
            for( int d=0; d<links.length; d++)
            {
                int id = Integer.valueOf(links[d]);
                for(int e=0; e<mValues.size(); e++)
                {
                    if( id==mValues.get(c).getId())
                    {
                        items_checked[c]=true;
                        itemsCount+=1;
                        break;
                    }
                }
            }
        }
        //form now a new list but with only linked items
        String[] items_new = new String[itemsCount];
        final DMenu[] dmenu_new = new DMenu[itemsCount];
        final boolean[] items_checked_new = new boolean[itemsCount];
        int index = 0;
        for(int c=0; c<items_checked.length; c++)
        {
            if(items_checked[c])
            {
                items_new[index]= mValues.get(c).getItem();
                dmenu_new[index] = mValues.get(c);
                index+=1;

            }
        }
        if(items_new.length==0)
        {
            dMenuList.add(dMenu);
            mListener.onMenuItemInteraction(dMenuList);
        }
        else
        {
            new AlertDialog.Builder(context)
                    .setTitle("Accompaniments")
                    .setMultiChoiceItems(items_new, items_checked_new, new DialogInterface.OnMultiChoiceClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked)
                        {
                            items_checked_new[which] = isChecked;
                        }
                    })
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            for(int c=0; c<items_checked_new.length; c++)
                            {
                                if(items_checked_new[c])
                                    dMenuList.add(dmenu_new[c]);
                            }
                            dMenuList.add(dMenu);
                            mListener.onMenuItemInteraction(dMenuList);
                        }
                    })
                    .setCancelable(false)
                    .create().show();
        }


    }
}