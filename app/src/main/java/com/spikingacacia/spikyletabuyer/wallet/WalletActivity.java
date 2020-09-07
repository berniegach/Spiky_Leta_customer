package com.spikingacacia.spikyletabuyer.wallet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.util.Utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WalletActivity extends AppCompatActivity
{
    private TextView t_amount;
    private String TAG = "wallet_a";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        t_amount = findViewById(R.id.amount);
        new WalletTask().execute((Void)null);
    }
    private class WalletTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get = LoginA.base_url+"get_wallet_buyer.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        private double wallet;

        WalletTask()
        {
            jsonParser = new JSONParser();
        }
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Log.d(TAG,"Account creation started....");        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            //building parameters
            List<NameValuePair> info=new ArrayList<NameValuePair>();
            info.add(new BasicNameValuePair("email",LoginA.getServerAccount().getEmail()));
            //getting the json object using post method
            JSONObject jsonObject=jsonParser.makeHttpRequest(url_get,"POST",info);
            Log.d("Create response",""+jsonObject.toString());
            try
            {
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    wallet = jsonObject.getDouble("wallet");
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
                String wallet_s="";
                String location = MainActivity.myLocation;
                String[] location_pieces = location.split(":");
                if(location_pieces.length==3)
                    wallet_s= Utils.getCurrencyCode(location_pieces[2])+" "+ wallet;
                else
                    wallet_s = String.valueOf(wallet);
                t_amount.setText(wallet_s);
            }

        }

    }
}