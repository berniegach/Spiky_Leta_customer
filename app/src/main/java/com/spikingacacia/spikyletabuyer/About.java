package com.spikingacacia.spikyletabuyer;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;

public class About extends Preference
{
    public About(Context context)
    {
        super(context);
        setLayoutResource(R.layout.about);
    }

    public About(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        setLayoutResource(R.layout.about);
    }
    public About(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context,attrs,defStyleAttr);
        setLayoutResource(R.layout.about);

    }
}
