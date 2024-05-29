/*
 *  TradeBuilder.java
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
package io.nut.finance.trade;

import io.nut.base.math.Nums;
import io.nut.base.math.Round;
import io.nut.base.util.Utils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author franci
 */
public class TradeBuilder
{
    
    public final double equity;
    public final double power;
    public final double tradeRisk;
    public final double monthRisk;
    public final double flatFee;
    public final double ratioFee;
    public final double shareFee;
    public final double minFee;
    public final double maxFee;
    public final double maxRatioFee;
    public final double sellTax;
    public final int scale;
    public final double tickSize;
    
    private final Round round;
    private final Round roundUp;
    private final Round roundDw;
    
    public TradeBuilder(double equity, double power, double tradeRisk, double monthRisk, double flatFee, double ratioFee, double shareFee, double minFee, double maxFee, double maxRatioFee, double sellTax, double tickSize, int scale)
    {
        this.equity = equity;
        this.power = power;
        this.tradeRisk = tradeRisk;
        this.monthRisk = monthRisk;
        this.flatFee = flatFee;
        this.ratioFee = ratioFee;
        this.shareFee = shareFee;
        this.minFee = minFee>0 ? minFee : 0.0;
        this.maxFee = maxFee>0 ? maxFee : Integer.MAX_VALUE;
        this.maxRatioFee = maxRatioFee>0 ? maxRatioFee : Integer.MAX_VALUE;
        this.sellTax = sellTax;
        this.scale = scale;
        this.tickSize = tickSize;
        
        this.round = Round.getHalfUpInstance(scale);
        this.roundUp = Round.getUpInstance(scale);
        this.roundDw = Round.getDownInstance(scale);

        assert (this.equity > 0);
        assert (this.power > 0);
        assert (this.tradeRisk > 0);
        assert (this.monthRisk > 0);
        assert (this.flatFee >= 0);
        assert (this.ratioFee >= 0);
        assert (this.shareFee >= 0);
        assert (this.minFee<=this.maxFee);
        assert (this.sellTax >= 0);
        assert (this.tickSize > 0.000_000_000_001);
    }
        
    public enum TradeType
    {
        BuyLong, SellShort
    }
    public static class Trade
    {
        public final double entryLimit;
        public final double stopLoss;
        public final double shareSlippage;
        public final double availableEquity;
        public final double availableRisk;
        public final int sharesLimit;
        public final double tickSize;

        public final TradeType tradeType;
        public final int shares;
        public final double worstEntry;
        public final double worstExit;
        public final double feeEntry;
        public final double feeExit;
        public final double amountEntry;
        public final double amountExit;
        public final double amountRisk;

        public Trade(double entryLimit, double stopLoss, double shareSlippage, double availableEquity, double availableRisk, int sharesLimit, double tickSize, TradeType tradeType, int shares, double worstEntry, double worstExit, double feeEntry, double feeExit, double amountEntry, double amountExit, double amountRisk)
        {
            assert Nums.equalsEnough(Math.abs(amountEntry-amountExit),amountRisk, 0.0001) : String.format(Locale.ROOT,"%.2f != %.2f", Math.abs(amountEntry-amountExit), amountRisk);
            this.entryLimit = entryLimit;
            this.stopLoss = stopLoss;
            this.shareSlippage = shareSlippage;
            this.availableEquity = availableEquity;
            this.availableRisk = availableRisk;
            this.sharesLimit = sharesLimit;
            this.tickSize = tickSize;
            this.tradeType = tradeType;
            this.shares = shares;
            this.worstEntry = worstEntry;
            this.worstExit = worstExit;
            this.feeEntry = feeEntry;
            this.feeExit = feeExit;
            this.amountEntry = amountEntry;
            this.amountExit = amountExit;
            this.amountRisk = amountRisk;
        }
        public String getFees()
        {
            return String.format(Locale.ROOT, "%.2f + %.2f = %.2f", this.feeEntry, this.feeExit, this.feeEntry+this.feeExit);
        }
        public String getAmounts()
        {
            String fmt = tradeType==TradeType.SellShort ? "%.2f - %.2f = %.2f" : "-%.2f + %.2f = %.2f";
            return String.format(Locale.ROOT, fmt, this.amountEntry, this.amountExit, this.amountRisk);
        }


        @Override
        public boolean equals(Object o)
        {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Trade trade = (Trade) o;
            return Double.compare(trade.entryLimit, entryLimit) == 0 &&
                   Double.compare(trade.stopLoss, stopLoss) == 0 &&
                   Double.compare(trade.shareSlippage, shareSlippage) == 0 &&
                   Double.compare(trade.availableEquity, availableEquity) == 0 &&
                   Double.compare(trade.availableRisk, availableRisk) == 0 &&
                   sharesLimit == trade.sharesLimit &&
                   Double.compare(trade.tickSize, tickSize) == 0 &&
                   shares == trade.shares &&
                   Double.compare(trade.worstEntry, worstEntry) == 0 &&
                   Double.compare(trade.worstExit, worstExit) == 0 &&
                   Double.compare(trade.feeEntry, feeEntry) == 0 &&
                   Double.compare(trade.feeExit, feeExit) == 0 &&
                   Double.compare(trade.amountEntry, amountEntry) == 0 &&
                   Double.compare(trade.amountExit, amountExit) == 0 &&
                   tradeType == trade.tradeType;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(entryLimit, stopLoss, shareSlippage, availableEquity, availableRisk, sharesLimit, tickSize, tradeType, shares, worstEntry, worstExit, feeEntry, feeExit, amountEntry, amountExit);
        }
    }

    public static TradeType getTradeType(double entryLimit, double stopLoss)
    {
        TradeType tradeType = null;
        if(entryLimit>stopLoss)
        {
            tradeType = TradeType.BuyLong;
        }
        else if(entryLimit<stopLoss)
        {
            tradeType = TradeType.SellShort;
        }
        return tradeType;
    }
    public Trade getSize(TradeType tradeType, double entryLimit, double stopLoss, double shareSlippage, double availableEquity, double availableRisk, int sharesLimit)
    {
        assert(shareSlippage>=0);

        availableEquity = Math.min(this.equity*this.power, availableEquity>0 ? availableEquity : Double.MAX_VALUE);
        availableRisk   = Math.min(this.tradeRisk, availableRisk>0 ? availableRisk : Double.MAX_VALUE);
        final double feeStart = Math.max(this.minFee, this.flatFee)*2;

        if(availableEquity<=0 || availableRisk<=0 || feeStart>availableRisk)
        {
            return null;
        }

        boolean entrySell=false;
        boolean exitSell=false;

        double shareRisk = 0;
        double worstEntry = entryLimit;
        double worstExit = 0;
        switch(tradeType)
        {
            case BuyLong:
                exitSell = true;
                worstExit = stopLoss-shareSlippage;
                shareRisk = Math.max(entryLimit-worstExit,0.0);
                break;
            case SellShort:
                entrySell = true;
                worstExit = stopLoss+shareSlippage;
                shareRisk = Math.max(worstExit-entryLimit,0.0);
                break;
        }

        sharesLimit = sharesLimit!=0? sharesLimit : Integer.MAX_VALUE;

        final int maxShares = (int) (shareRisk > 0 ? (availableRisk - feeStart) / shareRisk : availableEquity / entryLimit);

        for(int shares = Math.min(maxShares,sharesLimit) ;shares>0;shares--)
        {
            final double feeEntry = getFee(shares, worstEntry, entrySell);
            final double feeExit  = getFee(shares, worstExit, exitSell);
            
            final double amountEntry= entrySell ? shares*worstEntry - feeEntry : shares*worstEntry + feeEntry;
            final double amountExit = exitSell  ? shares*worstExit  - feeExit  : shares*worstExit  + feeExit;

            final double risk = shares*shareRisk+feeEntry+feeExit;
            if(risk>availableRisk)
            {
                continue;
            }
            if(amountEntry > availableEquity)
            {
                continue;
            }
            return new Trade(entryLimit, stopLoss, shareSlippage, availableEquity, availableRisk, sharesLimit, tickSize, tradeType, shares, worstEntry, worstExit, feeEntry, feeExit, amountEntry, amountExit, risk);
        }
        
        return null;
    }
    public Trade getSize(TradeType tradeType, double entryLimit, double stopLoss, double shareSlippage, double availableEquity, double availableRisk)
    {
        return getSize(tradeType, entryLimit, stopLoss, shareSlippage, availableEquity, availableRisk, 0);
    }
    public double getFee(int shares, double price, boolean sell)
    {
        double fee = this.flatFee + shares*this.shareFee + this.ratioFee*shares*price;
        fee = Math.max(fee, this.minFee);
        fee = Math.min(fee, this.maxFee);
        fee = Math.min(fee, this.maxRatioFee*shares*price);

        fee += (sell && this.sellTax>0) ? this.sellTax*shares*price : 0;

        return BigDecimal.valueOf(fee).setScale(this.scale, RoundingMode.CEILING).doubleValue();
    }
    
    public static double getGainLossRatio(double target, double entryLimit, double stopLoss)
    {
        return (target-entryLimit)/(entryLimit-stopLoss);
    }
    public static double getGainLossRatio(double target, double entryLimit, double stopLoss, double minRatio, double usedRange)
    {
        double maxRatio = getGainLossRatio(target, entryLimit, stopLoss);
        return minRatio*usedRange + maxRatio*(1-usedRange);
    }
    public double getEntryLimit(double target, double stopLoss, double gainLossRatio)
    {
        double entryLimit = (gainLossRatio*stopLoss + target) / (gainLossRatio+1);
        entryLimit = this.round.round(entryLimit, this.tickSize);
        if( (entryLimit<target && entryLimit<stopLoss) || (entryLimit>target && entryLimit>stopLoss))
        {
            entryLimit = stopLoss;
        }
        return entryLimit;
    }

    public static class BreakEven
    {
        public final TradeType tradeType;
        public final int shares;
        public final double entry;
        public final double exit;
        public final double slippage;
        public final double feeEntry;
        public final double feeExit;
        public BreakEven(TradeType tradeType, int shares, double entry, double exit, double slippage, double feeEntry, double feeExit)
        {
            this.tradeType = tradeType;
            this.shares = shares;
            this.entry = entry;
            this.exit = exit;
            this.slippage = slippage;
            this.feeEntry = feeEntry;
            this.feeExit = feeExit;
        }
    }
    public BreakEven getBreakEven(int shares, double entry, double stopLoss, double slippage)
    {
        boolean entrySell=false;
        boolean exitSell=false;
        double delta=0;
        Round roundBreak = this.round;

        TradeType tradeType = getTradeType(entry, stopLoss);
        switch(tradeType)
        {
            case BuyLong:
                exitSell = true;
                delta = +this.tickSize;
                roundBreak = this.roundUp;
                break;
            case SellShort:
                entrySell = true;
                delta = -this.tickSize;
                roundBreak = this.roundDw;
                break;
        }
        double entryFee = this.getFee(shares, entry, entrySell);
        double entryAmount = round.round(shares * entry);

        for( double breakEven = roundBreak.round(entry, this.tickSize) ; delta!=0 && breakEven<Integer.MAX_VALUE; breakEven += delta)
        {
            double lowerFee = this.getFee(shares, breakEven-slippage, exitSell);
            double equalFee = this.getFee(shares, breakEven, exitSell);
            double upperFee = this.getFee(shares, breakEven+slippage, exitSell);
            double exitFee = Utils.max(lowerFee, upperFee, equalFee);
            
            double lowerAmount = shares*(breakEven-slippage);
            double equalAmount = shares*breakEven;
            double upperAmount = shares*(breakEven+slippage);

            double lowerDiff = Math.abs(entryAmount-lowerAmount);
            double equalDiff = Math.abs(entryAmount-upperAmount);
            double upperDiff = Math.abs(entryAmount-equalAmount);
            
            double diffAmount  = round.round(Utils.min(lowerDiff, equalDiff, upperDiff));
            
            double fees = round.round(entryFee + exitFee);
            
            if( diffAmount>= fees)
            {
                return new BreakEven(tradeType, shares, entry, round.round(breakEven), slippage, round.round(entryFee), round.round(exitFee));
            }
        }
        
        return null;
    }

    public double round(double value)
    {
        return this.round.round(value, this.tickSize);
    }
    public double roundUp(double value)
    {
        return this.roundUp.round(value, this.tickSize);
    }
    public double roundDown(double value)
    {
        return this.roundDw.round(value, this.tickSize);
    }
}
