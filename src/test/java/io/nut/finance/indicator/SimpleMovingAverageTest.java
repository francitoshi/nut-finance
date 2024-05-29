/*
 * SimpleMovingAverageTest.java
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
package io.nut.finance.indicator;

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
public class SimpleMovingAverageTest
{
    /**
     * Test of get method, of class SimpleMovingAverage.
     */
    @Test
    public void testGet()
    {
        {   //https://www.tradingview.com/wiki/Moving_Average
            double[] value = {5, 6, 7, 8, 9};
            SimpleMovingAverage instance = new SimpleMovingAverage(3);
            double[] expect = {5, 5.333, 6, 7, 8};
            double[] result = instance.get1st(value);
            assertArrayEquals(expect, result, 0.001);
        }
        {
            double[] value = {11,12,13,14,15,16,17};
            SimpleMovingAverage instance = new SimpleMovingAverage(5);
            double[] expect = {11, 11.2, 11.6, 12.2, 13,14,15};
            double[] result = instance.get1st(value);
            assertArrayEquals(expect, result, 0.001);
        }
        {
            double[] value = {15,6,10,15,9,7,11,12,14,11,5};
            SimpleMovingAverage instance = new SimpleMovingAverage(10);
            double[] expect = {15,14.1,13.6,13.6,13,12.2,11.8,11.5,11.4, 11,10};
            double[] result = instance.get1st(value);
            assertArrayEquals(expect, result, 0.001);
        }
        {
            double[] value = {22.2734, 22.1940, 22.0847, 22.1741, 22.1840, 22.1344, 22.2337, 22.4323, 22.2436, 22.2933, 22.1542, 22.3926, 22.3816, 22.6109, 23.3558, 24.0519, 23.7530, 23.8324, 23.9516, 23.6338, 23.8225, 23.8722, 23.6537, 23.1870, 23.0976, 23.3260, 22.6805, 23.0976, 22.4025, 22.1725};
            SimpleMovingAverage instance = new SimpleMovingAverage(10);
            double[] expect = {22.2734, 22.26546, 22.24659, 22.23666, 22.22772, 22.21382, 22.20985, 22.22574, 22.22276, 
            22.224750, 22.212830, 22.232690, 22.262380, 22.306060, 22.423240, 22.614990, 22.766920, 22.906930, 23.077730, 23.211780, 23.378610, 23.526570, 23.653780, 23.711390, 23.685570, 23.612980, 23.505730, 23.432250, 23.277340, 23.131210};
            double[] result = instance.get1st(value);
            assertArrayEquals(expect, result, 0.001);
        }
        {
            double[] value =  {280, 288, 266,295,302,310,303,328,309,315,320,332,310,308,320};
            SimpleMovingAverage instance3 = new SimpleMovingAverage(3);
            double[] expect3 = {280, 282.666, 278,283,287.666,302.333,305,313.666,313.333,317.333,314.666,322.333,320.666,316.666,312.666};
            double[] result3 = instance3.get1st(value);
            assertArrayEquals(expect3, result3, 0.001);
            
            SimpleMovingAverage instance4 = new SimpleMovingAverage(4);
            double[] expect4 = {280, 282.0, 278.5,282.25,287.75,293.25,302.5,310.75,312.5,313.75,318.0,319.0,319.25,317.5,317.5};
            double[] result4 = instance4.get1st(value);
            assertArrayEquals(expect4, result4, 0.01);
        }
    }
    
}
