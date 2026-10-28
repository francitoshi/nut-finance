/*
 * Copyright (C) 2024-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.quotes;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author franci
 */
public class DateQuoteTest
{
    /**
     * Test of toString method, of class DateQuote.
     */
    @Test
    public void testToString()
    {
        Quote instance = new DateQuote(0, BigDecimal.valueOf(2), BigDecimal.valueOf(4), BigDecimal.valueOf(1), BigDecimal.valueOf(3));
        String result = instance.toString();
        assertEquals("1970-01-01 O=2 H=4 L=1 C=3", instance.toString());
    }
}
