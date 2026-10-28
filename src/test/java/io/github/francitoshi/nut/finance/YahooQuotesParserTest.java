/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance;

import io.github.francitoshi.nut.base.commons.time.JavaTime;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class YahooQuotesParserTest
{
    /**
     * Test of importQuotes method, of class YahooQuotesParser.
     * @throws java.io.IOException
     */
    @Test
    public void testImportQuotes() throws IOException, ParseException
    {
        LocalDate start = JavaTime.parseLocalDate("2016-08-11", JavaTime.UTC);
        LocalDate end = JavaTime.parseLocalDate("2017-08-10", JavaTime.UTC);
        try( InputStream in = new GZIPInputStream(YahooQuotesParserTest.class.getResourceAsStream("finance.yahoo-ko.csv.gz")))
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            YahooQuotesParser instance = new YahooQuotesParser();
            instance.importQuotes(in, sq);
        }
        try( InputStream in = new GZIPInputStream(YahooQuotesParserTest.class.getResourceAsStream("finance.yahoo-msft.csv.gz")))
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            YahooQuotesParser instance = new YahooQuotesParser();
            instance.importQuotes(in, sq);
        }
    }

    /**
     * Test of getQuotes method, of class YahooQuotesParser.
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testGetQuotes() throws MalformedURLException, IOException, ParseException
    {
        if(StockQuotesTest.DO_ONLINE_TESTS)
        {
            YahooQuotesParser instance = new YahooQuotesParser();

    //        HttpUrl httpUrl = HttpUrl.parse("https://finance.yahoo.com/");
    //        Cookie cookie = Cookie.parse(httpUrl, "B=ecu2bh1cs5sd5&b=3&s=b8; expires=Thu, 20 Sep 2018 22:54:29 GMT; domain=yahoo.com; path=/");
    //        ArrayList list = new ArrayList();
    //        list.add(cookie);
    //        instance.loadCookiesAndCrumb(list, "2Hz2L\u002FUHaZX");

            LocalDate start = JavaTime.parseLocalDate("2016-08-11", JavaTime.UTC);
            LocalDate end = JavaTime.parseLocalDate("2017-08-10", JavaTime.UTC);

            StockQuotes sq = new StockQuotes(2, 0.01);
            {        
                URL url = instance.buildQuotesUrl("KO", start, end);
                InputStream result = instance.getQuotes(url);
                instance.importQuotes(result, sq);
            }
            {
                URL url = instance.buildDividendsUrl("KO", start, end);
                InputStream result = instance.getDividends(url);
                instance.importDividends(result, sq);
            }
        }
    }

}
