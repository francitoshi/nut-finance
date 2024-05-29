/*
 * ExponentialMovingAverageTest.java
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
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class ExponentialMovingAverageTest
{
    /**
     * Test of get method, of class ExponentialMovingAverage.
     */
    @Test
    public void testGet()
    {
        {//http://etfhq.com/blog/2010/11/08/exponential-moving-average/
            double[] value = {1,2,3,4,5,6};
            ExponentialMovingAverage instance = new ExponentialMovingAverage(3);
            double[] expect = {1,1.5,2.25,3.13,4.06,5.03};
            double[] result = instance.get1st(value);
            assertArrayEquals(expect, result, 0.01);
        }
    }
    
}



