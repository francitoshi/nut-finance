/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.indicator;

import io.github.francitoshi.nut.finance.indicator.ExponentialMovingAverage;
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



