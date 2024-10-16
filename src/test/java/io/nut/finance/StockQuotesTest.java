/*
 * StockQuotesTest.java
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

import io.nut.finance.StockQuotes.Quote;
import io.nut.base.time.JavaTime;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author franci
 */
public class StockQuotesTest
{
    
    public static final boolean DEBUG = false;
    public static final boolean DO_ONLINE_TESTS = true;

    /**
     * Test of getOpen method, of class StockQuotes.
     * @throws java.io.IOException
     * @throws java.text.ParseException
     */
    @Test
    public void testGetOpen_LocalDate_LocalDate() throws IOException, ParseException
    {
        LocalDate start = JavaTime.parseLocalDate("11-Aug-16", JavaTime.UTC);
        LocalDate end = JavaTime.parseLocalDate("10-Aug-17", JavaTime.UTC);
        try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("finance.google-ko.csv.gz")))
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            GoogleQuotesParser parser = new GoogleQuotesParser();
            parser.importQuotes(in, sq);
            
            double[] result = sq.getOpen(start, end, false);
            assertEquals(250 ,result.length);
        }
    }
    /**
     * Test of getOpen method, of class StockQuotes.
     * @throws java.io.IOException
     * @throws java.text.ParseException
     */
    @Test
    public void testGetOpen_int_LocalDate() throws IOException, ParseException
    {
        LocalDate start = JavaTime.parseLocalDate("11-Aug-16", JavaTime.UTC);
        LocalDate end = JavaTime.parseLocalDate("10-Aug-17", JavaTime.UTC);
        try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("finance.google-ko.csv.gz")))
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            GoogleQuotesParser parser = new GoogleQuotesParser();
            parser.importQuotes(in, sq);
            
            double[] result = sq.getOpen(100, end, false);
            assertEquals(100 ,result.length);
        }
    }

    static final int[] OPEN = {6, 8, 8, 8, 8, 8, 4, 1, 7, 9};
    static final int[] HIGH = {7, 8, 9, 10,8, 8, 8, 3, 9, 9};
    static final int[] LOW  = {4, 5, 6, 8, 2, 5, 4, 1, 6, 5};
    static final int[] CLOSE= {5, 5, 7, 9, 4, 6, 8, 1, 7, 5};
    /**
     * Test of getEnvelope method, of class StockQuotes.
     * @throws java.io.IOException
     * @throws java.text.ParseException
     */
    @Test
    public void testGetEnvelope() throws IOException, ParseException
    {
        {
            
            StockQuotes daily = new StockQuotes(2, 0.01);
            
            for(int i=0;i<110;i++)
            {
                daily.add(LocalDate.ofEpochDay(i),2,4,2,3,1);
            }
            double[] sma = daily.getSimpleMovingAverage(100, null, 10, false);
            double[] ema = daily.getExponentialMovingAverage(100, null, 10, false);

            StockQuotes.Envelope dse = daily.getEnvelope(100, null, 10, false, StockQuotes.Coverage.ByPrice, 0.95, 0.0001);
            StockQuotes.Envelope dee = daily.getEnvelope(100, null, 10, true, StockQuotes.Coverage.ByPrice, 0.95, 0.0001);
            assertEquals(dse.firstDay, dee.firstDay);
            
            //test envelope on weekly chart
            StockQuotes weekly = daily.getWeekly();            
            StockQuotes.Envelope wse = weekly.getEnvelope(100, null, 10, false, StockQuotes.Coverage.ByPrice, 0.95, 0.0001);
            StockQuotes.Envelope wee = weekly.getEnvelope(100, null, 10, true, StockQuotes.Coverage.ByPrice, 0.95, 0.0001);            
            assertEquals(wse.firstDay, wee.firstDay);
        }        
        {
            
            StockQuotes sq = new StockQuotes(2, 0.01);
            
            for(int i=0;i<OPEN.length;i++)
            {
                sq.add(LocalDate.ofEpochDay(i),OPEN[i],HIGH[i],LOW[i],CLOSE[i],1);
            }
            double[] sma = sq.getSimpleMovingAverage(10, null, 3, false);
            double[] ema = sq.getExponentialMovingAverage(10, null, 3, false);
            StockQuotes.Envelope se = sq.getEnvelope(5, null, 3, false, StockQuotes.Coverage.ByPrice, 0.95, 0.0005);
            StockQuotes.Envelope ee = sq.getEnvelope(5, null, 3, true, StockQuotes.Coverage.ByPrice, 0.95, 0.0005);
        }        
        
        
        LocalDate start = JavaTime.parseLocalDate("01-Jul-16", JavaTime.UTC);
        LocalDate end = JavaTime.parseLocalDate("01-Aug-17", JavaTime.UTC);
        try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("finance.google-ko.csv.gz")))
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            GoogleQuotesParser instance = new GoogleQuotesParser();
            instance.importQuotes(in, sq);

            StockQuotes.Envelope resultPrice = sq.getEnvelope(100, end, 26, true, StockQuotes.Coverage.ByPrice, 0.95, 0.0005);
            StockQuotes.Envelope resultBar = sq.getEnvelope(100, end, 26, true, StockQuotes.Coverage.ByBar, 0.95, 0.0005);       
            //assertEquals(0.01285, result, 0.0);
        }
        
        if(StockQuotesTest.DO_ONLINE_TESTS)
        {
            YahooQuotesParser instance = new YahooQuotesParser();
            URL url = instance.buildQuotesUrl("KO", start, LocalDate.now());
            System.out.println("url="+url);
            try( InputStream in = instance.getQuotes(url))
            {
                StockQuotes sq = new StockQuotes(2, 0.01);
                instance.importQuotes(in, sq);

                StockQuotes.Envelope resultPrice = sq.getEnvelope(100, end, 26, true, StockQuotes.Coverage.ByPrice, 0.95, 0.0005);

                StockQuotes.Envelope resultBar = sq.getEnvelope(100, end, 26, true, StockQuotes.Coverage.ByBar, 0.95, 0.0005);

                //            assertEquals(0.0341, result, 0.0005);
            }
        }
    }

    /**
     * Test of getMovingAverageConvergenceDivergence method, of class StockQuotes.
     */
    @Test
    public void testGetMovingAverageConvergenceDivergence_3args_2() throws IOException, ParseException
    {
        LocalDate end = JavaTime.parseLocalDate("08-Sep-17", JavaTime.UTC);

        StockQuotes sq = new StockQuotes(2, 0.01);
        YahooQuotesParser parser = new YahooQuotesParser();

        try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("finance.yahoo-ko.csv.gz")))
        {
            parser.importQuotes(in, sq);
        }
        try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("finance.yahoo-ko-dividends.csv.gz")))
        {
            parser.importDividends(in, sq);
        }
        double[][] macd100 = sq.getMovingAverageConvergenceDivergence(100, end, 12, 26, 9, false);
        double[][] macd200 = sq.getMovingAverageConvergenceDivergence(200, end, 12, 26, 9, false);
        
        LocalDate[] date = sq.getDate(100, end, false);
        
        if(DEBUG)
        {
            for(int i=0;i<macd100[0].length;i++)
            {
                System.out.printf("%d= %s %.2f %.2f %.2f | %.4f %.4f %.4f \n", i, date[i].toString(),
                        macd100[0][i], macd100[1][i], macd100[2][i], 
                        macd200[0][i+100], macd200[1][i+100], macd200[2][i+100]);
            }
        }
    }

    
    /**
     * Test of adjust method, of class StockQuotes.
     */
    @Test
    public void testAdjust()
    {
        double[] src = {46.72,  46.87,  46.11,  46.18};
        double[] div = {0,      0,      0.37,   0};
        double[] exp = {46.35,  46.50,  46.11,  46.18};
        
        StockQuotes.adjust(src, div);
        assertArrayEquals(exp,src, 0.000001);
    }

    /**
     * Test of getGap method, of class StockQuotes.
     */
    @Test
    public void testGetGap()
    {
//666        System.out.println("getGap");
//        int count = 0;
//        LocalDate endAt = null;
//        double coverage = 0.0;
//        StockQuotes instance = new StockQuotes(2, 0.01);
//        StockQuotes.Gap expResult = null;
//        StockQuotes.Gap result = instance.getGap(count, endAt, coverage);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getAverageTrueRange method, of class StockQuotes.
     */
    @Test
    public void testGetAverageTrueRange() throws IOException, ParseException
    {
        //https://estrategiastrading.com/indicador-history/
        //https://docs.google.com/spreadsheets/d/16F6Buk3DUUdcvmfbvzuNNfuVw6CbE3-C97RRRhOSvLI/edit#gid=478070705
        
        LocalDate end = LocalDate.now();

        StockQuotes sq = new StockQuotes(2, 0.01);
        GoogleQuotesParser parser = new GoogleQuotesParser();

        try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("atr-data.csv.gz")))
        {
            parser.importQuotes(in, sq);
        }
        
        StockQuotes.AverageTrueRange atr = sq.getAverageTrueRange(100, end, 14);
        
        assertEquals(1.25, atr.value, 0.005);
    }

    /**
     * Test of getParabolicStop method, of class StockQuotes.
     * @throws java.io.IOException
     */
    @Test
    public void testGetParabolicStop() throws IOException, ParseException
    {
        //https://estrategiastrading.com/stop-parabolico-sar/
        //https://docs.google.com/spreadsheets/d/1_zzFgu-gp__JklLCh7hZ-oMClSUdARQxuS0TNxmq_Aw/edit#gid=0
        
        LocalDate end = LocalDate.now();

        StockQuotes sq = new StockQuotes(2, 0.01);
        GoogleQuotesParser parser = new GoogleQuotesParser();

        try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("parabolic-stop.csv.gz")))
        {
            parser.importQuotes(in, sq);
        }
        
        LocalDate entry = sq.firstKey();
        LocalDate until = sq.lastKey();
                
        StockQuotes.TrailingStop ts = sq.getParabolicStop(entry, until, 50, 0.02, 0.20, false);     
        
        assertEquals(50.0470, ts.historyValues[0], 0.00005);
        assertEquals(52.1000, ts.historyValues[12], 0.00005);
        assertEquals(53.9238, ts.historyValues[16], 0.00005);
        assertEquals(56.2792, ts.historyValues[20], 0.00005);
        assertEquals(56.6233, ts.value, 0.00005);
        assertEquals(56.6233, ts.exitValue, 0.00005);
        
    }

    /**
     * Test of getSafeZoneStop method, of class StockQuotes.
     * @throws java.io.IOException
     * @throws java.text.ParseException
     */
    @Test
    public void testGetSafeZoneStop() throws IOException, ParseException
    {

        //from Come Into My Trading Room - Alexander Elder
        StockQuotes sq = new StockQuotes(3, 0.01);
        GoogleQuotesParser parser = new GoogleQuotesParser();

        try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("safezone-stop.csv.gz")))
        {
            parser.importQuotes(in, sq);
        }
        
        int period = 10;
        LocalDate entry = sq.getDate(null, null, false)[period];
        LocalDate until = sq.lastKey();

        {        
            StockQuotes.TrailingStop ts = sq.getSafeZoneStop(entry, until, 100.00, 2.0, period , false);     
            assertNotNull(ts);

            // book value is 109.90 but I think it is wrong it must be 109.89 (stops must round protecting, never risking)
            double[] stops = {109.89,109.89,113.00, 113.50, 113.50, 113.60, 113.60, 113.60, 113.60, 113.60, 113.60, 113.60, 115.13, 115.13};

            for(int i=0;i<stops.length;i++)
            {
                assertEquals(stops[i], ts.historyValues[i], 0.005, i+"=["+ts.historyDates[i]+"]");
            }
        }
        {        
            StockQuotes.TrailingStop ts = sq.getSafeZoneStop(entry, until, 200.00, 2.0, period , true);     
            assertNotNull(ts);

            double[] stops = {116.45, 116.45};

            for(int i=0;i<stops.length;i++)
            {
                assertEquals(stops[i], ts.historyValues[i], 0.005, i+"=["+ts.historyDates[i]+"]");
            }
        }
    }

    /**
     * Test of getChandelierStop method, of class StockQuotes.
     */
    @Test
    public void testGetChandelierStop() throws IOException, ParseException
    {
        {        
            StockQuotes sq = new StockQuotes(3, 0.01);
            GoogleQuotesParser parser = new GoogleQuotesParser();

            try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("chandelier-stop-bull.csv.gz")))
            {
                parser.importQuotes(in, sq);
            }

            int period = 2;
            LocalDate entry = sq.getDate(null, null, false)[0];
            LocalDate until = sq.lastKey();

            StockQuotes.TrailingStop ts = sq.getChandelierStop(entry, until, 8.0, 2.0, period, false);     
            assertNotNull(ts);

            double[] stops = {8.5, 9.5, 10, 10, 10, 10, 12, 17, 17, 17};

            for(int i=1;i<stops.length;i++)
            {
                assertEquals(stops[i], ts.historyValues[i], 0.005, i+"=["+ts.historyDates[i]+"]");
            }
        }
        {        
            StockQuotes sq = new StockQuotes(3, 0.01);
            GoogleQuotesParser parser = new GoogleQuotesParser();

            try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("chandelier-stop-bear.csv.gz")))
            {
                parser.importQuotes(in, sq);
            }

            int period = 2;
            LocalDate entry = sq.getDate(null, null, false)[0];
            LocalDate until = sq.lastKey();

            StockQuotes.TrailingStop ts = sq.getChandelierStop(entry, until, 24.0, 2.0, period, true);     
            assertNotNull(ts);

            double[] stops = {23, 23, 23, 23, 23, 23, 21, 21, 19, 16, 14.5};

            for(int i=1;i<stops.length;i++)
            {
                assertEquals(stops[i], ts.historyValues[i], 0.005, i+"=["+ts.historyDates[i]+"]");
            }
        }
    }
    /**
     * Test of getChandelierStop method, of class StockQuotes.
     */
    @Test
    public void testTrailingStopExits() throws IOException, ParseException
    {
        {        
            StockQuotes sq = new StockQuotes(3, 0.01);
            YahooQuotesParser parser = new YahooQuotesParser();

            try( InputStream in = new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("spy-trailing-stops.csv.gz")))
            {
                parser.importQuotes(in, sq);
            }

            LocalDate start = LocalDate.parse("2018-01-03");
            LocalDate exit  = LocalDate.parse("2018-01-30");
            LocalDate end  = LocalDate.parse("2018-01-31");

            StockQuotes.TrailingStop parabolic = sq.getParabolicStop(start, end, 266.68, 0.02, 0.20, false);
            StockQuotes.TrailingStop safeZone  = sq.getSafeZoneStop(start, end, 266.68, 3, 26, false);
            StockQuotes.TrailingStop chandelier= sq.getChandelierStop(start, end, 266.68, 3, 26, false);
            
            assertEquals(exit, parabolic.exitAt);
            assertEquals(exit, safeZone.exitAt);
            assertEquals(exit, chandelier.exitAt);
            
            assertEquals(282.60, parabolic.exitValue, 0.006);
            assertEquals(282.60, safeZone.exitValue, 0.006);
            assertEquals(281.26, chandelier.exitValue, 0.006);
            
        }
    }

    /**
     * Test of getWeekly method, of class StockQuotes.
     */
    @Test
    public void testGetWeekly() throws IOException, ParseException
    {
        try( InputStream in = getSPY1993to2018())
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            YahooQuotesParser parser = new YahooQuotesParser();
            parser.importQuotes(in, sq);
            
            StockQuotes weekly = sq.getWeekly();

            LocalDate start = LocalDate.parse("2000-01-03");

            LocalDate[] date     = weekly.getDate(start, null, false);
            double[] open   = weekly.getOpen(start, null, false);
            double[] high   = weekly.getHigh(start, null, false);
            double[] low    = weekly.getLow(start, null, false);
            double[] close  = weekly.getClose(start, null, false);
            double[] volume = weekly.getVomume(start, null, false);

            assertEquals(date.length ,open.length);
            assertEquals(date.length ,high.length);
            assertEquals(date.length ,low.length);
            assertEquals(date.length ,close.length);
            assertEquals(date.length ,volume.length);
            assertEquals(148.25, open[0], 0.005);
            assertEquals(148.25, high[0], 0.005);
            assertEquals(137.25, low[0], 0.005);
            assertEquals(145.75, close[0], 0.005);//adj 104.03
            assertEquals(42_725_700,volume[0], 100);

            assertEquals(146.25, open[1], 0.005);
            assertEquals(147.47, high[1], 0.005);
            assertEquals(142.88, low[1], 0.005);
            assertEquals(146.97, close[1], 0.005);//adj 104.90
            assertEquals(32_748_700,volume[1], 1);  
        }
    }

    /**
     * Test of getMonthly method, of class StockQuotes.
     * @throws java.io.IOException
     */
    @Test
    public void testGetMonthly() throws IOException, ParseException
    {
        try( InputStream in = getSPY1993to2018())
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            YahooQuotesParser parser = new YahooQuotesParser();
            parser.importQuotes(in, sq);
            
            StockQuotes monthly = sq.getMonthly();

            LocalDate start = LocalDate.parse("2017-01-01");

            LocalDate[] date     = monthly.getDate(start, null, false);
            double[] open   = monthly.getOpen(start, null, false);
            double[] high   = monthly.getHigh(start, null, false);
            double[] low    = monthly.getLow(start, null, false);
            double[] close  = monthly.getClose(start, null, false);
            double[] volume = monthly.getVomume(start, null, false);

            assertEquals(date.length ,open.length);
            assertEquals(date.length ,high.length);
            assertEquals(date.length ,low.length);
            assertEquals(date.length ,close.length);
            assertEquals(date.length ,volume.length);
            
            					
            
            assertEquals(225.04, open[0], 0.005);
            assertEquals(229.71, high[0], 0.005);
            assertEquals(223.88, low[0], 0.005);
            assertEquals(227.53, close[0], 0.005);//adj 223.19
            assertEquals(1_479_754_500,volume[0], 100);

            				
            assertEquals(227.53, open[1], 0.005);
            assertEquals(237.31, high[1], 0.005);
            assertEquals(226.82, low[1], 0.005);
            assertEquals(236.47, close[1], 0.005);//adj 231.96
            assertEquals(1_361_822_100,volume[1], 1);  
        }
    }
    /**
     * Test of getMonthly method, of class StockQuotes.
     */
    @Test
    public void testBestDay() throws IOException, ParseException
    {
        try( InputStream in = getSPY1993to2018())
        {
            StockQuotes sq = new StockQuotes(2, 0.01);
            YahooQuotesParser parser = new YahooQuotesParser();
            parser.importQuotes(in, sq);
            
            StockQuotes monthly = sq.getMonthly();
            
            double[] dowSum = new double[5];
            double[] dowPosSum = new double[5];
            double[] dowNegSum = new double[5];
            int[] dowCount = new int[5];
            int[] dowPosCount = new int[5];
            int[] dowNegCount = new int[5];

            double[] domSum = new double[31];
            int[] domCount = new int[31];
            
            double[] moySum = new double[12];
            int[] moyCount = new int[12];
            
            for(Quote q : sq.getQuotes())
            {
                double diff = q.close - q.open;
                LocalDate date = q.date;
                int dowDay = date.getDayOfWeek().getValue()-1;
                dowSum[dowDay] += diff;
                dowCount[dowDay]++;
                if(diff>0)
                {
                    dowPosSum[dowDay] += diff;
                    dowPosCount[dowDay]++;
                }
                else if(diff<0)
                {
                    dowNegSum[dowDay] += diff;
                    dowNegCount[dowDay]++;
                }
                                
                int domDay = date.getDayOfMonth()-1;
                domSum[domDay] += diff;
                domCount[domDay]++;

                int moyDay = date.getMonthValue()-1;
                moySum[moyDay] += diff;
                moyCount[moyDay]++;
            }
            System.out.println("----- day of week ------------------------------");
            
            new SimpleDateFormat().getDateFormatSymbols().getMonths();
            
            for(int i=0;i<dowSum.length;i++)
            {
                dowSum[i] /= dowCount[i];
                dowPosSum[i] /= dowPosCount[i];
                dowNegSum[i] /= dowNegCount[i];
                System.out.println((i+1)+"="+dowSum[i]+" ( "+dowPosSum[i]+" ["+dowPosCount[i]+"] "+dowNegSum[i]+" ["+dowNegCount[i]+"] )");
            }
            System.out.println("----- day of month -----------------------------");
            for(int i=0;i<domSum.length;i++)
            {
                domSum[i] /= domCount[i];
                System.out.println((i+1)+"="+domSum[i]);
            }
            System.out.println("----- month of year ----------------------------");
            for(int i=0;i<moySum.length;i++)
            {
                moySum[i] /= moyCount[i];
                System.out.println((i+1)+"="+moySum[i]);
            }
            System.out.println("------------------------------------------------");
        }
    }

    public static GZIPInputStream getSPY1993to2018() throws IOException
    {
        return new GZIPInputStream(StockQuotesTest.class.getResourceAsStream("finance.yahoo_spy_1993-01-29_2018-02-23.csv.gz"));
    }
}

