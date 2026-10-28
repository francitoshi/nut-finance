/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author franci
 */
public class MakerPrice
{
    private final BigDecimal tick;
    private final int scale;
    private final long takerNanoTime; 
    private final int takerCount; 
    private volatile int count;
    private volatile BigDecimal lastBid = BigDecimal.ZERO;
    private volatile BigDecimal lastAsk = BigDecimal.valueOf(Integer.MAX_VALUE);
    
   
    public MakerPrice(BigDecimal tick, int scale)
    {
        this.tick = tick;
        this.scale = scale;
        this.takerNanoTime = Long.MAX_VALUE;
        this.takerCount = Integer.MAX_VALUE;
    }
    public MakerPrice(BigDecimal tick, int scale, int takerMillis, int takerCount)
    {
        this.tick = tick;
        this.scale = scale;
        this.takerNanoTime = System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(takerMillis);
        this.takerCount = takerCount;
    }

    public BigDecimal getBid(BigDecimal bid, BigDecimal ask)
    {
        BigDecimal bidCount = BigDecimal.valueOf(++count);
        BigDecimal tickOffset = tick.multiply(bidCount);
        
        BigDecimal spread = ask.subtract(bid);
        BigDecimal spreadOffset = spread.divide(BigDecimal.valueOf(this.takerCount), scale+1, RoundingMode.HALF_UP).multiply(bidCount);
        BigDecimal offset = tickOffset.max(spreadOffset); 
        bid = bid.add(offset).max(lastBid.add(tick));
        System.err.printf("%d %s %s %s %s\n", count, tickOffset, spreadOffset, offset, bid);
        ask = allowTaker() ? ask : ask.subtract(tick);

        boolean jump = ask.subtract(bid).compareTo(tickOffset)<0;

        return lastBid = roundBid( jump ? ask : bid.min(ask) );
    }
    public BigDecimal getAsk(BigDecimal bid, BigDecimal ask)
    {
        BigDecimal offset = tick.multiply(BigDecimal.valueOf(++count));
        bid = bid.add(tick);
        ask = ask.subtract(offset);
        boolean jump = ask.subtract(bid).compareTo(offset)<0;
        return lastAsk = roundAsk( jump ? bid : bid.max(ask) );
    }
            
    public BigDecimal roundBid(BigDecimal value)
    {
        return value.setScale(this.scale, RoundingMode.HALF_DOWN);
    }
    public BigDecimal roundAsk(BigDecimal value)
    {
        return value.setScale(this.scale, RoundingMode.HALF_UP);
    }

    public int getCount()
    {
        return count;
    }

    public boolean allowTaker()
    {
        return (this.count>this.takerCount) || (System.nanoTime()>this.takerNanoTime);
    }
    
}
