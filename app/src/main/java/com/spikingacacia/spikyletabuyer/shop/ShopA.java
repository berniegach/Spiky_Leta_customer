package com.spikingacacia.spikyletabuyer.shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.MyBounceInterpolator;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.database.Categories;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.shop.cart.CartContent;
import com.spikingacacia.spikyletabuyer.shop.cart.CartFragment;
import com.spikingacacia.spikyletabuyer.util.Mpesa;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.LoginA.serverAccount;

public class ShopA extends AppCompatActivity
        implements
        BSOrderFragment.OnFragmentInteractionListener,
        menuFragment.OnListFragmentInteractionListener,
        CartFragment.OnListFragmentInteractionListener,
        OrderParamsFragment.OnFragmentInteractionListener
{
    public static LinkedHashMap<Integer, Categories> categoriesLinkedHashMap;
    public static LinkedHashMap<Integer, DMenu> menuLinkedHashMap;
    private String TAG="ShopA";
    private JSONParser jsonParser;
    public static String sellerEmail;
    private int sellerOrderRadius=2;
    private double buyerDistance=0;
    private int numberOfTables=10;
    private int tableNumber;
    public static int which; // which can either be 1 for normal ordering in a restaurant or 2 for pre ordering
    public static String diningOptions;
    private int backgroundTasksProgress=0;
    private final int finalProgressCount=3;
    int whichFragment=1;
    String previousTitle[]=new String[2];
    public static int cartCount=0;
    public static  double totalPrice=0;
    private  ExtendedFloatingActionButton fab;
    public static List<Integer>items;
    public static List<String>names;
    public static List<Double>price;
    Preferences preferences;
    public static LinkedHashMap<String,Integer> cartLinkedHashMap;
    public static LinkedHashMap<String,Integer> tempCartLinkedHashMap;
    public static LinkedHashMap<String,Integer> itemPriceSizeLinkedHashMap;
    public static Double tempTotal =0.0;
    private boolean hasPayment = false;
    private boolean preOrder = false;
    private String mPesaTillNumber;
    private int taskCounter = 0; //counter for updating order and adding a new mpesa response request in the database
    private ProgressBar progressBar;
    private View mainFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        progressBar = findViewById(R.id.progress);
        mainFragment = findViewById(R.id.base);

        //preference
        preferences=new Preferences(getBaseContext());
        which = getIntent().getIntExtra("which",1);
        sellerEmail =getIntent().getStringExtra("seller_email");
        sellerOrderRadius=getIntent().getIntExtra("order_radius",2);
        buyerDistance=getIntent().getDoubleExtra("buyer_distance",0);
        numberOfTables=getIntent().getIntExtra("number_of_tables",10);
        tableNumber=getIntent().getIntExtra("table_number",-1);
        hasPayment =  getIntent().getBooleanExtra("has_payment",false);
        mPesaTillNumber = getIntent().getStringExtra("m_code");
        diningOptions = getIntent().getStringExtra("dining_options");
        if(which == 1)
            setTitle("Menu");
        else if(which == 2)
        {
            setTitle("Menu - Pre order");
            preOrder = true;
        }

        Fragment fragment= menuFragment.newInstance();
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"menu");
        transaction.commit();
        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cartClicked();
            }
        });
        jsonParser=new JSONParser();
        categoriesLinkedHashMap = new LinkedHashMap<>();
        menuLinkedHashMap = new LinkedHashMap<>();
        items=new ArrayList<>();
        names=new ArrayList<>();
        price=new ArrayList<>();
        cartCount=0;
        totalPrice=0;
        cartLinkedHashMap = new LinkedHashMap<>();
        tempCartLinkedHashMap = new LinkedHashMap<>();
        itemPriceSizeLinkedHashMap = new LinkedHashMap<>();
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if (cartLinkedHashMap.size()>0)
        {
            if(!fab.isShown())
            {
                fab.show();
                fab.setText(String.valueOf(getCartCount()));
            }
        }
    }
    void cartClicked()
    {
        if(cartLinkedHashMap.size()<=0)
        {
            Toast.makeText(getBaseContext(),"Cart empty",Toast.LENGTH_SHORT).show();
            return;
        }
        fab.hide();
        tempCartLinkedHashMap=cartLinkedHashMap;
        getTotal();
        Fragment fragment= CartFragment.newInstance(1);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"cart");
        transaction.addToBackStack("cart");
        transaction.commit();
    }
    void onOrderClickedPreOder()
    {
        fab.hide();
        getSupportFragmentManager().popBackStack();
        Fragment fragment= OrderParamsFragment.newInstance(hasPayment,"");
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.base,fragment,"order");
        transaction.addToBackStack("order");
        transaction.commit();
    }


    /**
     * implementation of BSOrderFragment.java*/
    @Override
    public void onOrderItems()
    {

    }

    @Override
    public void onMenuItemInteraction(final List<DMenu> dMenuList)
    {
        if(!fab.isShown())
        {
            fab.show();
            fab.setText(String.valueOf(getCartCount()));
        }
        for( int index=0; index< dMenuList.size(); index++)
        {
            final DMenu item = dMenuList.get(index);
            //cartLinkedHashMap.put(item.getId(),1);
            //display a list of different sizes and prices for the customer to choose
            String[] sizes = item.getSizes().split(":");
            String[] prices = item.getPrices().split(":");
            if(sizes.length == 1)
            {
                //there is only one size
                itemPriceSizeLinkedHashMap.put(item.getId()+":"+0, 0);
                cartLinkedHashMap.put(item.getId()+":"+0,1);
                updateFab();
            }
            else
            {
                String[] sizesPrices = new String[sizes.length];
                for( int c=0; c< sizesPrices.length; c++)
                    sizesPrices[c] = sizes[c]+" @ "+ prices[c];
                new AlertDialog.Builder(ShopA.this)
                        .setTitle("Size for "+item.getItem())
                        .setItems(sizesPrices, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                itemPriceSizeLinkedHashMap.put(item.getId()+":"+which, which);
                                cartLinkedHashMap.put(item.getId()+":"+which,1);
                                updateFab();
                                /*//animate the fab button
                                final Animation myAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.bounce);
                                // Use bounce interpolator with amplitude 0.2 and frequency 20
                                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
                                myAnim.setInterpolator(interpolator);
                                fab.startAnimation(myAnim);*/
                            }
                        })
                        .setNegativeButton("No Thanks!!!", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create().show();
            }

            //play_notification();
        }

    }
    private void updateFab()
    {
        if(getCartCount()>0)
            fab.setVisibility(View.VISIBLE);
        fab.setText(Integer.toString(getCartCount()));
        //animate the fab button
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
        fab.startAnimation(myAnim);
        vibrate_on_click();
    }
    /*************************************************************************************************************************************************************************************
     * implementation of CartFragment.java
     * ************************************************************************************************************************************************************************************/
    @Override
    public void onListFragmentInteraction(CartContent.CartItem item)
    {

    }


    @Override
    public void onProceed()
    {
        //first check if the the location is within the restaurants range
        if(!preOrder && buyerDistance>sellerOrderRadius)
        {
            Snackbar.make(fab,"You are not within the restaurant's order radius",Snackbar.LENGTH_LONG).show();
            return;
        }
        //set the type of order
        if(preOrder)
        {
            onOrderClickedPreOder();
        }
        else
        {
            new androidx.appcompat.app.AlertDialog.Builder(ShopA.this)
                    .setTitle("How will you take your order?")
                    .setItems(new String[]{"Sit in", "Take away"}, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, final int which)
                        {
                            // if pre order get collect time

                            if(tableNumber==-1 )
                            {
                                final NumberPicker numberPicker=new NumberPicker(getBaseContext());
                                numberPicker.setMinValue(1);
                                numberPicker.setMaxValue(numberOfTables);
                                new androidx.appcompat.app.AlertDialog.Builder(ShopA.this)
                                        .setTitle("Table Number?")
                                        .setView(numberPicker)
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setPositiveButton("Proceed", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                int tableNumber=numberPicker.getValue();
                                                new OrderTask(tableNumber,"null", which,"null","null","null").execute((Void)null);
                                            }
                                        })
                                        .create().show();
                            }
                            else
                                new OrderTask(tableNumber,"null", which,"null","null","null").execute((Void)null);
                        }
                    }).create().show();
        }



    }

    @Override
    public void totalItemsChanged()
    {
        fab.setText(Integer.toString(getCartCount()));
        if(getCartCount()>0)
            fab.setVisibility(View.VISIBLE);
        else
            fab.setVisibility(View.GONE);
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
        Log.d(TAG,"CART COUNT "+total_count);
        return total_count;
    }
    private void getTotal()
    {
        Iterator iterator= tempCartLinkedHashMap.entrySet().iterator();
        Double total=0.0;
        while(iterator.hasNext())
        {
            LinkedHashMap.Entry<String, Integer>set = (LinkedHashMap.Entry<String, Integer>) iterator.next();
            String id_size = set.getKey();
            String[] id_size_pieces = id_size.split(":");
            int id=Integer.parseInt(id_size_pieces[0]);
            int count=set.getValue();
            DMenu inv = menuLinkedHashMap.get(id);

            int pos = itemPriceSizeLinkedHashMap.get(id_size);
            String priceString = inv.getPrices();
           final String[] prices = priceString.split(":");
            Double price = Double.parseDouble( prices[pos].contentEquals("null")?"0":prices[pos]);

            total += count*price;

        }
        tempTotal = total;
    }
    void showProgress(boolean show)
    {
        progressBar.setVisibility(show? View.VISIBLE : View.GONE);
        mainFragment.setVisibility( show? View.INVISIBLE :View.VISIBLE);
        fab.setVisibility( show? View.INVISIBLE :View.VISIBLE);
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
    private void vibrate_on_click()
    {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator == null)
            Log.e(TAG,"No vibrator");
        else
            vibrator.vibrate(100);
    }
    /*************************************************************************************************************************************************************************************
     * implementation of OrderParamsFragment.java
     * ************************************************************************************************************************************************************************************/
    @Override
    public void onDetachCalled()
    {
        fab.hide();
    }

    @Override
    public void onPlacePreOrder(int which, String time, String mobile_mpesa, String mobile_delivery, String instructions)
    {
        new OrderTask(tableNumber,time, which, mobile_mpesa, mobile_delivery, instructions).execute((Void)null);
    }

    private class OrderTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_place_order = base_url + "place_order.php";
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
        private String orderNumber;
        private String dateAdded;


        public OrderTask(int tableNumber, String collectTime, int orderType, String mpesaMobile, String deliveryMobile, String deliveryInstructions)
        {
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
            info.add(new BasicNameValuePair("user_email",serverAccount.getEmail()));
            info.add(new BasicNameValuePair("items_ids",itemsIds));
            info.add(new BasicNameValuePair("items_sizes",itemSizes));
            info.add(new BasicNameValuePair("items_prices",itemPrices));
            info.add(new BasicNameValuePair("table_number",String.valueOf(tableNumber)));
            info.add(new BasicNameValuePair("has_payment", hasPayment? "1" : "0"));
            info.add(new BasicNameValuePair("pre_order", preOrder? "1" : "0")); // 1 means pre order 0 means not
            info.add(new BasicNameValuePair("collect_time", preOrder? collectTime : "null"));
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
                    if(!preOrder)
                        showMobileNumberDialog(orderNumber, dateAdded);
                    new LipaNaMpesaStkPush(orderNumber,dateAdded,mpesaMobile).execute((Void)null);
                }
                else
                {
                    showProgress(false);
                    Snackbar.make(fab,"Order Placed",Snackbar.LENGTH_LONG).show();
                    cartLinkedHashMap.clear();
                    tempCartLinkedHashMap.clear();
                    tempTotal = 0.0;
                    fab.setText(Integer.toString(getCartCount()));
                    fab.setVisibility(View.GONE);
                    onBackPressed();
                    play_notification();
                }
            }
            else
            {
                Snackbar.make(fab,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
        void  formData()
        {
            Iterator iterator= ShopA.tempCartLinkedHashMap.entrySet().iterator();
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
    private  void showMobileNumberDialog(final String orderNumber, final String dateAdded)
    {
        String title = "Pay with mpesa";
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(R.layout.item_dialog_mobile_number)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Dialog dialog1 = (Dialog) dialog;
                        EditText editText = dialog1.findViewById(R.id.edittext);
                        String msisdn = editText.getText().toString();
                        if(TextUtils.isEmpty(msisdn))
                        {
                            editText.setError("Please enter a mobile number");
                            return;
                        }
                        if(msisdn.contains("+254") || msisdn.startsWith("07") )
                        {
                            editText.setError("The number should begin with 254");
                            return;
                        }
                        //make the payment
                        new LipaNaMpesaStkPush(orderNumber,dateAdded,msisdn).execute((Void)null);
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        showProgress(false);
                    }
                }).create().show();
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
                String amount = String.valueOf(tempTotal.intValue());
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
                Snackbar.make(fab,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
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
            info.add(new BasicNameValuePair("buyer_email",serverAccount.getEmail()));
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
                Snackbar.make(fab,"Order Placed",Snackbar.LENGTH_LONG).show();
                cartLinkedHashMap.clear();;
                tempCartLinkedHashMap.clear();
                tempTotal = 0.0;
                fab.setText(Integer.toString(getCartCount()));
                fab.setVisibility(View.GONE);
                onBackPressed();
            }
            else if(!successful)
            {
                Snackbar.make(fab,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
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
            info.add(new BasicNameValuePair("user_email",serverAccount.getEmail()));
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
                Snackbar.make(fab,"Order Placed",Snackbar.LENGTH_LONG).show();
                cartLinkedHashMap.clear();;
                tempCartLinkedHashMap.clear();
                tempTotal = 0.0;
                fab.setText(Integer.toString(getCartCount()));
                fab.setVisibility(View.GONE);
                onBackPressed();
            }
            else if(!successful)
            {
                Snackbar.make(fab,"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
