/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 9/29/20 10:37 AM
 */

package com.spikingacacia.spikyletabuyer.shop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.spikingacacia.spikyletabuyer.util.Utils;


import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.util.Utils.getCurrencyCode;


public class MymenuRecyclerViewAdapter extends RecyclerView.Adapter<MymenuRecyclerViewAdapter.ViewHolder>
{
    private List<DMenu> mValues;
    private List<DMenu>itemsCopy;
    private final OnListFragmentInteractionListener mListener;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private Context context;
    private FragmentManager fragmentManager;
    private static int lastImageFaded = -1;
    OptionsListener optionsListener;
    private String TAG = "my_menu_rva";


    public interface OptionsListener
    {
        void onOptionsMenuChooseAccompaniments(final DMenu dMenu, List<DMenu> dMenuList);
    }

    public MymenuRecyclerViewAdapter(OnListFragmentInteractionListener listener, Context context, FragmentManager fragmentManager, OptionsListener optionsListener)
    {
        mListener = listener;
        mValues = new LinkedList<>();
        itemsCopy = new LinkedList<>();
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.optionsListener = optionsListener;
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
        /////
        String[] sizes = mValues.get(position).getSizes().split(":");
        String[] prices = mValues.get(position).getPrices().split(":");
        String[] sizePrice;
        String location = MainActivity.myLocation;
        String[] location_pieces = location.split(":");
        if(sizes.length == 1)
        {
            if(location_pieces.length==3)
                sizePrice = new String[]{getCurrencyCode(location_pieces[2])+" "+prices[0]};
            else
                sizePrice = new String[]{prices[0]};
        }
        else
        {
            sizePrice = new String[sizes.length];
            for(int c=0; c<sizes.length; c++)
            {

                if(location_pieces.length==3)
                    sizePrice[c] = sizes[c]+" @ "+getCurrencyCode(location_pieces[2])+" "+prices[c];
                else
                    sizePrice[c] = sizes[c]+" @ "+prices[c];
            }
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, sizePrice);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.mPriceView.setAdapter(adapter);
        holder.mPriceView.setSelection(0);
        /////
        if(holder.mItem.isAvailable())
        {
            holder.mAddButton.setVisibility(View.VISIBLE);
            holder.mUnvailableButton.setVisibility(View.GONE);
        }
        else
        {
            holder.mAddButton.setVisibility(View.GONE);
            holder.mUnvailableButton.setVisibility(View.VISIBLE);
        }
        holder.mAddButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //first choose accompaniment
                chooseLinkedFood(holder.mItem, holder.mPriceView.getSelectedItemPosition());
            }
        });

        // image
        final String url=image_url+String.valueOf(mValues.get(position).getId())+'_'+String.valueOf(mValues.get(position).getImageType());
        Glide.with(context).load(url).into(holder.image);
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
        public final Spinner mPriceView;
        public final ImageButton mUnvailableButton;
        public final ImageButton mAddButton;
        public DMenu mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            image = view.findViewById(R.id.image);
            mItemView = (TextView) view.findViewById(R.id.item);
            mDescriptionView = view.findViewById(R.id.description);
            mPriceView = view.findViewById(R.id.price);
            mUnvailableButton = view.findViewById(R.id.unavailable);
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
    public void filterCategory(int category_id, int group_id)
    {
        mValues.clear();
        if(category_id == 0)
            mValues.addAll(itemsCopy);
        else
        {
            for(DMenu item:itemsCopy)
            {
                if(item.getCategoryId() == category_id && item.getGroupId() == group_id)
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
    private void chooseLinkedFood(final DMenu dMenu, int main_item_size)
    {
        //when chhosing linked food don't use mValues data . Use the itemscopy
        //mvalues data may reflect less items if the user had filtered the menu
        //items copy retains all menu info no matter what
        final List<DMenu> dMenuList = new ArrayList<>();
        final List<Integer>items_new_sizes_prices_index = new ArrayList<>();
        String linked_foods = dMenu.getLinkedItems();
        String[] links = linked_foods.split(":");
        String[] items = new String[itemsCopy.size()];
        final String[] ids = new String[itemsCopy.size()];
        final boolean[] items_checked = new boolean[itemsCopy.size()];

        if(links.length==1 && links[0].contentEquals("null"))
            links[0]="-1";
        else if(links.length==1 && links[0].contentEquals(""))
            links[0]="-1";
        int itemsCount = 0;
        for(int c=0; c<items.length; c++)
        {
            items[c] = itemsCopy.get(c).getItem();
            ids[c] = String.valueOf(itemsCopy.get(c).getId());
            //set the linked item to true
            for( int d=0; d<links.length; d++)
            {
                int id = Integer.valueOf(links[d]);
                for(int e=0; e<itemsCopy.size(); e++)
                {
                    if( id==itemsCopy.get(c).getId())
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
        final boolean[] items_new_free = new boolean[itemsCount];
        int index = 0;
        for(int c=0; c<items_checked.length; c++)
        {
            if(items_checked[c])
            {
                items_new[index]= itemsCopy.get(c).getItem();
                dmenu_new[index] = itemsCopy.get(c);
                index+=1;

            }
        }
        if(items_new.length==0)
        {
            dMenuList.add(dMenu);
            items_new_sizes_prices_index.add(main_item_size);
            List<Boolean> areItemsFree = new ArrayList<>();
            areItemsFree.add(false);
            mListener.onMenuItemInteraction(dMenuList, items_new_sizes_prices_index , areItemsFree);
        }
        else
        {
            if(optionsListener!=null)
                optionsListener.onOptionsMenuChooseAccompaniments(dMenu,  itemsCopy);
        }


    }
}