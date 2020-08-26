package com.spikingacacia.spikyletabuyer.shop.cart;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.DMenu;
import com.spikingacacia.spikyletabuyer.main.MainActivity;

import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Locale;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CartFragment extends Fragment
{

    private static final String ARG_TEMP_TOTAL = "temp-total";
    private static final String ARG_CART = "cart";
    private static final String ARG_PRICE_SIZE = "price-size";
    private static final String ARG_MENU = "menu";
    private static Double mTempTotal;
    private static LinkedHashMap<String,Integer> mCartLinkedHashMap;
    private static LinkedHashMap<String,Integer> mItemPriceSizeLinkedHashMap;
    private static LinkedHashMap<Integer, DMenu> mMenuLinkedHashMap;
    private OnListFragmentInteractionListener mListener;

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
        TextView textViewTotal = view.findViewById(R.id.total);
        Button proceed = view.findViewById(R.id.proceed);
        String location = MainActivity.myLocation;
        String[] location_pieces = location.split(":");
        if(location_pieces.length==3)
            textViewTotal.setText(getCurrencyCode(location_pieces[2])+" "+ mTempTotal.intValue());
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
                    mListener.onProceed();
            }
        });
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
        void onProceed();
        void totalItemsChanged(LinkedHashMap<String,Integer> cartLinkedHashMap);
    }
    //to retrieve currency code
    private String getCurrencyCode(String countryCode) {
        return Currency.getInstance(new Locale("", countryCode)).getCurrencyCode();
    }

    //to retrieve currency symbol
    private String getCurrencySymbol(String countryCode) {
        return Currency.getInstance(new Locale("", countryCode)).getSymbol();
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
}
