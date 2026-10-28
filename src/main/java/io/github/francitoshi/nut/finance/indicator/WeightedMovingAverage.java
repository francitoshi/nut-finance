/*
 * Copyright (C) 2018-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.indicator;

/**
 *
 * @author franci
 */
public class WeightedMovingAverage implements Indicator
{
    final int period;

    public WeightedMovingAverage(int period)
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
            tmp[0] = value[0];
            for(int i=1;i<value.length;i++)
            {
                double num = 0;
                double den = 0;
                for(int j=0;j<=i&&j<this.period;j++)
                {
                    int p = this.period-j;
                    num += value[i-j] * p;
                    den += p;
                }
                tmp[i] = num / den;
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
