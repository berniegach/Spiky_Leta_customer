package com.spikingacacia.spikyletabuyer.main.tasty;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
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
import com.spikingacacia.spikyletabuyer.restaurants.SRRestaurantsA;
import com.spikingacacia.spikyletabuyer.shop.OrderParamsFragment;
import com.spikingacacia.spikyletabuyer.shop.ShopA;
import com.spikingacacia.spikyletabuyer.shop.cart.CartFragment;
import com.spikingacacia.spikyletabuyer.util.Mpesa;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

public class TastyBoardActivity extends AppCompatActivity implements
        TastyBoardOverviewFragment.OnListFragmentInteractionListener,
        CartFragment.OnListFragmentInteractionListener,
        OrderParamsFragment.OnFragmentInteractionListener
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
    private String diningOptions="1:1:1";
    private String sellerEmail="";
    private int taskCounter = 0; //counter for updating order and adding a new mpesa response request in the database
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasty_board);

        setTitle("Tasty Board");
        //get the tasty board
        tastyBoard = (TastyBoard) getIntent().getSerializableExtra("tasty_board");
        hasPayment =  getIntent().getBooleanExtra("has_payment",false);

        progressBar = findViewById(R.id.progress);
        mainFragment = findViewById(R.id.base);

        Fragment fragment= TastyBoardOverviewFragment.newInstance(tastyBoard);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.base,fragment,"overview");
        transaction.commit();
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
        DMenu dMenu = new DMenu(tastyBoard.getLinkedItemId(),-1,-1,"null",tastyBoard.getItemNames(),"",sizes,tastyBoard.getDiscountPrice(),
                ".jpg","","");
        menuLinkedHashMap.put(tastyBoard.getLinkedItemId(),dMenu);
        //add the cart

        Fragment fragment= CartFragment.newInstance(total, cartLinkedHashMap, itemPriceSizeLinkedHashMap, menuLinkedHashMap);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"cart");
        transaction.addToBackStack("cart");
        transaction.commit();
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
* implementation of CartFragment.java
 */
    @Override
    public void onProceed()
    {
        Fragment fragment= OrderParamsFragment.newInstance(hasPayment,diningOptions);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"order");
        transaction.addToBackStack("order");
        transaction.commit();
    }

    @Override
    public void totalItemsChanged(LinkedHashMap<String, Integer> cartLinkedHashMap)
    {
        this.cartLinkedHashMap = cartLinkedHashMap;
    }

    @Override
    public void onDetachCalled()
    {

    }

    @Override
    public void onPlacePreOrder(int which, String time, String mobile_mpesa, String mobile_delivery, String instructions)
    {
        new OrderTask(-1,time, which, mobile_mpesa, mobile_delivery, instructions).execute((Void)null);
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
        private String url_place_order = base_url + "place_order.php";
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
        private String orderNumber;
        private String dateAdded;


        public OrderTask(int tableNumber, String collectTime, int orderType, String mpesaMobile, String deliveryMobile, String deliveryInstructions)
        {
            jsonParser = new JSONParser();
            this.tableNumber=tableNumber;
            this.collectTime = collectTime;
            this.orderType = String.valueOf(orderType);
            this.mpesaMobile = mpesaMobile;
            this.deliveryMobile = deliveryMobile;
            this.deliveryInstructions = deliveryInstructions;
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
            info.add(new BasicNameValuePair("order_type", orderType)); // 0 means eat while eat , 1 is for take away and 2 is for delivery
            info.add(new BasicNameValuePair("delivery_mobile", deliveryMobile));
            info.add(new BasicNameValuePair("delivery_instructions", deliveryInstructions));
            info.add(new BasicNameValuePair("delivery_location", MainActivity.myLocation));
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
                if(hasPayment)
                {
                    // the order is succesfully registered, so we carry out the payment
                    new LipaNaMpesaStkPush(orderNumber,dateAdded,mpesaMobile).execute((Void)null);
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
    private class LipaNaMpesaStkPush extends AsyncTask<Void, Void, Boolean>
    {
        private String orderNumber;
        private String dateAdded;
        private String spikingAcaciaShortcode = "7004537";// "7004537";//"5267197";//; // This is spking acacaia till number 5267197 HO number
        private String msisdn; // MSISDN (phone number) sending the transaction, start with country code without the plus(+) sign.
        private String passkey="bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"; // this passkey is generated by safaricom on the portal https://developer.safaricom.co.ke/test_credentials
        private String passkeyProduction = "581d00f794be8b06879cacc830ae638861e266410c393dffdff3bcc34f107f11";// this passkey was given by safaricom for production purposes
        private String password;
        private String timeStamp;
        private String confirmationUrl = base_url+"mpesa_confirmation.php";//"https://sandbox.safaricom.co.ke/mpesa/"
        //result
        String MerchantRequestID;
        String CheckoutRequestID;
        String ResponseCode;
        String ResponseDescription;

        public LipaNaMpesaStkPush(String orderNumber, String dateAdded, String msisdn)
        {
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            //this.msisdn = "254708374149";
            this.msisdn = msisdn;
            timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            //shortcode = "174379";// partyA = 601362 , partyB = 600000, lipa na mpesa = 174379
        }
        @Override
        protected Boolean doInBackground(Void... voids)
        {
            //encode password
            String pass_to_encode = spikingAcaciaShortcode+passkeyProduction+timeStamp;
            byte[] bytes = new byte[0];
            try
            {
                bytes = pass_to_encode.getBytes("ISO-8859-1");
            }
            catch (UnsupportedEncodingException e)
            {
                Log.e(TAG," "+e.getMessage());
                e.printStackTrace();
                return false;
            }
            password = Base64.encodeToString(bytes, Base64.NO_WRAP | Base64.URL_SAFE);
            try
            {
                String amount = String.valueOf(total.intValue());
                JSONObject jobject= Mpesa.STKPushSimulation("7004537",password,
                        timeStamp,"CustomerBuyGoodsOnline",amount,msisdn,msisdn,"5267197",
                        "http://3.20.17.200/order/m_listener.php","https://sandbox.safaricom.co.ke/mpesa/",
                        "order","Testing stk push");
                MerchantRequestID = jobject.getString("MerchantRequestID");
                CheckoutRequestID = jobject.getString("CheckoutRequestID");
                ResponseCode = jobject.getString("ResponseCode");
                ResponseDescription = jobject.getString("ResponseDescription");
                Log.d(TAG,"JSON+"+jobject.toString());
                if(ResponseCode.contentEquals("0"))
                    return true;
            }
            catch (IOException | JSONException e)
            {
                Log.e(TAG," "+e.getMessage());
                e.printStackTrace();
                return false;
            }

            return false;
        }
        @Override
        protected void onPostExecute(final Boolean successful)
        {

            if (successful)
            {
                //check the status
                //status -3 = paid request not sent -2 = payment request sent successfuly, -1 = payment received is 0= delete 1=accepted 2=done 3 = collected
                new UpdateOrderTask(orderNumber, dateAdded,"-2","0").execute((Void)null);
                //also add the mpesa request for tracking
                new AddNewMpesaResponseTask(orderNumber,dateAdded,CheckoutRequestID,spikingAcaciaShortcode,password,timeStamp).execute((Void)null);
            }
            else
            {
                new UpdateOrderTask(orderNumber, dateAdded,"0","0").execute((Void)null);
                Snackbar.make(mainFragment,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
    }
    private class UpdateOrderTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update=base_url+"update_seller_order.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        final private String orderNumber;
        private final String dateAdded;
        private final String orderStatus;
        private final String updateSellerTotal;

        public  UpdateOrderTask(String orderNumber, String dateAdded, String orderStatus,  String updateSellerTotal)
        {
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            this.orderStatus = orderStatus; // order status for unpaid order is -1, delete is 0 and for a succesful order is 1
            this.updateSellerTotal = updateSellerTotal;
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("seller_email",sellerEmail));
            info.add(new BasicNameValuePair("buyer_email",LoginA.getServerAccount().getEmail()));
            info.add(new BasicNameValuePair("waiter_email", "unavailable"));
            info.add(new BasicNameValuePair("order_number",orderNumber));
            info.add(new BasicNameValuePair("status",orderStatus));
            info.add(new BasicNameValuePair("update_seller_total",updateSellerTotal));
            info.add(new BasicNameValuePair("m_message",""));
            info.add(new BasicNameValuePair("date_added",dateAdded));
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
            taskCounter+=1;
            showProgress(false);
            if (successful && orderStatus.contentEquals("-2") && taskCounter==2)
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
    private class AddNewMpesaResponseTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update=base_url+"add_mpesa_request.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        final private String orderNumber;
        private final String dateAdded;
        private final String CheckoutRequestID;
        private final String businessShortcode;
        private final String password;
        private final String timestamp;

        public AddNewMpesaResponseTask(String orderNumber, String dateAdded, String checkoutRequestID, String businessShortcode, String password, String timestamp)
        {
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            CheckoutRequestID = checkoutRequestID;
            this.businessShortcode = businessShortcode;
            this.password = password;
            this.timestamp = timestamp;
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
            info.add(new BasicNameValuePair("business_shortcode",businessShortcode));
            info.add(new BasicNameValuePair("password",password));
            info.add(new BasicNameValuePair("timestamp",timestamp));
            info.add(new BasicNameValuePair("chequeout_request_id",CheckoutRequestID));

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
            taskCounter+=1;
            showProgress(false);
            if (successful  && taskCounter==2)
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