/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 9/9/20 3:39 PM
 */

package com.spikingacacia.spikyletabuyer.orders;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.barcode.BarcodeCaptureActivity;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.database.Restaurants;
import com.spikingacacia.spikyletabuyer.main.MainActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;


public class OrdersActivity extends AppCompatActivity
    implements  OneOrderFragment.OnFragmentInteractionListener
{
    private String TAG="SOOrdersA";
    private String dateAdded;
    private String sellerEmail;
    private String orderNumber;
    private ProgressBar progressBar;
    private View mainView;
    ActivityResultLauncher<Intent> mGetBarcode = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                    Intent intent = result.getData();
                    try
                    {
                        Barcode barcode = intent.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        barcodeReceived(barcode);
                    } catch (NullPointerException excpetion)
                    {
                        Log.e(TAG, "no barcode");
                        // TODO: remove this its only for testing
                        //onCorrectScan();
                    }

                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
       String unique_order_name = getIntent().getStringExtra("unique_order_name");
       int order_format = getIntent().getIntExtra("order_format",1);
       int order_status = getIntent().getIntExtra("order_status",-2);
       int pre_order = getIntent().getIntExtra("pre_order",0);
       String seller_names = getIntent().getStringExtra("seller_names");
       List<Orders> ordersList = (List<Orders>) getIntent().getSerializableExtra("orders");
       setTitle("Order");
        progressBar = findViewById(R.id.progress);
        mainView = findViewById(R.id.base);

        Fragment fragment= OneOrderFragment.newInstance(unique_order_name, order_format, order_status, pre_order, ordersList);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.base,fragment,"order");
        transaction.commit();
    }
    void barcodeReceived(Barcode barcode)
    {
        showProgress(true);
        String[] loc = MainActivity.myLocation.split(":");
        new RestaurantQRTask(String.valueOf(loc[0]),String.valueOf(loc[1]),"null",barcode.displayValue).execute((Void)null);
    }
    void showProgress(boolean show)
    {
        progressBar.setVisibility(show? View.VISIBLE : View.GONE);
        mainView.setVisibility( show? View.INVISIBLE :View.VISIBLE);
    }

    @Override
    public void onPay(final String orderNumber, final String dateAdded, int total, String sellerEmail)
    {
        showMobileNumberDialog(orderNumber, dateAdded, total, sellerEmail);
    }

    @Override
    public void onAmHereClicked(String orderNumber, String dateAdded, String sellerEmail)
    {
        this.orderNumber = orderNumber;
        this.dateAdded = dateAdded;
        this.sellerEmail = sellerEmail;
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mGetBarcode.launch(intent);
    }

    private  void showMobileNumberDialog(final String orderNumber, final String dateAdded, final int total, final String sellerEmail)
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
                        //new LipaNaMpesaStkPush(orderNumber,dateAdded,msisdn, total,sellerEmail).execute((Void)null);
                        new MpesaStkPushTask(orderNumber, dateAdded, msisdn, String.valueOf(total), sellerEmail).execute((Void)null);
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        ;//showProgress(false);
                    }
                }).create().show();
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
        private final String sellerEmail;

        public MpesaStkPushTask(String orderNumber, String dateAdded,String mobile, String amount, String sellerEmail )
        {
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            this.amount = amount;
            this.mobile = mobile;
            this.sellerEmail = sellerEmail;
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
            if (successful)
            {
                Snackbar.make(getWindow().getDecorView().getRootView(),"Payment request sent",Snackbar.LENGTH_LONG).show();
                onBackPressed();
            }
            else
            {
                Snackbar.make(getWindow().getDecorView().getRootView(),"Payment not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
    }
    private class RestaurantQRTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_restaurants_qr=base_url+"get_restaurant_from_qr_code.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        //final private String country;
        final private String latitude;
        final private String longitude;
        final private String location;
        final private String barcode;
        private  int success;
        private Restaurants restaurants;


        public RestaurantQRTask( String latitude, String longitude, String location, String barcode)
        {
            this.latitude=latitude;
            this.longitude=longitude;
            this.location=location;
            this.barcode = barcode;
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
            info.add(new BasicNameValuePair("location",location));
            info.add(new BasicNameValuePair("which","1"));
            info.add(new BasicNameValuePair("url_code",barcode));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_restaurants_qr,"POST",info);
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
                    String opening_time = jsonObject_restaurants.getString("opening_time");
                    String closing_time = jsonObject_restaurants.getString("closing_time");
                    boolean opened = jsonObject_restaurants.getBoolean("opened");

                    restaurants =new Restaurants(id, email, names,distance,latitude,longitude,locality,country_code, order_radius,
                            tables, image_type, table_number, m_code, dining_options, opening_time, closing_time, opened);
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

            if (successful)
            {
                new AmHereTask(String.valueOf(restaurants.getTableNumber())).execute((Void)null);
            }
            else
            {
                showProgress(false);
                String message;
                if(barcode.contentEquals("https://play.google.com/store/apps/details?id=com.spikingacacia.spikyletabuyer"))
                    message = "Wrong QR code\nScan the bottom QR code";
                else if(success ==-3)
                    message = "You are too far away from the restaurant";
                else
                    message = "Error getting restaurants";
                Toast.makeText(getBaseContext(),message,Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class AmHereTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update=base_url+"update_buyer_order_am_here.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        private final String tableNumber;

        public AmHereTask(String tableNumber)
        {
            this.tableNumber = tableNumber;
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("table_number",tableNumber));
            info.add(new BasicNameValuePair("seller_email",sellerEmail));
            info.add(new BasicNameValuePair("order_number", orderNumber));
            info.add(new BasicNameValuePair("order_date_added",dateAdded));


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
                Snackbar.make(getWindow().getDecorView().getRootView(),"Welcome",Snackbar.LENGTH_LONG).show();
                onBackPressed();
            }
            else
            {
                Snackbar.make(getWindow().getDecorView().getRootView(),"Not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
