/*
 * WeightedMovingAverageTest.java
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
package io.nut.finance.indicator;


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
