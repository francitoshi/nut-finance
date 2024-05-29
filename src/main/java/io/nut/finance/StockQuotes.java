/*
 * StockQuotes.java
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
package io.nut.finance;

import io.nut.base.math.Stats;
import io.nut.finance.indicator.ExponentialMovingAverage;
import io.nut.finance.indicator.HullMovingAverage;
import io.nut.finance.indicator.Indicator;
import io.nut.finance.indicator.MovingAverageConvergenceDivergence;
import io.nut.finance.indicator.SimpleMovingAverage;
import io.nut.base.math.Nums;
import io.nut.base.math.Round;
import io.nut.base.time.JavaTime;
import io.nut.base.util.Sorts;
import io.nut.base.util.Utils;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author franci
 */
public class StockQuotes
{
    public enum Timeframe
    {
        Hourly, Daily, Weekly, Monthly, Yearly
    }
    public enum Field
    {
        Open(getOpen), High(getHigh), Low(getLow), Close(getClose), Volume(getVolume);
        final GetDouble get;
        Field(GetDouble get)
        {
            this.get = get;
        }
    }
    public static class Quote implements Comparable<Quote>
    {
        public final LocalDate date;
        public final double open;
        public final double high;
        public final double low;
        public final double close;
        public final double volume;
        public final double dividend;
        public Quote(LocalDate date, double open, double high, double low, double close, double volume)
        {
            this.date = date;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
            this.dividend = 0;
        }
        public Quote(LocalDate date, double open, double high, double low, double close, double volume, double dividend)
        {
            this.date = date;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
            this.dividend = dividend;
        }
        public Quote dividend(double value)
        {
            return new Quote(date, open, high, low, close, volume, value);
        }

        @Override
        public int compareTo(Quote other)
        {
            return this.date.compareTo(other.date);
        }

        private Quote merge(Quote item)
        {
            boolean lteq = this.date.compareTo(item.date)<=0;
            LocalDate dt = lteq ? this.date : item.date;
            double op = lteq ? this.open : item.open;
            double hi = Utils.maxOf(this.high, item.high);
            double lo = Utils.minOf(this.low, item.low);
            double cl = lteq ? item.close : this.close;
            double vol = this.volume + item.volume;
            double div = this.dividend + item.dividend;
            return new Quote(dt, op, hi, lo, cl, vol, div);
        }
        
    }
    private interface GetDouble
    {
        double get(Quote quote);
    }
    private static final GetDouble getOpen = new GetDouble()
    {
        @Override
        public double get(Quote quote)
        {
            return quote.open;
        }
    };
    private static final GetDouble getHigh = new GetDouble()
    {
        @Override
        public double get(Quote quote)
        {
            return quote.high;
        }
    };
    private static final GetDouble getLow = new GetDouble()
    {
        @Override
        public double get(Quote quote)
        {
            return quote.low;
        }
    };
    private static final GetDouble getClose = new GetDouble()
    {
        @Override
        public double get(Quote quote)
        {
            return quote.close;
        }
    };
    private static final GetDouble getVolume = new GetDouble()
    {
        @Override
        public double get(Quote quote)
        {
            return quote.volume;
        }
    };
    
    private final Object lock = new Object();
    private final String ticker;
    private final TreeMap<LocalDate,Quote> map = new TreeMap<>();
    private volatile boolean fixZeros = true;
    private volatile boolean applyDividend;
    private final int decimals;
    private final double step;
    private final Round roundCeiling;
    private final Round roundFloor;

    public StockQuotes()
    {
        this(null, false, 2, 0.01);
    }
    public StockQuotes(int decimals, double step)
    {
        this(null, false, decimals, step);
    }
    public StockQuotes(String ticker, boolean applyDividend, int decimals, double step)
    {
        this.ticker = ticker;
        this.applyDividend = applyDividend;
        this.decimals = decimals;
        this.step = step;
        this.roundCeiling = Round.getCeilingInstance(decimals);
        this.roundFloor = Round.getFloorInstance(decimals);
    }

    public String getTicker()
    {
        return ticker;
    }

    public void setApplyAdjust(boolean applyDividend)
    {
        this.applyDividend = applyDividend;
    }
    
    public boolean add(Quote e)
    {
        synchronized(lock)
        {
            return this.map.put(e.date, e)!=null;
        }
    }
    public boolean addDividend(LocalDate date, double dividend)
    {
        synchronized(lock)
        {
            Quote quote = this.map.get(date);
            if(quote!=null)
            {
                this.map.put(quote.date, quote.dividend(dividend));
                return true;
            }
            return false;
        }
    }
    public boolean add(LocalDate date, double open, double high, double low, double close, double volume)
    {
        return add(new Quote(date, open, high, low, close, volume, 0));
    }
    
    private double[] getValue(LocalDate start, LocalDate end, GetDouble getDouble, boolean reverseOrder)
    {
        LocalDate startTime = start!=null ? start : LocalDate.MIN;
        LocalDate endTime   = end!=null   ? end   : LocalDate.MAX;
       
        Quote[] quotes = this.map.subMap(startTime, true, endTime, true).values().toArray(new Quote[0]);
        Arrays.sort(quotes);
        boolean hasDiv = false;
        double[] data = new double[quotes.length];
        double[] div  = this.applyDividend ? new double[data.length] : null;
        for(int i=0;i<data.length;i++)
        {
            Quote item = quotes[i];
            double val = data[i] = getDouble.get(item);
            if(val==0 && fixZeros)
            {
                if(i>0)
                {
                    data[i] = data[i-1];
                }
                else if(getDouble==getHigh)
                {
                    data[i] = Utils.max(item.open,item.low,item.close);
                }
                else if(getDouble==getLow)
                {
                    data[i] = Utils.min(Utils.exclude(0.0, item.open,item.low,item.close));
                }
                else if(getDouble==getOpen)
                {
                    data[i] = Nums.avg(Utils.exclude(0.0, item.high,item.low,item.close));
                }
                else if(getDouble==getClose)
                {
                    data[i] = Nums.avg(Utils.exclude(0.0, item.open,item.high,item.low));
                }
            }
            if(div!=null)
            {
                div[i]  = item.dividend;
                hasDiv |= div[i]!=0;
            }
        }        
        if(hasDiv)
        {
            adjust(data, div);        
        }
        if(reverseOrder)
        {
            Sorts.reverse(data);
        }
        return data;
    }

    static void adjust(double[] data, double[] div)
    {
        double acum = 0.0;
        for(int i=data.length-1;i>=0;i--)
        {
            data[i] -= acum;
            acum += div[i];
        }
    }
    private double[] getValue(int count, LocalDate end, GetDouble getDouble, boolean reverseOrder)
    {
        SortedMap<LocalDate, Quote> sub = end!=null ? this.map.subMap(this.map.firstKey(), true, end, true) : this.map;
        
        ArrayList<Quote> quotes = new ArrayList<>();
        for(Map.Entry<LocalDate, Quote> entry : sub.entrySet())
        {
            quotes.add(entry.getValue());
        }
        
        Collections.sort(quotes);
        while(quotes.size()>count)
        {
            quotes.remove(0);
        }
        
        boolean hasDiv = false;
        double[] data = new double[quotes.size()];
        double[] div  = this.applyDividend ? new double[data.length] : null;
        for(int i=0;i<data.length;i++)
        {
            Quote item = quotes.get(i);
            double val = data[i] = getDouble.get(item);
            if(val==0 && fixZeros)
            {
                if(i>0)
                {
                    data[i] = data[i-1];
                }
                else if(getDouble==getHigh)
                {
                    data[i] = Utils.max(item.open,item.low,item.close);
                }
                else if(getDouble==getLow)
                {
                    data[i] = Utils.min(Utils.exclude(0.0, item.open,item.low,item.close));
                }
                else if(getDouble==getOpen)
                {
                    data[i] = Nums.avg(Utils.exclude(0.0, item.high,item.low,item.close));
                }
                else if(getDouble==getClose)
                {
                    data[i] = Nums.avg(Utils.exclude(0.0, item.open,item.high,item.low));
                }
            }
            if(div!=null)
            {
                div[i]  = item.dividend;
                hasDiv |= div[i]!=0;
            }
        }
        if(hasDiv)
        {
            adjust(data, div);        
        }
        if(reverseOrder)
        {
            Sorts.reverse(data);
        }
        return data;
    }

    public LocalDate[] getDate(LocalDate start, LocalDate end, boolean reverseOrder)
    {
        SortedMap<LocalDate, Quote> sub = this.map;
        if(start!=null && end!=null)
        {
            sub = this.map.subMap(start, true, end, true);
        }
        else if(start!=null)
        {
            sub = this.map.tailMap(start, true);
        }
        else if(end!=null)
        {
            sub = this.map.headMap(end, true);
        }
        LocalDate[] dates = sub.keySet().toArray(new LocalDate[0]);
        if(reverseOrder)
        {
            Sorts.reverse(dates);
        }
        return dates;
    }
    public double[] getOpen(LocalDate start, LocalDate end, boolean reverseOrder)
    {
        return getValue(start, end, getOpen, reverseOrder);
    }
    public double[] getHigh(LocalDate start, LocalDate end, boolean reverseOrder)
    {
        return getValue(start, end, getHigh, reverseOrder);
    }
    public double[] getLow(LocalDate start, LocalDate end, boolean reverseOrder)
    {
        return getValue(start, end, getLow, reverseOrder);
    }
    public double[] getClose(LocalDate start, LocalDate end, boolean reverseOrder)
    {
        return getValue(start, end, getClose, reverseOrder);
    }
    public double[] getVomume(LocalDate start, LocalDate end, boolean reverseOrder)
    {
        return getValue(start, end, getVolume, reverseOrder);
    }

    public LocalDate[] getDate(int count, LocalDate end, boolean reverseOrder)
    {
        SortedMap<LocalDate, Quote> sub = (end!=null) ? this.map.headMap(end, true) : this.map;
        LocalDate[] dates = sub.keySet().toArray(new LocalDate[0]);
        dates = count<dates.length ? Arrays.copyOfRange(dates, dates.length-count, dates.length) : dates;
        if(reverseOrder)
        {
            Sorts.reverse(dates);
        }
        return dates;
    }
    public double[] getOpen(int count, LocalDate end, boolean reverseOrder)
    {
        return getValue(count, end, getOpen, reverseOrder);
    }
    public double[] getHigh(int count, LocalDate end, boolean reverseOrder)
    {
        return getValue(count, end, getHigh, reverseOrder);
    }
    public double[] getLow(int count, LocalDate end, boolean reverseOrder)
    {
        return getValue(count, end, getLow, reverseOrder);
    }
    public double[] getClose(int count, LocalDate end, boolean reverseOrder)
    {
        return getValue(count, end, getClose, reverseOrder);
    }
    public double[] getVomume(int count, LocalDate end, boolean reverseOrder)
    {
        return getValue(count, end, getVolume, reverseOrder);
    }
    
    private double[] getIndicator1st(LocalDate start, LocalDate end, GetDouble getDouble, Indicator indicator, boolean reverseOrder)
    {
        double[] value = getValue(start, end, getDouble, reverseOrder);
        return indicator.get1st(value);
    }
    private double[] getIndicator1st(int count, LocalDate end, GetDouble getDouble, Indicator indicator, boolean reverseOrder)
    {
        double[] value = getValue(count, end, getDouble, reverseOrder);
        return indicator.get1st(value);
    }
    private double[][] getIndicatorAll(LocalDate start, LocalDate end, GetDouble getDouble, Indicator indicator, boolean reverseOrder)
    {
        double[] value = getValue(start, end, getDouble, reverseOrder);
        return indicator.getAll(value);
    }
    private double[][] getIndicatorAll(int count, LocalDate end, GetDouble getDouble, Indicator indicator, boolean reverseOrder)
    {
        double[] value = getValue(count, end, getDouble, reverseOrder);
        return indicator.getAll(value);
    }
    
    public double[] getSimpleMovingAverage(LocalDate start, LocalDate end, int period, boolean reverseOrder)
    {
        return getIndicator1st(start, end, getClose, new SimpleMovingAverage(period), reverseOrder);
    }
    public double[] getSimpleMovingAverage(int count, LocalDate end, int period, boolean reverseOrder)
    {
        return getIndicator1st(count, end, getClose, new SimpleMovingAverage(period), reverseOrder);
    }
    
    public double[] getExponentialMovingAverage(LocalDate start, LocalDate end, int period, boolean reverseOrder)
    {
        return getIndicator1st(start, end, getClose, new ExponentialMovingAverage(period), reverseOrder);
    }
    public double[] getExponentialMovingAverage(int count, LocalDate end, int period, boolean reverseOrder)
    {
        return getIndicator1st(count, end, getClose, new ExponentialMovingAverage(period), reverseOrder);
    }

    public double[] getHullMovingAverage(LocalDate start, LocalDate end, int period, boolean reverseOrder)
    {
        return getIndicator1st(start, end, getClose, new HullMovingAverage(period), reverseOrder);
    }
    public double[] getHullMovingAverage(int count, LocalDate end, int period, boolean reverseOrder)
    {
        return getIndicator1st(count, end, getClose, new HullMovingAverage(period), reverseOrder);
    }
    
    public double[][] getMovingAverageConvergenceDivergence(LocalDate start, LocalDate end, int fastPeriod, int slowPeriod, int signalPeriod, boolean reverseOrder)
    {
        return getIndicatorAll(start, end, getClose, new MovingAverageConvergenceDivergence(fastPeriod, slowPeriod, signalPeriod), reverseOrder);
    }
    public double[][] getMovingAverageConvergenceDivergence(int count, LocalDate end, int fastPeriod, int slowPeriod, int signalPeriod, boolean reverseOrder)
    {
        return getIndicatorAll(count, end, getClose, new MovingAverageConvergenceDivergence(fastPeriod, slowPeriod, signalPeriod), reverseOrder);
    }
    
    public enum Coverage { ByPrice, ByBar}

    public static class Calculus
    {
        public final String ticker;
        public final int count;
        public final LocalDate startAt; //day from
        public final LocalDate endAt;   //day until
        public final LocalDate firstDay;//first day found
        public final LocalDate lastDay; //last day found
        Calculus(String ticker, int count, LocalDate startAt, LocalDate endAt, LocalDate firstDay, LocalDate lastDay)
        {
            assert (startAt==null  || endAt==null   || !startAt.isAfter(endAt))    : startAt +" > "+endAt;
            assert (firstDay==null || lastDay==null || !firstDay.isAfter(lastDay)) : firstDay+" > "+lastDay;
            this.ticker = ticker;
            this.count = count;
            this.startAt = startAt;
            this.endAt = endAt;
            this.firstDay = firstDay;
            this.lastDay = lastDay;
        }

        @Override
        public boolean equals(Object o)
        {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Calculus calculus = (Calculus) o;
            return count == calculus.count &&
                   Objects.equals(ticker, calculus.ticker) &&
                   Objects.equals(startAt, calculus.startAt) &&
                   Objects.equals(endAt, calculus.endAt) &&
                   Objects.equals(firstDay, calculus.firstDay) &&
                   Objects.equals(lastDay, calculus.lastDay);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(ticker, count, startAt, endAt, firstDay, lastDay);
        }
    }
    public static class Envelope extends Calculus
    {
        public final int period;
        public final boolean exponential;
        public final double value;
        //, Coverage by, double coverage, double delta
        Envelope(String ticker, int count, LocalDate startAt, LocalDate endAt, LocalDate firstDay, LocalDate lastDay, int period, boolean exponential, double value)
        {
            super(ticker, count, startAt, endAt, firstDay, lastDay);
            this.period = period;
            this.exponential = exponential;
            this.value = value;
        }

        @Override
        public boolean equals(Object o)
        {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            if(!super.equals(o)) return false;
            Envelope envelope = (Envelope) o;
            return period == envelope.period &&
                   exponential == envelope.exponential &&
                   Double.compare(envelope.value, value) == 0;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(super.hashCode(), period, exponential, value);
        }
    }
    public Envelope getEnvelope(int count, LocalDate endAt, int period, boolean exponential, Coverage by, double coverage, double delta)
    {
        int cp = count+period;
        double [] high = getHigh(cp, endAt, false);
        double [] low = getLow(cp, endAt, false);
        double[] ma = exponential ? getExponentialMovingAverage(cp, endAt, period, false) : getSimpleMovingAverage(cp, endAt, period, false);
        LocalDate[] date = getDate(count, endAt, false);
        
        count = count<=date.length ? count : date.length;
        
        final LocalDate firstDay = date[0];
        final LocalDate lastDay = date[count-1];
        double up;
        double md;
        double dw;
        double cover = 0;
        
        //go up with powers of 2
        for(up=delta/2;cover<coverage && up<Integer.MAX_VALUE;)
        {
            up*=2;
            cover = coverage(ma, high, low, period, up, by);
        }
        for(dw=up/2,md=up;!Nums.equalsEnough(dw, up, delta);)
        {
            md = (dw+up)/2;
            cover = coverage(ma, high, low, period, md, by);
            if(cover<coverage)
            {
                dw = md;
            }
            else if(cover>coverage)
            {
                up = md;
            }
            else
            {
                break;
            }
        }
        return new Envelope(this.ticker, count, null, endAt, firstDay, lastDay, period, exponential, md);
    }
    private double coverage(double[] ma, double[] high, double[] low, int period, double channel, Coverage by)
    {
        double range = 0;
        double cover = 0;
        
        for(int i=period;i< ma.length ;i++)
        {
            double hi = high[i];
            double lo = low[i];
            assert(hi-lo>=0) : "hi-lo<0";
            double avg = ma[i];
            double hi2 = Math.min(hi,avg+avg*channel);
            double lo2 = Math.max(lo,avg-avg*channel);
            switch(by)
            {
                case ByPrice:
                    range += hi-lo;
                    cover += Math.max(hi2-lo2,0.0);
                    break;
                case ByBar:
                    range++;
                    cover += (hi2>=hi && lo2<=lo ? 1 : 0);
            }
        }
        return cover/range;
    }

    public static class Gap extends Calculus
    {
        public final int stepCount;
        public final int gapCountUp;
        public final int gapCountDown;

        public final double coverage;

        public final double upMin;
        public final double upMax;
        public final double upAvg;
        public final double upMedian;
        public final double upStandardDeviation;
        public final double upCoverGap;

        public final double downMin;
        public final double downMax;
        public final double downAvg;
        public final double downMedian;
        public final double downStandardDeviation;
        public final double downCoverGap;

        public Gap(String ticker, int count, LocalDate startAt, LocalDate endAt, LocalDate firstDay, LocalDate lastDay, int stepCount, int gapCountUp, int gapCountDown, double coverage, double upMin, double upMax, double upAvg, double upMedian, double upStandardDeviation, double upCoverGap, double downMin, double downMax, double downAvg, double downMedian, double downStandardDeviation, double downCoverGap)
        {
            super(ticker, count, startAt, endAt, firstDay, lastDay);
            this.stepCount = stepCount;
            this.gapCountUp = gapCountUp;
            this.gapCountDown = gapCountDown;
            this.coverage = coverage;
            this.upMin = upMin;
            this.upMax = upMax;
            this.upAvg = upAvg;
            this.upMedian = upMedian;
            this.upStandardDeviation = upStandardDeviation;
            this.upCoverGap = upCoverGap;
            this.downMin = downMin;
            this.downMax = downMax;
            this.downAvg = downAvg;
            this.downMedian = downMedian;
            this.downStandardDeviation = downStandardDeviation;
            this.downCoverGap = downCoverGap;
        }
        @Override
        public int hashCode()
        {
            return Objects.hash(super.hashCode(), stepCount, gapCountUp, gapCountDown, coverage, upMin, upMax, upAvg, upMedian, upStandardDeviation, upCoverGap, downMin, downMax, downAvg, downMedian, downStandardDeviation, downCoverGap);
        }
        @Override
        public boolean equals(Object o)
        {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            if(!super.equals(o)) return false;
            Gap gap = (Gap) o;
            return stepCount == gap.stepCount &&
                   gapCountUp == gap.gapCountUp &&
                   gapCountDown == gap.gapCountDown &&
                   Double.compare(gap.coverage, coverage) == 0 &&
                   Double.compare(gap.upMin, upMin) == 0 &&
                   Double.compare(gap.upMax, upMax) == 0 &&
                   Double.compare(gap.upAvg, upAvg) == 0 &&
                   Double.compare(gap.upMedian, upMedian) == 0 &&
                   Double.compare(gap.upStandardDeviation, upStandardDeviation) == 0 &&
                   Double.compare(gap.upCoverGap, upCoverGap) == 0 &&
                   Double.compare(gap.downMin, downMin) == 0 &&
                   Double.compare(gap.downMax, downMax) == 0 &&
                   Double.compare(gap.downAvg, downAvg) == 0 &&
                   Double.compare(gap.downMedian, downMedian) == 0 &&
                   Double.compare(gap.downStandardDeviation, downStandardDeviation) == 0 &&
                   Double.compare(gap.downCoverGap, downCoverGap) == 0;
        }
    }
    public Gap getGap(int count, LocalDate startAt, LocalDate endAt, double coverage)
    {
        double [] open = startAt!=null ? getOpen(startAt, endAt, false) : getOpen(count, endAt, false);
        double [] high = startAt!=null ? getHigh(startAt, endAt, false) : getHigh(count, endAt, false);
        double [] low  = startAt!=null ? getLow(startAt, endAt, false)  : getLow(count, endAt, false);
        double [] gapUp = new double[open.length-1];
        double [] gapDown = new double[open.length-1];

        LocalDate[] date = startAt!=null ? getDate(startAt, endAt, false) : getDate(count, endAt, false);
        final LocalDate firstDay = date[0];
        final LocalDate lastDay = date[date.length-1];

        int stepCount=0;
        int gapCountUp=0;
        int gapCountDown=0;

        double upMin=Double.MAX_VALUE;
        double upMax=0;
        double upAvg=0;

        double downMin=Double.MAX_VALUE;
        double downMax=0;
        double downAvg=0;

        for(int i=1;i<open.length;i++)
        {
            stepCount++;
            double gap = 0;
            //close open
            if(open[i]>high[i-1])
            {
                gap = open[i]-high[i-1];
                gapUp[gapCountUp++]=gap;
                upMin = Math.min(upMin, gap);
                upMax = Math.max(upMax, gap);
                upAvg+= gap;
            }
            else if(open[i]<low[i-1])
            {
                gap = low[i-1]-open[i];
                gapDown[gapCountDown++]=gap;
                downMin = Math.min(downMin, gap);
                downMax = Math.max(downMax, gap);
                downAvg+= gap;
            }
        }
        upAvg   /= gapCountUp;
        downAvg /= gapCountDown;

        gapUp = Arrays.copyOf(gapUp, gapCountUp);
        gapDown = Arrays.copyOf(gapDown, gapCountDown);

        double upMedian = Stats.median(gapUp);
        double upStandardDeviation = Stats.standardDeviation(gapUp);
        double downMedian = Stats.median(gapDown);
        double downStandardDeviation = Stats.standardDeviation(gapDown);

        double upCoverGap   = coverGap(gapUp, coverage);
        double downCoverGap = coverGap(gapDown, coverage);

        return gapCountUp+gapCountDown>0 ? new Gap(this.ticker, count, startAt, endAt, firstDay, lastDay, stepCount, gapCountUp, gapCountDown, coverage, upMin, upMax, upAvg, upMedian, upStandardDeviation, upCoverGap, downMin, downMax, downAvg, downMedian, downStandardDeviation, downCoverGap)
                : new Gap(this.ticker, 0, null, null, null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private double coverGap(double[] gaps, double coverage)
    {
        Arrays.sort(gaps);
        int i = (int) Utils.bound(0,gaps.length-1,(gaps.length*coverage)-Double.MIN_VALUE);
        return gaps[i];
    }
    
    public static class AverageTrueRange extends Calculus
    {
        public final double value;
        public final double [] history;
        public AverageTrueRange(String ticker, int count, LocalDate endAt, LocalDate firstDay, LocalDate lastDay, double value, double [] history)
        {
            super(ticker, count, null, endAt, firstDay, lastDay);
            this.value = value;
            this.history = history;
        }
    }
    public AverageTrueRange getAverageTrueRange(int count, LocalDate until, int period)
    {
        int cp = count+period;

        double [] close = getClose(cp, until, false);
        double [] high = getHigh(cp, until, false);
        double [] low  = getLow(cp, until, false);
        double [] atr = new double[close.length];
        
        assert close.length==high.length;
        assert high.length==low.length;
        
        if(atr.length==0)
        {
            return null;
        }
        
        LocalDate[] date = getDate(count, until, false);
        final LocalDate firstDay = date[0];
        final LocalDate lastDay = date[date.length-1];

        double value = 0;
        for(int i=1;i<atr.length;i++)
        {   
            double hl = high[i]-low[i];
            double ch = Math.abs(close[i-1]-high[i]);
            double cl = Math.abs(close[i-1]-low[i]);
            double tr = Utils.max(hl,ch,cl);
            atr[i] = value = i>1 ? (value*(period-1)+tr)/period : tr;
        }
        atr = Arrays.copyOfRange(atr, period, atr.length);
        return new AverageTrueRange(this.ticker, count, until, firstDay, lastDay, value, atr);
    }

    public static class TrailingStop extends Calculus
    {
        public final double value;
        public final LocalDate[] historyDates;
        public final double[] historyValues;
        public final LocalDate exitAt;
        public final double exitValue;
        public TrailingStop(String ticker, int count, LocalDate startAt, LocalDate endAt, LocalDate firstDay, LocalDate lastDay, double value, LocalDate[] historyDates, double[] historyValues, LocalDate exitAt, double exitValue)
        {
            super(ticker, count, startAt, endAt, firstDay, lastDay);
            this.value = value;
            this.historyDates = historyDates;
            this.historyValues = historyValues;
            this.exitAt = exitAt;
            this.exitValue = exitValue;
        }
    }
    
    public TrailingStop getParabolicStop(LocalDate start, LocalDate end, double stopLoss, double accelerationFactor, double accelerationLimit, boolean sellShort)
    {
        assert accelerationFactor>0;
        assert stopLoss>0;
        assert start==null || end==null || start.isBefore(end) || start.equals(end);
        
        double sign = sellShort ? -1 : +1;
        double af = accelerationFactor;
        double[] open = getOpen(start, end, false);
        double[] high = getHigh(start, end, false);
        double[] low  = getLow(start, end, false);
        double[] close  = getClose(start, end, false);
        double[] stop = new double[high.length];
        double[] extreme = sellShort ? low : high;
        double[] nearby = sellShort ? high : low;
        
        assert high.length == low.length;
        
        if(stop.length==0)
        {
            return null;
        }
        
        LocalDate[] date = getDate(start, end, false);
        final LocalDate firstDay = date[0];
        final LocalDate lastDay = date[date.length-1];
        LocalDate exitAt = null;
        double exitValue = 0;
        
        double ep = extreme[0];
        for(int i=0;i<stop.length;i++)
        {
            if( sellShort ? extreme[i]<ep : extreme[i]>ep)
            {
                ep = extreme[i];
                af = Math.min(af + accelerationFactor, accelerationLimit);
            }

            if(i>0 && exitAt==null && (sellShort ? stopLoss<high[i] : stopLoss>low[i]))
            {
                exitAt = date[i];
                exitValue = stopExit(stopLoss, open[i], high[i], low[i], close[i], sellShort);
            }

            double stopOffer = stopLoss + sign*af*Math.abs(ep-stopLoss);

            if(sellShort ? stopOffer<nearby[i] : stopOffer>nearby[i])
            {
                stopOffer = sellShort ? Math.min(nearby[i], stopLoss) : Math.max(nearby[i], stopLoss);
            }
            else if(i>0 && (sellShort ? stopOffer<nearby[i-1] : stopOffer>nearby[i-1]))
            {
                stopOffer = sellShort ? Math.min(nearby[i-1], stopLoss) : Math.max(nearby[i-1], stopLoss);
            }
            stop[i] = stopLoss = stopOffer;
        }
        return new TrailingStop(this.ticker, stop.length, start, end, firstDay, lastDay, stopLoss, date, stop, exitAt, exitValue);
    }
    
    double[] getAverageDownsidePenetration(double[] signal, int period, boolean sellShort)
    {
        double[] noise = new double[signal.length];
        double[] avg = new double[signal.length];
        if(avg.length==0)
        {
            return avg;
        }
        
        int noiseCount = 0;
        double noiseTotal = 0;
        
        
        for(int i=1;i<signal.length;i++)
        {
            noise[i] = sellShort ? Math.max(signal[i]-signal[i-1],0) : Math.min(signal[i]-signal[i-1], 0);
            if(noise[i]!=0)
            {
                noiseTotal += noise[i];
                noiseCount++;
            }
            if(i>period)
            {
                if(noise[i-period]!=0)
                {
                    noiseTotal -= noise[i-period];
                    noiseCount--;
                }
            }
            avg[i] = noiseCount>0 ? noiseTotal/noiseCount : 0;
        }
        return avg;
    }
    public TrailingStop getSafeZoneStop(LocalDate start, LocalDate end, double stopLoss, double coefficient, int period, boolean sellShort)
    {
        assert coefficient>0;
        assert stopLoss>0;
        assert start==null || end==null || start.isBefore(end) || start.equals(end);
        assert period>0;
        
        LocalDate seedStart = null;
        if(start!=null)
        {
            LocalDate[] seeds = getDate(period+2, start, false);
            if(seeds!=null && seeds.length>0)
            {
                seedStart = seeds[0];
            }            
        }
        
        double[] open = getOpen(seedStart, end, false);
        double[] high = getHigh(seedStart, end, false);
        double[] low  = getLow(seedStart, end, false);
        double[] close= getClose(seedStart, end, false);
        double[] signal = sellShort ? high : low;
        double[] stop = new double[signal.length];
        
        if(stop.length==0)
        {
            return null;
        }
        
        double[] noise = getAverageDownsidePenetration(signal, period, sellShort);
        
        LocalDate[] date = getDate(seedStart, end, false);
        LocalDate firstDay = date[0];
        final LocalDate lastDay = date[date.length-1];
        LocalDate exitAt = null;
        double exitValue = 0;

        int firstIndex = 0;

        Round round = sellShort ? roundCeiling : roundFloor;
        for(int i=0;i<signal.length;i++)
        {
            final LocalDate curDay = date[i];

            if(start!=null && curDay.isBefore(start))
            {
                firstIndex = i;
                firstDay = curDay;
                stop[i] = stopLoss;
                continue;
            }
            
            if(start!=null && firstDay.isBefore(start))
            {
                firstIndex = i;
                firstDay = curDay;
            }

            if(i>0 && exitAt==null && (sellShort ? stopLoss<high[i] : stopLoss>low[i]))
            {
                exitAt = date[i];
                exitValue = stopExit(stopLoss, open[i], high[i], low[i], close[i], sellShort);
            }
            
            double stopOffer = round.round(signal[i] + noise[i]*coefficient, this.step);
            
            stop[i] = stopLoss = sellShort ?  Math.min(stopOffer,stopLoss) : Math.max(stopOffer,stopLoss);        
        }
        if(firstIndex>0)
        {
            stop = Arrays.copyOfRange(stop, firstIndex, stop.length);
            date = Arrays.copyOfRange(date, firstIndex, date.length);
        }
        return new TrailingStop(this.ticker, stop.length, start, end, firstDay, lastDay, stopLoss, date, stop, exitAt, exitValue);
    }
    public TrailingStop getChandelierStop(LocalDate start, LocalDate end, double stopLoss, double coefficient, int period, boolean sellShort)
    {
        assert coefficient>0;
        assert stopLoss>0;
        assert start==null || end==null || start.isBefore(end) || start.equals(end);
        assert period>0;
        
        LocalDate seedStart = null;
        if(start!=null)
        {
            LocalDate[] seeds = getDate(period+2, start, false);
            if(seeds!=null && seeds.length>0)
            {
                seedStart = seeds[0];
            }            
        }

        double [] open = getOpen(seedStart, end, false);
        double [] high = getHigh(seedStart, end, false);
        double [] low  = getLow(seedStart, end, false);
        double [] close = getClose(seedStart, end, false);
        double[] ep = sellShort ? low : high;
        double[] stop = new double[ep.length];
        
        if(stop.length==0)
        {
            return null;
        }
        
        assert close.length == high.length;
        assert close.length == low.length;
        
        double[] atr = getAverageTrueRange(high, low, close, period);
        
        LocalDate[] date = getDate(seedStart, end, false);
        LocalDate firstDay = date[0];
        final LocalDate lastDay = date[date.length-1];
        LocalDate exitAt = null;
        double exitValue = 0;
        
        assert (!firstDay.isAfter(lastDay)) : firstDay+" > "+lastDay;

        int firstIndex = 0;
        
        coefficient = sellShort ? coefficient : -coefficient;

        Round round = sellShort ? roundCeiling : roundFloor;
        for(int i=0;i<atr.length;i++)
        {
            final LocalDate curDay = date[i];

            if(start!=null && curDay.isBefore(start))
            {
                firstIndex = i;
                firstDay = curDay;
                stop[i] = stopLoss;
                continue;
            }
            
            if(start!=null && firstDay.isBefore(start))
            {
                firstIndex = i;
                firstDay = curDay;
            }

            if(i>0 && exitAt==null && (sellShort ? stopLoss<high[i] : stopLoss>low[i]))
            {
                exitAt = date[i];
                exitValue = stopExit(stopLoss, open[i], high[i], low[i], close[i], sellShort);
            }
            
            double stopOffer = round.round(ep[i] + atr[i]*coefficient, this.step);
            
            stop[i] = stopLoss = sellShort ?  Math.min(stopOffer,stopLoss) : Math.max(stopOffer,stopLoss);            
        }
        if(firstIndex>0)
        {
            stop = Arrays.copyOfRange(stop, firstIndex, stop.length);
            date = Arrays.copyOfRange(date, firstIndex, date.length);
        }
        return new TrailingStop(this.ticker, stop.length, start, end, firstDay, lastDay, stopLoss, date, stop, exitAt, exitValue);
    }
    private static double stopExit(double stop, double open, double high, double low, double close, boolean sellShort)
    {
        if(sellShort)
        {
            if(open>stop)
            {
                return open;
            }
            if(high>stop)
            {
                return stop;
            }
        }
        else
        {
            if(open<stop)
            {
                return open;
            }
            if(low<stop)
            {
                return stop;
            }
        }
        return 0;
    }

    double[] getAverageTrueRange(double[] high, double[] low, double[] close, int period)
    {
        assert high.length==low.length;
        assert high.length==close.length;
        
        double[] atr = new double[high.length];
        double[] avg = new double[high.length];
        
        if(avg.length==0)
        {
            return avg;
        }
        
        int atrCount = 1;
        double atrTotal = avg[0] = atr[0] = high[0]-low[0];
        
        assert atrTotal>=0;
        
        for(int i=1;i<avg.length;i++)
        {
            assert (high[i]>=low[i]) :(high[i]+"<"+low[i]);
            
            atr[i] = Utils.maxOf(high[i]-low[i], Math.abs(high[i]-close[i-1]), Math.abs(low[i]-close[i-1]));
            if(atr[i]!=0)
            {
                atrTotal += atr[i];
                atrCount++;
            }
            if(i>=period)
            {
                if(atr[i-period]!=0)
                {
                    atrTotal -= atr[i-period];
                    atrCount--;
                }
            }
            avg[i] = atrCount>0 ? atrTotal/atrCount : 0;
        }
        return avg;
    }

    public LocalDate firstKey()
    {
        return map.firstKey();
    }
    public Quote firstValue()
    {
        return map.firstEntry().getValue();
    }

    public LocalDate lastKey()
    {
        return map.lastKey();
    }
    public Quote lastValue()
    {
        return map.lastEntry().getValue();
    }

    public boolean merge(StockQuotes stockQuotes)
    {
        boolean ret = false;
        for(Map.Entry<LocalDate,Quote> entry: stockQuotes.map.entrySet())
        {
            if(!this.map.containsKey(entry.getKey()))
            {
                this.map.put(entry.getKey(),entry.getValue());
                ret = true;
            }
        }
        return ret;
    }
    public StockQuotes getSubStockQuotes(int count, LocalDate end)
    {
        Quote[] quotes = this.map.subMap(LocalDate.MIN, end).values().toArray(new Quote[0]);
        Arrays.sort(quotes);
        Sorts.reverse(quotes);

        StockQuotes stockQuotes = new StockQuotes(this.ticker, this.applyDividend, this.decimals, this.step);

        for(int i=0;i<quotes.length && i<count;i++)
        {
            stockQuotes.add(quotes[i]);
        }
        return stockQuotes;
    }
    public StockQuotes getSubStockQuotes(LocalDate start, LocalDate end)
    {
        Quote[] quotes = this.map.subMap(start, end).values().toArray(new Quote[0]);
        Arrays.sort(quotes);
        Sorts.reverse(quotes);

        StockQuotes stockQuotes = new StockQuotes(this.ticker, this.applyDividend, this.decimals, this.step);

        for(int i=0;i<quotes.length;i++)
        {
            stockQuotes.add(quotes[i]);
        }
        return stockQuotes;
    }

    public int size()
    {
        return map.size();
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }


    public List<Quote> getQuotes()
    {
        return new ArrayList<>(this.map.values());
    }
    
    public StockQuotes getWeekly()
    {
        HashMap<LocalDate, Quote> wmap = new HashMap<>();
        
        for( Quote item : this.map.values())
        {
            LocalDate key = JavaTime.atStartOfWeek(item.date);
            Quote value = wmap.get(key);
            value = value!= null ? value.merge(item) : item;
            wmap.put(key, value);
        }
        
        StockQuotes weekly = new StockQuotes(this.ticker, this.applyDividend, this.decimals, step);
        
        for(Quote item : wmap.values())
        {
            weekly.add(item);
        }
        
        return weekly;
    }
    public StockQuotes getMonthly()
    {
        HashMap<LocalDate, Quote> wmap = new HashMap<>();
        
        for( Quote item : this.map.values())
        {
            LocalDate key = item.date.withDayOfMonth(1);
            Quote value = wmap.get(key);
            value = value!= null ? value.merge(item) : item;
            wmap.put(key, value);
        }
        
        StockQuotes monthly = new StockQuotes(this.ticker, this.applyDividend, this.decimals, this.step);
        
        for(Quote item : wmap.values())
        {
            monthly.add(item);
        }
        
        return monthly;
    }
    
//666    public double getTickSize()
//    {
//        for( Quote item : this.map.values())
//        {
//            item.close;
//        }
//    }

//Google
//Google let you download daily and weekly eod quotes for US, Canada, UK, China and Hong Kong stock markets.
//Here is the URL to grab this data: 
//https://www.google.com/finance/historical?output=csv&q=[Symbol name]
//http://www.google.com/finance/historical?cid=6550&startdate=Aug+14%2C+2015&enddate=Aug+13%2C+2017&num=30&ei=zsiQWemYJMiHswGPk5rQDQ&output=csv    

//Investopedia
//Investopedia provides end of day quotes, dividends and splits data for the US market.
//https://simulator.investopedia.com/stocks/historicaldata.aspx?Download=1&s=[Symbol name]    
    
//Quotemedia
//Quotemedia has US and Canadian stocks historical data.
//https://app.quotemedia.com/quotetools/getHistoryDownload.csv?&webmasterId=501&startDay=02&startMonth=02&startYear=2002&endDay=02&endMonth=07&endYear=2009&isRanged=false&symbol=[Symbol name]


}
