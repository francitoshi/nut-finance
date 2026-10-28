/*
 * Copyright (C) 2024-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.quotes;

import io.github.francitoshi.nut.base.commons.time.JavaTime;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 *
 * @author franci
 */
public class DateTimeQuote extends Quote
{
    public DateTimeQuote(long time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close)
    {
        super(time, open, high, low, close);
    }
    public DateTimeQuote(LocalDateTime time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close)
    {
        super(time.toEpochSecond(ZoneOffset.UTC), open, high, low, close);
    }

    @Override
    public String toString()
    {
        LocalDateTime dateTime = Instant.ofEpochSecond(time).atZone(JavaTime.UTC).toLocalDateTime();
        return dateTime.format(JavaTime.YYYY_MM_DD_HH_MM_SS) + " O=" + open + " H=" + high + " L=" + low + " C=" + close;
    }
}
