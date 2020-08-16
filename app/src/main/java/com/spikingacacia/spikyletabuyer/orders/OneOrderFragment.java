package com.spikingacacia.spikyletabuyer.orders;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.database.Orders;
import com.spikingacacia.spikyletabuyer.main.orders_list.OrdersFragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static com.spikingacacia.spikyletabuyer.LoginA.base_url;
import static com.spikingacacia.spikyletabuyer.LoginA.serverAccount;

/**
 * A placeholder fragment containing a simple view.
 */
public class OneOrderFragment extends Fragment
{

    private static final String ARG_ORDER = "order";
    private static final String ARG_FORMAT = "order_format";
    private static final String ARG_ORDER_STATUS = "order_status";
    private static final String ARG_PRE_ORDER = "pre_order";
    private String mOrder;
    private int mOrderFormat;
    private int mOrderStatus;
    private int mPreOrder;
    private OnFragmentInteractionListener mListener;
    private String TAG = "one_order_f";
    private String sellerEmail;
    private int orderNumber;
    private String dateAdded;
    private int total;


    public static OneOrderFragment newInstance(String order, int format, int status, int pre_order)
    {
        OneOrderFragment fragment = new OneOrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER, order);
        args.putInt(ARG_FORMAT, format);
        args.putInt(ARG_ORDER_STATUS, status);
        args.putInt(ARG_PRE_ORDER, pre_order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mOrder = getArguments().getString(ARG_ORDER);
            mOrderFormat = getArguments().getInt(ARG_FORMAT);
            mOrderStatus = getArguments().getInt(ARG_ORDER_STATUS);
            mPreOrder = getArguments().getInt(ARG_PRE_ORDER);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_one_orders, container, false);
        ProgressBar progressBar=view.findViewById(R.id.progress);
        LinearLayout l_base=view.findViewById(R.id.orders_base);
        TextView t_date=view.findViewById(R.id.date);
        TextView t_username=view.findViewById(R.id.username);
        TextView t_table=view.findViewById(R.id.table);
        TextView t_waiter=view.findViewById(R.id.waiter);
        TextView t_status = view.findViewById(R.id.status);
        CardView c_table = view.findViewById(R.id.c_table);
        CardView c_pre_order = view.findViewById(R.id.pre_order);
        CardView c_collect_time = view.findViewById(R.id.c_collect_time);
        TextView t_collect_time = view.findViewById(R.id.collect_time);
        CardView c_paid = view.findViewById(R.id.paid);
        TextView t_order_type = view.findViewById(R.id.order_type);
        LinearLayout l_payment_failed = view.findViewById(R.id.l_payment_failed);
        Button b_delete = view.findViewById(R.id.b_delete);
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
       //the order status are
        // -3 for new order, -2 = unpaid, -1 = paid, 0 = deleted, 1 = pending, 2 = ..... until 5 = finished
        String[] status_strings_1 = new String[]{"pending","in progress","delivery","payment","finished"};
        String[] status_strings_2 = new String[]{"pending","payment","in progress","delivery","finished"};

        if(mPreOrder == 1)
        {
            c_pre_order.setVisibility(View.VISIBLE);
            c_collect_time.setVisibility(View.VISIBLE);
        }
        if(mOrderStatus == -1)
        {
            t_status.setText(status_strings_1[0]);
            c_paid.setVisibility(View.VISIBLE);
        }
        else if( mOrderStatus == -2)
        {
            //payment not gone through yet
        }
        else if( mOrderStatus == -3)
        {
            //payment refused
            l_payment_failed.setVisibility(View.VISIBLE);
            b_pay.setVisibility(View.VISIBLE);
        }
        else
            t_status.setText(mOrderFormat==1?status_strings_1[mOrderStatus-1]:status_strings_2[mOrderStatus-1]);
        //show the respective buttons and change their labels accordingly
        //for pending the buttons remain as they are
        if(mOrderFormat==1)
        {
            //pending--in progress--delivery--payment--finished
            if(mOrderStatus == 4)
                ;//b_pay.setVisibility(View.VISIBLE);

        }
        else
        {
            //pending--payment--in progress--delivery--finished
            if(mOrderStatus == 2)
                ;//b_pay.setVisibility(View.VISIBLE);
        }
        String username="";
        int table=0;
        int count=0;
        Double total_price=0.0;
        String date_to_show="";
        String waiter="";
        String collect_time="";
        int i_order_type=0;
        Iterator iterator= OrdersFragment.ordersLinkedHashMap.entrySet().iterator();
        while (iterator.hasNext())
        {
            LinkedHashMap.Entry<Integer, Orders>set=(LinkedHashMap.Entry<Integer, Orders>) iterator.next();
            Orders orders =set.getValue();
            int itemId= orders.getItemId();
            int order_number= orders.getOrderNumber();
            int orderStatus= orders.getOrderStatus();
            String orderName= orders.getItem();
            orderName=orderName.replace("_"," ");
            String size = orders.getSize();
            double price= orders.getPrice();
            String date_added= orders.getDateAdded();
            String[] date=date_added.split(" ");
            if(!(date[0]+":"+order_number).contentEquals(mOrder))
                continue;
            username= orders.getSellerNames();
            waiter= orders.getWaiterNames();
            table= orders.getTableNumber();
            collect_time = orders.getCollectTime();
            i_order_type = orders.getOrderType();
            if(count==0)
            {
                progressBar.setProgress(orderStatus);
            }
            sellerEmail = orders.getSellerEmail();
            orderNumber = orders.getOrderNumber();
            dateAdded = orders.getDateAdded();
            //add the layouts
            //cardview
            View layout = inflater.inflate(R.layout.order_cardview_layout,null);
            TextView t_count = layout.findViewById(R.id.count);
            TextView t_item = layout.findViewById(R.id.item);
            TextView t_price = layout.findViewById(R.id.price);

            t_count.setText(String.valueOf(count+1));
            t_item.setText(orderName);
            t_price.setText(size+" @ "+String.valueOf(price));
            l_base.addView(layout);


            count+=1;
            total_price+=price;
            date_to_show=dateAdded;
        }
        ((TextView)view.findViewById(R.id.total)).setText("Total "+String.valueOf(total_price));
        total = total_price.intValue();
        // l_base.addView(t_total);
        //set date text
        t_date.setText(date_to_show);
        t_username.setText(username);
        t_collect_time.setText(collect_time);
        if(table!=-1)
            t_table.setText("Table "+table);
        else
            c_table.setVisibility(View.GONE);
        t_waiter.setText(waiter);
        String[] order_types = new String[]{"In house", "Take away", "Delivery"};
        t_order_type.setText(order_types[i_order_type]);
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
            info.add(new BasicNameValuePair("buyer_email",serverAccount.getEmail()));
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
}