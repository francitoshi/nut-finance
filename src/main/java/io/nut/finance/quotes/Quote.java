/*
 *  Quote.java
 *
 *  Copyright (c) 2024 francitoshi@gmail.com
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

import io.nut.base.math.Nums;
import static io.nut.base.math.Nums.MC16HU;
import io.nut.base.util.Utils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidParameterException;
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
