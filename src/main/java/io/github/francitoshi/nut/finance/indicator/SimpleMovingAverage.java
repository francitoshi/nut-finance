/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.indicator;

/**
 *
 * @author franci
 */
public class SimpleMovingAverage implements Indicator
{
    final int period;

    public SimpleMovingAverage(int period)
    {
        this.period = period;
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
            double sma = tmp[0] = value[0];
            for(int i=1;i<value.length;i++)
            {
                int i_p = Math.max(i-period,0);
                tmp[i] = sma = sma + (value[i]/period) - (value[i_p]/period);
            }
        }        
        return tmp;
    }

    @Override
    public double[][] getAll(double[] value)
    {
        return new double[][]{get1st(value)};
    }

    @Override
    public int seedSize(double weight)
    {
        return period;
    }
}
