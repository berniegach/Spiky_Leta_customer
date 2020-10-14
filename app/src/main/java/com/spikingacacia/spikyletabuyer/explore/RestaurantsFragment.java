/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 9/3/20 8:47 PM
 */

package com.spikingacacia.spikyletabuyer.explore;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.spikingacacia.spikyletabuyer.BuildConfig;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.main.orders_list.OrdersFragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

/**
 * A fragment representing a list of Items.
 */
public class RestaurantsFragment extends Fragment
{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private MyRestaurantsRecyclerViewAdapter myRestaurantsRecyclerViewAdapter;
    boolean isLoading = false;
    private int lastOrderId = 0;
    private boolean refreshList = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RestaurantsFragment()
    {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static RestaurantsFragment newInstance(int columnCount)
    {
        RestaurantsFragment fragment = new RestaurantsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_explore_restaurants_list, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_container);
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new SpacesItemDecoration(20));
        myRestaurantsRecyclerViewAdapter = new MyRestaurantsRecyclerViewAdapter(mListener,getContext());
        recyclerView.setAdapter(myRestaurantsRecyclerViewAdapter);
        initScrollListener();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                refreshList();
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        refreshList();
        return view;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onItemClicked(Restaurants item);
    }
    private int getHorizontalItemCount()
    {
        int screenSize = getContext().getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return 4;

            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return 3;

            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return 2;

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
    private void initScrollListener()
    {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading)
                {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == myRestaurantsRecyclerViewAdapter.getItemCount() - 1) {
                        //bottom of list!
                        loadMore();
                        isLoading = true;
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });


    }
    private void loadMore() {
        myRestaurantsRecyclerViewAdapter.listAddProgressBar();
        try
        {
            String[] loc = MainActivity.myLocation.split(":");
            new RestaurantsTask(lastOrderId, true,String.valueOf(loc[0]),String.valueOf(loc[1]),"null").execute((Void)null);

        }
        catch (Exception e)
        {
            Toast.makeText(getContext(),"Location unavailable",Toast.LENGTH_SHORT);
        }

    }
    private void refreshList()
    {
        refreshList = true;
        try
        {
            String[] loc = MainActivity.myLocation.split(":");
            new RestaurantsTask(-1, false,String.valueOf(loc[0]),String.valueOf(loc[1]),"null").execute((Void)null);

        }
        catch (Exception e)
        {
            Toast.makeText(getContext(),"Location unavailable",Toast.LENGTH_SHORT);
        }
    }
    private class RestaurantsTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_restaurants=base_url+"get_near_restaurants_1.php";
        private JSONParser jsonParser;
        private int last_index;
        private boolean load_more;
        final private String latitude;
        final private String longitude;
        final private String location;
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private List<Restaurants> list;

        public RestaurantsTask( int last_index, boolean load_more, String latitude, String longitude, String location)
        {
            jsonParser = new JSONParser();
            this.last_index = last_index;
            this.load_more = load_more;
            this.latitude=latitude;
            this.longitude=longitude;
            this.location=location;
            list = new LinkedList<>();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            Log.e("LAST","LAST "+last_index);
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("last_index",String.valueOf(last_index)));
            info.add(new BasicNameValuePair("latitude",latitude));
            info.add(new BasicNameValuePair("longitude",longitude));
            info.add(new BasicNameValuePair("location",location));
            info.add(new BasicNameValuePair("which","2"));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_restaurants,"POST",info);
            Log.d("cTasks",""+jsonObject.toString());
            try
            {
                JSONArray restArrayList=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    restArrayList=jsonObject.getJSONArray("restaurants");
                    restArrayList=restArrayList.getJSONArray(0);
                    for(int count=0; count<restArrayList.length(); count+=1)
                    {
                        JSONObject jsonObject_restaurants=restArrayList.getJSONObject(count);
                        int id=jsonObject_restaurants.getInt("id");
                        if (!BuildConfig.DEBUG && id == 10)
                        {
                            // do not show the testing restaurant which is spiky leta
                            continue;
                        }
                        String email = jsonObject_restaurants.getString("email");
                        String names=jsonObject_restaurants.getString("username");
                        double distance=jsonObject_restaurants.getDouble("distance");
                        double latitude=jsonObject_restaurants.getDouble("latitude");
                        double longitude=jsonObject_restaurants.getDouble("longitude");
                        String locality=jsonObject_restaurants.getString("locality");
                        String country_code = jsonObject_restaurants.getString("country_code");
                        int order_radius=jsonObject_restaurants.getInt("order_radius");
                        int number_of_tables=jsonObject_restaurants.getInt("number_of_tables");
                        String image_type=jsonObject_restaurants.getString("image_type");
                        int table_number = jsonObject_restaurants.getInt("table_number");
                        String m_code = jsonObject_restaurants.getString("m_code");
                        String dining_options = jsonObject_restaurants.getString("dining_options");


                        Restaurants restaurants =new Restaurants(id,email,names,distance,latitude,longitude,locality,country_code, order_radius, number_of_tables, image_type, table_number, m_code, dining_options);
                        list.add(restaurants);
                        if(lastOrderId<id)
                            lastOrderId = id;
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
        protected void onPostExecute(final Boolean successful)
        {

            if(load_more)
            {
                myRestaurantsRecyclerViewAdapter.listRemoveProgressBar();
                isLoading = false;
            }
            if (successful)
            {
                if(load_more)
                {
                    myRestaurantsRecyclerViewAdapter.listAddItems(list);
                }
                else
                {
                    myRestaurantsRecyclerViewAdapter.listUpdated(list);
                    if (refreshList)
                    {
                        refreshList = false;
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }


            }
        }
    }
}