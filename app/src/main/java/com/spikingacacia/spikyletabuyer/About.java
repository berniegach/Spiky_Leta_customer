package com.spikingacacia.spikyletabuyer;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class About extends Preference
{
    private Preferences preferences;
    private Context context;
    public About(Context context)
    {
        super(context);
        setLayoutResource(R.layout.about);
        this.context=context;
        preferences = new Preferences(context);
    }

    public About(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        setLayoutResource(R.layout.about);
        this.context=context;
        preferences = new Preferences(context);
    }
    public About(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context,attrs,defStyleAttr);
        setLayoutResource(R.layout.about);
        this.context=context;
        preferences = new Preferences(context);
    }
    @Override
    public void onBindViewHolder(PreferenceViewHolder view)
    {
        super.onBindViewHolder(view);
        if(!preferences.isDark_theme_enabled())
        {
            view.itemView.setBackgroundColor(context.getResources().getColor(R.color.secondary_background_light));
        }
    }
}
