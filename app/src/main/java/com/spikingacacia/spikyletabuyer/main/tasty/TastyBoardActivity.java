package com.spikingacacia.spikyletabuyer.main.tasty;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.database.TastyBoard;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.shop.OrderParamsBottomSheet;
import com.spikingacacia.spikyletabuyer.shop.ShopA;
import com.spikingacacia.spikyletabuyer.shop.CartBottomSheet;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class TastyBoardActivity extends AppCompatActivity implements
         TastyBoardFragment.OnListFragmentInteractionListener,
        TastyBoardOverviewFragment.OnListFragmentInteractionListener,
        CartBottomSheet.OnListFragmentInteractionListener,
        OrderParamsBottomSheet.OnFragmentInteractionListener
{
    private TastyBoard tastyBoard;
    private boolean hasPayment;
    private String TAG = "tast_board_a";
    private ProgressBar progressBar;
    private View mainFragment;
    private LinkedHashMap<Integer, DMenu> menuLinkedHashMap = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> cartLinkedHashMap = new LinkedHashMap<>();
    private LinkedHashMap<String,Integer> itemPriceSizeLinkedHashMap = new LinkedHashMap<>();
    private Double total = 0.0;
    private Double deliveryCharge = 0.0;
    private String diningOptions="1:1:0";
    private String sellerEmail="";
    private int taskCounter = 0; //counter for updating order and adding a new mpesa response request in the database
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasty_board);

        setTitle("Tasty Board");
        progressBar = findViewById(R.id.progress);
        mainFragment = findViewById(R.id.base);

        Fragment fragment= TastyBoardFragment.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.base,fragment,"tasty");
        transaction.commit();
    }
    /*
     * implementation of TastyBoardFragment.java
     */
    @Override
    public void onTastyBoardItemClicked(TastyBoard tastyBoard)
    {
        //if the location of the hotel is kenya then we ask for mpesa payment
        hasPayment = false;
            /*if( item.getmCode().contentEquals("") ||  item.getmCode().contentEquals("null") || item.getmCode().contentEquals("NULL"))
                has_payment = false;*/
        if(tastyBoard.getCountry().contentEquals("KE"))
            hasPayment = true;
        TastyBoardOverviewBottomSheet.newInstance(tastyBoard).show(getSupportFragmentManager(), "dialog");
        /*Fragment fragment= TastyBoardOverviewFragment.newInstance(tastyBoard);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"overview");
        transaction.addToBackStack(null);
        transaction.commit();*/
    }
    /*
     * implementation of TastyBoardOverviewFragment.java
     */
    @Override
    public void onTastyBoardItemPreOrder(TastyBoard tastyBoard)
    {
        diningOptions = tastyBoard.getDiningOptions();
        sellerEmail = tastyBoard.getSellerEmail();
        //prices and sizes are stores in the form size1,price1:size2,price2
        String[] prices_and_sizes = tastyBoard.getSizeAndPrice().split(":");
        String[] discount_prices = tastyBoard.getDiscountPrice().split(":");
        //get the sizes
        String sizes = "";
        for(int c=0; c<prices_and_sizes.length; c++)
        {
            if(c!=0)
                sizes+=":";
            sizes += prices_and_sizes[c].split(",")[0];
            itemPriceSizeLinkedHashMap.put(tastyBoard.getLinkedItemId()+":"+c, c);
            cartLinkedHashMap.put(tastyBoard.getLinkedItemId()+":"+c,1);
            total += Double.parseDouble(discount_prices[c]);
        }
        DMenu dMenu = new DMenu(tastyBoard.getLinkedItemId(),-1,-1,"null","null",tastyBoard.getItemNames(),"",sizes,tastyBoard.getDiscountPrice(),
                ".jpg",true,"","");
        menuLinkedHashMap.put(tastyBoard.getLinkedItemId(),dMenu);
        //add the cart

        CartBottomSheet.newInstance(total, cartLinkedHashMap, itemPriceSizeLinkedHashMap, menuLinkedHashMap, hasPayment, true, this).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onGotoMenu()
    {
        showProgress(true);
        if(!MainActivity.myLocation.contentEquals(""))
        {
            String[] location = MainActivity.myLocation.split(":");
            new RestaurantTask(location[0], location[1]).execute((Void)null);
        }
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

       /* NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(SMenuA.this, default_notification_channel_id )
                        .setSmallIcon(R.mipmap.ic_launcher )
                        .setContentTitle( "New Order" )
                        .setContentText( "a new order has arrived" ) ;
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context. NOTIFICATION_SERVICE );
        mNotificationManager.notify(( int ) System. currentTimeMillis () ,
                mBuilder.build());*/
    }
    private void showRestaurants(Restaurants item)
    {
        //if the location of the hotel is kenya then we ask for mpesa payment
        boolean has_payment = false;
            /*if( item.getmCode().contentEquals("") ||  item.getmCode().contentEquals("null") || item.getmCode().contentEquals("NULL"))
                has_payment = false;*/
        if(item.getCountryCode().contentEquals("KE"))
            has_payment = true;
        Intent intent=new Intent(this, ShopA.class);
        intent.putExtra("which",2);
        intent.putExtra("seller_names", item.getNames());
        intent.putExtra("seller_email",item.getEmail());
        intent.putExtra("order_radius",item.getRadius());
        intent.putExtra("buyer_distance",item.getDistance());
        intent.putExtra("number_of_tables",item.getNumberOfTables());
        intent.putExtra("table_number",item.getTableNumber());
        intent.putExtra("has_payment", has_payment);
        intent.putExtra("m_code", has_payment ? item.getmCode() : "");
        intent.putExtra("dining_options", item.getDiningOptions());
        startActivity(intent);


    }
/*
* implementation of CartBottomSheet.java
 */
    @Override
    public void onProceed(double new_total, int payment_type)
    {
        total = new_total;
        OrderParamsBottomSheet.newInstance(hasPayment,diningOptions,deliveryCharge,total, payment_type,this).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void totalItemsChanged(LinkedHashMap<String, Integer> cartLinkedHashMap)
    {
        this.cartLinkedHashMap = cartLinkedHashMap;
    }


    @Override
    public void onPlacePreOrder(int which, String time, String mobile_mpesa, String mobile_delivery, String instructions,  int payment_type)
    {
        new OrderTask(-1,time, which, mobile_mpesa, mobile_delivery, instructions, payment_type).execute((Void)null);
    }

    private class RestaurantTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_restaurant=base_url+"get_restaurant_from_tasty_board.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        //final private String country;
        final private String latitude;
        final private String longitude;
        private  int success;
        private Restaurants restaurant;


        public RestaurantTask( String latitude, String longitude)
        {
            this.latitude=latitude;
            this.longitude=longitude;
            jsonParser=new JSONParser();

        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            //info.add(new BasicNameValuePair("country",country));
            info.add(new BasicNameValuePair("latitude",latitude));
            info.add(new BasicNameValuePair("longitude",longitude));
            info.add(new BasicNameValuePair("location",""));
            info.add(new BasicNameValuePair("seller_email",tastyBoard.getSellerEmail()));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_restaurant,"POST",info);
            //Log.d(TAG,"json:"+jsonObject.toString());
            try
            {
                success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    JSONArray accountArray=jsonObject.getJSONArray("restaurants");
                    JSONObject jsonObject_restaurants=accountArray.getJSONObject(0);

                    int id=jsonObject_restaurants.getInt("id");
                    String email = jsonObject_restaurants.getString("email");
                    String names=jsonObject_restaurants.getString("username");
                    double distance=jsonObject_restaurants.getDouble("distance");
                    double latitude=jsonObject_restaurants.getDouble("latitude");
                    double longitude=jsonObject_restaurants.getDouble("longitude");
                    String locality=jsonObject_restaurants.getString("locality");
                    String country_code = jsonObject_restaurants.getString("country_code");
                    int order_radius=jsonObject_restaurants.getInt("order_radius");
                    int tables = jsonObject_restaurants.getInt("number_of_tables");
                    String image_type=jsonObject_restaurants.getString("image_type");
                    int table_number = jsonObject_restaurants.getInt("table_number");
                    String m_code = jsonObject_restaurants.getString("m_code");
                    String dining_options = jsonObject_restaurants.getString("dining_options");

                    restaurant =new Restaurants(id, email, names,distance,latitude,longitude,locality,country_code, order_radius, tables, image_type, table_number, m_code, dining_options);
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
            if (successful)
            {
                showRestaurants( restaurant);
            }
            else
            {
                Toast.makeText(getBaseContext(),"Error getting restaurant",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class OrderTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_place_order = base_url + "place_order_1.php";
        private String TAG_MESSAGE = "message";
        private String TAG_SUCCESS = "success";
        private JSONParser jsonParser;
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
            jsonParser = new JSONParser();
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

            Log.d("ORDERING","starting....");
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
            info.add(new BasicNameValuePair("pre_order",  "1" )); // 1 means pre order 0 means not
            info.add(new BasicNameValuePair("collect_time", collectTime ));
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
                    String amount = String.valueOf(total.intValue());
                    new MpesaStkPushTask(orderNumber, dateAdded, mpesaMobile, amount).execute((Void)null);
                    //new LipaNaMpesaStkPush(orderNumber,dateAdded,mpesaMobile).execute((Void)null);
                }
                else
                {
                    showProgress(false);
                    Snackbar.make(mainFragment,"Order Placed",Snackbar.LENGTH_LONG).show();
                    cartLinkedHashMap.clear();
                    total = 0.0;
                    onBackPressed();
                    play_notification();
                }
            }
            else
            {
                Snackbar.make(mainFragment,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
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
                String  price =  prices[pos].contentEquals("null")?"0":prices[pos];
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
            if (successful)
            {
                Snackbar.make(mainFragment,"Order Placed",Snackbar.LENGTH_LONG).show();
                cartLinkedHashMap.clear();;
                total = 0.0;
                new UpdateTastyBoardTask().execute((Void)null);
                //we call the onbackpresses twice to go from orderparams fragments->cartfragment->tastyboardfragment
                onBackPressed();
                onBackPressed();
            }
            else if(!successful)
            {
                Snackbar.make(mainFragment,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
    }
    private class UpdateTastyBoardTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update= base_url+"update_tasty_board_orders.php";
        private JSONParser jsonParser;
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";

        @Override
        protected void onPreExecute()
        {
            jsonParser = new JSONParser();
            super.onPreExecute();
        }
        public UpdateTastyBoardTask()
        {
            //which is 1 for view and 2 for likes
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("tasty_board_id",String.valueOf(tastyBoard.getId())));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_update,"POST",info);
            try
            {
                JSONArray array=null;
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                    return true;
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

            if (successful)
            {
                Log.d(TAG,"updated tasty board orders");
            }
            else
            {

            }
        }

    }



}