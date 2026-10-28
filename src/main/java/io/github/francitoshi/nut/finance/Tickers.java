/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance;

import java.util.regex.Pattern;

/**
 *
 * @author franci
 */
public abstract class Tickers
{
    public static final String EXCHANGE_PATTERN = "([A-Za-z][A-Za-z0-9]*)";
    public static final String TICKER_PATTERN = "([$\\^]?[A-Za-z][A-Za-z0-9.-]*)";
    public static final String EXCHANGE_TICKER_PATTERN = EXCHANGE_PATTERN+':'+TICKER_PATTERN;
    public static final String EXCHANGE_TICKER_LENIENT_PATTERN = "("+EXCHANGE_PATTERN+":)?"+TICKER_PATTERN;
    
    public static final Pattern exchangePattern = Pattern.compile(EXCHANGE_PATTERN);
    public static final Pattern tickerPattern = Pattern.compile(TICKER_PATTERN);
    public static final Pattern exchangeTickerPattern = Pattern.compile(EXCHANGE_TICKER_PATTERN);
    public static final Pattern exchangeTickerLenientPattern = Pattern.compile(EXCHANGE_TICKER_LENIENT_PATTERN);
    
    public static boolean isExchange(String s)
    {
        return exchangePattern.matcher(s).matches();
    }
    public static boolean isTicker(String s)
    {
        return tickerPattern.matcher(s).matches();
    }
    public static boolean isExchangeTicker(String s, boolean lenient)
    {
        return (lenient?exchangeTickerLenientPattern:exchangeTickerPattern).matcher(s).matches();
    }
    
}
