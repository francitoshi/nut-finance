/*
 *  WeightedMovingAverage.java
 *
 *  Copyright (C) 2018-2024 francitoshi@gmail.com
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
