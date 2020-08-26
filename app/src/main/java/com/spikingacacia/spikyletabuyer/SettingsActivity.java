package com.spikingacacia.spikyletabuyer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.spikingacacia.spikyletabuyer.database.ServerAccount;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity
{
    private static final String TITLE_TAG = "Settings";
    private UpdateAccount updateTask;
    public static boolean settingsChange;
    // public static boolean permissionsChanged;
    static private Context context;
    private static ServerAccount tempServerAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        tempServerAccount =new ServerAccount();
        tempServerAccount =LoginA.getServerAccount();
        updateTask=new UpdateAccount();
        context = getBaseContext();
    }
    @Override
    protected void onDestroy()
    {
        /*new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (settingsChanged)
                {
                    updateTask.execute((Void)null);
                }
            }
        }).start();*/
        super.onDestroy();
    }
    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            //username
            EditTextPreference preference_est=findPreference("username");
            preference_est.setText(LoginA.getServerAccount().getUsername());
            preference_est.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o)
                {
                    EditTextPreference pref = (EditTextPreference) preference;
                    String name = o.toString();
                    tempServerAccount.setUsername(name);
                    pref.setText(name);
                    updateSettings();
                    return false;
                }
            });

            //you cannot change the email
            EditTextPreference preference_est_type=findPreference("email");
            preference_est_type.setText(LoginA.getServerAccount().getEmail());

            //location
            //String[] pos=LoginA.getServerAccount().getLocation().split(",");
            //final Preference pref_location=findPreference("location");
            //pref_location.setSummary(pos.length==3?pos[2]:"Please set your location");
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragmentCompat
    {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.pref_about, rootKey);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class TermsAndConditionsPreferenceFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.pref_terms_and_conditions,rootKey);
        }

    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrivacyPolicyPreferenceFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.pref_privacy_policy,rootKey);
        }

    }
    public static void setTempServerAccountLocation(String location)
    {
        tempServerAccount.setLocation(location);
    }
    public static void updateSettings()
    {
        new UpdateAccount().execute((Void)null);
    }
    private static class UpdateAccount extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update_account= LoginA.base_url+"update_buyer_account.php";
        private JSONParser jsonParser;
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        UpdateAccount()
        {
            Log.d("settings","update started...");
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //building parameters
            List<NameValuePair> info=new ArrayList<NameValuePair>();
            info.add(new BasicNameValuePair("id",Integer.toString(tempServerAccount.getId())));
            info.add(new BasicNameValuePair("password", tempServerAccount.getPassword()));
            info.add(new BasicNameValuePair("username", tempServerAccount.getUsername()));
            info.add(new BasicNameValuePair("location", tempServerAccount.getLocation()));
            info.add(new BasicNameValuePair("image_type", tempServerAccount.getImageType()));
            //getting all account details by making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_update_account,"POST",info);
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
        protected void onPostExecute(final Boolean success)
        {
            Log.d("settings","finished");
            if(success)
            {
                Log.d("settings", "update done");
                LoginA.setServerAccount(tempServerAccount);
                //settingsChanged=false;
            }
            else
            {
                Log.e("settings", "error");
                Toast.makeText(context,"Your Account was not updated",Toast.LENGTH_LONG).show();
            }

        }
    }
    public class DeleteAccount extends AsyncTask<Void, Void, Boolean>
    {
        private String url_delete_account= LoginA.base_url+"delete_buyer_account.php";
        private JSONParser jsonParser;
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";

        DeleteAccount()
        {
            Log.d("DELETINGACCOUNT","delete started started...");
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //building parameters
            List<NameValuePair>info=new ArrayList<NameValuePair>();
            info.add(new BasicNameValuePair("id",String.valueOf(LoginA.getServerAccount().getId())));
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_delete_account,"POST",info);
            Log.d("jsonaccountdelete",jsonObject.toString());
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
        protected void onPostExecute(final Boolean success)
        {
            Log.d("settings permissions","finished");
            if(success)
            {
                Toast.makeText(context,"Account deleted",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(context,LoginA.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
            else
            {

            }

        }
    }
}