package com.spikingacacia.spikyletabuyer.shop;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.shop.menuFragment.OnListFragmentInteractionListener;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.util.Utils.getCurrencyCode;


/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     LinkedItemListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class LinkedItemListDialogFragment extends BottomSheetDialogFragment
{

    private static final String ARG_MENU_ITEM = "arg1";
    private static final String ARG_MENU_INDEX = "arg2";
    private static final String ARG_MENU_LIST = "arg3";
    private static final String ARG_LISTENER = "arg4";
    private DMenu dMenu;
    private int menu_index;
    private List<DMenu> dMenuList;
    RecyclerView recyclerView;
    private boolean[] items_checked_new;
    private String[] items_new;
    private DMenu[] dmenu_new;
    private Integer[] items_new_sizes_prices_index;
    public interface UpdateListener extends Serializable
    {
        void onLinkedItemUpdateDone(int menu_id, String linked_items);
    }
    private  OnListFragmentInteractionListener mListener;
    public static LinkedItemListDialogFragment newInstance(DMenu dMenu,  List<DMenu> dMenuList, OnListFragmentInteractionListener listener)
    {
        final LinkedItemListDialogFragment fragment = new LinkedItemListDialogFragment();
        final Bundle args = new Bundle();
        args.putSerializable(ARG_MENU_ITEM,dMenu);
        args.putSerializable(ARG_MENU_LIST, (Serializable) dMenuList);
        args.putSerializable(ARG_LISTENER,listener);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {

        return inflater.inflate(R.layout.fragment_item_list_dialog_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        dMenu = (DMenu) getArguments().getSerializable(ARG_MENU_ITEM);
        dMenuList = (List<DMenu>) getArguments().getSerializable(ARG_MENU_LIST);
        mListener = (OnListFragmentInteractionListener) getArguments().getSerializable(ARG_LISTENER);
        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new LinkedItemAdapter(getArguments().getInt(ARG_MENU_INDEX)));
        TextView t_title = view.findViewById(R.id.title);
        t_title.setText(dMenu.getItem());
        Spinner spinner = view.findViewById(R.id.spinner);
        Button b_add = view.findViewById(R.id.b_add);
        b_add.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                List<DMenu> checked = new ArrayList<>();
                List<Integer> checkedPrices = new ArrayList<>();
                for(int c=0; c<items_checked_new.length; c++)
                {
                    if(items_checked_new[c])
                    {
                        checked.add(dmenu_new[c]);
                        checkedPrices.add(items_new_sizes_prices_index[c]);
                    }
                }
                checked.add(dMenu);
                checkedPrices.add(spinner.getSelectedItemPosition());
                mListener.onMenuItemInteraction(checked, checkedPrices);
                dismiss();
            }
        });
        //sizes and prices
        /////
        String[] sizes = dMenu.getSizes().split(":");
        String[] prices = dMenu.getPrices().split(":");
        String[] sizePrice;
        String location = MainActivity.myLocation;
        String[] location_pieces = location.split(",");
        if(sizes.length == 1)
        {
            if(location_pieces.length==3)
                sizePrice = new String[]{getCurrencyCode(location_pieces[3])+" "+prices[0]};
            else
                sizePrice = new String[]{prices[0]};
        }
        else
        {
            sizePrice = new String[sizes.length];
            for(int c=0; c<sizes.length; c++)
            {

                if(location_pieces.length==4)
                    sizePrice[c] = sizes[c]+" @ "+getCurrencyCode(location_pieces[3])+" "+prices[c];
                else
                    sizePrice[c] = sizes[c]+" @ "+prices[c];
            }
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, sizePrice);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
    }


    private class ViewHolder extends RecyclerView.ViewHolder
    {

        final CheckBox checkBox;
        final Spinner spinner;

        ViewHolder(LayoutInflater inflater, ViewGroup parent)
        {
            super(inflater.inflate(R.layout.fragment_item_list_dialog_list_dialog_item, parent, false));
            checkBox = itemView.findViewById(R.id.checkbox);
            spinner = itemView.findViewById(R.id.spinner);
        }
    }

    private class LinkedItemAdapter extends RecyclerView.Adapter<ViewHolder>
    {

        private final int mItemCount;

        LinkedItemAdapter(int itemCount)
        {
            mItemCount = itemCount;

            //final List<DMenu> dMenuList = new ArrayList<>();
            String linked_foods = dMenu.getLinkedItems();
            String[] links = linked_foods.split(":");
            String[] items = new String[dMenuList.size()];
            String[] ids = new String[dMenuList.size()];
            boolean[] items_checked = new boolean[dMenuList.size()];


            if(links.length==1 && links[0].contentEquals("null"))
                links[0]="-1";
            else if(links.length==1 && links[0].contentEquals(""))
                links[0]="-1";
            int itemsCount = 0;
            for(int c = 0; c< items.length; c++)
            {
                items[c] = dMenuList.get(c).getItem();
                ids[c] = String.valueOf(dMenuList.get(c).getId());
                //set the linked item to true
                for( int d=0; d<links.length; d++)
                {
                    int id = Integer.valueOf(links[d]);
                    for(int e=0; e<dMenuList.size(); e++)
                    {
                        if( id==dMenuList.get(c).getId())
                        {
                            items_checked[c]=true;
                            itemsCount+=1;
                            break;
                        }
                    }
                }
            }
            //form now a new list but with only linked items
            items_new = new String[itemsCount];
            dmenu_new = new DMenu[itemsCount];
            items_checked_new = new boolean[itemsCount];
            items_new_sizes_prices_index = new Integer[itemsCount];
            int index = 0;
            for(int c = 0; c< items_checked.length; c++)
            {
                if(items_checked[c])
                {
                    items_new[index]= dMenuList.get(c).getItem();
                    dmenu_new[index] = dMenuList.get(c);
                    index+=1;

                }
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {
            holder.checkBox.setText(items_new[position]);
            //holder.checkBox.setChecked(items_checked[position]);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    items_checked_new[position] = isChecked;
                }
            });
            //sizes and prices
            /////
            String[] sizes = dmenu_new[position].getSizes().split(":");
            String[] prices =  dmenu_new[position].getPrices().split(":");
            String[] sizePrice;
            String location = MainActivity.myLocation;
            String[] location_pieces = location.split(",");
            if(sizes.length == 1)
            {
                if(location_pieces.length==4)
                    sizePrice = new String[]{getCurrencyCode(location_pieces[3])+" "+prices[0]};
                else
                    sizePrice = new String[]{prices[0]};
            }
            else
            {
                sizePrice = new String[sizes.length];
                for(int c=0; c<sizes.length; c++)
                {

                    if(location_pieces.length==4)
                        sizePrice[c] = sizes[c]+" @ "+getCurrencyCode(location_pieces[3])+" "+prices[c];
                    else
                        sizePrice[c] = sizes[c]+" @ "+prices[c];
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, sizePrice);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinner.setAdapter(adapter);
            holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    items_new_sizes_prices_index[position] = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                    items_new_sizes_prices_index[position] = 0;
                }
            });
            holder.spinner.setSelection(0);
        }

        @Override
        public int getItemCount()
        {
            return items_new.length;
        }

    }

}