/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;

/**
 *
 * @author franci
 */
public interface QuotesParser
{
    int importQuotes(InputStream in, StockQuotes sq) throws ParseException;
    int importDividends(InputStream in, StockQuotes sq) throws ParseException;
    
    URL buildQuotesUrl(String ticker, LocalDate start, LocalDate end) throws MalformedURLException;
    URL buildDividendsUrl(String ticker, LocalDate start, LocalDate end) throws MalformedURLException;
    
    InputStream getQuotes(URL url) throws IOException;
    InputStream getDividends(URL url) throws IOException;
    
}
