/*
 *  GoogleQuotesParser.java
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

import io.nut.base.time.JavaTime;
import io.nut.base.util.CharSets;
import io.nut.base.util.Parsers;
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
