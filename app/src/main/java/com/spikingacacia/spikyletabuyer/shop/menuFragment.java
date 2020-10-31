/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 10/10/20 10:55 PM
 */

package com.spikingacacia.spikyletabuyer.shop;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Categories;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.database.Groups;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

/**
 * A fragment representing a list of Items.
 */
public class menuFragment extends Fragment implements MymenuRecyclerViewAdapter.OptionsListener
{

    private static final String ARG_SELLER_EMAIL = "seller-email";
    private String mSellerEmail;
    private  RecyclerView recyclerViewMenu;
    public static MymenuRecyclerViewAdapter mymenuRecyclerViewAdapter;
    private OnListFragmentInteractionListener mListener;
    private String TAG = "menuF";
    private ChipGroup chipGroupCategeories;
    private ChipGroup chipGroupGroups;
    private int chipCategoryChecked;

    public menuFragment()
    {
    }

    public static menuFragment newInstance(String mSellerEmail)
    {
        menuFragment fragment = new menuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELLER_EMAIL, mSellerEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null)
        {
            mSellerEmail = getArguments().getString(ARG_SELLER_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_menu_list, container, false);
        chipGroupCategeories = view.findViewById(R.id.chip_group_category);
        chipGroupGroups = view.findViewById(R.id.chip_group_group);

        recyclerViewMenu = view.findViewById(R.id.list);
        Context context = view.getContext();
        if (getHorizontalItemCount()<=1)
        {
            recyclerViewMenu.setLayoutManager(new LinearLayoutManager(context));
        } else
        {
            recyclerViewMenu.setLayoutManager(new GridLayoutManager(context, getHorizontalItemCount()));
        }
        mymenuRecyclerViewAdapter = new MymenuRecyclerViewAdapter(mListener, getContext(), getChildFragmentManager(), this);
        recyclerViewMenu.setAdapter(mymenuRecyclerViewAdapter);
        recyclerViewMenu.addItemDecoration(new SpacesItemDecoration(20));
        chipGroupCategeories.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId)
            {
                Chip chip = chipGroupCategeories.findViewById( chipGroupCategeories.getCheckedChipId() );
                if(chip != null)
                {
                    int category_id = (int)chip.getTag();
                    mymenuRecyclerViewAdapter.filterCategory(category_id);
                    chipCategoryChecked = category_id;
                    hideAllChipsInGroup();
                   showGroupChipsInCategory(category_id);
                }
                else
                {
                    mymenuRecyclerViewAdapter.filterCategory(0);
                    hideAllChipsInGroup();
                }
            }
        });
        chipGroupGroups.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId)
            {
                Chip chip = chipGroupGroups.findViewById( chipGroupGroups.getCheckedChipId() );
                if(chip != null)
                {
                    String tag = (String) chip.getTag();
                    int category_id = Integer.parseInt(tag.split(":")[0]);
                    int group_id = Integer.parseInt(tag.split(":")[1]);
                    mymenuRecyclerViewAdapter.filterCategory(category_id, group_id);
                }
                else
                    mymenuRecyclerViewAdapter.filterCategory(chipCategoryChecked);
            }
        });
        new CategoriesTask().execute((Void)null);
        new GroupsTask().execute((Void)null);
        new MenuTask().execute((Void)null);
        return view;
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener)
        {
            mListener = (OnListFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment_menu, menu);


        // Associate searchable configuration with the SearchView
        SearchView searchView =  (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                MymenuRecyclerViewAdapter adapter=( MymenuRecyclerViewAdapter) recyclerViewMenu.getAdapter();
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                MymenuRecyclerViewAdapter adapter=( MymenuRecyclerViewAdapter) recyclerViewMenu.getAdapter();
                adapter.filter(newText);
                return true;
            }
        });

    }

    @Override
    public void onOptionsMenuChooseAccompaniments(DMenu dMenu, List<DMenu> dMenuList)
    {
        Log.d(TAG," ACCOMPANIEMTS CALLED");
        LinkedItemListDialogFragment.newInstance(dMenu,dMenuList, mListener).show(getChildFragmentManager(), "dialog");
    }


    public interface OnListFragmentInteractionListener extends Serializable
    {
        //void onEditMenu(int which, DMenu dMenu);
        void onMenuItemInteraction(List<DMenu> item, List<Integer>items_new_sizes_prices_index, List<Boolean> areItemsFree);
        //void onCategoryItemInteraction(Categories item);
    }
    private int getHorizontalItemCount()
    {
        int screenSize = getContext().getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return 3;

            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return 2;

            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return 1;

            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            default:
                return 1;
        }
    }
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            int pos = parent.getChildLayoutPosition(view);
            int items = getHorizontalItemCount();

            // Add top margin only for the first item to avoid double space between items
            if (pos < items) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }
    private void addCategoryChipLayouts(List<Categories>list)
    {
        for(int c=0; c<list.size(); c++)
        {
            Categories categories =list.get(c);
            Chip chip = new Chip(getContext());
            chip.setText(categories.getTitle());
            chip.setTag(categories.getId());
            chip.setClickable(true);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.colorButtonBackgroundTint_1);
            chip.setCheckedIconTintResource(R.color.colorIcons);
            chipGroupCategeories.addView(chip);
        }
    }
    private void addGroupChipLayouts(List<Groups>list)
    {
        for(int c=0; c<list.size(); c++)
        {
            Groups groups =list.get(c);
            Chip chip = new Chip(getContext());
            chip.setText(groups.getTitle());
            chip.setTag(groups.getCategoryId()+":"+groups.getId());
            chip.setClickable(true);
            chip.setCheckable(true);
            chip.setVisibility(View.GONE);
            chip.setChipBackgroundColorResource(R.color.colorButtonBackgroundTint_1);
            chip.setCheckedIconTintResource(R.color.colorIcons);
            chipGroupGroups.addView(chip);
        }
    }
    private void hideAllChipsInGroup()
    {
        for(int c=0; c<chipGroupGroups.getChildCount(); c++)
        {
            Chip chip_group = (Chip) chipGroupGroups.getChildAt(c);
            chip_group.setVisibility(View.GONE);

        }
    }
    private void showGroupChipsInCategory(int category_id)
    {
        for(int c=0; c<chipGroupGroups.getChildCount(); c++)
        {
            Chip chip_group = (Chip) chipGroupGroups.getChildAt(c);
            String tag = (String) chip_group.getTag();
            int categoryid = Integer.parseInt(tag.split(":")[0]);
            if(category_id == categoryid)
                chip_group.setVisibility(View.VISIBLE);
        }
    }
    private class CategoriesTask extends AsyncTask<Void, Void, Boolean>
    {
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private  JSONParser jsonParser;
        private List<Categories> list;
        @Override
        protected void onPreExecute()
        {
            jsonParser = new JSONParser();
            list = new ArrayList<>();
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("email",mSellerEmail));
            // making HTTP request
            String url_get_s_categories = base_url + "get_seller_categories.php";
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_s_categories,"POST",info);
            Log.d("sCategories",""+jsonObject.toString());
            try
            {
                JSONArray categoriesArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    categoriesArrayList=jsonObject.getJSONArray("categories");
                    for(int count=0; count<categoriesArrayList.length(); count+=1)
                    {
                        JSONObject jsonObjectNotis=categoriesArrayList.getJSONObject(count);
                        int id=jsonObjectNotis.getInt("id");
                        int id_index=jsonObjectNotis.getInt("id_index");
                        String title=jsonObjectNotis.getString("title");
                        String description=jsonObjectNotis.getString("description");
                        String image_type=jsonObjectNotis.getString("image_type");
                        String date_added=jsonObjectNotis.getString("date_added");
                        String date_changed=jsonObjectNotis.getString("date_changed");

                        Categories categories =new Categories(id, id_index, title,description,image_type,date_added,date_changed);
                        list.add(categories);
                        ShopA.categoriesLinkedHashMap.put(id,categories);
                    }
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    Log.e(TAG_MESSAGE,""+message);
                    return false;
                }
            }
            catch (JSONException e)
            {
                Log.e("JSON",""+e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean successful) {

            if (successful)
            {
                //mymenuCategoryRecyclerViewAdapter.listUpdated(list);
                addCategoryChipLayouts(list);

            }
            else
            {

            }
        }
    }
    private class GroupsTask extends AsyncTask<Void, Void, Boolean>
    {
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private  JSONParser jsonParser;
        private List<Groups> list;
        @Override
        protected void onPreExecute()
        {
            jsonParser = new JSONParser();
            list = new ArrayList<>();
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("email",mSellerEmail));
            // making HTTP request
            String url_get_s_categories = base_url + "get_seller_groups.php";
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_s_categories,"POST",info);
            try
            {
                JSONArray categoriesArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    categoriesArrayList=jsonObject.getJSONArray("groups");
                    for(int count=0; count<categoriesArrayList.length(); count+=1)
                    {
                        JSONObject jsonObjectNotis=categoriesArrayList.getJSONObject(count);
                        int id=jsonObjectNotis.getInt("id");
                        int category_id=jsonObjectNotis.getInt("category_id");
                        String title=jsonObjectNotis.getString("title");
                        String description=jsonObjectNotis.getString("description");
                        String image_type=jsonObjectNotis.getString("image_type");
                        String date_added=jsonObjectNotis.getString("date_added");
                        String date_changed=jsonObjectNotis.getString("date_changed");

                        Groups groups =new Groups(id,category_id,title,description,image_type,date_added,date_changed);
                        list.add(groups);
                        ShopA.groupsLinkedHashMap.put(id,groups);
                    }
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    Log.e(TAG_MESSAGE,""+message);
                    return false;
                }
            }
            catch (JSONException e)
            {
                Log.e("JSON",""+e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean successful) {

            if (successful)
            {
                //mymenuGroupRecyclerViewAdapter.listUpdated(list);
                addGroupChipLayouts(list);
            }
            else
            {

            }
        }
    }
    private class MenuTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_s_items = base_url + "get_seller_items.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        private List<DMenu>list;
        @Override
        protected void onPreExecute()
        {
            Log.d("SITEMS: ","starting....");
            jsonParser = new JSONParser();
            list = new ArrayList<>();
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("email",mSellerEmail));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_s_items,"POST",info);
            Log.d("sItems",""+jsonObject.toString());
            try
            {
                JSONArray itemsArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    itemsArrayList=jsonObject.getJSONArray("items");
                    for(int count=0; count<itemsArrayList.length(); count+=1)
                    {
                        JSONObject jsonObjectNotis=itemsArrayList.getJSONObject(count);
                        int id=jsonObjectNotis.getInt("id");
                        int category_id=jsonObjectNotis.getInt("category_id");
                        int group_id;
                        try
                        {
                            group_id=jsonObjectNotis.getInt("group_id");
                        }
                        catch (Exception e)
                        {
                            group_id = -1;
                        }
                        String linked_items = jsonObjectNotis.getString("linked_items");
                        String linked_items_price = jsonObjectNotis.getString("linked_items_price");
                        String item=jsonObjectNotis.getString("item");
                        String description=jsonObjectNotis.getString("description");
                        String sizes = jsonObjectNotis.getString("sizes");
                        String prices = jsonObjectNotis.getString("prices");
                        String image_type=jsonObjectNotis.getString("image_type");
                        boolean available = jsonObjectNotis.getInt("available") == 1;
                        String date_added=jsonObjectNotis.getString("date_added");
                        String date_changed=jsonObjectNotis.getString("date_changed");

                        DMenu dMenu =new DMenu(id,category_id,group_id,linked_items, linked_items_price,item,description,sizes, prices,image_type,available,date_added,date_changed);
                        list.add(dMenu);
                        ShopA.putIntoMenu(id,dMenu);
                    }
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    Log.e(TAG_MESSAGE,""+message);
                    return false;
                }
            }
            catch (JSONException e)
            {
                Log.e("JSON",""+e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean successful) {


            if (successful)
            {
                mymenuRecyclerViewAdapter.listUpdated(list);
            }
            else
            {

            }
        }
    }

}