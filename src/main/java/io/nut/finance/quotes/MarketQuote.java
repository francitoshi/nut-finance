/*
 *  Quote.java
 *
 *  Copyright (C) 2018-2023 francitoshi@gmail.com
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
package io.nut.finance.quotes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
import io.nut.base.util.Coinage;
import java.time.LocalDateTime;
/**
 *
 * @author franci
 */
public class MarketQuote
{
    private final MathContext mc;
    public final String exchange;
    public final String pair;
    public final String pairBase;
    public final String pairCounter;
    public final LocalDateTime timestamp;
    public final long nanoTime;

    public final BigDecimal ask;
    public final BigDecimal askSize;
    
    public final BigDecimal bid;
    public final BigDecimal bidSize;
    
    public final BigDecimal last;
    public final BigDecimal fee;

    public MarketQuote(MathContext mc, String exchange, String pair, LocalDateTime timestamp, BigDecimal ask, BigDecimal askSize, BigDecimal bid, BigDecimal bidSize, BigDecimal last, BigDecimal fee)
    {
        this.mc = mc;
        this.exchange = exchange;
        this.pair = pair;
        String[] items = Coinage.splitPair(pair);
        this.pairBase = items.length>0 ? items[0] : pair;
        this.pairCounter = items.length>1 ? items[1] : pair;
        this.timestamp = timestamp;
        this.nanoTime = System.nanoTime();
        
        if(ask==null || bid==null || ask.compareTo(bid)>=0)
        {
            this.ask = ask;
            this.askSize = askSize;
            this.bid = bid;
            this.bidSize = bidSize;
        }
        else
        {
            this.ask = bid;
            this.askSize = bidSize;
            this.bid = ask;
            this.bidSize = askSize;
            System.err.println(exchange+" "+pair+" ask="+ask+"<bid="+bid);
        }

        this.last = last;
        this.fee = fee;
    }

    @Override
    public String toString()
    {
        return "MarketQuote{" + "exchange=" + exchange + ", pair=" + pair + ", pairBase=" + pairBase + ", pairCounter=" + pairCounter + ", timestamp=" + timestamp + ", ask=" + ask + ", askSize=" + askSize + ", bid=" + bid + ", bidSize=" + bidSize + ", last=" + last + ", fee=" + fee + '}';
    }
    public String toBriefString()
    {
        return String.format(Locale.US, "ask=%.8f bid=%.8f last=%.8f fee=%.8f", ask, bid, last, fee);
    }
    
    public MarketQuote invert()
    {
        final String iPair =pairCounter+pairBase;
        final String iPairBase = pairCounter;
        final String iPairCounter = pairBase;

        final BigDecimal iAsk = BigDecimal.ONE.divide(ask, mc);
        final BigDecimal iAskSize=BigDecimal.ONE.divide(askSize, mc);

        final BigDecimal iBid = BigDecimal.ONE.divide(bid,mc);
        final BigDecimal iBidSize = BigDecimal.ONE.divide(bidSize,mc);

        final BigDecimal iLast = BigDecimal.ONE.divide(last,mc);
        
        return new MarketQuote(mc, exchange, iPair, timestamp, iAsk, iAskSize, iBid, iBidSize, iLast, fee);
    }

}
