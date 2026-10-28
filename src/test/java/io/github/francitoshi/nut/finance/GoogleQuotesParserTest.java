/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance;

import io.github.francitoshi.nut.base.commons.time.JavaTime;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class GoogleQuotesParserTest
{
    /**
     * Test of importQuotes method, of class GoogleQuotesParser.
     * @throws java.io.IOException
     * @throws java.text.ParseException
     */
    @Test
    public void testImportQuotes() throws IOException, ParseException
    {
        LocalDate start = JavaTime.parseLocalDate("11-Aug-16", JavaTime.UTC);
        LocalDate end = JavaTime.parseLocalDate("10-Aug-17", JavaTime.UTC);
        try( InputStream in = new GZIPInputStream(GoogleQuotesParserTest.class.getResourceAsStream("finance.google-ko.csv.gz")))
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            GoogleQuotesParser instance = new GoogleQuotesParser();
            instance.importQuotes(in, sq);
        }
        try( InputStream in = new GZIPInputStream(GoogleQuotesParserTest.class.getResourceAsStream("finance.google-msft.csv.gz")))
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            GoogleQuotesParser instance = new GoogleQuotesParser();
            instance.importQuotes(in, sq);
        }
    }

//    /**
//     * Test of getQuotes method, of class GoogleQuotesParser.
//     * @throws java.net.MalformedURLException
//     * @throws java.text.ParseException
//     */
//    @Test
//    public void testGetQuotes() throws MalformedURLException, IOException, ParseException
//    {
//        if(StockQuotesTest.DO_ONLINE_TESTS)
//        {
//            GoogleQuotesParser instance = new GoogleQuotesParser();
//            LocalDate start = JavaTime.parseLocalDate("11-Aug-16", JavaTime.UTC);
//            LocalDate end = JavaTime.parseLocalDate("10-Aug-17", JavaTime.UTC);
//
//            URL url = instance.buildQuotesUrl("KO", start, end);
//            InputStream result = instance.getQuotes(url);
//            StockQuotes sq = new StockQuotes(2, 0.01);
//            instance.importQuotes(result, sq);
//        }
//    }

}
