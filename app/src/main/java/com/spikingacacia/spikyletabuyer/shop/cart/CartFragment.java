package com.spikingacacia.spikyletabuyer.shop.cart;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.util.Utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CartFragment extends Fragment
{
    private String TAG = "cart_f";
    private static final String ARG_TEMP_TOTAL = "temp-total";
    private static final String ARG_CART = "cart";
    private static final String ARG_PRICE_SIZE = "price-size";
    private static final String ARG_MENU = "menu";
    private static Double mTempTotal;
    private static LinkedHashMap<String,Integer> mCartLinkedHashMap;
    private static LinkedHashMap<String,Integer> mItemPriceSizeLinkedHashMap;
    private static LinkedHashMap<Integer, DMenu> mMenuLinkedHashMap;
    private OnListFragmentInteractionListener mListener;
    private double wallet = 0.0;
    private LinearLayout l_wallet;
    private TextView t_wallet;
    private double deduction = 0.0;
    private TextView textViewTotal;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CartFragment()
    {
    }
    public static CartFragment newInstance(Double mTempTotal, LinkedHashMap<String,Integer> mCartLinkedHashMap, LinkedHashMap<String,Integer> mItemPriceSizeLinkedHashMap, LinkedHashMap<Integer, DMenu> mMenuLinkedHashMap)
    {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_TEMP_TOTAL, mTempTotal);
        args.putSerializable(ARG_CART, mCartLinkedHashMap);
        args.putSerializable(ARG_PRICE_SIZE, mItemPriceSizeLinkedHashMap);
        args.putSerializable(ARG_MENU, mMenuLinkedHashMap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mTempTotal = getArguments().getDouble(ARG_TEMP_TOTAL);
            mCartLinkedHashMap = (LinkedHashMap<String,Integer>) getArguments().getSerializable(ARG_CART);
            mItemPriceSizeLinkedHashMap = (LinkedHashMap<String,Integer>) getArguments().getSerializable(ARG_PRICE_SIZE);
            mMenuLinkedHashMap = (LinkedHashMap<Integer, DMenu>) getArguments().getSerializable(ARG_MENU);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_cart_list, container, false);
        Context context=getContext();


        RecyclerView recyclerView=view.findViewById(R.id.list);
        textViewTotal = view.findViewById(R.id.total);
        Button proceed = view.findViewById(R.id.proceed);
        l_wallet = view.findViewById(R.id.layout_wallet);
        t_wallet = view.findViewById(R.id.wallet);
        EditText e_deduct = view.findViewById(R.id.deduct);
        String location = MainActivity.myLocation;
        String[] location_pieces = location.split(":");
        if(location_pieces.length==3 && (!location_pieces[2].contentEquals("null")))
            textViewTotal.setText(Utils.getCurrencyCode(location_pieces[2])+" "+ mTempTotal.intValue());
        else
            textViewTotal.setText( mTempTotal.intValue());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        CartContent content=new CartContent();
        recyclerView.setAdapter(new MyItemRecyclerViewAdapter(content.ITEMS, mListener, getContext(), textViewTotal));
        proceed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mTempTotal<=0.0)
                    Toast.makeText(getContext(),"Cart empty",Toast.LENGTH_SHORT).show();
                else
                {
                    //first check if there is any amount to deduct

                    removeFromTempTotal(deduction);
                   mListener.onProceed(mTempTotal);
                }
            }
        });
        e_deduct.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(!TextUtils.isEmpty(s))
                {
                    int num = Integer.parseInt(s.toString());
                    //we can't allow the final total be less than 5
                    if(mTempTotal-num<5)
                        e_deduct.setError("Total amount cannot be less than 0");
                    else if(num>wallet)
                        e_deduct.setError("Too high");
                    else
                    {
                        double new_total = mTempTotal-num;
                        textViewTotal.setText(String.valueOf((int) new_total));
                        deduction = num;

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
        new WalletTask().execute((Void)null);
        return view;
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener)
        {
            mListener = (OnListFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener
    {
        void onProceed(double new_total);
        void totalItemsChanged(LinkedHashMap<String,Integer> cartLinkedHashMap);
    }

    public static Double getmTempTotal()
    {
        return mTempTotal;
    }
    public static void addToTempTotal(Double value)
    {
        mTempTotal += value;
    }
    public static void removeFromTempTotal(Double value)
    {
        mTempTotal -= value;
    }
    public static LinkedHashMap<String,Integer> getmCartLinkedHashMap()
    {
        return mCartLinkedHashMap;
    }
    public static void cartRemove( String id)
    {
        mCartLinkedHashMap.remove(id);
    }
    public static void putIntoCart(String id, int count)
    {
        mCartLinkedHashMap.put(id, count);
    }
    public static LinkedHashMap<String,Integer> getmItemPriceSizeLinkedHashMap()
    {
        return mItemPriceSizeLinkedHashMap;
    }
    public static LinkedHashMap<Integer, DMenu> getmMenuLinkedHashMap()
    {
        return mMenuLinkedHashMap;
    }
    private class WalletTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_get = LoginA.base_url+"get_wallet_buyer.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;

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
                if(wallet>0)
                {
                    l_wallet.setVisibility(View.VISIBLE);
                    t_wallet.setText(String.valueOf(wallet));
                }
            }

        }

    }
}
