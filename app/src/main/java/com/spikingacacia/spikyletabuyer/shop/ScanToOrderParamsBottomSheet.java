package com.spikingacacia.spikyletabuyer.shop;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;

import java.io.Serializable;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     OrderParamsBottomSheet.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class ScanToOrderParamsBottomSheet extends BottomSheetDialogFragment
{

    private static final String ARG_SHOW_MPESA = "param1";
    private static final String ARG_TABLE_NUMBER = "param2";
    private static final String ARG_TABLES_COUNT = "param3";
    private static final String ARG_SUB_TOTAL = "param4";
    private static final String ARG_PAYMENT_TYPE = "param5";
    private static final String ARG_LISTENER = "param6";
    private boolean showMpesa;
    private int mTableNumber;
    private int mNumberOfTables;
    private Double mTotal;
    private int paymentType;
    private OnFragmentInteractionListener mListener;
    private String mobile_mpesa = "";
    private Preferences preferences;
    public interface OnFragmentInteractionListener extends Serializable
    {
        void onScanToOrderPlaceOrder(int which, int table, String mobile_mpesa, int payment_type);
    }

    public static ScanToOrderParamsBottomSheet newInstance(boolean showMpesa, int tableNumber, int number_of_tables, Double total, int paymentType,OnFragmentInteractionListener listener)
    {
        final ScanToOrderParamsBottomSheet fragment = new ScanToOrderParamsBottomSheet();
        final Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_MPESA, showMpesa);
        args.putInt(ARG_TABLE_NUMBER, tableNumber);
        args.putInt(ARG_TABLES_COUNT,number_of_tables);
        args.putDouble(ARG_SUB_TOTAL,total);
        args.putInt(ARG_PAYMENT_TYPE, paymentType);
        args.putSerializable(ARG_LISTENER, listener);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.scan_to_order_params_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        if (getArguments() != null)
        {
            showMpesa = getArguments().getBoolean(ARG_SHOW_MPESA);
            mTableNumber = getArguments().getInt(ARG_TABLE_NUMBER);
            mNumberOfTables = getArguments().getInt(ARG_TABLES_COUNT);
            mTotal = getArguments().getDouble(ARG_SUB_TOTAL);
            paymentType = getArguments().getInt(ARG_PAYMENT_TYPE);
            mListener = (OnFragmentInteractionListener) getArguments().getSerializable(ARG_LISTENER);
        }
        preferences = new Preferences(getContext());
        ChipGroup chipGroup = view.findViewById(R.id.chip_group);
        Chip chip_sit_in = view.findViewById(R.id.chip_sit_in);
        Chip chip_take_away = view.findViewById(R.id.chip_take_away);
        final NumberPicker numberPicker = view.findViewById(R.id.number_picker);
        final LinearLayout l_mpesa = view.findViewById(R.id.l_mpesa);
        final TextView t_m_pesa_mobile = view.findViewById(R.id.edit_payment_mobile);
        TextView t_sub_total = view.findViewById(R.id.sub_total);
        TextView t_total = view.findViewById(R.id.total);
        Button b_order = view.findViewById(R.id.button_order);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(mNumberOfTables);

        if(preferences.getMpesa_mobile()!=null)
            t_m_pesa_mobile.setText(preferences.getMpesa_mobile());

        if(!showMpesa || paymentType!=0)
            l_mpesa.setVisibility(View.GONE);
        if(mTableNumber!=-1)
        {
            numberPicker.setValue(mTableNumber);
            numberPicker.setEnabled(false);
        }

        t_sub_total.setText(String.valueOf(mTotal.intValue()));
        t_total.setText(String.valueOf(mTotal));


        b_order.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int which = 0;
                int chip_id = chipGroup.getCheckedChipId();
                if(chip_id == R.id.chip_sit_in)
                    which = 0;
                else if(chip_id == R.id.chip_take_away)
                    which = 1;
                else
                {
                    Toast.makeText(getContext(),"Please choose order type",Toast.LENGTH_SHORT).show();
                    return;
                }
                //tablenumber
                int table = numberPicker.getValue();
                //sit in and pick up
                String msisdn = t_m_pesa_mobile.getText().toString();
                if(showMpesa && paymentType == 0)
                {
                    if(TextUtils.isEmpty(msisdn))
                    {
                        t_m_pesa_mobile.setError("Please enter a mobile number");
                        return;
                    }
                    if(msisdn.contains("+254") || msisdn.startsWith("07") )
                    {
                        t_m_pesa_mobile.setError("The number should begin with 254");
                        return;
                    }
                }

                mobile_mpesa = msisdn;
                if(mListener!=null)
                {
                    if(!mobile_mpesa.contentEquals(""))
                        preferences.setMpesa_mobile(mobile_mpesa);
                    mListener.onScanToOrderPlaceOrder(which,table,mobile_mpesa, paymentType);
                    dismiss();
                }

            }
        });
    }


}