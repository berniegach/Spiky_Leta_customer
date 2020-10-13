/*
 * Created by Benard Gachanja on 25/07/20 9:28 AM
 * Copyright (c) 2020 . Spiking Acacia. All rights reserved.
 * Last modified 7/25/20 9:29 AM
 */

package com.spikingacacia.spikyletabuyer;

public class MyBounceInterpolator implements android.view.animation.Interpolator
{
    private double mAmplitude = 1;
    private double mFrequency = 10;

        public MyBounceInterpolator(double amplitude, double frequency)
        {
        mAmplitude = amplitude;
        mFrequency = frequency;
        }

    public float getInterpolation(float time)
    {
            return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) *
            Math.cos(mFrequency * time) + 1);
    }
}
