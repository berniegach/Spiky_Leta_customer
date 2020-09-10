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

}
