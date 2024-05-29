/*
 * Tickers.java
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
package io.nut.finance;

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
