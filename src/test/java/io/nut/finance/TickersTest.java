/*
 * TickersTest.java
 *
 *  Copyright (C) 2018-2024 francitoshi@gmail.com
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
package io.nut.finance;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class TickersTest
{
    /**
     * Test of isExchange method, of class Tickers.
     */
    @Test
    public void testIsExchange()
    {
        assertFalse(Tickers.isExchange(""));
        assertFalse(Tickers.isExchange("^"));
        assertFalse(Tickers.isExchange("$"));
        assertFalse(Tickers.isExchange("1"));
        assertFalse(Tickers.isExchange(":1"));
        assertFalse(Tickers.isExchange(":a"));
        assertFalse(Tickers.isExchange(":^a"));
        assertFalse(Tickers.isExchange(":$a"));
        assertFalse(Tickers.isExchange("@"));
        assertFalse(Tickers.isExchange("@:1"));
        assertFalse(Tickers.isExchange("^a"));
        assertFalse(Tickers.isExchange("e:a"));
        assertFalse(Tickers.isExchange("e:^a"));
        assertFalse(Tickers.isExchange("e:$a"));
        assertFalse(Tickers.isExchange("e1:a2"));
        assertFalse(Tickers.isExchange("e1:^a2"));
        assertFalse(Tickers.isExchange("e1:$a2"));
        
        assertTrue(Tickers.isExchange("a"));
        assertTrue(Tickers.isExchange("a1"));
        assertTrue(Tickers.isExchange("A"));
        assertTrue(Tickers.isExchange("A1"));
        
        assertTrue(Tickers.isExchange("NYSE"));
        assertTrue(Tickers.isExchange("NYSEARCA"));
    }

    /**
     * Test of isTicker method, of class Tickers.
     */
    @Test
    public void testIsTicker()
    {
        assertFalse(Tickers.isTicker(""));
        assertFalse(Tickers.isTicker("^"));
        assertFalse(Tickers.isTicker("$"));
        assertFalse(Tickers.isTicker("1"));
        assertFalse(Tickers.isTicker(":1"));
        assertFalse(Tickers.isTicker(":a"));
        assertFalse(Tickers.isTicker(":^a"));
        assertFalse(Tickers.isTicker(":$a"));
        assertFalse(Tickers.isTicker("@"));
        assertFalse(Tickers.isTicker("@:1"));
        assertFalse(Tickers.isTicker("e:a"));
        assertFalse(Tickers.isTicker("e:^a"));
        assertFalse(Tickers.isTicker("e:$a"));
        assertFalse(Tickers.isTicker("e1:a2"));
        assertFalse(Tickers.isTicker("e1:^a2"));
        assertFalse(Tickers.isTicker("e1:$a2"));

        assertTrue(Tickers.isTicker("a"));
        assertTrue(Tickers.isTicker("a1"));
        assertTrue(Tickers.isTicker("IBM"));
        assertTrue(Tickers.isTicker("^USD"));
        assertTrue(Tickers.isTicker("$USD"));
    }

    /**
     * Test of isExchangeTicker method, of class Tickers.
     */
    @Test
    public void testIsExchangeTicker()
    {
        assertFalse(Tickers.isExchangeTicker("", false));
        assertFalse(Tickers.isExchangeTicker("^", false));
        assertFalse(Tickers.isExchangeTicker("$", false));
        assertFalse(Tickers.isExchangeTicker("1", false));
        assertFalse(Tickers.isExchangeTicker(":1", false));
        assertFalse(Tickers.isExchangeTicker(":a", false));
        assertFalse(Tickers.isExchangeTicker(":^a", false));
        assertFalse(Tickers.isExchangeTicker(":$a", false));
        assertFalse(Tickers.isExchangeTicker("@", false));
        assertFalse(Tickers.isExchangeTicker("@:1", false));
        assertFalse(Tickers.isExchangeTicker("^a", false));
        assertFalse(Tickers.isExchangeTicker("a", false));
        assertFalse(Tickers.isExchangeTicker("a1", false));
        assertFalse(Tickers.isExchangeTicker("A", false));
        assertFalse(Tickers.isExchangeTicker("A1", false));
        
        assertTrue(Tickers.isExchangeTicker("e1:$a2", false));
        assertTrue(Tickers.isExchangeTicker("e1:^a2", false));
        assertTrue(Tickers.isExchangeTicker("e:^a", false));
        assertTrue(Tickers.isExchangeTicker("e:$a", false));
        assertTrue(Tickers.isExchangeTicker("e1:a2", false));
        assertTrue(Tickers.isExchangeTicker("e:a", false));
        
        assertFalse(Tickers.isExchangeTicker("NYSE", false));
        assertFalse(Tickers.isExchangeTicker("NYSEARCA", false));
        
        assertFalse(Tickers.isExchangeTicker("", true));
        assertFalse(Tickers.isExchangeTicker("^", true));
        assertFalse(Tickers.isExchangeTicker("$", true));
        assertFalse(Tickers.isExchangeTicker("1", true));
        assertFalse(Tickers.isExchangeTicker(":1", true));
        assertFalse(Tickers.isExchangeTicker(":a", true));
        assertFalse(Tickers.isExchangeTicker(":^a", true));
        assertFalse(Tickers.isExchangeTicker(":$a", true));
        assertFalse(Tickers.isExchangeTicker("@", true));
        assertFalse(Tickers.isExchangeTicker("@:1", true));
        
        assertTrue(Tickers.isExchangeTicker("e:^a", true));
        assertTrue(Tickers.isExchangeTicker("e:$a", true));
        assertTrue(Tickers.isExchangeTicker("e1:a2", true));
        assertTrue(Tickers.isExchangeTicker("e1:^a2", true));
        assertTrue(Tickers.isExchangeTicker("e1:$a2", true));
        assertTrue(Tickers.isExchangeTicker("e:a", true));
        assertTrue(Tickers.isExchangeTicker("^a", true));
        assertTrue(Tickers.isExchangeTicker("a", true));
        assertTrue(Tickers.isExchangeTicker("a1", true));
        assertTrue(Tickers.isExchangeTicker("A", true));
        assertTrue(Tickers.isExchangeTicker("A1", true));
        
        assertTrue(Tickers.isExchangeTicker("IBM", true));
        assertTrue(Tickers.isExchangeTicker("KO", true));
        assertTrue(Tickers.isExchangeTicker("KO.A", true));
        assertTrue(Tickers.isExchangeTicker("KO-1", true));
    }
    
}
