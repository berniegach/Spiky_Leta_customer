package com.spikingacacia.spikyletabuyer.orders;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.JSONParser;
import com.spikingacacia.spikyletabuyer.LoginA;
import com.spikingacacia.spikyletabuyer.database.BOrders;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.spikingacacia.spikyletabuyer.LoginA.bOrdersList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BOOverviewF.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BOOverviewF#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BOOverviewF extends Fragment
{
    private static JSONParser jsonParser;
    private static String TAG_SUCCESS = "success";
    private static String TAG_MESSAGE = "message";
    private static final String ARG_ORDER_FORMAT = "order_format";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private int orderFormat;
    private TextView tOrderFormat;
    private TextView tMainCount;
    private int currentCount=0;
    private int pendingCount = 0;
    private int inProgressCount = 0;
    private int deliveryCount = 0;
    private int paymentCount = 0;
    private int finishedCount = 0;
    private LinearLayout lCurrent;
   // private LinearLayout lPending;
   // private LinearLayout lInProgress;
    //private LinearLayout lDelivery;
    //private LinearLayout lPayment;
    private LinearLayout lFinished;
    private TextView tCurrentCount;
   // private TextView tPendingCount;
   // private TextView tInProgressCount;
   // private TextView tDeliveryCount;
   // private TextView tPaymentCount;
    private TextView tFinishedCount;
   // private TextView tInProgressName;
   // private TextView tDeliveryName;
    //private TextView tPaymentName;
    private int countToShow = 0;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPreferencesEditor;

    public BOOverviewF()
    {
        // Required empty public constructor
    }

    public static BOOverviewF newInstance(int orderFormat)
    {
        BOOverviewF fragment = new BOOverviewF();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_FORMAT, orderFormat);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null)
        {
            orderFormat = getArguments().getInt(ARG_ORDER_FORMAT);
        }
        jsonParser = new JSONParser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.f_booverview, container, false);
        loginPreferences = getContext().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPreferencesEditor = loginPreferences.edit();
        //layouts
        lCurrent=((LinearLayout) view.findViewById(R.id.current));
        /*lPending = ((LinearLayout) view.findViewById(R.id.pending));
        lInProgress = ((LinearLayout) view.findViewById(R.id.inprogress));
        lDelivery = ((LinearLayout) view.findViewById(R.id.delivery));
        lPayment = ((LinearLayout) view.findViewById(R.id.payment));*/
        lFinished = ((LinearLayout) view.findViewById(R.id.finished));
        //views
        //tOrderFormat = view.findViewById(R.id.order_format);
        tMainCount = view.findViewById(R.id.main_count);
        tCurrentCount=view.findViewById(R.id.current_count);
        /*tPendingCount = view.findViewById(R.id.pending_count);
        tInProgressCount = view.findViewById(R.id.inprogress_count);
        tDeliveryCount = view.findViewById(R.id.delivery_count);
        tPaymentCount = view.findViewById(R.id.payment_count);*/
        tFinishedCount = view.findViewById(R.id.finished_count);
        //names
       /* tInProgressName = view.findViewById(R.id.inprogress_name);
        tDeliveryName = view.findViewById(R.id.delivery_name);*/
        //tPaymentName = view.findViewById(R.id.payment_name);
        //on click listeners
        lCurrent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (currentCount == 0)
                {
                    Snackbar.make(tMainCount, "Empty", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mListener != null)
                    mListener.onChoiceClicked(0, orderFormat);
            }
        });
        /*lPending.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (pendingCount == 0)
                {
                    Snackbar.make(tMainCount, "Empty", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mListener != null)
                    mListener.onChoiceClicked(1, orderFormat);
            }
        });
        lInProgress.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (inProgressCount == 0)
                {
                    Snackbar.make(tMainCount, "Empty", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mListener != null)
                    mListener.onChoiceClicked(2, orderFormat);
            }
        });
        lDelivery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (deliveryCount == 0)
                {
                    Snackbar.make(tMainCount, "Empty", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mListener != null)
                    mListener.onChoiceClicked(3, orderFormat);
            }
        });
        lPayment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (paymentCount == 0)
                {
                    Snackbar.make(tMainCount, "Empty", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mListener != null)
                    mListener.onChoiceClicked(4, orderFormat);
            }
        });*/
        lFinished.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (finishedCount == 0)
                {
                    Snackbar.make(tMainCount, "Empty", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mListener != null)
                    mListener.onChoiceClicked(5, orderFormat);
            }
        });
        return view;
    }

    @Override
    public void onResume()
    {
        //we set the following variables because of the following
        //1. so that every time we enter a task fragment and then get back to the overview the variables are set to
        //correct values otherwise they will just add to the before values
        //2. so we can set the texviews after setting the values. if not done here the texviews will show 0 during the initial run
        //3 so we can set the piechart with correct values during the initial run as above 2
        super.onResume();
        countToShow = loginPreferences.getInt("order_format_to_show_count", 0);
        orderFormat = loginPreferences.getInt("order_format", 1);
        currentCount = 0;
        pendingCount = 0;
        inProgressCount = 0;
        deliveryCount = 0;
        paymentCount = 0;
        finishedCount = 0;
        orderFormat = 1;
        setCounts();
        //set the formats
       /* if (orderFormat == 1)
        {
            tInProgressName.setText("In Progress");
            tDeliveryName.setText("Delivery");
            tPaymentName.setText("Payment");
            tOrderFormat.setText("Pay Last");
        } else
        {
            tInProgressName.setText("Payment");
            tDeliveryName.setText("In Progress");
            tPaymentName.setText("Delivery");
            tOrderFormat.setText("Pay First");
        }
        final LinearLayout[] layouts_format = new LinearLayout[]{lPending, lInProgress, lDelivery, lPayment, lFinished};
        final int[] counts = new int[]{pendingCount, inProgressCount, deliveryCount, paymentCount, finishedCount};
        for (int count = 0; count <= 4; count += 1)
        {
            if (count == countToShow)
            {
                layouts_format[count].setBackgroundColor(ContextCompat.getColor(getContext(), R.color.secondary_background));
                tMainCount.setText(String.valueOf(counts[count]));
            } else
            {
                layouts_format[count].setBackgroundColor(ContextCompat.getColor(getContext(), R.color.tertiary_background));
            }
        }*/

        //set the counts
        tMainCount.setText(String.valueOf(currentCount));
        tCurrentCount.setText(String.valueOf(currentCount));
        /*tPendingCount.setText(String.valueOf(pendingCount));
        tInProgressCount.setText(String.valueOf(inProgressCount));
        tDeliveryCount.setText(String.valueOf(deliveryCount));
        tPaymentCount.setText(String.valueOf(paymentCount));*/
        tFinishedCount.setText(String.valueOf(finishedCount));

    }
   /* @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu,inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.boorders_menu, menu);
        final MenuItem menu_format=menu.findItem(R.id.format);
        menu_format.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                new AlertDialog.Builder(getContext())
                        .setItems(new String[]{"Pay last ", "Pay first"}, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                loginPreferencesEditor.putInt("order_format",i+1);
                                loginPreferencesEditor.commit();
                                formatChanged(orderFormat,i+1);
                                orderFormat=i+1;

                            }
                        }).create().show();
                return true;
            }
        });
        final String[] strings_format_1=new String[]{"Pending", "In Progress", "Delivery", "Payment", "Finished"};
        final String[] strings_format_2=new String[]{"Pending", "Payment", "In Progress", "Delivery", "Finished"};
        final LinearLayout[] layouts_format=new LinearLayout[]{ lPending, lInProgress, lDelivery, lPayment, lFinished};
        final int[] counts=new int[]{ pendingCount, inProgressCount, deliveryCount, paymentCount, finishedCount};
        final MenuItem menu_station=menu.findItem(R.id.station);
        menu_station.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                new AlertDialog.Builder(getContext())
                        .setItems(orderFormat==1?strings_format_1:strings_format_2, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                for(int count=0; count<=4; count+=1)
                                {
                                    if(count==i)
                                    {
                                        layouts_format[count].setBackgroundColor(ContextCompat.getColor(getContext(),R.color.secondary_background));
                                        countToShow=i;
                                        loginPreferencesEditor.putInt("order_format_to_show_count",countToShow);
                                        loginPreferencesEditor.commit();
                                        tMainCount.setText(String.valueOf(counts[count]));
                                    }
                                    else
                                    {
                                        layouts_format[count].setBackgroundColor(ContextCompat.getColor(getContext(),R.color.tertiary_background));
                                    }
                                }
                            }
                        }).create().show();
                return true;
            }
        });
    }*/

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
        void onChoiceClicked(int id, int format);
    }
   /* private void formatChanged(int previous_format, int new_format)
    {
        Snackbar.make(lFinished,"Format updated",Snackbar.LENGTH_SHORT).show();
        if(new_format==1)
        {
            tInProgressName.setText("In Progress");
            tDeliveryName.setText("Delivery");
            tPaymentName.setText("Payment");
            tOrderFormat.setText("Pay Last");
        }
        else
        {
            tInProgressName.setText("Payment");
            tDeliveryName.setText("In Progress");
            tPaymentName.setText("Delivery");
            tOrderFormat.setText("Pay First");
        }
        //changes the count display
        if(previous_format!=new_format)
        {
            if(previous_format==1)
            {
                if(countToShow==1)
                    countToShow=2;
                else if(countToShow==2)
                    countToShow=3;
                else if(countToShow==3)
                    countToShow=1;
            }
            else
            {
                if(countToShow==1)
                    countToShow=3;
                else if(countToShow==2)
                    countToShow=1;
                else if(countToShow==3)
                    countToShow=2;
            }
        }
        final LinearLayout[] layouts_format=new LinearLayout[]{ lPending, lInProgress, lDelivery, lPayment, lFinished};
        for(int count=0; count<=4; count+=1)
        {
            if(count==countToShow)
            {
                layouts_format[count].setBackgroundColor(ContextCompat.getColor(getContext(),R.color.secondary_background));
            }
            else
            {
                layouts_format[count].setBackgroundColor(ContextCompat.getColor(getContext(),R.color.tertiary_background));
            }
        }
    }*/
    private void setCounts()
    {
        List<String> order_numbers = new ArrayList<>();
        Iterator iterator = bOrdersList.entrySet().iterator();
        while (iterator.hasNext())
        {
            LinkedHashMap.Entry<Integer, BOrders> set = (LinkedHashMap.Entry<Integer, BOrders>) iterator.next();
            BOrders bOrders = set.getValue();
            int order_number = bOrders.getOrderNumber();
            int order_status = bOrders.getOrderStatus();
            String date_added = bOrders.getDateAdded();
            String[] date_pieces = date_added.split(" ");
            String unique_name = date_pieces[0] + ":" + order_number + ":" + order_status;
            order_numbers.add(unique_name);
        }
        Set<String> unique = new HashSet<>(order_numbers);
        List<String> order_counts = new ArrayList<>(unique);
        Iterator<String> iterator_2 = order_counts.iterator();
        while (iterator_2.hasNext())
        {
            String unique_name = iterator_2.next();
            String[] pieces = unique_name.split(":");
            if (pieces[2].contentEquals("1"))
                pendingCount += 1;
            if (orderFormat == 1)
            {
                if (pieces[2].contentEquals("2"))
                    inProgressCount += 1;
                else if (pieces[2].contentEquals("3"))
                    deliveryCount += 1;
                else if (pieces[2].contentEquals("4"))
                    paymentCount += 1;
            } else if (orderFormat == 2)
            {
                if (pieces[2].contentEquals("2"))
                    paymentCount += 1;
                else if (pieces[2].contentEquals("3"))
                    inProgressCount += 1;
                else if (pieces[2].contentEquals("4"))
                    deliveryCount += 1;
            }
            if (pieces[2].contentEquals("5"))
                finishedCount += 1;

        }
        currentCount=pendingCount+inProgressCount+deliveryCount+paymentCount;
        //return unique.size();
    }
}
