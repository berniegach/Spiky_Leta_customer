package com.spikingacacia.spikyletabuyer.main.orders_list;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Orders;
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

import static com.spikingacacia.spikyletabuyer.LoginA.bMessagesList;
import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.LoginA.serverAccount;

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
    private Thread ordersThread;

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
        // Set the adapter
        if (view instanceof RecyclerView)
        {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            myOrderRecyclerViewAdapter = new MyOrderRecyclerViewAdapter(mListener);
            recyclerView.setAdapter(myOrderRecyclerViewAdapter);
        }
        return view;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        getOrders();
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
        ordersThread.interrupt();
        mListener = null;
    }
    public interface OnListFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onOrderClicked(Orders item);
    }
    private void getOrders()
    {
        ordersThread=new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    while(true)
                    {
                        new OrdersTask().execute((Void)null);
                        sleep(5000);
                    }
                }
                catch (InterruptedException e)
                {
                    //Log.e(TAG,"order thread quit");
                }
            }
        };
        ordersThread.start();
    }
    /**
     * Following code will get the buyers orders
     * The returned infos are id, itemId, orderNumber, orderStatus, orderName, price, dateAdded,
     * * Arguments are:
     * id==boss id.
     * Returns are:
     * success==1 successful get
     * success==0 for id argument missing
     **/
    private class OrdersTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_b_orders = base_url + "get_buyer_orders.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        private List<Orders> list;
        private LinkedHashMap<String,Orders> uniqueOrderLinkedHashMap;
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
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("email",serverAccount.getEmail()));
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


                        String date_added=jsonObjectNotis.getString("date_added");
                        String date_changed=jsonObjectNotis.getString("date_changed");
                        String item=jsonObjectNotis.getString("item");
                        String size = jsonObjectNotis.getString("size");
                        double selling_price=jsonObjectNotis.getDouble("price");
                        int seller_id = jsonObjectNotis.getInt("seller_id");
                        String seller_image_type = jsonObjectNotis.getString("seller_image_type");
                        String username=jsonObjectNotis.getString("username");
                        int order_format = jsonObjectNotis.getInt("order_format");
                        String waiter_names=jsonObjectNotis.getString("waiter_names");
                        int table_number=jsonObjectNotis.getInt("table_number");
                        int pre_order = jsonObjectNotis.getInt("pre_order");
                        String collect_time = jsonObjectNotis.getString("collect_time");

                        Orders orders =new Orders(id,waiter_email,item_id,order_number,order_status,date_added,date_changed,item,size,selling_price,seller_id, seller_image_type,
                                username,waiter_names,order_format,table_number, pre_order, collect_time);
                        list.add( orders);
                        ordersLinkedHashMap.put(id,orders);
                        String[] date_pieces=date_added.split(" ");
                        String unique_name=date_pieces[0]+":"+order_number+":"+order_status;
                        uniqueOrderLinkedHashMap.put(unique_name,orders);
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
                List<Orders> unique_order= new ArrayList<>();
                Iterator iterator = uniqueOrderLinkedHashMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                    LinkedHashMap.Entry<String, Orders> value = ( LinkedHashMap.Entry<String, Orders>) iterator.next();
                    unique_order.add(value.getValue());
                }
                checkIfOrderChanged(unique_order);

            }
            else
            {

            }
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
