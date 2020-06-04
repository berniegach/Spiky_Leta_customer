package com.spikingacacia.spikyletabuyer.main.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.spikingacacia.spikyletabuyer.R;
import com.spikingacacia.spikyletabuyer.orders.BOOrderC;
import com.spikingacacia.spikyletabuyer.orders.BOOrderF;

public class OrderFragment extends Fragment
{
    private OnFragmentInteractionListener mListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_order, container, false);
        Button scan = view.findViewById(R.id.scan);
        Button use_my_location = view.findViewById(R.id.use_my_location);
        scan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onFindRestaurantMenuClicked(1);
            }
        });
        use_my_location.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onFindRestaurantMenuClicked(2);
            }
        });

        return view;
    }
    public interface OnFragmentInteractionListener
    {
        void onFindRestaurantMenuClicked(int id);
    }
}