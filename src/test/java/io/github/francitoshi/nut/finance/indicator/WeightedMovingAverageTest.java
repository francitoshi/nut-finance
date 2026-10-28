/*
 * Copyright (C) 2018-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.indicator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class WeightedMovingAverageTest
{
    /**
     * Test of get method, of class WeightedMovingAverage.
     */
    @Test
    public void testGet()
    {
        {   //http://www.onlinetradingconcepts.com/TechnicalAnalysis/MAWeighted.html
            double[] value = {5, 4, 8};
            WeightedMovingAverage instance = new WeightedMovingAverage(3);
            double[] expect = {5, 4.4, 6.17};
            double[] result = instance.get1st(value);
            assertArrayEquals(expect, result, 0.005);
        }
        {   //https://www.investopedia.com/ask/answers/071414/whats-difference-between-moving-average-and-weighted-moving-average.asp
            double[] value = {90.91, 90.83, 90.28, 90.36, 90.90};
            WeightedMovingAverage instance = new WeightedMovingAverage(5);
            double[] expect = {90.91, 90.87, 90.62, 90.52, 90.62};
            double[] result = instance.get1st(value);
            assertArrayEquals(expect, result, 0.005);
        }
    }
    
}
