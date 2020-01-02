package com.spikingacacia.spikyletabuyer;


import android.content.Context;
import androidx.preference.Preference;
import android.util.AttributeSet;

import androidx.fragment.app.Fragment;



/**
 * A simple {@link Fragment} subclass.
 */
public class BHelpF extends Preference
{


    public BHelpF(Context context)
    {
        super(context);
        setLayoutResource(R.layout.f_bhelp);
    }

    public BHelpF(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        setLayoutResource(R.layout.f_bhelp);
    }
    public BHelpF(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context,attrs,defStyleAttr);
        setLayoutResource(R.layout.f_bhelp);

    }

}
