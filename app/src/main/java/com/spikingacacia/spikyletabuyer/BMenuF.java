package com.spikingacacia.spikyletabuyer;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.database.BMessages;
import com.spikingacacia.spikyletabuyer.database.BOrders;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static com.spikingacacia.spikyletabuyer.LoginA.bMessagesList;
import static com.spikingacacia.spikyletabuyer.LoginA.bOrdersList;
import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.LoginA.buyerAccount;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BMenuF.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BMenuF#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BMenuF extends Fragment {
    private String url_get_b_orders=base_url+"get_buyer_orders.php";
    private String url_get_b_notifications=base_url+"get_buyer_notifications.php";
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private String TAG="BMenuF";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private JSONParser jsonParser;
    private TextView tOrdersCount;
    private TextView tMessagesCount;
    private int ordersCount=0;
    private int messagesCount=0;
    Preferences preferences;

    public BMenuF() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BMenuF.
     */
    // TODO: Rename and change types and number of parameters
    public static BMenuF newInstance(String param1, String param2) {
        BMenuF fragment = new BMenuF();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        jsonParser=new JSONParser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.f_bmenu, container, false);
        //preference
        preferences=new Preferences(getContext());
        tOrdersCount=view.findViewById(R.id.orders_count);
        tMessagesCount=view.findViewById(R.id.messages_count);
        //restaurant
        ((LinearLayout)view.findViewById(R.id.restaurant)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mListener!=null)
                    mListener.onMenuClicked(1);
            }
        });
        //orders
        ((LinearLayout)view.findViewById(R.id.orders)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mListener!=null)
                    mListener.onMenuClicked(2);
            }
        });
        //explore
        ((LinearLayout)view.findViewById(R.id.explore)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mListener!=null)
                    mListener.onMenuClicked(3);

            }
        });
        //tasty board
        ((LinearLayout)view.findViewById(R.id.board)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mListener!=null)
                    mListener.onMenuClicked(4);

            }
        });
        //messages
        ((LinearLayout)view.findViewById(R.id.messages)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mListener!=null)
                    mListener.onMenuClicked(5);
            }
        });
        //settings
        ((LinearLayout)view.findViewById(R.id.settings)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mListener!=null)
                    mListener.onMenuClicked(6);
            }
        });
        final Handler handler=new Handler();
        final Runnable runnable=new Runnable()
        {
            @Override
            public void run()
            {
                if(ordersCount!=getOrdersCounts())
                {
                    if(mListener!=null)
                        mListener.play_notification();
                    ordersCount=getOrdersCounts();
                    tOrdersCount.setText(String.valueOf(ordersCount));
                    Log.d(TAG,"orders count changed");
                }
                if(messagesCount!=bMessagesList.size())
                {
                    if(mListener!=null)
                        mListener.play_notification();
                    messagesCount=bMessagesList.size();
                    tMessagesCount.setText(String.valueOf(messagesCount));
                    Log.d(TAG,"messages count changed");
                }
            }
        };
        Thread thread=new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    while(true)
                    {
                        sleep(2000);
                        refreshOrders();
                        refreshMessages();
                        handler.post(runnable);
                    }
                }
                catch (InterruptedException e)
                {
                    Log.e(TAG,"error sleeping "+e.getMessage());
                }
            }
        };
        thread.start();
        if(!preferences.isDark_theme_enabled())
        {
            view.findViewById(R.id.restaurant).setBackgroundColor(getResources().getColor(R.color.secondary_background_light));
            view.findViewById(R.id.orders).setBackgroundColor(getResources().getColor(R.color.secondary_background_light));
            view.findViewById(R.id.explore).setBackgroundColor(getResources().getColor(R.color.secondary_background_light));
            view.findViewById(R.id.board).setBackgroundColor(getResources().getColor(R.color.secondary_background_light));
            view.findViewById(R.id.messages).setBackgroundColor(getResources().getColor(R.color.secondary_background_light));
            view.findViewById(R.id.settings).setBackgroundColor(getResources().getColor(R.color.secondary_background_light));
        }
        return view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.bmenu, menu);
        final MenuItem logout=menu.findItem(R.id.action_logout);
        logout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                if(mListener!=null)
                    mListener.onLogOut();
                return true;
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onResume()
    {
        super.onResume();
        //set the counts
        tOrdersCount.setText(String.valueOf(getOrdersCounts()));
        tMessagesCount.setText(String.valueOf(LoginA.bMessagesList.size()));

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        void onMenuClicked(int id);
        void onLogOut();
        void play_notification();
    }
    private int getOrdersCounts()
    {
        List<String>order_numbers=new ArrayList<>();
        Iterator iterator= bOrdersList.entrySet().iterator();
        while (iterator.hasNext())
        {
            LinkedHashMap.Entry<Integer, BOrders>set=(LinkedHashMap.Entry<Integer, BOrders>) iterator.next();
            BOrders bOrders=set.getValue();
            int order_number=bOrders.getOrderNumber();
            String date_added=bOrders.getDateAdded();
            String[] date_pieces=new String[]{};
            date_pieces=date_added.split(" ");
            String unique_name=date_pieces[0]+":"+order_number;
            order_numbers.add(unique_name);
        }
        Set<String>unique=new HashSet<>(order_numbers);
        return unique.size();
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
    private void refreshOrders()
    {
        LinkedHashMap<Integer,BOrders>tempOrdersLHM=new LinkedHashMap<>();
        //getting columns list
        List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
        info.add(new BasicNameValuePair("userid",Integer.toString(buyerAccount.getId())));
        // making HTTP request
        JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_b_orders,"POST",info);
       // Log.d("sItems",""+jsonObject.toString());
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
                    int item_id=jsonObjectNotis.getInt("itemid");
                    int order_number=jsonObjectNotis.getInt("ordernumber");
                    int orderstatus=jsonObjectNotis.getInt("orderstatus");
                    int table_number=jsonObjectNotis.getInt("table_number");
                    String dateadded=jsonObjectNotis.getString("dateadded");
                    String datechanged=jsonObjectNotis.getString("datechanged");
                    String item=jsonObjectNotis.getString("item");
                    double selling_price=jsonObjectNotis.getDouble("sellingprice");
                    int order_format=jsonObjectNotis.getInt("order_format");
                    String restaurant=jsonObjectNotis.getString("restaurant_name");
                    String waiter_names=jsonObjectNotis.getString("waiter_names");


                    BOrders bOrders=new BOrders(id,item_id,order_number,orderstatus,item,selling_price, order_format,table_number,restaurant, waiter_names,dateadded);
                    tempOrdersLHM.put(id,bOrders);
                    //bOrdersList.put(id,bOrders);
                }
                bOrdersList=tempOrdersLHM;
                //return true;
            }
            else
            {
                String message=jsonObject.getString(TAG_MESSAGE);
                Log.e(TAG_MESSAGE,""+message);
            }
        }
        catch (JSONException e)
        {
            Log.e("JSON",""+e.getMessage());
        }
    }
    private void refreshMessages()
    {
        LinkedHashMap<String,BMessages>tempMessagesLHM=new LinkedHashMap<>();
        //getting columns list
        List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
        info.add(new BasicNameValuePair("id",Integer.toString(buyerAccount.getId())));
        // making HTTP request
        JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_b_notifications,"POST",info);
       // Log.d("bNotis",""+jsonObject.toString());
        try
        {
            JSONArray notisArrayList=null;
            int success=jsonObject.getInt(TAG_SUCCESS);
            if(success==1)
            {
                notisArrayList=jsonObject.getJSONArray("notis");
                for(int count=0; count<notisArrayList.length(); count+=1)
                {
                    JSONObject jsonObjectNotis=notisArrayList.getJSONObject(count);
                    int id=jsonObjectNotis.getInt("id");
                    int classes=jsonObjectNotis.getInt("classes");
                    String message=jsonObjectNotis.getString("messages");
                    String date=jsonObjectNotis.getString("dateadded");

                    BMessages oneBMessage=new BMessages(id,classes,message,date);
                    tempMessagesLHM.put(String.valueOf(id),oneBMessage);
                }
                bMessagesList=tempMessagesLHM;
            }
            else
            {
                String message=jsonObject.getString(TAG_MESSAGE);
                Log.e(TAG_MESSAGE,""+message);
            }
        }
        catch (JSONException e)
        {
            Log.e("JSON",""+e.getMessage());
        }
    }
}
