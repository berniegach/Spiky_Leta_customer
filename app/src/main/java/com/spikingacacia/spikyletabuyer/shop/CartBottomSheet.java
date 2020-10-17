/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 10/5/20 8:36 PM
 */

package com.spikingacacia.spikyletabuyer.shop;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.explore.RestaurantsFragment;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.util.Utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     CartBottomSheet.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class CartBottomSheet extends BottomSheetDialogFragment
{
    private static final String ARG_TOTAL = "arg1";
    private static final String ARG_CART = "arg2";
    private static final String ARG_PRICE_SIZE = "arg3";
    private static final String ARG_MENU = "arg4";
    private static final String ARG_HAS_MPESA = "arg5";
    private static final String ARG_PRE_ORDER = "arg6";
    private static final String ARG_LISTENER = "arg7";
    private Double mTotal;
    private LinkedHashMap<String,Integer> mCartLinkedHashMap;
    private LinkedHashMap<String,Integer> mItemPriceSizeLinkedHashMap;
    private LinkedHashMap<Integer, DMenu> mMenuLinkedHashMap;
    private boolean hasMpesa;
    private boolean preOrder;
    private List<CartItem> mValues;
    private double wallet = 0.0;
    private LinearLayout l_wallet;
    private TextView t_wallet;
    private EditText e_deduct;
    private double deduction = 0.0;
    private TextView textViewTotal;
    private TextView t_cart_count;
    private OnListFragmentInteractionListener mListener;
    private String currency = null;
    public interface OnListFragmentInteractionListener extends Serializable
    {
        void onProceed(double new_total, int payment_type, boolean pay_with_wallet_fully);
        void totalItemsChanged(LinkedHashMap<String,Integer> cartLinkedHashMap);
    }

    public static CartBottomSheet newInstance(Double mTempTotal, LinkedHashMap<String,Integer> mCartLinkedHashMap, LinkedHashMap<String,Integer> mItemPriceSizeLinkedHashMap,
                                              LinkedHashMap<Integer, DMenu> mMenuLinkedHashMap, boolean hasMpesa, boolean preOrder, OnListFragmentInteractionListener listener)
    {
        final CartBottomSheet fragment = new CartBottomSheet();
        final Bundle args = new Bundle();
        args.putDouble(ARG_TOTAL, mTempTotal);
        args.putSerializable(ARG_CART, mCartLinkedHashMap);
        args.putSerializable(ARG_PRICE_SIZE, mItemPriceSizeLinkedHashMap);
        args.putSerializable(ARG_MENU, mMenuLinkedHashMap);
        args.putBoolean(ARG_HAS_MPESA, hasMpesa);
        args.putBoolean(ARG_PRE_ORDER, preOrder);
        args.putSerializable(ARG_LISTENER, listener);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.cart_bottom_sheet_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        if(getArguments() != null)
        {
            mTotal = getArguments().getDouble(ARG_TOTAL);
            mCartLinkedHashMap = (LinkedHashMap<String,Integer>) getArguments().getSerializable(ARG_CART);
            mItemPriceSizeLinkedHashMap = (LinkedHashMap<String,Integer>) getArguments().getSerializable(ARG_PRICE_SIZE);
            mMenuLinkedHashMap = (LinkedHashMap<Integer, DMenu>) getArguments().getSerializable(ARG_MENU);
            hasMpesa = getArguments().getBoolean(ARG_HAS_MPESA);
            preOrder = getArguments().getBoolean(ARG_PRE_ORDER);
            mListener = (OnListFragmentInteractionListener) getArguments().getSerializable(ARG_LISTENER);
        }
        t_cart_count = view.findViewById(R.id.cart_count);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group);
        Chip chip_mpesa = view.findViewById(R.id.chip_mpesa);
        Chip chip_cash = view.findViewById(R.id.chip_cash);
        textViewTotal = view.findViewById(R.id.total);
        Button proceed = view.findViewById(R.id.proceed);
        l_wallet = view.findViewById(R.id.layout_wallet);
        t_wallet = view.findViewById(R.id.wallet);
        e_deduct = view.findViewById(R.id.deduct);
        String location = MainActivity.myLocation;
        String[] location_pieces = location.split(":");
        if(location_pieces.length==3 && (!location_pieces[2].contentEquals("null")))
        {
            currency = Utils.getCurrencyCode(location_pieces[2]);
            textViewTotal.setText("Total "+currency+" "+ mTotal.intValue());
        }
        else
            textViewTotal.setText( String.valueOf(mTotal.intValue()));
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new CartAdapter());
        //if the person is not in kenya offer cash only
        String[] loc = MainActivity.myLocation.split(":");
        if(loc.length==1)
        {
            Toast.makeText(getContext(),"Location unavailable",Toast.LENGTH_SHORT).show();
            dismiss();
        }
        else
        {
            if(loc[2].contentEquals("KE"))
                chip_cash.setVisibility(View.GONE);
            else
                chip_mpesa.setVisibility(View.GONE);

        }
        proceed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int payment_type = 1;
                int chip_id = chipGroup.getCheckedChipId();
                if(chip_id == R.id.chip_mpesa)
                    payment_type = 0;
                else if(chip_id == R.id.chip_cash)
                    payment_type = 1;
                else
                {
                    Toast.makeText(getContext(),"Please choose payment type",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mTotal<=0.0)
                    Toast.makeText(getContext(),"Cart empty",Toast.LENGTH_SHORT).show();
                else if(mTotal - deduction <0)
                    e_deduct.setError("Too high");
                else
                {
                    //first check if there is any amount to deduct
                    boolean pay_with_wallet_fully = false;
                    if(mTotal-deduction==0)
                        pay_with_wallet_fully = true;
                    removeFromTempTotal(deduction);
                    mListener.onProceed(mTotal, payment_type, pay_with_wallet_fully);
                    dismiss();
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
                    if(num>wallet)
                    {
                        e_deduct.setError("Too high");
                        e_deduct.setText("0");
                    }
                    else
                    {
                        double new_total = mTotal-num;
                        textViewTotal.setText("Total "+(currency != null ?currency :"")+ " "+String.valueOf((int) new_total));
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
    }
    public void addToTempTotal(Double value)
    {
        mTotal += value;
    }
    public void removeFromTempTotal(Double value)
    {
        mTotal -= value;
    }
    public void cartRemove( String id)
    {
        mCartLinkedHashMap.remove(id);
    }
    public void putIntoCart(String id, int count)
    {
        mCartLinkedHashMap.put(id, count);
    }
    private class ViewHolder extends RecyclerView.ViewHolder
    {

        public final ImageView mImageView;
        //public final TextView mPriceView;
        public final TextView mItemView;
        public final TextView mSizeView;
        public final ImageButton mPlusView;
        public final TextView mCountView;
        public final ImageButton mMinusView;
        public CartItem mItem;

        ViewHolder(LayoutInflater inflater, ViewGroup parent)
        {
            super(inflater.inflate(R.layout.cart_bottom_sheet__item, parent, false));
            mImageView = itemView.findViewById(R.id.image);
            //mPriceView = itemView.findViewById(R.id.price);
            mItemView = itemView.findViewById(R.id.item);
            mSizeView = itemView.findViewById(R.id.size);
            mPlusView = itemView.findViewById(R.id.plus);
            mCountView = itemView.findViewById(R.id.count);
            mMinusView = itemView.findViewById(R.id.minus);
        }
    }

    private class CartAdapter extends RecyclerView.Adapter<ViewHolder>
    {


        CartAdapter()
        {
            mValues = new ArrayList<>();
            int cart_count=0;
            for (LinkedHashMap.Entry<String, Integer> set : mCartLinkedHashMap.entrySet())
            {
                String id_size = set.getKey();
                String[] id_size_pieces = id_size.split(":");
                int id = Integer.parseInt(id_size_pieces[0]);
                int count = set.getValue();
                DMenu inv = mMenuLinkedHashMap.get(id);
                int serverInvId = inv.getId();
                String name = inv.getItem();
                String imageType = inv.getImageType();

                int pos = mItemPriceSizeLinkedHashMap.get(id_size);
                String priceString = inv.getPrices();
                final String[] prices = priceString.split(":");
                String[] sizes = inv.getSizes().split(":");
                Double price = 0.0;
                if(pos>=0)
                    price = Double.parseDouble(prices[pos].contentEquals("null") ? "0" : prices[pos]);
                if(pos <0)
                    pos = pos+100;
                String size = sizes[pos];
                String description = inv.getDescription();

                mValues.add(new CartItem(String.valueOf(id), serverInvId, name, price, size, description, imageType, count));
                cart_count += count;
            }
            t_cart_count.setText(cart_count+" items");
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {
            final int count= mValues.get(position).count;
            holder.mItem = mValues.get(position);
            //holder.mPriceView.setText("Ksh. "+Integer.toString(mValues.get(position).price.intValue()));
            holder.mItemView.setText(mValues.get(position).name);
            holder.mCountView.setText(Integer.toString(count));
            holder.mSizeView.setText(mValues.get(position).size + " "+(currency != null ?currency :"")+ " "+Integer.toString(mValues.get(position).price.intValue()) );

            holder.mPlusView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int count2= count + 1;
                    holder.mItem.count=count2;
                    addToTempTotal(holder.mItem.price);
                    new CountTask(count2,  mValues.get(position).id, position).execute((Void)null);
                }
            });
            holder.mMinusView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int count2= count - 1;
                    holder.mItem.count=count2;
                    removeFromTempTotal(holder.mItem.price);
                    new CountTask(count2,  mValues.get(position).id, position).execute((Void)null);
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return mValues.size();
        }
        private class CountTask extends AsyncTask<Void, Void, Boolean>
        {
            private int count;
            private String id;
            private int position;
            public CountTask(int count, String id, int position)
            {
                this.count=count;
                this.id=id;
                this.position = position;
            }
            @Override
            protected Boolean doInBackground(Void... voids)
            {
                if(count==0)
                {
                    mValues.remove(position);
                    cartRemove(id);
                }
                else
                {
                    putIntoCart(id, count);
                }
                return null;
            }

            @Override
            protected void onPostExecute(final Boolean successful)
            {
                if(count==0)
                    notifyDataSetChanged();
                else
                {
                    notifyItemChanged(position);
                }
                textViewTotal.setText("Total "+(currency != null ?currency :"")+ " "+ mTotal.intValue());
                mListener.totalItemsChanged(mCartLinkedHashMap);
            }
        }

    }
    public  class CartItem
    {
        public final String id;
        public final int inventoryId;
        public final String name;
        public final Double price;
        public final String size;
        public final String description;
        public final String imageType;
        public int count;

        public CartItem(String id, int serverInvId, String name, Double price, String size, String description, String imageType, int count)
        {
            this.id = id;
            inventoryId = serverInvId;
            this.name = name;
            this.price = price;
            this.size = size;
            this.description = description;
            this.imageType = imageType;
            this.count = count;
        }

        @Override
        public String toString()
        {
            return name;
        }
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
        }

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
                    e_deduct.setEnabled(true);
                    t_wallet.setText(String.valueOf(wallet));
                }
            }

        }

    }

}