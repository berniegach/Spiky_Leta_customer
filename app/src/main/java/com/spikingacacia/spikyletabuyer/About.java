package com.spikingacacia.spikyletabuyer;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class About extends Preference
{
    private Context context;
    public About(Context context)
    {
        super(context);
        setLayoutResource(R.layout.about);
        this.context=context;
    }

    public About(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        setLayoutResource(R.layout.about);
        this.context=context;
    }
    public About(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context,attrs,defStyleAttr);
        setLayoutResource(R.layout.about);
        this.context=context;
    }
    @Override
    public void onBindViewHolder(PreferenceViewHolder view)
    {
        super.onBindViewHolder(view);
    }
}
