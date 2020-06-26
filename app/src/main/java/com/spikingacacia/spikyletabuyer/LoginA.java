package com.spikingacacia.spikyletabuyer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.spikingacacia.spikyletabuyer.database.Messages;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.database.ServerAccount;
import com.spikingacacia.spikyletabuyer.main.MainActivity;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class LoginA extends AppCompatActivity implements View.OnClickListener
{
    private static final int OVERLAY_PERMISSION_CODE=541;
    //REMEMBER TO CHANGE THIS WHEN CHANGING BETWEEN ONLINE AND LOCALHOST
    //public static final String base_url="https://www.spikingacacia.com/leta_project/android/"; //online
    public static final String base_url="http://10.0.2.2/leta_project/android/"; //localhost no connection for testing user accounts coz it doesnt require subscription checking
    private String TAG="LoginA";
    private Intent intentLoginProgress;
    private static int loginProgress;
    public static boolean AppRunningInThisActivity=true;//check if the app is running the in this activity
    //whenever you add a background asynctask make sure to update the finalprogress variables accordingly
    private static int sFinalProgress=5;
    private static int bFinalProgress=2;
    //buyers
    public static ServerAccount serverAccount;
    public static LinkedHashMap<String, Messages> bMessagesList;
    public static LinkedHashMap<Integer, Orders>bOrdersList;
    public static int who;
    Preferences preferences;
    public static GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 21;
    static public GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_login);
        setTitle("Login");

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //initialize the containers
        //buyers
        serverAccount =new ServerAccount();
        bMessagesList=new LinkedHashMap<>();
        bOrdersList=new LinkedHashMap<>();



    }
    @Override
    public void onStart()
    {

        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        account = GoogleSignIn.getLastSignedInAccount(this);
        //proceed to sign in
        if(account!=null)
        {
            proceedToLogin();
        }

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask)
    {
        try {
            account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            if(account!=null)
            {
                proceedToLogin();
                Log.d(TAG, "email: " + account.getEmail());
                new RegisterTask(account.getEmail(),"null").execute((Void)null);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }
    private void proceedToLogin()
    {
        Intent intent=new Intent(LoginA.this, MainActivity.class);
        //prevent this activity from flickering as we call the next one
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        //get the account details
        new LoginTask(account.getEmail()).execute((Void)null);
    }
    public class LoginTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get_account =base_url+"get_buyer_account.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private final String mEmail;
        private  JSONParser jsonParser;
        private int success=0;

        LoginTask(String email) {
            mEmail = email;
            jsonParser = new JSONParser();
        }
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //logIn=handler.LogInStaff(mEmail,mPassword);
            //building parameters
            List<NameValuePair>info=new ArrayList<NameValuePair>();
            info.add(new BasicNameValuePair("email",mEmail));
            //getting all account details by making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_account,"POST",info);
            try
            {
                success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    //seccesful
                    JSONArray accountArray=jsonObject.getJSONArray("account");
                    JSONObject accountObject=accountArray.getJSONObject(0);

                    serverAccount.setId(accountObject.getInt("id"));
                    serverAccount.setEmail(accountObject.getString("email"));
                    serverAccount.setPassword("");
                    serverAccount.setUsername(accountObject.getString("username"));
                    serverAccount.setLocation(accountObject.getString("location"));
                    serverAccount.setImageType(accountObject.getString("image_type"));
                    serverAccount.setDateadded(accountObject.getString("dateadded"));
                    serverAccount.setDatechanged(accountObject.getString("datechanged"));
                    return true;
                }
                else
                {
                    String message=jsonObject.getString(TAG_MESSAGE);
                    Log.e(TAG_MESSAGE+TAG,""+message);
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


            if (successful) {

            }
            else
            {
                Toast.makeText(getBaseContext(), "Sign in failed", Toast.LENGTH_SHORT).show();
                mGoogleSignInClient.signOut().addOnCompleteListener(LoginA.this, new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Log.d(TAG,"gmail signed out");
                    }
                });
            }
        }

    }
    public class RegisterTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_create_account = LoginA.base_url+"create_buyer_account.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        private final String mEmail;
        private final String mPassword;
        private int success;

        RegisterTask(String email, String password)
        {
            mEmail = email;
            mPassword = password;
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
            info.add(new BasicNameValuePair("email",mEmail));
            info.add(new BasicNameValuePair("password",mPassword));
            //getting the json object using post method
            JSONObject jsonObject=jsonParser.makeHttpRequest(url_create_account,"POST",info);
            Log.d("Create response",""+jsonObject.toString());
            try
            {
                success=jsonObject.getInt(TAG_SUCCESS);
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
        protected void onPostExecute(final Boolean successful) {
            if (successful)
            {
                Toast.makeText(getBaseContext(), "Successfully registered", Toast.LENGTH_SHORT).show();
            }
            else if(success==-1)
            {
                //email already there do nothing
                Log.d(TAG,"email already there");
            }
            else
            {
                Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                mGoogleSignInClient.signOut().addOnCompleteListener(LoginA.this, new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Log.d(TAG,"gmail signed out");
                    }
                });
            }
        }

    }






}
