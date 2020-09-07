package com.spikingacacia.spikyletabuyer.shop;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanToOrderParamsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanToOrderParamsFragment extends Fragment
{
    private static final String ARG_SHOW_MPESA = "param1";
    private static final String ARG_TABLE_NUMBER = "param2";
    private static final String ARG_TABLES_COUNT = "param3";
    private boolean showMpesa;
    private int mTableNumber;
    private int mNumberOfTables;
    private OnFragmentInteractionListener mListener;
    private int which = 2;
    private String mobile_mpesa = "";
    private Preferences preferences;

    public ScanToOrderParamsFragment()
    {
        // Required empty public constructor
    }

    public static ScanToOrderParamsFragment newInstance(boolean showMpesa, int tableNumber, int number_of_tables)
    {
        ScanToOrderParamsFragment fragment = new ScanToOrderParamsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_MPESA, showMpesa);
        args.putInt(ARG_TABLE_NUMBER, tableNumber);
        args.putInt(ARG_TABLES_COUNT,number_of_tables);
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
            mTableNumber = getArguments().getInt(ARG_TABLE_NUMBER);
            mNumberOfTables = getArguments().getInt(ARG_TABLES_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_to_order_params, container, false);

        preferences = new Preferences(getContext());
        final NumberPicker numberPicker = view.findViewById(R.id.number_picker);
        RadioGroup radioGroup_order_type = view.findViewById(R.id.radio_order_type);
        RadioButton radio_sit_in = ((RadioButton)view.findViewById(R.id.radio_sit_in));
        RadioButton radio_take_away = ((RadioButton)view.findViewById(R.id.radio_take_away));
        final CardView c_mpesa = view.findViewById(R.id.cardview_mpesa);
        final TextView t_m_pesa_mobile = view.findViewById(R.id.edit_payment_mobile);
        Button b_order = view.findViewById(R.id.button_order);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(mNumberOfTables);

        if(preferences.getMpesa_mobile()!=null)
            t_m_pesa_mobile.setText(preferences.getMpesa_mobile());

        if(!showMpesa)
            c_mpesa.setVisibility(View.GONE);
        if(mTableNumber!=-1)
        {
            numberPicker.setValue(mTableNumber);
            numberPicker.setEnabled(false);
        }

        radioGroup_order_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if(checkedId == R.id.radio_sit_in)
                {
                    which = 0;
                }
                else if(checkedId == R.id.radio_take_away)
                {
                    which = 1;
                }
            }
        });


        b_order.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //tablenumber
                int table = numberPicker.getValue();
                //sit in and pick up
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
                if(mListener!=null)
                {
                    if(!mobile_mpesa.contentEquals(""))
                        preferences.setMpesa_mobile(mobile_mpesa);
                    mListener.onScanToOrderPlaceOrder(which,table,mobile_mpesa);
                }

            }
        });

        return view;
    }
    public interface OnFragmentInteractionListener
    {
        void onScanToOrderDetachCalled();
        void onScanToOrderPlaceOrder(int which, int table, String mobile_mpesa);
        //void onEditMenu(int which, DMenu dMenu);
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        if(mListener!=null)
            mListener.onScanToOrderDetachCalled();
        super.onDetach();
        mListener = null;
    }
}