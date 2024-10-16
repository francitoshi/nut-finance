/*
 *  DateQuoteTest.java
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

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author franci
 */
public class DateTimeQuoteTest
{
    /**
     * Test of toString method, of class DateTimeQuote.
     */
    @Test
    public void testToString()
    {
        Quote instance = new DateTimeQuote(0, BigDecimal.valueOf(2), BigDecimal.valueOf(4), BigDecimal.valueOf(1), BigDecimal.valueOf(3));
        String result = instance.toString();
        assertEquals("T=1970-01-01 00:00:00 O=2 H=4 L=1 C=3", instance.toString());
    }
}
