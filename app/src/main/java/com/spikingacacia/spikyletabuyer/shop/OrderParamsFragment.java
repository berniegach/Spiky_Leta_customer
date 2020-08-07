package com.spikingacacia.spikyletabuyer.shop;

import android.content.Context;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.spikingacacia.spikyletabuyer.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderParamsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderParamsFragment extends Fragment
{
    private static final String ARG_SHOW_MPESA = "param1";
    private static final String ARG_PARAM2 = "param2";
    private boolean showMpesa;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private int which = 2;
    private String time;
    private String mobile_mpesa = "";
    private String mobile_delivery = "";
    private String instructions = "";

    public OrderParamsFragment()
    {
        // Required empty public constructor
    }

    public static OrderParamsFragment newInstance(boolean showMpesa, String param2)
    {
        OrderParamsFragment fragment = new OrderParamsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_MPESA, showMpesa);
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
            showMpesa = getArguments().getBoolean(ARG_SHOW_MPESA);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_params, container, false);

        final TimePicker timePicker = view.findViewById(R.id.time_picker);
        RadioGroup radioGroup_order_type = view.findViewById(R.id.radio_order_type);
        final CardView c_deliver_to_my_location = view.findViewById(R.id.cardview_deliver_to_my_location);
        final CardView c_instructions = view.findViewById(R.id.cardview_instructions);
        final TextView t_order_delivery_info = view.findViewById(R.id.t_order_info);
        final CardView c_mpesa = view.findViewById(R.id.cardview_mpesa);
        final CardView c_delivery_mobile = view.findViewById(R.id.cardview_delivery_mobile);
        final TextView t_m_pesa_mobile = view.findViewById(R.id.edit_payment_mobile);
        final TextView t_mobile_delivery = view.findViewById(R.id.edit_delivery_mobile);
        Button b_order = view.findViewById(R.id.button_order);


        if(!showMpesa)
            c_mpesa.setVisibility(View.GONE);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener()
        {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
            {
                if(hourOfDay == 12)
                    time =  String.format("%d:%d pm",hourOfDay, minute);
                else
                    time = hourOfDay>12? String.format("%d:%d pm",hourOfDay - 12, minute) : String.format("%d:%d am",hourOfDay, minute);
            }
        });
        radioGroup_order_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if(checkedId == R.id.radio_sit_in)
                {
                    timePicker.setVisibility(View.VISIBLE);
                    t_m_pesa_mobile.setEnabled(false);
                    t_mobile_delivery.setEnabled(false);
                    c_delivery_mobile.setVisibility(View.GONE);
                    c_instructions.setVisibility(View.GONE);
                    c_deliver_to_my_location.setVisibility(View.GONE);
                    which = 0;
                }
                else if(checkedId == R.id.radio_take_away)
                {
                    timePicker.setVisibility(View.VISIBLE);
                    t_m_pesa_mobile.setEnabled(false);
                    t_mobile_delivery.setEnabled(false);
                    c_delivery_mobile.setVisibility(View.GONE);
                    c_instructions.setVisibility(View.GONE);
                    c_deliver_to_my_location.setVisibility(View.GONE);
                    which = 1;
                }
                else if(checkedId == R.id.radio_delivery)
                {
                    timePicker.setVisibility(View.GONE);
                    t_m_pesa_mobile.setEnabled(true);
                    t_mobile_delivery.setEnabled(true);
                    c_delivery_mobile.setVisibility(View.VISIBLE);
                    c_instructions.setVisibility(View.VISIBLE);
                    c_deliver_to_my_location.setVisibility(View.VISIBLE);
                    which = 2;
                }
            }
        });
        b_order.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(which == 2)
                {
                    String msisdn = t_m_pesa_mobile.getText().toString();
                    if(showMpesa)
                    {
                        if(TextUtils.isEmpty(msisdn))
                        {
                            t_m_pesa_mobile.setError("Please enter a mobile number");
                            return;
                        }
                    }
                    if(msisdn.contains("+254") || msisdn.startsWith("07") )
                    {
                        t_m_pesa_mobile.setError("The number should begin with 254");
                        return;
                    }
                    mobile_mpesa = msisdn;
                    msisdn = t_mobile_delivery.getText().toString();
                    if(msisdn.length()<10)
                    {
                        t_mobile_delivery.setError("Please enter a valid mobile number");
                        return;
                    }
                    mobile_delivery = msisdn;
                    instructions = t_order_delivery_info.getText().toString();


                }
                if(mListener!=null)
                    mListener.onPlacePreOrder(which,time,mobile_mpesa,mobile_delivery,instructions);

            }
        });

        return view;
    }
    public interface OnFragmentInteractionListener
    {
        void onDetachCalled();
        void onPlacePreOrder(int which, String time, String mobile_mpesa, String mobile_delivery, String instructions);
        //void onEditMenu(int which, DMenu dMenu);
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof menuFragment.OnListFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        if(mListener!=null)
            mListener.onDetachCalled();
        super.onDetach();
        mListener = null;
    }
}