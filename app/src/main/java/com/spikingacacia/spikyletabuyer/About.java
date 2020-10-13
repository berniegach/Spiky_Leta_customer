/*
 * Created by Benard Gachanja on 09/10/19 4:18 PM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 6/8/20 1:03 PM
 */

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
