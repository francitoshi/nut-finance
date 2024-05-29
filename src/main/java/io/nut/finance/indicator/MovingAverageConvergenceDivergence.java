/*
 *  MovingAverageConvergenceDivergence.java
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

import io.nut.base.util.Utils;

/**
 *
 * @author franci
 */
public class MovingAverageConvergenceDivergence implements Indicator
{
    final int fastPeriod;
    final int slowPeriod;
    final int signalPeriod;
    
    final ExponentialMovingAverage fastEma;
    final ExponentialMovingAverage slowEma;
    final ExponentialMovingAverage signalEma;
    
    final boolean smaStart;

    public MovingAverageConvergenceDivergence()
    {
        this(12, 26, 9, false);
    }
    public MovingAverageConvergenceDivergence(int fastPeriod, int slowPeriod, int signalPeriod)
    {
        this(fastPeriod, slowPeriod, signalPeriod, false);
    }
    public MovingAverageConvergenceDivergence(int fastPeriod, int slowPeriod, int signalPeriod, boolean smaStart)
    {
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
        this.fastEma = new ExponentialMovingAverage(fastPeriod, smaStart);
        this.slowEma = new ExponentialMovingAverage(slowPeriod, smaStart);
        this.signalEma = new ExponentialMovingAverage(signalPeriod, smaStart);
        this.smaStart = smaStart;
    }
    
    @Override
    public double[] get1st(double[] value)
    {
        return getAll(value)[0];
    }
    @Override
    public double[][] getAll(double[] value)
    {
        double[] fast = this.fastEma.get1st(value);
        double[] slow = this.slowEma.get1st(value);
        
        double[] macd = new double[value.length];
        for(int i=0;i<macd.length;i++)
        {
            macd[i] = fast[i]-slow[i];
        }
        
        double[] signal = this.signalEma.get1st(macd);
        
        double[] histogram = new double[value.length];
        for(int i=0;i<histogram.length;i++)
        {
            histogram[i] = macd[i]-signal[i];
        }
        return new double[][]{macd, signal, histogram};
    }

    @Override
    public int seedSize(double weight)
    {
        int fastSeed = fastEma.seedSize(weight);
        int slowSeed = slowEma.seedSize(weight);
        int signalSeed = signalEma.seedSize(weight);
        return Utils.max(fastSeed, slowSeed)+signalSeed;
    }
}
