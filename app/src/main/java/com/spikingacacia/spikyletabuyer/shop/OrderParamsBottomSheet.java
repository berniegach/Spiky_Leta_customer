package com.spikingacacia.spikyletabuyer.shop;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

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
public class OrderParamsBottomSheet extends BottomSheetDialogFragment
{

    private static final String ARG_SHOW_MPESA = "param1";
    private static final String ARG_DINING_OPTIONS = "param2";
    private static final String ARG_DELIVERY_CHARGE = "param3";
    private static final String ARG_SUB_TOTAL = "param4";
    private static final String ARG_PAYMENT_TYPE = "param5";
    private static final String ARG_LISTENER = "arg5";
    private boolean showMpesa;
    private String mDiningOptions;
    private Double mDeliveryCharge;
    private Double mSubTotal;
    private int paymentType;
    private OnFragmentInteractionListener mListener;
    private int which = 2;
    private String time = null;
    private String mobile_mpesa = "";
    private String mobile_delivery = "";
    private String instructions = "";
    private Preferences preferences;
    // TODO: make sure time is set since its null in the begining
    public interface OnFragmentInteractionListener extends Serializable
    {
        void onPlacePreOrder(int which, String time, String mobile_mpesa, String mobile_delivery, String instructions, int payment_type);
    }

    public static OrderParamsBottomSheet newInstance(boolean showMpesa, String dining_options, Double delivery_charge, Double sub_total, int paymentType, OnFragmentInteractionListener listener)
    {
        final OrderParamsBottomSheet fragment = new OrderParamsBottomSheet();
        final Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_MPESA, showMpesa);
        args.putString(ARG_DINING_OPTIONS, dining_options);
        args.putDouble(ARG_DELIVERY_CHARGE, delivery_charge);
        args.putDouble(ARG_SUB_TOTAL,sub_total);
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
        return inflater.inflate(R.layout.order_params_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        if (getArguments() != null)
        {
            showMpesa = getArguments().getBoolean(ARG_SHOW_MPESA);
            mDiningOptions = getArguments().getString(ARG_DINING_OPTIONS);
            mDeliveryCharge = getArguments().getDouble(ARG_DELIVERY_CHARGE);
            mSubTotal = getArguments().getDouble(ARG_SUB_TOTAL);
            paymentType = getArguments().getInt(ARG_PAYMENT_TYPE);
            mListener = (OnFragmentInteractionListener) getArguments().getSerializable(ARG_LISTENER);
        }
        preferences = new Preferences(getContext());
        ChipGroup chipGroup = view.findViewById(R.id.chip_group);
        Chip chip_sit_in = view.findViewById(R.id.chip_sit_in);
        Chip chip_take_away = view.findViewById(R.id.chip_take_away);
        Chip chip_delivery = view.findViewById(R.id.chip_delivery);
        final TimePicker timePicker = view.findViewById(R.id.time_picker);
        //final CardView c_deliver_to_my_location = view.findViewById(R.id.cardview_deliver_to_my_location);
        final LinearLayout l_instructions = view.findViewById(R.id.l_instructions);
        final TextView t_order_delivery_info = view.findViewById(R.id.t_order_info);
        final LinearLayout l_mpesa = view.findViewById(R.id.l_mpesa);
        final LinearLayout l_delivery_mobile = view.findViewById(R.id.l_delivery_contact);
        final TextView t_m_pesa_mobile = view.findViewById(R.id.edit_payment_mobile);
        final TextView t_mobile_delivery = view.findViewById(R.id.edit_delivery_mobile);
        TextView t_delivery_charges = view.findViewById(R.id.delivery_charge);
        TextView t_sub_total = view.findViewById(R.id.sub_total);
        TextView t_total = view.findViewById(R.id.total);
        Button b_order = view.findViewById(R.id.button_order);
        LinearLayout l_delivery = view.findViewById(R.id.l_delivery);

        t_delivery_charges.setText(String.valueOf(mDeliveryCharge.intValue()));
        t_sub_total.setText(String.valueOf(mSubTotal.intValue()));
        t_total.setText(String.valueOf(mDeliveryCharge.intValue()+mSubTotal));
        if(preferences.getMpesa_mobile()!=null)
            t_m_pesa_mobile.setText(preferences.getMpesa_mobile());
        if(preferences.getDelivery_mobile()!=null)
            t_mobile_delivery.setText(preferences.getDelivery_mobile());
        if(preferences.getOrder_instructions()!=null)
            t_order_delivery_info.setText(preferences.getOrder_instructions());

        if(!showMpesa  || paymentType!=0)
            l_mpesa.setVisibility(View.GONE);
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

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId)
            {
                if(checkedId == R.id.chip_sit_in)
                {
                    timePicker.setVisibility(View.VISIBLE);
                    //t_m_pesa_mobile.setEnabled(false);
                    t_mobile_delivery.setEnabled(false);
                    l_delivery_mobile.setVisibility(View.GONE);
                    l_instructions.setVisibility(View.GONE);
                    //l_deliver_to_my_location.setVisibility(View.GONE);
                    which = 0;
                    l_delivery.setVisibility(View.GONE);
                }
                else if(checkedId == R.id.chip_take_away)
                {
                    timePicker.setVisibility(View.VISIBLE);
                    //t_m_pesa_mobile.setEnabled(false);
                    t_mobile_delivery.setEnabled(false);
                    l_delivery_mobile.setVisibility(View.GONE);
                    l_instructions.setVisibility(View.GONE);
                    //l_deliver_to_my_location.setVisibility(View.GONE);
                    which = 1;
                    l_delivery.setVisibility(View.GONE);
                }
                else if(checkedId == R.id.chip_delivery)
                {
                    timePicker.setVisibility(View.GONE);
                    t_m_pesa_mobile.setEnabled(true);
                    t_mobile_delivery.setEnabled(true);
                    l_delivery_mobile.setVisibility(View.VISIBLE);
                    l_instructions.setVisibility(View.VISIBLE);
                    //l_deliver_to_my_location.setVisibility(View.VISIBLE);
                    which = 2;
                    l_delivery.setVisibility(View.VISIBLE);
                }
            }
        });
        //check dining options
        String[] s_dining_options = mDiningOptions.split(":");
        if(s_dining_options.length==1 || s_dining_options.length==0)
            s_dining_options = new String[]{"1","1","0"};
        int[] dining_options = new int[]{Integer.parseInt(s_dining_options[0]), Integer.parseInt(s_dining_options[1]), Integer.parseInt(s_dining_options[2])};
        chip_sit_in.setEnabled(dining_options[0] == 1);
        chip_take_away.setEnabled(dining_options[1] == 1);
        chip_delivery.setEnabled(dining_options[2] == 1);

        if(dining_options[0] == 0)
        {
            //sit in disabled
            if(dining_options[1] == 0)
                chip_delivery.setChecked(true);
            else
                chip_take_away.setChecked(true);
        }
        else if(dining_options[1] == 0)
        {
            //take away disabled
            if(dining_options[0] == 0)
                chip_delivery.setChecked(true);
            else
                chip_sit_in.setChecked(true);
        }
        else if(dining_options[2] == 0)
        {
            if(dining_options[0] == 0)
                chip_take_away.setChecked(true);
            else
                chip_sit_in.setChecked(true);
        }

        b_order.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(which == 2)
                {
                    //delivery only
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
                else
                {
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
                }
                if(mListener!=null)
                {
                    if(!mobile_mpesa.contentEquals(""))
                        preferences.setMpesa_mobile(mobile_mpesa);
                    if(!mobile_delivery.contentEquals(""))
                        preferences.setDelivery_mobile(mobile_delivery);
                    if(!instructions.contentEquals(""))
                        preferences.setOrder_instructions(instructions);
                    mListener.onPlacePreOrder(which,time,mobile_mpesa,mobile_delivery,instructions, paymentType);
                    dismiss();
                }

            }
        });
    }


}