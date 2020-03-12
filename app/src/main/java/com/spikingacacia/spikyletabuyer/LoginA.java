package com.spikingacacia.spikyletabuyer;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.spikingacacia.spikyletabuyer.database.BMessages;
import com.spikingacacia.spikyletabuyer.database.BOrders;
import com.spikingacacia.spikyletabuyer.database.BuyerAccount;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class LoginA extends AppCompatActivity
    implements   SignInF.OnFragmentInteractionListener, CreateAccountF.OnFragmentInteractionListener
{
    private static final int OVERLAY_PERMISSION_CODE=541;
    //REMEMBER TO CHANGE THIS WHEN CHANGING BETWEEN ONLINE AND LOCALHOST
    public static final String base_url="https://www.spikingacacia.com/leta_project/android/"; //online
    //public static final String base_url="http://10.0.2.2/leta_project/android/"; //localhost no connection for testing user accounts coz it doesnt require subscription checking
    //buyers php files
    private String url_get_b_notifications=base_url+"get_buyer_notifications.php";
    private String url_get_b_orders=base_url+"get_buyer_orders.php";
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private String TAG="LoginActivity";
    private JSONParser jsonParser;
    private Intent intentLoginProgress;
    private static int loginProgress;
    public static boolean AppRunningInThisActivity=true;//check if the app is running the in this activity
    //whenever you add a background asynctask make sure to update the finalprogress variables accordingly
    private static int sFinalProgress=5;
    private static int bFinalProgress=2;
    //buyers
    public static BuyerAccount buyerAccount;
    public static LinkedHashMap<String, BMessages> bMessagesList;
    public static LinkedHashMap<Integer, BOrders>bOrdersList;
    public static int who;
    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout=findViewById(R.id.collapsingToolbar);
        final Typeface tf= ResourcesCompat.getFont(this,R.font.amita);
        collapsingToolbarLayout.setCollapsedTitleTypeface(tf);
        collapsingToolbarLayout.setExpandedTitleTypeface(tf);
        setSupportActionBar(toolbar);
        //preference
        preferences=new Preferences(getBaseContext());
        //dark theme prefernce
        View main_view=findViewById(R.id.main);
        if(!preferences.isDark_theme_enabled())
        {
            setTheme(R.style.AppThemeLight);
            main_view.setBackgroundColor(getResources().getColor(R.color.main_background_light));
            findViewById(R.id.sec_main).setBackgroundColor(getResources().getColor(R.color.secondary_background_light));
            ((TextView)findViewById(R.id.who)).setTextColor(getResources().getColor(R.color.text_light));
            collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.text_light));
            collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.text_light));
            collapsingToolbarLayout.setBackgroundColor(getResources().getColor(R.color.main_background_light));
        }
        //background intent
        intentLoginProgress=new Intent(LoginA.this,ProgressView.class);
        loginProgress=0;
        jsonParser=new JSONParser();
        //initialize the containers
        //buyers
        buyerAccount=new BuyerAccount();
        bMessagesList=new LinkedHashMap<>();
        bOrdersList=new LinkedHashMap<>();
        //firebase links
        if(preferences.isVerify_email() || preferences.isReset_password())
        {
            Toast.makeText(getBaseContext(),"Please wait",Toast.LENGTH_SHORT).show();
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                        @Override
                        public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                            // Get deep link from result (may be null if no link is found)
                            Uri deepLink = null;
                            if (pendingDynamicLinkData != null)
                            {
                                deepLink = pendingDynamicLinkData.getLink();
                                if(preferences.isVerify_email())
                                {
                                    setTitle("Sign Up");
                                    Fragment fragment=CreateAccountF.newInstance(1,preferences.getEmail_to_verify());
                                    FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.loginbase,fragment,"createnewaccount");
                                    transaction.addToBackStack("createaccount");
                                    transaction.commit();
                                }
                                else if(preferences.isReset_password())
                                {
                                    setTitle("Reset Password");
                                    Fragment fragment=CreateAccountF.newInstance(2,preferences.getEmail_to_reset_password());
                                    FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.loginbase,fragment,"createnewaccount");
                                    transaction.addToBackStack("createaccount");
                                    transaction.commit();
                                }

                            }


                            // Handle the deep link. For example, open the linked
                            // content, or apply promotional credit to the user's
                            // account.
                            // ...

                            // ...
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "getDynamicLink:onFailure", e);
                        }
                    });
        }

        //fragment manager
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
        {
            @Override
            public void onBackStackChanged()
            {
                int count=getSupportFragmentManager().getBackStackEntryCount();
                if(count==0)
                    setTitle("Sign In");
            }
        });
        setTitle("Sign In");
        Fragment fragment=SignInF.newInstance("","");
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.loginbase,fragment,"signin");
        transaction.commit();
    }
    @Override
    protected void onDestroy()
    {
        //super.onDestroy();
        if(intentLoginProgress!=null)
            stopService(intentLoginProgress);
        super.onDestroy();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        //clear the variables . if not done youll find some list contents add up on top of the previous ones
        loginProgress=0;
        //buyers
        if(!bMessagesList.isEmpty())bMessagesList.clear();
        if(!bOrdersList.isEmpty())bOrdersList.clear();
        AppRunningInThisActivity=true;
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==OVERLAY_PERMISSION_CODE)
        {
            if(Settings.canDrawOverlays(this))
            {
                startService(intentLoginProgress);
            }
            startBackgroundTasks();
        }
    }


    /** Implementation of SignInFragment.java**/
    @Override
    public void onSuccesfull()
    {
        //start the floating service
        if(Build.VERSION.SDK_INT>=23)
        {
            if(!Settings.canDrawOverlays(this))
            {
                //open permissions page
                Intent intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:"+getPackageName()));
                startActivityForResult(intent,OVERLAY_PERMISSION_CODE);
                //return;
            }
            else
                startBackgroundTasks();
        }
        else
            startBackgroundTasks();
    }
    private void startBackgroundTasks()
    {
        startService(intentLoginProgress);
        new BMessagesTask().execute((Void)null);
        new BOrdersTask().execute((Void)null);
        Intent intent=new Intent(this, BMenuA.class);
        intent.putExtra("NOTHING","nothing");
        startActivity(intent);
    }
    @Override
    public void createAccount()
    {
        setTitle("Sign Up");
        Fragment fragment=CreateAccountF.newInstance(0,"");
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.loginbase,fragment,"createnewaccount");
        transaction.addToBackStack("createaccount");
        transaction.commit();
    }
    /** Implementation of CreateAccountF.java**/
    @Override
    public  void onRegisterFinished()
    {
        setTitle("Sign In");
        onBackPressed();
    }
    /**
     * Following code will get the buyers notifications
     * The returned infos are id,  classes, messages, dateadded.
     * Arguments are:
     * id==boss id.
     * Returns are:
     * success==1 successful get
     * success==0 for id argument missing
     **/
    private class BMessagesTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            Log.d("BNOTIFICATIONS: ","starting....");
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("id",Integer.toString(buyerAccount.getId())));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_b_notifications,"POST",info);
            Log.d("bNotis",""+jsonObject.toString());
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
                        bMessagesList.put(String.valueOf(id),oneBMessage);
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
            Log.d("BNOTIFICATIONS: ","finished...."+"progress: "+String.valueOf(loginProgress));
            loginProgress+=1;
            if(who==0)
            {
                if (loginProgress == sFinalProgress)
                    stopService(intentLoginProgress);
            }
            else
            {
                if (loginProgress == bFinalProgress)
                    stopService(intentLoginProgress);
            }

            if (successful)
            {

            }
            else
            {

            }
        }
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
    private class BOrdersTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            Log.d("BORDERS: ","starting....");
            if(!bOrdersList.isEmpty())bOrdersList.clear();
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("userid",Integer.toString(buyerAccount.getId())));
            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_b_orders,"POST",info);
            Log.d("sItems",""+jsonObject.toString());
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

                        BOrders bOrders=new BOrders(id,item_id,order_number,orderstatus,item,selling_price, order_format,table_number,restaurant,waiter_names,dateadded);
                        bOrdersList.put(id,bOrders);
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
            Log.d("BORDERS: ","finished...."+"progress: "+String.valueOf(loginProgress));
            loginProgress+=1;
            if(who==0)
            {
                if (loginProgress == sFinalProgress)
                    stopService(intentLoginProgress);
            }
            else
            {
                if (loginProgress == bFinalProgress)
                    stopService(intentLoginProgress);
            }

            if (successful)
            {

            }
            else
            {

            }
        }
    }



}
