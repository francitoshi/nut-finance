/*
 * Copyright (C) 2018-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.quotes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
import io.github.francitoshi.nut.base.util.Coinage;
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
