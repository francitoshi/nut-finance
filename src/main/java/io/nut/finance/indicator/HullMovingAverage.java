/*
 * SimpleMovingAverage.java
 *
 *  Copyright (C) 2017-2024 francitoshi@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Report bugs or new features to: francitoshi@gmail.com
 */
package io.nut.finance.indicator;

/**
 *
 * @author franci
 */
public class HullMovingAverage implements Indicator
{
    //https://www.incrediblecharts.com/indicators/hull-moving-average.php
    //    Alan Hull uses three Weighted Moving Averages (WMA) in his formula:
    //    Calculate the WMA for the Period (e.g. 13 Weeks).
    //    Divide the Period by 2 and use the Integer value to calculate a second WMA.
    //    Multiply the second WMA by 2 then subtract the first WMA.
    //    Calculate the Square Root of the Period and take the Integer value.
    //    Use the resulting Integer value to calculate a third WMA of the result from the first two WMAs.
    //
    //    Notation
    //    Here is more mathematical notation for n periods:
    //    WMA(Integer(SQRT(n)),WMA(2*Integer(n/2),data) - WMA(n,data))
    //


    final int period;

    public HullMovingAverage(int period)
    {
        this.period = period;
    }
    
    @Override
    public double[] get1st(double[] value)
    {
        double[] hma = new double[value.length];
        if(period==0)
        {
            return hma;
        }

        //HMA= WMA(2*WMA(n/2) − WMA(n)),sqrt(n))
        if(hma.length>0)
        {
            int sqrt = (int)Math.sqrt(this.period);
            double[] wma1 = new WeightedMovingAverage(period).get1st(value);
            double[] wma2 = new WeightedMovingAverage(period/2).get1st(value);
            
            
            
            for(int i=0;i<value.length;i++)
            {
                hma[i] = wma2[i]*2-wma1[i];
            }
            hma = new WeightedMovingAverage(sqrt).get1st(hma);

            for(int i=0;i<wma1.length;i++)
            {
                System.out.printf("%.2f \t%.2f \t%.2f\n", wma2[i], wma1[i], hma[i]);
            }
            
        }        
        return hma;
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
