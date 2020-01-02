package com.spikingacacia.spikyletabuyer.shop;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BSOrderFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BSOrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BSOrderFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private  LinearLayout l_base;
    private TextView t_total;

    public BSOrderFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BSOrderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BSOrderFragment newInstance(String param1, String param2)
    {
        BSOrderFragment fragment = new BSOrderFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.f_bsorder, container, false);
        Button b_order=view.findViewById(R.id.order);
        l_base=view.findViewById(R.id.orders_base);
        t_total=view.findViewById(R.id.total);
        b_order.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(ShopA.cartCount==0)
                {
                    Snackbar.make(l_base,"Cart is empty",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(mListener!=null)
                    mListener.onOrderItems();
            }
        });
        t_total.setText("Total: "+ShopA.totalPrice);
        addOrderLayout();

        return view;
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

    public interface OnFragmentInteractionListener
    {
        void onOrderItems();
    }
    void addOrderLayout()
    {
        for(int c=0; c<ShopA.names.size(); c++)
        {
            final int index=c;
            //main layout
            final LinearLayout l_main=new LinearLayout(getContext());
            LinearLayout.LayoutParams l_main_layoutparams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            l_main.setWeightSum(10);
            l_main.setLayoutParams(l_main_layoutparams);
            l_main.setOrientation(LinearLayout.HORIZONTAL);
            //names textview
            TextView t_names=new TextView(getContext());
            t_names.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,6));
            String names=ShopA.names.get(c);
            names=names.replace("_"," ");
            t_names.setText((c+1)+" "+names);
            t_names.setPadding(16,16,16,16);

            //price textview
            TextView t_price=new TextView(getContext());
            t_price.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,3));
            t_price.setText(String.valueOf(ShopA.price.get(c)));
            //remove textview
            TextView textViewRemove = new TextView(getContext());
            textViewRemove.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_remove,0,0,0);
            textViewRemove.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1));
            textViewRemove.setPadding(16,16,16,16);
            textViewRemove.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //remove this layout
                    ShopA.totalPrice-=ShopA.price.get(index);
                    ShopA.names.remove(index);
                    ShopA.items.remove(index);
                    ShopA.price.remove(index);
                    ShopA.cartCount-=1;
                    l_base.removeAllViews();
                    t_total.setText("Total: "+ShopA.totalPrice);
                    addOrderLayout();
                }
            });
            //add the layouts
            l_main.addView(t_names);
            l_main.addView(t_price);
            l_main.addView(textViewRemove);
            l_base.addView(l_main);
            LinearLayout countLayout = new LinearLayout(getContext());
            countLayout.setOrientation(LinearLayout.HORIZONTAL);
            countLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1));
            countLayout.setGravity(Gravity.END);
        }

    }
}
