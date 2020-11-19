/*
 * Created by Benard Gachanja on 10/13/20 5:26 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 9/23/20 3:23 PM
 */

package com.spikingacacia.spikyletabuyer.orders;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.main.MainActivity;
import com.spikingacacia.spikyletabuyer.util.Utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;

/**
 * A placeholder fragment containing a simple view.
 */
public class OneOrderFragment extends Fragment
{
    private static final String ARG_ORDERS = "arg1";
    private static final String ARG_STATUS = "arg2";
    private List<Orders> ordersList;
    private CheckPaymentTask checkPaymentTask;
    private OnFragmentInteractionListener mListener;
    private String TAG = "one_order_f";
    private String sellerEmail;
    private int orderNumber;
    private String dateAdded;
    private int total;


    public static OneOrderFragment newInstance(String order, int format, int status, int pre_order, List<Orders> ordersList)
    {
        OneOrderFragment fragment = new OneOrderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDERS, (Serializable) ordersList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            ordersList = (List<Orders>) getArguments().getSerializable(ARG_ORDERS);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_one_orders, container, false);
        LinearLayout l_base=view.findViewById(R.id.orders_base);
        TextView t_date=view.findViewById(R.id.date);
        TextView t_username=view.findViewById(R.id.username);
        TextView t_order_number = view.findViewById(R.id.order_number);
        TextView t_table=view.findViewById(R.id.table);
        TextView t_waiter=view.findViewById(R.id.waiter);
        TextView t_status = view.findViewById(R.id.status);
        TextView t_collect_time = view.findViewById(R.id.collect_time);
        TextView t_order_type = view.findViewById(R.id.order_type);
        Button b_delete = view.findViewById(R.id.b_delete);
        Button b_check_payment = view.findViewById(R.id.check_payment);
        Button b_am_here = view.findViewById(R.id.am_here);
        ImageView image_qr_code = view.findViewById(R.id.qr_code);
        TextView t_payment_type = view.findViewById(R.id.payment_type);
        Button b_cancel = view.findViewById(R.id.cancel);

        //set the buttons listeners
        Button b_pay=view.findViewById(R.id.pay);
        b_pay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener!=null)
                    mListener.onPay(String.valueOf(orderNumber),dateAdded, total, sellerEmail);
            }
        });
        b_delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new UpdateOrderTask(sellerEmail,String.valueOf(orderNumber),dateAdded,"0","0").execute((Void)null);
            }
        });
        b_check_payment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(checkPaymentTask == null  || checkPaymentTask.getStatus() != AsyncTask.Status.RUNNING)
                {
                    checkPaymentTask = new CheckPaymentTask(sellerEmail,String.valueOf(orderNumber),dateAdded);
                    checkPaymentTask.execute((Void)null);
                }
                else
                {
                    Toast.makeText(getContext(),"Task already running",Toast.LENGTH_SHORT).show();
                }
            }
        });
        b_am_here.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener!=null)
                    mListener.onAmHereClicked(String.valueOf(orderNumber),dateAdded, sellerEmail);
            }
        });
        b_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(getContext())
                        .setTitle("Cancel Order")
                        .setMessage("Are you sure you want to cancel this order?\nThe amount paid will be credited to your wallet.")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                new UpdateOrderTask(sellerEmail,String.valueOf(orderNumber),dateAdded,"0","0").execute((Void)null);
                            }
                        }).create().show();
            }
        });
       //the order status are
        // -3 for new order, -2 = unpaid, -1 = paid, 0 = deleted, 1 = pending, 2 = ..... until 5 = finished
        String[] status_strings_1 = new String[]{"pending","in progress","delivery","payment","finished"};
        String[] status_strings_2 = new String[]{"pending","payment","in progress","delivery","finished"};
        int mOrderStatus = ordersList.get(0).getOrderStatus();
        Log.d(TAG,"ORDER STATUS =="+mOrderStatus);
        if( mOrderStatus == -2)
        {
            //payment not gone through yet
            b_check_payment.setEnabled(true);
        }
        else if(mOrderStatus == -1)
        {
            b_cancel.setEnabled(true);
        }
        else if( mOrderStatus == -3)
        {
            //payment refused
            ;//l_payment_failed.setVisibility(View.VISIBLE);
            b_pay.setEnabled(true);
            b_delete.setEnabled(true);
        }
        String username="";
        int table=0;
        int count=0;
        Double total_price=0.0;
        String date_to_show="";
        String waiter="";
        String collect_time="";
        int payment_type = -1;
        int i_order_type=0;
        String url_code_start_delivery ="";
        String url_code_end_delivery = "";
        int order_number = 0;
        int mPreOrder = 0;
        for(int c = 0; c<ordersList.size(); c++)
        {
            //Orders orders = set.getValue();
            Orders orders = ordersList.get(c);
            order_number = orders.getOrderNumber();
            String orderName = orders.getItem();
            orderName = orderName.replace("_", " ");
            String size = orders.getSize();
            double price = orders.getPrice();
            String date_added_local = orders.getDateAddedLocal();
            username = orders.getSellerNames();
            waiter = orders.getWaiterNames();
            table = orders.getTableNumber();
            collect_time = orders.getCollectTime();
            payment_type = orders.getPaymentType();
            i_order_type = orders.getOrderType();

            if (mOrderStatus == 3 || mOrderStatus == 4)
            {
                url_code_start_delivery = orders.getUrlCodeStartDelivery();
                url_code_end_delivery = orders.getUrlCodeEndDelivery();
            }
            sellerEmail = orders.getSellerEmail();
            orderNumber = orders.getOrderNumber();
            dateAdded = orders.getDateAdded();
            mPreOrder = orders.getPreOrder();
            //add the layouts
            //cardview
            View layout = inflater.inflate(R.layout.order_cardview_layout, null);
            TextView t_count = layout.findViewById(R.id.count);
            TextView t_item = layout.findViewById(R.id.item);
            TextView t_price = layout.findViewById(R.id.price);

            t_count.setText(String.valueOf(count + 1));
            t_item.setText(orderName);
            t_price.setText(size + " @ " + String.valueOf(price));
            l_base.addView(layout);


            count += 1;
            total_price += price;
            date_to_show = date_added_local;
        }
        ((TextView)view.findViewById(R.id.total)).setText("Total "+String.valueOf(total_price));
        total = total_price.intValue();
        // l_base.addView(t_total);
        //set date text
        t_date.setText(date_to_show);
        t_username.setText(username);
        t_collect_time.setText(collect_time);
        t_order_number.setText("Order "+String.valueOf(order_number));
        if(table!=-1)
            t_table.setText("Table "+table);
        t_waiter.setText(" served by "+waiter);
        String[] order_types = new String[]{"In house", "Take away", "Delivery"};
        t_order_type.setText(order_types[i_order_type]);
        if(table == -1 && mPreOrder == 1 && mOrderStatus>2)
            b_am_here.setEnabled(true);
        //set order status
        String status;
        switch(mOrderStatus)
        {
            case -3:
                status = "Unpaid";
                break;
            case -2:
                status = "Processing payment";
                break;
            case -1:
                status = "Paid & Pending";
                break;
            case 1:
                status = "UnPaid & Pending";
                break;
            case 2:
                status = "Pending payment";
                break;
            case 3:
                status = "In progress";
                break;
            case 4:
                status = "Delivery";
                break;
            case 5:
                status = "Finished";
                break;
            default:
                status = "";
        }
        t_status.setText(status);
        if(mOrderStatus == 4)
        {
            if(url_code_end_delivery.length()>10)
            {
                image_qr_code.setVisibility(View.VISIBLE);
                image_qr_code.setImageBitmap(Utils.generateQRCode(url_code_end_delivery));
            }
        }
        //payment type
        String[] payments = new String[]{"M-Pesa","Cash"};
        if(payment_type == 0)
            t_payment_type.setText("Paid by "+payments[0]);
        else if(payment_type == 1)
        {
            if(mOrderStatus!=5)
                t_payment_type.setText("TO BE PAID BY CASH");
            else
                t_payment_type.setText("Paid by "+payments[1]);
        }

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
        void onPay(final String orderNumber, final String dateAdded, int total, String sellerEmail);
        void onAmHereClicked(final String orderNumber, final String dateAdded, String sellerEmail);
    }
    private class UpdateOrderTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update=base_url+"update_seller_order.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        final private String orderNumber;
        private final String dateAdded;
        private final String orderStatus;
        private final String updateSellerTotal;
        private final String sellerEmail;

        public  UpdateOrderTask(String sellerEmail, String orderNumber, String dateAdded, String orderStatus, String updateSellerTotal)
        {
            this.sellerEmail = sellerEmail;
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            this.orderStatus = orderStatus; // order status for unpaid order is -1, delete is 0 and for a succesful order is 1
            this.updateSellerTotal = updateSellerTotal;
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("seller_email", sellerEmail));
            info.add(new BasicNameValuePair("buyer_email", LoginA.getServerAccount().getEmail()));
            info.add(new BasicNameValuePair("waiter_email", "unavailable"));
            info.add(new BasicNameValuePair("order_number",orderNumber));
            info.add(new BasicNameValuePair("status",orderStatus));
            info.add(new BasicNameValuePair("update_seller_total",updateSellerTotal));
            info.add(new BasicNameValuePair("m_message",""));
            info.add(new BasicNameValuePair("date_added",dateAdded));

            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_update,"POST",info);
            Log.d(TAG,jsonObject.toString());
            try
            {
                int success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
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
        protected void onPostExecute(final Boolean successful)
        {
            if (successful )
            {
                Log.d(TAG,"order update succesful");
                //since there can be multiple asyntasks running at the same time m_index may generate IndexOutOfBoundsException
                requireActivity().onBackPressed();
            }
            else
            {
                Log.e(TAG,"update order failed");
            }
        }
    }
    private class CheckPaymentTask extends AsyncTask<Void, Void, Boolean>
    {
        private String url_update=base_url+"m_lnm_check.php";
        private String TAG_SUCCESS="success";
        private String TAG_MESSAGE="message";
        private JSONParser jsonParser;
        final private String orderNumber;
        private final String dateAdded;
        private final String sellerEmail;
        private int success = 0;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Toast.makeText(getContext(),"Please wait",Toast.LENGTH_LONG).show();
        }

        public  CheckPaymentTask(String sellerEmail, String orderNumber, String dateAdded)
        {
            this.sellerEmail = sellerEmail;
            this.orderNumber = orderNumber;
            this.dateAdded = dateAdded;
            jsonParser = new JSONParser();
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {
            //getting columns list
            List<NameValuePair> info=new ArrayList<NameValuePair>(); //info for staff count
            info.add(new BasicNameValuePair("user_email", LoginA.getServerAccount().getEmail()));
            info.add(new BasicNameValuePair("seller_email", sellerEmail));
            info.add(new BasicNameValuePair("order_number",orderNumber));
            info.add(new BasicNameValuePair("order_date_added",dateAdded));

            // making HTTP request
            JSONObject jsonObject= jsonParser.makeHttpRequest(url_update,"POST",info);
            //Log.d(TAG,""+jsonObject.toString());
            try
            {
                success=jsonObject.getInt(TAG_SUCCESS);
                if(success==1)
                {
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
        protected void onPostExecute(final Boolean successful)
        {
            if (successful )
            {
                Log.d(TAG,"payment check succesful");
                //since there can be multiple asyntasks running at the same time m_index may generate IndexOutOfBoundsException
                requireActivity().onBackPressed();
            }
            else
            {
                if(success == -8)
                    Toast.makeText(getContext(),"Payment already applied",Toast.LENGTH_LONG).show();
                else if(success == -9)
                    Toast.makeText(getContext(),"Payment failed. Please pay",Toast.LENGTH_LONG).show();
                Log.e(TAG,"payment check failed");
            }
        }
    }
}