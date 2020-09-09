package com.spikingacacia.spikyletabuyer.main.orders_list;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.database.TastyBoard;
import com.spikingacacia.spikyletabuyer.main.tasty.TastyBoardFragment;
import com.spikingacacia.spikyletabuyer.orders.MyOrderRecyclerViewAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class OrdersFragment extends Fragment
{
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_WHICH_ORDER = "which-order";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private int mWhichOrder=0;
    private static RecyclerView recyclerView;
    public static LinkedHashMap<Integer,Orders> ordersLinkedHashMap;
    private static MyOrderRecyclerViewAdapter myOrderRecyclerViewAdapter;
    private String TAG = "orders_f";
    boolean isLoading = false;
    private int lastOrderId = 0;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrdersFragment()
    {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static OrdersFragment newInstance(int columnCount, int whichOrder)
    {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_WHICH_ORDER,whichOrder);
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
            mWhichOrder = getArguments().getInt(ARG_WHICH_ORDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_orders_list, container, false);
        ordersLinkedHashMap = new LinkedHashMap<>();
        Context context = view.getContext();


        // Set the adapter
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        myOrderRecyclerViewAdapter = new MyOrderRecyclerViewAdapter(mListener, getContext());
        recyclerView.setAdapter(myOrderRecyclerViewAdapter);
        initScrollListener();
        Button b_unfinished = view.findViewById(R.id.unfinished);
        Button b_all = view.findViewById(R.id.all);
        b_unfinished.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.black)));
                }
                ((Button)v).setTextColor(getResources().getColor(android.R.color.white));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    b_all.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white)));
                }
                b_all.setTextColor(getResources().getColor(android.R.color.black));
                myOrderRecyclerViewAdapter.filter(1);
            }
        });
        b_all.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.black)));
                }
                ((Button)v).setTextColor(getResources().getColor(android.R.color.white));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    b_unfinished.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white)));
                }
                b_unfinished.setTextColor(getResources().getColor(android.R.color.black));
                myOrderRecyclerViewAdapter.filter(0);
            }
        });
        //getOrders();
        new OrdersTask(-1, false).execute((Void)null);
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
    public interface OnListFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onOrderClicked(Orders item);
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
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == myOrderRecyclerViewAdapter.getItemCount() - 1) {
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
        myOrderRecyclerViewAdapter.listAddProgressBar();
        new OrdersTask(lastOrderId, true).execute((Void)null);

    }
    private class OrdersTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_b_orders = base_url + "get_buyer_orders_1.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        private List<Orders> list;
        private LinkedHashMap<String,Orders> uniqueOrderLinkedHashMap;
        private int last_index;
        private boolean load_more;
        @Override
        protected void onPreExecute()
        {
            jsonParser = new JSONParser();
            Log.d("BORDERS: ","starting....");
            list = new LinkedList<>();
            uniqueOrderLinkedHashMap = new LinkedHashMap<>();
            //ordersLinkedHashMap.clear();
            super.onPreExecute();
        }
        public OrdersTask(int last_index, boolean load_more)
        {
            this.last_index = last_index;
            this.load_more = load_more;
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("last_index",String.valueOf(last_index)));
            info.add(new BasicNameValuePair("email", LoginA.getServerAccount().getEmail()));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_b_orders,"POST",info);
            //Log.d("sItems",""+jsonObject.toString());
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
                        String waiter_email = jsonObjectNotis.getString("waiter_email");
                        int item_id=jsonObjectNotis.getInt("item_id");
                        int order_number=jsonObjectNotis.getInt("order_number");
                        int order_status=jsonObjectNotis.getInt("order_status");
                        String url_code_start_delivery = jsonObjectNotis.getString("url_code_start_delivery");
                        String url_code_end_delivery = jsonObjectNotis.getString("url_code_end_delivery");
                        String date_added=jsonObjectNotis.getString("date_added");
                        String date_changed=jsonObjectNotis.getString("date_changed");
                        String date_added_local=jsonObjectNotis.getString("date_added_local");
                        String item=jsonObjectNotis.getString("item");
                        String size = jsonObjectNotis.getString("size");
                        double selling_price=jsonObjectNotis.getDouble("price");
                        int seller_id = jsonObjectNotis.getInt("seller_id");
                        String seller_email = jsonObjectNotis.getString("seller_email");
                        String seller_image_type = jsonObjectNotis.getString("seller_image_type");
                        String username=jsonObjectNotis.getString("username");
                        int order_format = jsonObjectNotis.getInt("order_format");
                        String waiter_names=jsonObjectNotis.getString("waiter_names");
                        int table_number=jsonObjectNotis.getInt("table_number");
                        int pre_order = jsonObjectNotis.getInt("pre_order");
                        String collect_time = jsonObjectNotis.getString("collect_time");
                        int order_type = jsonObjectNotis.getInt("order_type");
                        String mpesa_message = jsonObjectNotis.getString("m_message");

                        Orders orders =new Orders(id,waiter_email,item_id,order_number,order_status,url_code_start_delivery, url_code_end_delivery,date_added,date_changed,date_added_local,item,size,selling_price,seller_id, seller_email,seller_image_type,
                                username,waiter_names,order_format,table_number, pre_order, collect_time, order_type, mpesa_message);
                        list.add( orders);
                        ordersLinkedHashMap.put(id,orders);
                        String[] date_pieces=date_added.split(" ");
                        String unique_name=date_pieces[0]+":"+order_number+":"+order_status;
                        uniqueOrderLinkedHashMap.put(unique_name,orders);
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
                myOrderRecyclerViewAdapter.listRemoveProgressBar();
                isLoading = false;
            }
            if (successful)
            {
                if(load_more)
                {
                    myOrderRecyclerViewAdapter.listAddItems(list);
                }
                else
                {
                    myOrderRecyclerViewAdapter.listUpdated(list);
                }


            }

            /*if (successful)
            {
                List<Orders> unique_order= new ArrayList<>();
                Iterator iterator = uniqueOrderLinkedHashMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                    LinkedHashMap.Entry<String, Orders> value = ( LinkedHashMap.Entry<String, Orders>) iterator.next();
                    unique_order.add(value.getValue());
                }
                checkIfOrderChanged(unique_order);

            }*/

        }
    }
    private void checkIfOrderChanged(List<Orders> unique_order)
    {
        //check size
        if(myOrderRecyclerViewAdapter.getItemCount()!=unique_order.size())
        {
            myOrderRecyclerViewAdapter.listUpdated(unique_order);
            recyclerView.scrollToPosition(myOrderRecyclerViewAdapter.getItemCount()-1);
        }

    }

}
