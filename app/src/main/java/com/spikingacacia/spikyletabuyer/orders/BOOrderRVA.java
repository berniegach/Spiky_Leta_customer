package com.spikingacacia.spikyletabuyer.orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.spikingacacia.spikyletabuyer.Preferences;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.orders.BOOrderC.OrderItem;
import com.spikingacacia.spikyletabuyer.orders.BOOrderF.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link OrderItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BOOrderRVA extends RecyclerView.Adapter<BOOrderRVA.ViewHolder>
{

    private final List<OrderItem> mValues;
    private List<OrderItem> itemsCopy;
    private final OnListFragmentInteractionListener mListener;
    private Context mContext;
    private  int mWhichOrder;
    Preferences preferences;

    public BOOrderRVA(List<OrderItem> items, OnListFragmentInteractionListener listener, Context context, int whichOrder)
    {
        mValues = items;
        mListener = listener;
        itemsCopy=new ArrayList<>();
        itemsCopy.addAll(items);
        mContext=context;
        mWhichOrder=whichOrder;
        //preference
        preferences=new Preferences(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.f_boorder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        holder.mItem = mValues.get(position);
        holder.mPositionView.setText(mValues.get(position).position);
        holder.mOrderView.setText("Order "+mValues.get(position).orderNumber);
        holder.mTableView.setText("Table "+mValues.get(position).tableNumber);
        holder.mUsernameView.setText(mValues.get(position).restaurantName);
        holder.mDateView.setText(mValues.get(position).dateAdded);

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mValues.size();
    }
    public void filter(String text)
    {
        mValues.clear();
        if(text.isEmpty())
            mValues.addAll(itemsCopy);
        else
        {
            text=text.toLowerCase();
            for(OrderItem orderItem:itemsCopy)
            {
                if(orderItem.restaurantName.toLowerCase().contains(text))
                    mValues.add(orderItem);
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final TextView mPositionView;
        public final TextView mOrderView;
        public final TextView mTableView;
        public final TextView mUsernameView;
        public final TextView mDateView;
        public OrderItem mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            mPositionView = (TextView) view.findViewById(R.id.position);
            mOrderView = (TextView) view.findViewById(R.id.order_number);
            mTableView = (TextView) view.findViewById(R.id.table_number);
            mUsernameView = (TextView) view.findViewById(R.id.username);
            mDateView = (TextView) view.findViewById(R.id.date);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + mUsernameView.getText() + "'";
        }
    }
}
