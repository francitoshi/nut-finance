/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.indicator;

/**
 *
 * @author franci
 */
public interface Indicator
{
    int seedSize(double weight);
    double[] get1st(double[] value);
    double[][] getAll(double[] value);    
}
