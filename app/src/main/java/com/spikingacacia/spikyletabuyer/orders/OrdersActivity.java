package com.spikingacacia.spikyletabuyer.orders;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.main.orders_list.OrdersFragment;
import com.spikingacacia.spikyletabuyer.shop.ShopA;
import com.spikingacacia.spikyletabuyer.util.Mpesa;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;


public class OrdersActivity extends AppCompatActivity
    implements  OneOrderFragment.OnFragmentInteractionListener
{
    private String TAG="SOOrdersA";

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
       setTitle(seller_names);

        Fragment fragment= OneOrderFragment.newInstance(unique_order_name, order_format, order_status, pre_order);
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.base,fragment,"order");
        transaction.commit();
    }


    @Override
    public void onPay(final String orderNumber, final String dateAdded, int total, String sellerEmail)
    {
        showMobileNumberDialog(orderNumber, dateAdded, total, sellerEmail);
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
                        new LipaNaMpesaStkPush(orderNumber,dateAdded,msisdn, total,sellerEmail).execute((Void)null);
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
        private int total;
        private String sellerEmail;
        //result
        String MerchantRequestID;
        String CheckoutRequestID;
        String ResponseCode;
        String ResponseDescription;

        public LipaNaMpesaStkPush(String orderNumber, String dateAdded, String msisdn, int total, String sellerEmail)
        {
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            //this.msisdn = "254708374149";
            this.msisdn = msisdn;
            timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            this.total = total;
            this.sellerEmail = sellerEmail;
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
                String amount = String.valueOf(total);
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
                new UpdateOrderTask(orderNumber, dateAdded,"-2","0",sellerEmail).execute((Void)null);
                //also add the mpesa request for tracking
                new AddNewMpesaResponseTask(orderNumber,dateAdded,CheckoutRequestID,spikingAcaciaShortcode,password,timeStamp, sellerEmail).execute((Void)null);
                onBackPressed();
            }
            else
            {
                Snackbar.make(getWindow().getDecorView().getRootView(),"Order was not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
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
        private String sellerEmail;

        public  UpdateOrderTask(String orderNumber, String dateAdded, String orderStatus,  String updateSellerTotal, String sellerEmail)
        {
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            this.orderStatus = orderStatus; // order status for unpaid order is -1, delete is 0 and for a succesful order is 1
            this.updateSellerTotal = updateSellerTotal;
            this.sellerEmail = sellerEmail;
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
            if (successful )
            {
                Snackbar.make(getWindow().getDecorView().getRootView(),"Payment request sent",Snackbar.LENGTH_LONG).show();
                onBackPressed();
            }
            else if(!successful)
            {
                Snackbar.make(getWindow().getDecorView().getRootView(),"Payment not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
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
        private String sellerEmail;

        public AddNewMpesaResponseTask(String orderNumber, String dateAdded, String checkoutRequestID, String businessShortcode, String password, String timestamp, String sellerEmail)
        {
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            CheckoutRequestID = checkoutRequestID;
            this.businessShortcode = businessShortcode;
            this.password = password;
            this.timestamp = timestamp;
            this.sellerEmail =sellerEmail;
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("user_email", LoginA.getServerAccount().getEmail()));
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
            if (successful)
            {
                //Snackbar.make(getWindow().getDecorView().getRootView(),"Order Paid",Snackbar.LENGTH_LONG).show();
            }
            else if(!successful)
            {
                Snackbar.make(getWindow().getDecorView().getRootView(),"Payment not successful.\nPlease try again.",Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
