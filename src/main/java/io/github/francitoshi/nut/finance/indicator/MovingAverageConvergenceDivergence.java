/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.indicator;

import io.github.francitoshi.nut.base.util.Utils;

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
