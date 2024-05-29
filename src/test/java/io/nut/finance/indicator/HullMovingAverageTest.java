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

import java.io.IOException;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class HullMovingAverageTest
{
    /**
     * Test of get method, of class WeightedMovingAverage.
     */
    @Test
    public void testGet() throws IOException, ParseException
    {
        {   
            double[] value = {109.460, 107.070, 108.480, 107.290, 106.660, 109.680, 109.650, 109.660, 108.030, 107.960, 107.160, 106.110, 102.870, 102.200, 102.760, 103.220, 104.210, 107.530, 107.870, 107.420, 109.230, 109.570, 111.410, 111.730, 112.140, 111.960, 112.000, 109.510, 109.680, 109.150};
            HullMovingAverage instance = new HullMovingAverage(9);
            double[] expect = {107.740, 107.193, 107.650, 108.382, 109.674, 110.617, 110.348, 109.705, 108.566, 106.968, 104.678, 102.523, 101.258, 101.527, 102.650, 104.178, 105.688, 106.834, 107.474, 108.625, 110.207, 111.510, 112.613, 113.007, 112.874, 112.093, 110.867, 109.218, 107.706, 106.128};
            double[] result = instance.get1st(value);
            assertArrayEquals(expect, result, 0.005);
        }
       
       
    }
    
}
