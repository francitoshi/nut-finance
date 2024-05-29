/*
 * QuotesParser.java
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
