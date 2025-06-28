/*
 * MakerPrice.java
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
