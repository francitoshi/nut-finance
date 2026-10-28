/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.indicator;

import io.github.francitoshi.nut.base.commons.lang.Round;

/**
 *
 * @author franci
 */
public class ExponentialMovingAverage implements Indicator
{
    final int period;
    final boolean smaStart;

    public ExponentialMovingAverage(int period, boolean smaStart)
    {
        this.period = period;
        this.smaStart = smaStart;
    }
    public ExponentialMovingAverage(int period)
    {
        this(period, false);
    }
    
    @Override
    public double[] get1st(double[] value)
    {
        double[] tmp = new double[value.length];

        if(period==0)
        {
            return tmp;
        }

        if(tmp.length>0)
        {
            double k = 2.0/(period+1);
            double ema = tmp[0] = value[0];

            int i = 1;
            if(smaStart)
            {
                for(;i<period;i++)
                {
                    tmp[i] = ema = ema + (value[i]/period) - (value[0]/period); // as SMA
                }
            }
            for(;i<value.length;i++)
            {
                tmp[i] = ema = ema + k*(value[i]-ema); //this is the same as value[i]*k + ema*(1-k);
            }
        }        
        return tmp;
    }

    @Override
    public double[][] getAll(double[] value)
    {
        return new double[][]{get1st(value)};
    }

    private static final Round seedRound = Round.getCeilingInstance(0);
    
    @Override
    public int seedSize(double weight)
    {
        double error = 1-weight;
        double alpha = 2.0/(period+1);
        return (int)seedRound.round( Math.log10(error) / Math.log10(1.0-alpha) );
    }
    
}

//https://bolsawallstreet.com/medias-moviles-simples-y-exponenciales/