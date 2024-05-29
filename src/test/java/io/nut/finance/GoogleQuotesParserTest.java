/*
 * GoogleQuotesParserTest.java
 *
 * Copyright (c) 2017-2023 francitoshi@gmail.com
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

import io.nut.base.time.JavaTime;
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
