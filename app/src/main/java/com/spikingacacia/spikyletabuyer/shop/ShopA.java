/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 10/5/20 9:48 PM
 */

package com.spikingacacia.spikyletabuyer.shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.MyBounceInterpolator;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.database.Categories;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.database.Groups;
import com.spikingacacia.spikyletabuyer.main.MainActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class ShopA extends AppCompatActivity
        implements
        menuFragment.OnListFragmentInteractionListener,
        CartBottomSheet.OnListFragmentInteractionListener,
        OrderParamsBottomSheet.OnFragmentInteractionListener,
        ScanToOrderParamsBottomSheet.OnFragmentInteractionListener
{
    private static LinkedHashMap<Integer, DMenu> menuLinkedHashMap;
    public static LinkedHashMap<Integer, Categories> categoriesLinkedHashMap;
    public static LinkedHashMap<Integer, Groups> groupsLinkedHashMap;
    private String TAG="ShopA";
    private JSONParser jsonParser;
    private String sellerEmail;
    private int sellerOrderRadius=2;
    private double buyerDistance=0;
    private int numberOfTables=10;
    private int tableNumber;
    private  int which; // which can either be 1 for normal ordering in a restaurant or 2 for pre ordering
    private  String diningOptions;
    private int backgroundTasksProgress=0;
    private final int finalProgressCount=3;
    int whichFragment=1;
    String previousTitle[]=new String[2];
    private int cartCount=0;
    private  double totalPrice=0;
    private  List<Integer>items;
    private  List<String>names;
    private  List<Double>prices;
    private Preferences preferences;
    private LinkedHashMap<String,Integer> cartLinkedHashMap;
    private LinkedHashMap<String,Integer> itemPriceSizeLinkedHashMap;
    private Double tempTotal =0.0;
    private Double deliveryCharge = 0.0;
    private boolean hasPayment = false;
    private boolean preOrder = false;
    private String mPesaTillNumber;
    private int taskCounter = 0; //counter for updating order and adding a new mpesa response request in the database
    private ProgressBar progressBar;
    private View mainFragment;
    private FloatingActionButton floatingActionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        progressBar = findViewById(R.id.progress);
        mainFragment = findViewById(R.id.base);
        floatingActionButton = findViewById(R.id.fab);

        //preference
        preferences=new Preferences(getBaseContext());
        which = getIntent().getIntExtra("which",1);
        String seller_names = getIntent().getStringExtra("seller_names");
        sellerEmail =getIntent().getStringExtra("seller_email");
        sellerOrderRadius=getIntent().getIntExtra("order_radius",2);
        buyerDistance=getIntent().getDoubleExtra("buyer_distance",0);
        numberOfTables=getIntent().getIntExtra("number_of_tables",10);
        tableNumber=getIntent().getIntExtra("table_number",-1);
        hasPayment =  getIntent().getBooleanExtra("has_payment",false);
        mPesaTillNumber = getIntent().getStringExtra("m_code");
        diningOptions = getIntent().getStringExtra("dining_options");
        if(!seller_names.contentEquals("null") && !seller_names.contentEquals("NULL") && !seller_names.contentEquals(""))
            setTitle(seller_names);
        else
        {
            if(which == 1)
                setTitle("Menu");
            else if(which == 2)
            {
                setTitle("Menu - Pre order");
                preOrder = true;
            }
        }
        if(which == 2)
            preOrder = true;


        Fragment fragment= menuFragment.newInstance(sellerEmail);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"menu");
        transaction.commit();
        floatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getTotal();
                CartBottomSheet.newInstance(tempTotal, cartLinkedHashMap, itemPriceSizeLinkedHashMap, menuLinkedHashMap,hasPayment, preOrder, ShopA.this).show(getSupportFragmentManager(), "dialog");
            }
        });

        jsonParser=new JSONParser();
        menuLinkedHashMap = new LinkedHashMap<>();
        categoriesLinkedHashMap = new LinkedHashMap<>();
        groupsLinkedHashMap = new LinkedHashMap<>();
        items=new ArrayList<>();
        names=new ArrayList<>();
        prices=new ArrayList<>();
        cartCount=0;
        totalPrice=0;
        cartLinkedHashMap = new LinkedHashMap<>();
        itemPriceSizeLinkedHashMap = new LinkedHashMap<>();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shop_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if( id == R.id.action_cart)
        {
            getTotal();
            CartBottomSheet.newInstance(tempTotal, cartLinkedHashMap, itemPriceSizeLinkedHashMap, menuLinkedHashMap,hasPayment, preOrder, this).show(getSupportFragmentManager(), "dialog");
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
    static void putIntoMenu(int id, DMenu dMenu)
    {
        menuLinkedHashMap.put(id,dMenu);
    }


    @Override
    public void onMenuItemInteraction(final List<DMenu> dMenuList, List<Integer>items_new_sizes_prices_index, List<Boolean> areItemsFree)
    {
        for( int index=0; index< dMenuList.size(); index++)
        {
            final DMenu item = dMenuList.get(index);
            //items whose price spinner has not been selected may be null therefore set the price to first item
            if(items_new_sizes_prices_index.get(index) == null)
                items_new_sizes_prices_index.set(index, 0);
            //since there are those items which are accompaniments and thier prices are set to free...
            //we add an array position of -100-index ...
            //eg if index == 2 array position is -100-2== -102
            //when we want to get the size we just use -102+100 == 2
            itemPriceSizeLinkedHashMap.put(item.getId()+":"+items_new_sizes_prices_index.get(index), areItemsFree.get(index) ? -100-items_new_sizes_prices_index.get(index) : items_new_sizes_prices_index.get(index));
            cartLinkedHashMap.put(item.getId()+":"+items_new_sizes_prices_index.get(index),1);
        }
        Snackbar.make(mainFragment,dMenuList.get(dMenuList.size()-1).getItem()+" added",Snackbar.LENGTH_SHORT).show();
        showCartAddition();
    }

    private void showCartAddition()
    {
        //animate the fab button
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
        floatingActionButton.startAnimation(myAnim);
        vibrate_on_click();
    }
    /*************************************************************************************************************************************************************************************
     * implementation of CartBottomSheet.java
     * ************************************************************************************************************************************************************************************/
    @Override
    public void onProceed(double new_total, int payment_type)
    {
        tempTotal = new_total;
        //first check if the the location is within the restaurants range
        if(!preOrder && buyerDistance>sellerOrderRadius)
        {
            Snackbar.make(mainFragment,"You are not within the restaurant's order radius",Snackbar.LENGTH_LONG).show();
            return;
        }
        //set the type of order
        if(preOrder)
        {
            //onOrderClickedPreOder();
            OrderParamsBottomSheet.newInstance(hasPayment,diningOptions,deliveryCharge,tempTotal, payment_type,this).show(getSupportFragmentManager(), "dialog");
        }
        else
        {
            //onOrderClickedScanToOder();
            ScanToOrderParamsBottomSheet.newInstance(hasPayment,tableNumber, numberOfTables, tempTotal, payment_type, this).show(getSupportFragmentManager(), "dialog");
        }



    }

    @Override
    public void totalItemsChanged( LinkedHashMap<String,Integer> cartLinkedHashMap)
    {
        this.cartLinkedHashMap = cartLinkedHashMap;
    }


    private int getCartCount()
    {
        int total_count=0;
        Iterator iterator = cartLinkedHashMap.entrySet().iterator();
        while(iterator.hasNext())
        {
            LinkedHashMap.Entry<String, Integer>set = (LinkedHashMap.Entry<String, Integer>) iterator.next();
            int count = set.getValue();
            total_count += count;
        }
        return total_count;
    }
    private void getTotal()
    {
        Iterator iterator= cartLinkedHashMap.entrySet().iterator();
        double total=0.0;
        while(iterator.hasNext())
        {
            LinkedHashMap.Entry<String, Integer>set = (LinkedHashMap.Entry<String, Integer>) iterator.next();
            String id_size = set.getKey();
            String[] id_size_pieces = id_size.split(":");
            int id=Integer.parseInt(id_size_pieces[0]);
            int count=set.getValue();
            DMenu inv = menuLinkedHashMap.get(id);

            double price = 0.0;
            int pos = itemPriceSizeLinkedHashMap.get(id_size);
            if(pos >=0 )
            {
                String priceString = inv.getPrices();
                final String[] prices = priceString.split(":");
                price = Double.parseDouble( prices[pos].contentEquals("null")?"0":prices[pos]);
            }



            total += count*price;

        }
        tempTotal = total;
    }
    void showProgress(boolean show)
    {
        progressBar.setVisibility(show? View.VISIBLE : View.GONE);
        mainFragment.setVisibility( show? View.INVISIBLE :View.VISIBLE);
    }
    private void play_notification()
    {
        Uri alarmSound =
                RingtoneManager. getDefaultUri (RingtoneManager. TYPE_NOTIFICATION );
        MediaPlayer mp = MediaPlayer. create (getBaseContext(), alarmSound);
        mp.start();

    }
    private void vibrate_on_click()
    {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator == null)
            Log.e(TAG,"No vibrator");
        else
            vibrator.vibrate(100);
    }


    /*************************************************************************************************************************************************************************************
     * implementation of OrderParamsBottomSheet.java
     * ************************************************************************************************************************************************************************************/


    @Override
    public void onPlacePreOrder(int which, String time, String mobile_mpesa, String mobile_delivery, String instructions, int payment_type)
    {
        floatingActionButton.setVisibility(View.GONE);
        new OrderTask(tableNumber,time, which, mobile_mpesa, mobile_delivery, instructions, payment_type).execute((Void)null);
    }

    /*************************************************************************************************************************************************************************************
     * implementation of ScanToOrderParamsBottomSheet.java
     * ************************************************************************************************************************************************************************************/


    @Override
    public void onScanToOrderPlaceOrder(int which, int table, String mobile_mpesa, int payment_type)
    {
        new OrderTask(table,"null", which,mobile_mpesa,"null","null", payment_type).execute((Void)null);
    }

    private class OrderTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_place_order = base_url + "place_order_1.php";
        private String TAG_MESSAGE = "message";
        private String TAG_SUCCESS = "success";
        private String itemsIds="";
        private String itemSizes="";
        private String itemPrices="";
        private int tableNumber=0;
        private String collectTime;
        private String orderType;
        private String deliveryMobile;
        private String mpesaMobile;
        private String deliveryInstructions;
        private int paymentType;
        private String orderNumber;
        private String dateAdded;


        public OrderTask(int tableNumber, String collectTime, int orderType, String mpesaMobile, String deliveryMobile, String deliveryInstructions, int paymentType)
        {
            this.tableNumber=tableNumber;
            this.collectTime = collectTime;
            this.orderType = String.valueOf(orderType);
            this.mpesaMobile = mpesaMobile;
            this.deliveryMobile = deliveryMobile;
            this.deliveryInstructions = deliveryInstructions;
            this.paymentType = paymentType;
            formData();
        }
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            showProgress(true);

            for(int c=0; c<items.size(); c++)
            {
                itemsIds+=String.valueOf(items.get(c));
                if(c!=items.size()-1)
                    itemsIds+=",";
            }
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("seller_email",sellerEmail));
            info.add(new BasicNameValuePair("user_email", LoginA.getServerAccount().getEmail()));
            info.add(new BasicNameValuePair("items_ids",itemsIds));
            info.add(new BasicNameValuePair("items_sizes",itemSizes));
            info.add(new BasicNameValuePair("items_prices",itemPrices));
            info.add(new BasicNameValuePair("table_number",String.valueOf(tableNumber)));
            info.add(new BasicNameValuePair("has_payment", hasPayment? "1" : "0"));
            info.add(new BasicNameValuePair("pre_order", preOrder? "1" : "0")); // 1 means pre order 0 means not
            info.add(new BasicNameValuePair("collect_time", preOrder? collectTime : "null"));
            info.add(new BasicNameValuePair("payment_type", String.valueOf(paymentType))); // 0 means mpesa , 1 is for cash payment
            info.add(new BasicNameValuePair("order_type", orderType)); // 0 means eat while eat , 1 is for take away and 2 is for delivery
            info.add(new BasicNameValuePair("delivery_mobile", deliveryMobile));
            info.add(new BasicNameValuePair("delivery_instructions", deliveryInstructions));
            info.add(new BasicNameValuePair("delivery_location", MainActivity.myLocation));
            info.add(new BasicNameValuePair("delivery_charge", String.valueOf(deliveryCharge.intValue())));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_place_order,"POST",info);
            Log.d("sItems",""+jsonObject.toString());
            try
            {
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    orderNumber = jsonObject.getString("order_number");
                    dateAdded = jsonObject.getString("date_added");
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
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
                //check if we can be able to do payments
                if(hasPayment && paymentType == 0)
                {
                    // the order is succesfully registered, so we carry out the payment
                    //new LipaNaMpesaStkPush(orderNumber,dateAdded,mpesaMobile).execute((Void)null);
                    String amount = String.valueOf(tempTotal.intValue());
                    new MpesaStkPushTask(orderNumber, dateAdded, mpesaMobile, amount).execute((Void)null);
                }
                else
                {
                    showProgress(false);
                    floatingActionButton.setVisibility(View.VISIBLE);
                    Snackbar.make(mainFragment,"Order Placed",Snackbar.LENGTH_LONG).show();
                    cartLinkedHashMap.clear();
                    tempTotal = 0.0;
                    //onBackPressed();
                    play_notification();
                }
            }
            else
            {
                Snackbar.make(mainFragment,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
                floatingActionButton.setVisibility(View.VISIBLE);
            }
        }
        void  formData()
        {
            Iterator iterator= cartLinkedHashMap.entrySet().iterator();
            while(iterator.hasNext())
            {
                LinkedHashMap.Entry<String, Integer>set = (LinkedHashMap.Entry<String, Integer>) iterator.next();
                String id_size = set.getKey();
                String[] id_size_pieces = id_size.split(":");
                int id=Integer.parseInt(id_size_pieces[0]);
                DMenu inv = menuLinkedHashMap.get(id);
                int quantity = set.getValue();

                int pos = itemPriceSizeLinkedHashMap.get(id_size);
                String priceString = inv.getPrices();
                final String[] prices = priceString.split(":");
                String[] sizes = inv.getSizes().split(":");
                String price = "0";
                if(pos >=0 )
                    price =  prices[pos].contentEquals("null")?"0":prices[pos];
                if(pos <0)
                    pos = pos+100;
                String size = sizes[pos];

                for(int c=0; c<quantity; c++)
                {
                    if (!itemsIds.contentEquals(""))
                    {
                        itemsIds += ",";
                        itemPrices += ",";
                        itemSizes += ",";
                    }
                    itemsIds += String.valueOf(inv.getId());
                    itemPrices += price;
                    itemSizes += size;
                }
            }

        }
    }
    private class MpesaStkPushTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update=base_url+"m_lnm.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        final private String orderNumber;
        private final String dateAdded;
        private final String amount;
        private final String mobile;

        public MpesaStkPushTask(String orderNumber, String dateAdded,String mobile, String amount )
        {
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            this.amount = amount;
            this.mobile = mobile;
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("user_email",LoginA.getServerAccount().getEmail()));
            info.add(new BasicNameValuePair("seller_email",sellerEmail));
            info.add(new BasicNameValuePair("order_number", orderNumber));
            info.add(new BasicNameValuePair("order_date_added",dateAdded));
            info.add(new BasicNameValuePair("amount",amount));
            info.add(new BasicNameValuePair("mobile",mobile));


            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_update,"POST",info);
            try
            {
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
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
            showProgress(false);
            floatingActionButton.setVisibility(View.VISIBLE);
            if (successful)
            {
                Snackbar.make(mainFragment,"Order Placed",Snackbar.LENGTH_LONG).show();
                cartLinkedHashMap.clear();;
                tempTotal = 0.0;
                //onBackPressed();
            }
            else
            {
                Snackbar.make(mainFragment,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
