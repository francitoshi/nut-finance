/*
 *  SimpleMovingAverage.java
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
