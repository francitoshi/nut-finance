/*
 * YahooQuotesParser.java
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

import io.nut.base.os.Debug;
import io.nut.base.util.CharSets;
import io.nut.base.util.Parsers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author franci
 */
public class YahooQuotesParser implements QuotesParser
{
    private static final String CODE_NOT_FOUND = "\"code\":\"not found\"";
    private static final String DATE_OPEN_HIGH_LOW_CLOSE_VOLUME = "Date,Open,High,Low,Close,Adj Close,Volume";
    private static final String DATE_DIVIDENDS = "Date,Dividends";

    static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private static boolean debug = Debug.isDebuggable();
    
    @Override
    public int importQuotes(InputStream in, StockQuotes sq) throws ParseException
    {
        int count=0;
        boolean firstLine = true;
        Scanner sc = new Scanner(in, CharSets.USASCII);

        try
        {
            while(sc.hasNext())
            {
                String quote = sc.nextLine().trim();
                if(firstLine)
                {
                    firstLine = false;
                    if(quote.isEmpty() || quote.startsWith(DATE_OPEN_HIGH_LOW_CLOSE_VOLUME))
                    {
                        continue;
                    }
                    if(quote.toLowerCase().contains(CODE_NOT_FOUND))
                    {
                        break;
                    }
                }
                String[] data = quote.split(",");

                LocalDate date = LocalDate.parse(data[0], yyyyMMdd);
                double open = Parsers.safeParseDouble(data[1]);
                double high = Parsers.safeParseDouble(data[2]);
                double low = Parsers.safeParseDouble(data[3]);
                double close = Parsers.safeParseDouble(data[4]);
                double closeAdj = Parsers.safeParseDouble(data[5]);
                double volume = Parsers.safeParseDouble(data[6]);

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
    public int importDividends(InputStream in, StockQuotes sq) throws ParseException
    {
        int count=0;
        boolean firstLine = true;
        Scanner sc = new Scanner(in, CharSets.USASCII);

        try
        {
            while(sc.hasNext())
            {
                String quote = sc.nextLine().trim();
                if(firstLine)
                {
                    firstLine = false;
                    if(quote.isEmpty() || quote.contains(DATE_DIVIDENDS))
                    {
                        continue;
                    }
                    if(quote.toLowerCase().contains(CODE_NOT_FOUND))
                    {
                        break;
                    }
                }

                String[] data = quote.split(",");
                //2017-09-14,0.37
                LocalDate date = LocalDate.parse(data[0], yyyyMMdd);
                double dividend = Parsers.safeParseDouble(data[1]);

                sq.addDividend(date, dividend);
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
        if(crumb==null || cookieStore==null || cookieStore.isEmpty())
        {
            getCrumb(ticket);
        }
        
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(cookieJar).build();
        Request request = new Request.Builder().url(url+"&crumb="+crumb+"").build();

        Response response = client.newCall(request).execute();
        return response.body().byteStream();
    }
    @Override
    public InputStream getDividends(URL url) throws IOException
    {
        if(crumb==null || cookieStore==null || cookieStore.isEmpty())
        {
            getCrumb(ticket);
        }
        
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(cookieJar).build();
        Request request = new Request.Builder().url(url+"&crumb="+crumb+"").build();

        Response response = client.newCall(request).execute();
        return response.body().byteStream();
    }
    
    private volatile String ticket = "KO";
    @Override
    public URL buildQuotesUrl(String ticker, LocalDate start, LocalDate end) throws MalformedURLException
    {
        this.ticket=ticker;
        return buildUrl(ticker, start, end, "&interval=1d&events=history");
    }
    @Override
    public URL buildDividendsUrl(String ticker, LocalDate start, LocalDate end) throws MalformedURLException
    {
        this.ticket=ticker;
        return buildUrl(ticker, start, end, "&interval=1d&events=div");
    }
    private static URL buildUrl(String ticker, LocalDate start, LocalDate end, String tail) throws MalformedURLException
    {
        //https://query1.finance.yahoo.com/v7/finance/download/KO?period1=1504908000&period2=1505599200&interval=1d&events=history&crumb=/f2lzNMwC9/
        //https://query1.finance.yahoo.com/v7/finance/download/KO?period1=1474138165&period2=1505674165&interval=1d&events=div&crumb=3nzTmdbFq6/
        
        StringBuilder url = new StringBuilder("https://query1.finance.yahoo.com/v7/finance/download/").append(ticker).append('?');
        if(start!=null)
        {
            url.append("&period1=").append(start.atStartOfDay().toInstant(ZoneOffset.UTC).getEpochSecond());
        }
        if(end!=null)
        {
            url.append("&period2=").append(end.atStartOfDay().toInstant(ZoneOffset.UTC).getEpochSecond());
        }
        url.append(tail);
        
        return new URL(url.toString());
    }
    
    private volatile String crumb = null;
    private volatile List<Cookie> cookieStore = new ArrayList<>();
    
    final CookieJar cookieJar = new CookieJar() 
    {
        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) 
        {
            cookieStore = cookies;
            saveCookies(url, cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) 
        {
            return cookieStore;
        }
    };

    public void saveCookies(HttpUrl url, List<Cookie> cookies)
    {
        if(cookies!=null)
        {
            if(debug) System.out.println("yahoo.url="+url);
            for(Cookie item : cookies)
            {
                if(debug) System.out.println("yahoo.cookies="+item);
            }
        }
    }
    public void saveCrumb(String crumb)
    {
        if(debug) System.out.println("yahoo.crumb="+crumb);
    }
    public void loadCookiesAndCrumb(List<Cookie> cookies, String crumb)
    {
        this.cookieStore = cookies;
        this.crumb = crumb;
    }
    public void clearAll()
    {
        this.cookieStore = null;
        this.crumb = null;
    }

    private static final Pattern crumbPattern = Pattern.compile("\"crumb\":\"([^\"{}]+)\"");
    private void getCrumb(String ticker) throws IOException
    {
        URL url = new URL("https://finance.yahoo.com/quote/"+ticker+"/history?p="+ticker);
        
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(cookieJar).build();
        Request request = new Request.Builder().url(url).build();
        
        Response response = client.newCall(request).execute();
        
        String body = response.body().string();

        HashMap<String,Integer> map = new HashMap<>();
        int maxCount=0;
        String maxKey=null;
        
        Matcher matcher = crumbPattern.matcher(body);
        while(matcher.find())
        {
            String cookie = matcher.group(1);
            Integer n = map.put(cookie, 1);
            if(n!=null)
            {
                map.put(cookie, ++n);
                if(n>maxCount)
                {
                    maxCount = n;
                    maxKey = cookie;
                }
            }
        }
        saveCrumb(this.crumb=maxKey);
    }
}

//https://query1.finance.yahoo.com/v7/finance/download/KO?period1=1473350771&period2=1504886771&interval=1d&events=history&crumb=/f2lzNMwC9/
//Date,Open,High,Low,Close,Adj Close,Volume
//2016-09-08,43.669998,43.730000,43.450001,43.630001,42.190968,10020400
