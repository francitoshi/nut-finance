/*
 *  DateQuote.java
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

import io.nut.base.time.JavaTime;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 *
 * @author franci
 */
public class DateQuote extends Quote
{
    public DateQuote(long time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close)
    {
        super(time, open, high, low, close);
    }
    public DateQuote(LocalDate time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close)
    {
        super(time.atStartOfDay().toEpochSecond(ZoneOffset.UTC), open, high, low, close);
    }

    @Override
    public String toString()
    {
        LocalDate date = Instant.ofEpochSecond(time).atZone(JavaTime.UTC).toLocalDate();
        return date.format(JavaTime.YYYY_MM_DD) + " O=" + open + " H=" + high + " L=" + low + " C=" + close;
    }  
}
