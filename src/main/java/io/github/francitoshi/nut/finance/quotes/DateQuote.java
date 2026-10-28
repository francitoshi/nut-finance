/*
 * Copyright (C) 2024-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.quotes;

import io.github.francitoshi.nut.base.commons.time.JavaTime;
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
