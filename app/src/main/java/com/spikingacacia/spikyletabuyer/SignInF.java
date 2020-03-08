package com.spikingacacia.spikyletabuyer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.LoginA.buyerAccount;


public class SignInF extends Fragment
{
    private String TAG="SignInF";
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private boolean justStarted=true;
    private boolean justStartedLogin=true;
    private String mParam1;
    private String mParam2;
    private String TAG_SUCCESS="success";
    private String TAG_MESSAGE="message";
    private String url_get_account_buyer =base_url+"get_buyer_account.php";
    private String url_confirm_email=base_url+"confirm_buyer_account.php";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private BuyerLoginTask mAuthTaskU = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String username;
    JSONParser jsonParser;
    Preferences preferences;

    private OnFragmentInteractionListener mListener;

    public SignInF()
    {
        // Required empty public constructor
    }
    public static SignInF newInstance(String param1, String param2)
    {
        SignInF fragment = new SignInF();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        jsonParser=new JSONParser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.f_sign_in, container, false);
        //remember me
        preferences=new Preferences(getContext());
        mEmailView =  view.findViewById(R.id.email);
        mPasswordView =  view.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        //set the drawablelft programmatically coz of devices under api 21
        CommonHelper.setVectorDrawable(getContext(),mEmailView,R.drawable.ic_email,0,0,0);
        CommonHelper.setVectorDrawable(getContext(),mPasswordView,R.drawable.ic_password,0,0,0);

        Button mEmailSignInButton = (Button) view.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();

            }
        });
        //creating new account
        TextView createaccounttxtview=view.findViewById(R.id.createaccounttxtview);
        createaccounttxtview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.createAccount();
            }
        });
        ((TextView)view.findViewById(R.id.forgot_password)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String email = mEmailView.getText().toString();
                // Check for a valid email address.
                if (TextUtils.isEmpty(email) || !isEmailValid(email))
                {
                    mEmailView.setError(getString(R.string.error_field_required));
                    mEmailView.requestFocus();
                }
                else
                {
                    try
                    {
                        InputMethodManager inputMethodManager=(InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                        Toast.makeText(getContext(),"Please wait",Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e)
                    {
                        Log.e(TAG,"error in hiding keyboard"+e.getMessage());
                    }
                    new BuyerEmailConfirmTask(email).execute((Void)null);
                }
            }
        });

        mLoginFormView = view.findViewById(R.id.login_form);
        mProgressView = view.findViewById(R.id.login_progress);
        username="";
        return view;
    }
    @Override
    public void onResume()
    {
        super.onResume();
        if(preferences.isRemember_me())
        {
            mEmailView.setText(preferences.getEmail_to_remember());
            mPasswordView.setText(preferences.getPassword_to_remember());
            if(justStartedLogin)
            {
                attemptLogin();
                justStartedLogin=false;
            }
        }
        else
        {
            Log.d("notjhf","hhghhjg");
        }
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTaskU != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTaskU = new BuyerLoginTask(email, password);
            mAuthTaskU.execute((Void) null);

        }
    }
    private boolean isEmailValid(String email)
    {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        return password.length() > 4;
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public interface OnFragmentInteractionListener
    {

        void onSuccesfull();
        void createAccount();
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class BuyerLoginTask extends AsyncTask<Void, Void, Boolean>
    {

        private final String mEmail;
        private final String mPassword;
        private int success=0;

        BuyerLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
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
            info.add(new BasicNameValuePair("password",mPassword));
            //getting all account details by making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_get_account_buyer,"POST",info);
            try
            {
                success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    //seccesful
                    JSONArray accountArray=jsonObject.getJSONArray("account");
                    JSONObject accountObject=accountArray.getJSONObject(0);

                    buyerAccount.setId(accountObject.getInt("id"));
                    buyerAccount.setEmail(accountObject.getString("email"));
                    buyerAccount.setPassword(accountObject.getString("password"));
                    buyerAccount.setUsername(accountObject.getString("username"));
                    buyerAccount.setCountry(accountObject.getString("country"));
                    buyerAccount.setLocation(accountObject.getString("location"));
                    buyerAccount.setOrders(accountObject.getString("orders"));
                    buyerAccount.setDateadded(accountObject.getString("dateadded"));
                    buyerAccount.setDatechanged(accountObject.getString("datechanged"));
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
            mAuthTaskU = null;
            showProgress(false);


            if (successful) {
                username = mEmail;
                preferences.setRemember_me(true);
                preferences.setEmail_to_remember(mEmail);
                preferences.setPassword_to_remember(mPassword);
                mListener.onSuccesfull();
            }
            else
            {
                if (success==-1) {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
                else if(success==-2)
                {
                    mEmailView.setError("This email is incorrect");
                    mEmailView.requestFocus();
                }
            }
        }
        @Override
        protected void onCancelled()
        {
            mAuthTaskU = null;
            showProgress(false);
        }
    }

    public class BuyerEmailConfirmTask extends AsyncTask<Void, Void, Boolean>
    {

        private final String mEmail;
        private int success=0;

        BuyerEmailConfirmTask(String email) {
            mEmail = email;
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
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_confirm_email,"POST",info);
            try
            {
                success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
                    //seccesful
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

            if (successful) {
                ActionCodeSettings actionCodeSettings =
                        ActionCodeSettings.newBuilder()
                                // URL you want to redirect back to. The domain (www.example.com) for this
                                // URL must be whitelisted in the Firebase Console.
                                //The link will redirect the user to this URL if the app is not installed on their device and the app was not able to be installed
                                .setUrl("https://spiking-acacia-1548914219992.firebaseapp.com")
                                // This must be true
                                .setHandleCodeInApp(true)
                                .setAndroidPackageName(
                                        "com.spikingacacia.spikyletabuyer",
                                        true, /* installIfNotAvailable */
                                        "1"    /* minimumVersion */)
                                .build();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendSignInLinkToEmail(mEmail, actionCodeSettings)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Snackbar.make(mEmailView,"Email sent.\nPlease click on the link sent in the email address to continue.",Snackbar.LENGTH_LONG).show();
                                    preferences.setReset_password(true);
                                    preferences.setEmail_to_reset_password(mEmail);
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.e(TAG,"2"+e.getMessage());
                    }
                });

            }
            else if(success==-2)
            {
                mEmailView.setError("This email is incorrect");
                mEmailView.requestFocus();
            }
        }
    }
}
