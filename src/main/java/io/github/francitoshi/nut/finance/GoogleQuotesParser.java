/*
 * Copyright (C) 2017-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance;

import io.github.francitoshi.nut.base.commons.lang.CharSets;
import io.github.francitoshi.nut.base.commons.time.JavaTime;
import io.github.francitoshi.nut.base.commons.lang.Parsers;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Scanner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 *
 * @author franci
 */
public class GoogleQuotesParser implements QuotesParser
{
    private static final String DATE_OPEN_HIGH_LOW_CLOSE_VOLUME = "Date,Open,High,Low,Close,Volume";
    
    @Override
    public int importQuotes(InputStream in, StockQuotes sq) throws ParseException
    {
        int count=0;
        Scanner sc = new Scanner(in, CharSets.ISO88591);
        try
        {
            while(sc.hasNext())
            {
                String quote = sc.nextLine().trim();
                if(quote.isEmpty() || quote.contains(DATE_OPEN_HIGH_LOW_CLOSE_VOLUME))
                {
                    continue;
                }
                String[] data = quote.split(",");

                LocalDate date = JavaTime.parseLocalDate(data[0], JavaTime.UTC);
                double open = Parsers.safeParseDouble(data[1]);
                double high = Parsers.safeParseDouble(data[2]);
                double low = Parsers.safeParseDouble(data[3]);
                double close = Parsers.safeParseDouble(data[4]);
                double volume = Parsers.safeParseDouble(data[5]);

                sq.add(date, open, high, low, close, volume);
                count++;
            }
        }
        catch(IllegalArgumentException ex)
        {
            ParseException parseException = new ParseException(ex.getMessage(), count);
            parseException.initCause(ex);
            throw parseException;
        }
        return count;
    }
    @Override
    public InputStream getQuotes(URL url) throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        Response response = client.newCall(request).execute();
        if(this.successful=response.isSuccessful())
        {
            this.message = response.message();
            return response.body().byteStream();
        }
        throw new IOException(this.message = response.message());
    }
    private volatile boolean successful=false;
    private volatile String message=null;

    public boolean isSuccessful()
    {
        return successful;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public URL buildQuotesUrl(String ticker, LocalDate start, LocalDate end) throws MalformedURLException
    {
        StringBuilder url = new StringBuilder("http://www.google.com/finance/historical?output=csv&q=").append(ticker);
        
        if(start!=null)
        {
            url.append("&startdate=").append(start.format(JavaTime.D_MMM_YY));
        }
        if(end!=null)
        {
            url.append("&enddate=").append(end.format(JavaTime.D_MMM_YY));
        }
        return new URL(url.toString());
    }

    @Override
    public int importDividends(InputStream in, StockQuotes sq)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InputStream getDividends(URL url) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public URL buildDividendsUrl(String ticker, LocalDate start, LocalDate end) throws MalformedURLException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
