/*
 * Copyright (C) 2024-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.quotes;

import io.github.francitoshi.nut.base.math.Nums;
import static io.github.francitoshi.nut.base.math.Nums.MC16HU;
import io.github.francitoshi.nut.base.commons.time.JavaTime;
import io.github.francitoshi.nut.base.util.Utils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author franci
 */
public class Quote
{
    public final long time;//Epoc Seconds
    public final BigDecimal open;
    public final BigDecimal high;
    public final BigDecimal low;
    public final BigDecimal close;

    public Quote(long time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close)
    {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public LocalDate getLocalDate()
    {
        return JavaTime.asLocalDate(time);
    }

    public LocalDateTime getLocalDateTime()
    {
        return JavaTime.asLocalDateTime(time);
    }

    @Override
    public String toString()
    {
        return "T=" + time + " O=" + open + " H=" + high + " L=" + low + " C=" + close;
    }
    
    private static final BigDecimal SIX = BigDecimal.valueOf(6);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);

    public BigDecimal getAvg(int scale)
    {
        return low.add(open).add(high).add(close).divide(FOUR, scale, RoundingMode.HALF_UP);
    }

    public BigDecimal getLowAvg(int scale)
    {
        return low.add(low).add(low).add(open).add(high).add(close).divide(SIX, scale, RoundingMode.HALF_UP);
    }

    public BigDecimal getHighAvg(int scale)
    {
        return low.add(open).add(high).add(close).add(high).add(high).divide(SIX, scale, RoundingMode.HALF_UP);
    }

    public BigDecimal getLowHighLevel(int scale, double level)
    {
        BigDecimal range = this.high.subtract(this.low);
        return this.low.add(range.multiply(BigDecimal.valueOf(level), MC16HU));
    }

    public BigDecimal getGaussianAvg(double gaussian, int scale)
    {
        BigDecimal mean = this.getLowAvg(scale);
        BigDecimal deviation = high.subtract(low).divide(SIX, Nums.MC16HU);
        return Utils.nextGaussian(gaussian, mean, deviation, this.low, this.high, false);
    }
    public Quote relativeQuote(Quote other, int decimals)
    {
        if(this.time!=other.time)
        {
            throw new InvalidParameterException(this.time+" != "+other.time);
        }

        BigDecimal o = this.open.divide(other.open, MC16HU).setScale(decimals, RoundingMode.HALF_UP);
        BigDecimal hi = this.high.divide(other.high, MC16HU).setScale(decimals, RoundingMode.HALF_UP);
        BigDecimal lo = this.low.divide(other.low, MC16HU).setScale(decimals, RoundingMode.HALF_UP);
        BigDecimal c = this.close.divide(other.close, MC16HU).setScale(decimals, RoundingMode.HALF_UP);

        hi = o.max(hi).max(lo).max(c);
        lo = o.min(hi).min(lo).min(c);

        return new Quote(time, o, hi, lo, c);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + (int) (this.time ^ (this.time >>> 32));
        hash = 83 * hash + Objects.hashCode(this.open);
        hash = 83 * hash + Objects.hashCode(this.high);
        hash = 83 * hash + Objects.hashCode(this.low);
        hash = 83 * hash + Objects.hashCode(this.close);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Quote other = (Quote) obj;
        if (this.time != other.time)
        {
            return false;
        }
        if (!Objects.equals(this.open, other.open))
        {
            return false;
        }
        if (!Objects.equals(this.high, other.high))
        {
            return false;
        }
        if (!Objects.equals(this.low, other.low))
        {
            return false;
        }
        return Objects.equals(this.close, other.close);
    }
    
}
