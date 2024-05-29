/*
 * TradeBuilderTest.java
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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class TradeBuilderTest
{   
    static final int EQUITY = 100_000;
    static final int POWER  = 200_000;
    static final int MAX_RISK = 2000;
    static final int MONTH_RISK = 6000;

    static final double FIXED_FEE = 1;
    static final double RATIO_FEE = 0.001;
    static final double SHARE_FEE = 0.0001;
    static final double MIN_FEE   = 2;
    static final double MAX_FEE   = 30;
    static final double MAX_RATIO_FEE = 0.5;
    static final double TICK_SIZE = 0.01;

    /**
     * Test of getSize method, of class TradeBuilder.
     */
    @Test
    public void testGetSize()
    {

        {
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0, 0, 0, 0, 0, 0, TICK_SIZE, 2);
            TradeBuilder.Trade result = instance.getSize(TradeBuilder.TradeType.BuyLong, 5.05, 4.75, 0.01, 0, 0);
            assertEquals(6451, result.shares);
        }
        {
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, FIXED_FEE, 0, 0, 0, 0, 0, 0, TICK_SIZE, 2);
            TradeBuilder.Trade result = instance.getSize(TradeBuilder.TradeType.BuyLong, 5.05, 4.75, 0.01, 0, 0);
            assertEquals(6445, result.shares);
        }
        {
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, FIXED_FEE, RATIO_FEE, 0, 0, 0, 0, 0, TICK_SIZE, 2);
            TradeBuilder.Trade result = instance.getSize(TradeBuilder.TradeType.BuyLong, 5.05, 4.75, 0.01, 0, 0);
            assertEquals(6247, result.shares);
        }
        {
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, FIXED_FEE, RATIO_FEE, SHARE_FEE, MIN_FEE, 0, 0, 0, TICK_SIZE, 2);
            TradeBuilder.Trade result = instance.getSize(TradeBuilder.TradeType.BuyLong, 5.05, 4.75, 0.01, 0, 0);
            assertEquals(6243, result.shares);
        }
        {
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, FIXED_FEE, RATIO_FEE, SHARE_FEE, MIN_FEE, MAX_FEE, 0, 0, TICK_SIZE, 2);
            TradeBuilder.Trade result = instance.getSize(TradeBuilder.TradeType.BuyLong, 5.05, 4.75, 0.01, 0, 0);
            assertEquals(6258, result.shares);
        }
        {
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, FIXED_FEE, RATIO_FEE, SHARE_FEE, MIN_FEE, MAX_FEE, 0.0005, 0, TICK_SIZE, 2);
            TradeBuilder.Trade result = instance.getSize(TradeBuilder.TradeType.BuyLong, 5.05, 4.75, 0.01, 0, 0);
            assertEquals(6351, result.shares);
            assertTrue(result.amountEntry-result.amountExit<=MAX_RISK);
        }
        {
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, FIXED_FEE, RATIO_FEE, SHARE_FEE, MIN_FEE, MAX_FEE, 0.0005, 0, 0.05, 2);
            TradeBuilder.Trade result = instance.getSize(TradeBuilder.TradeType.BuyLong, 5.05, 4.75, 0.01, 0, 0);
            assertEquals(6351, result.shares);
            assertTrue(result.amountEntry-result.amountExit<=MAX_RISK);
        }
    }

    /**
     * Test of getFee method, of class TradeBuilder.
     */
    @Test
    public void testGetFee()
    {
        // samples from https://www.interactivebrokers.com/en/index.php?f=1590&p=stocks1
        {   //United States
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0, 0.005, 1, 0, 0.005, 0, TICK_SIZE, 2);
            assertEquals(1.00, instance.getFee(100,  25, false), 0.01);
            assertEquals(5.00, instance.getFee(1000, 25, false), 0.01);
            assertEquals(1.25, instance.getFee(1000, 0.25, false), 0.0001);
        }
        {   //US API Directed Orders
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0, 0.0075, 1, 0, 0.005, 0.0, TICK_SIZE, 2);
            assertEquals(3.75, instance.getFee(500, 25, false), 0.0001);
        }
        {   //Guaranteed VWAP Orders
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0, 0.02, 1, 0, 0.005, 0.0, TICK_SIZE, 2);
            assertEquals(2.00, instance.getFee(100, 25, false), 0.0001);
        }
        {   //Canada
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0, 0.01, 1, 0, 0.005, 0.0, TICK_SIZE, 2);
            assertEquals(1.00, instance.getFee(100, 25, false), 0.0001);
        }
        {   //Mexico
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0.001, 0, 60, 0, 0, 0, TICK_SIZE, 2);
            assertEquals(70.00, instance.getFee(700, 100, false), 0.0001);
        }
        {   //Austria
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0.001, 0, 4, 120, 0, 0, TICK_SIZE, 2);
            assertEquals(5.00, instance.getFee(100, 50, false), 0.0001);
        }
        {   //Belgium
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0.001, 0, 4, 29, 0, 0, TICK_SIZE, 2);
            assertEquals(5.00, instance.getFee(100, 50, false), 0.0001);
            assertEquals(29.00, instance.getFee(1000, 50, false), 0.0001);
        }
        {   //France - USD-denominated
            TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0.001, 0, 5, 42, 0, 0, TICK_SIZE, 2);
            assertEquals(7.50, instance.getFee(100, 75, false), 0.0001);
        }
        {   //United Kingdom - > GBP 50,000 Trade Value
            TradeBuilder instanceLow = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 6, 0, 0, 0, 0, 0, 0, TICK_SIZE, 2);
            TradeBuilder instanceHigh= new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 0, 0.0005, 0, 0, 29, 0, 0, TICK_SIZE, 2);
            int shares = 30_000;
            int price = 2;
            int sharesLow = 50_000/price;
            int sharesHigh= shares-sharesLow;
            assertEquals(11.00, instanceLow.getFee(sharesLow, 2, false)+instanceHigh.getFee(sharesHigh, 2, false), 0.0001);
        }
        {   //USA - > $0.0000218 * $25.87 * 1,000 = $0.563966 (Section 31 Fee)

            TradeBuilder instanceLow = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0.0000218 , TICK_SIZE, 6);
            assertEquals(3.00, instanceLow.getFee(1000, 25.87, false), 0.0001);
            assertEquals(3.563966, instanceLow.getFee(1000, 25.87, true), 0.0001);
        }
        
    }

    /**
     * Test of getTradeType method, of class TradeBuilder.
     */
    @Test
    public void testGetType()
    {
        assertNull(TradeBuilder.getTradeType(0, 0));
        assertEquals(TradeBuilder.TradeType.SellShort, TradeBuilder.getTradeType(0, 1));
        assertEquals(TradeBuilder.TradeType.BuyLong, TradeBuilder.getTradeType(1, 0));
    }

    /**
     * Test of getTradeType method, of class TradeBuilder.
     */
    @Test
    public void testGetTradeType()
    {
        assertEquals(TradeBuilder.TradeType.BuyLong, TradeBuilder.getTradeType(9, 8));
        assertEquals(TradeBuilder.TradeType.SellShort, TradeBuilder.getTradeType(8, 9));
    }

    /**
     * Test of getGainLossRatio method, of class TradeBuilder.
     */
    @Test
    public void testGetGainLossRatio_3args()
    {
        assertEquals(1, TradeBuilder.getGainLossRatio(8, 6, 4), 0.0001);
        assertEquals(2, TradeBuilder.getGainLossRatio(8, 4, 2), 0.0001);
        assertEquals(4, TradeBuilder.getGainLossRatio(8, 4, 3), 0.0001);

        assertEquals(1, TradeBuilder.getGainLossRatio(4, 6, 8), 0.0001);
        assertEquals(2, TradeBuilder.getGainLossRatio(2, 6, 8), 0.0001);
        assertEquals(4, TradeBuilder.getGainLossRatio(3, 7, 8), 0.0001);
    }

    /**
     * Test of getGainLossRatio method, of class TradeBuilder.
     */
    @Test
    public void testGetGainLossRatio_5args()
    {
        assertEquals(4, TradeBuilder.getGainLossRatio(80, 40, 30, 2, 0.0), 0.0001);
        assertEquals(3, TradeBuilder.getGainLossRatio(80, 40, 30, 2, 0.5), 0.0001);
        assertEquals(2, TradeBuilder.getGainLossRatio(80, 40, 30, 2, 1.0), 0.0001);
        
        assertEquals(4, TradeBuilder.getGainLossRatio(30, 70, 80, 2, 0.0), 0.0001);
        assertEquals(3, TradeBuilder.getGainLossRatio(30, 70, 80, 2, 0.5), 0.0001);
        assertEquals(2, TradeBuilder.getGainLossRatio(30, 70, 80, 2, 1.0), 0.0001);
    }

    /**
     * Test of getStopLimit method, of class TradeBuilder.
     */
    @Test
    public void testGetEntryLimit()
    {
        TradeBuilder instance = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, TICK_SIZE, 2);
        assertEquals(53.58, instance.getEntryLimit(48.65, 56.05, 2.0), 0.000001);
    }

    /**
     * Test of getBreakEven method, of class TradeBuilder.
     */
    @Test
    public void testGetBreakEven()
    {
        TradeBuilder instance01 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, TICK_SIZE, 2);
        assertEquals(48.59, instance01.getBreakEven(100, 48.65, 56.05, 0.00).exit, 0.001);
        assertEquals(48.53, instance01.getBreakEven(100, 48.65, 56.05, 0.06).exit, 0.001);

        TradeBuilder instance05 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, 0.05, 2);
        assertEquals(48.55, instance05.getBreakEven(100, 48.65, 56.05, 0.00).exit, 0.001);
        assertEquals(48.50, instance05.getBreakEven(100, 48.65, 56.05, 0.05).exit, 0.001);
    }

    
    /**
     * Test of roundUp method, of class TradeBuilder.
     */
    @Test
    public void testRoundUp()
    {
        TradeBuilder instance01 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, TICK_SIZE, 2);
        assertEquals(0.01, instance01.roundUp(0.009), 0.000001);
        assertEquals(0.01, instance01.roundUp(0.010), 0.000001);
        assertEquals(0.02, instance01.roundUp(0.011), 0.000001);
        assertEquals(0.02, instance01.roundUp(0.019), 0.000001);
        

        TradeBuilder instance05 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, 0.05, 2);
        assertEquals(0.05, instance05.roundUp(0.049), 0.000001);
        assertEquals(0.05, instance05.roundUp(0.050), 0.000001);
        assertEquals(0.10, instance05.roundUp(0.051), 0.000001);
        assertEquals(0.10, instance05.roundUp(0.099), 0.000001);

        TradeBuilder instance25 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, 0.25, 2);
        assertEquals(0.25, instance25.roundUp(0.249), 0.000001);
        assertEquals(0.25, instance25.roundUp(0.250), 0.000001);
        assertEquals(0.50, instance25.roundUp(0.251), 0.000001);
        assertEquals(0.50, instance25.roundUp(0.499), 0.000001);
    }

    /**
     * Test of roundDown method, of class TradeBuilder.
     */
    @Test
    public void testRoundDown()
    {
        TradeBuilder instance01 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, TICK_SIZE, 2);
        assertEquals(0.00, instance01.roundDown(0.009), 0.000001);
        assertEquals(0.01, instance01.roundDown(0.010), 0.000001);
        assertEquals(0.01, instance01.roundDown(0.011), 0.000001);
        assertEquals(0.01, instance01.roundDown(0.019), 0.000001);
        

        TradeBuilder instance05 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, 0.05, 2);
        assertEquals(0.00, instance05.roundDown(0.049), 0.000001);
        assertEquals(0.05, instance05.roundDown(0.050), 0.000001);
        assertEquals(0.05, instance05.roundDown(0.051), 0.000001);
        assertEquals(0.05, instance05.roundDown(0.099), 0.000001);

        TradeBuilder instance25 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, 0.25, 2);
        assertEquals(0.00, instance25.roundDown(0.249), 0.000001);
        assertEquals(0.25, instance25.roundDown(0.250), 0.000001);
        assertEquals(0.25, instance25.roundDown(0.251), 0.000001);
        assertEquals(0.25, instance25.roundDown(0.499), 0.000001);
    }

    /**
     * Test of round method, of class TradeBuilder.
     */
    @Test
    public void testRound()
    {
        TradeBuilder instance01 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, TICK_SIZE, 2);
        assertEquals(0.01, instance01.round(0.009), 0.000001);
        assertEquals(0.01, instance01.round(0.010), 0.000001);
        assertEquals(0.01, instance01.round(0.011), 0.000001);
        assertEquals(0.02, instance01.round(0.016), 0.000001);
        

        TradeBuilder instance05 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, 0.05, 2);
        assertEquals(0.05, instance05.round(0.049), 0.000001);
        assertEquals(0.05, instance05.round(0.050), 0.000001);
        assertEquals(0.05, instance05.round(0.051), 0.000001);
        assertEquals(0.10, instance05.round(0.099), 0.000001);

        TradeBuilder instance25 = new TradeBuilder(EQUITY, POWER, MAX_RISK, MONTH_RISK, 3, 0, 0, 0, 0, 0, 0, 0.25, 2);
        assertEquals(0.25, instance25.round(0.249), 0.000001);
        assertEquals(0.25, instance25.round(0.250), 0.000001);
        assertEquals(0.25, instance25.round(0.251), 0.000001);
        assertEquals(0.50, instance25.round(0.499), 0.000001);
    }
    
}
