/*
 *  CsvQuotes.java
 *
 *  Copyright (c) 2023-2024 francitoshi@gmail.com
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
package io.nut.finance.quotes;

import io.nut.base.math.Nums;
import io.nut.base.time.JavaTime;
import io.nut.base.util.Parsers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author franci
 */
public class CsvQuotes
{
    public enum TimeType { EpochSecond, Date, DateTime};
    
    public static final String TIME_NAME = "Date";
    public static final String OPEN_NAME = "Open";
    public static final String HIGH_NAME = "High";
    public static final String LOW_NAME = "Low";
    public static final String CLOSE_NAME = "Close";
    
    private final HashMap<String,Quote> fixed = new HashMap<>();
    private final HashMap<String,HashMap<Long,Quote>> quotes = new HashMap<>();

    private final String fiatCoin;
    private final TimeType timeType;
    private final String time_name;
    private final String open_name;
    private final String high_name;
    private final String low_name;
    private final String close_name;

    public CsvQuotes(String fiatCoin, TimeType timeType, String time_name, String open_name, String high_name, String low_name, String close_name)
    {
        this.fiatCoin = fiatCoin;
        this.timeType = timeType;
        this.time_name = time_name;
        this.open_name = open_name;
        this.high_name = high_name;
        this.low_name = low_name;
        this.close_name = close_name;
    }

    public CsvQuotes(String fiatCoin, TimeType timeType)
    {
        this(fiatCoin, timeType, TIME_NAME, OPEN_NAME, HIGH_NAME, LOW_NAME, CLOSE_NAME);
    }
    public CsvQuotes(String fiatCoin)
    {
        this(fiatCoin, TimeType.Date, TIME_NAME, OPEN_NAME, HIGH_NAME, LOW_NAME, CLOSE_NAME);
    }

    private CSVParser getParser(InputStream data) throws IOException
    {
        return getFormat().parse(new InputStreamReader(data));
    }
    
    public final int load(String coin, File data) throws IOException, FileNotFoundException, ParseException
    {
        return load(coin, new FileInputStream(data), null);
    }
    public final int load(String coin, File data, String counterCoin) throws IOException, FileNotFoundException, ParseException
    {
        return load(coin, new FileInputStream(data), counterCoin);
    }
    protected CSVFormat getFormat() throws IOException
    {
        return CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setIgnoreSurroundingSpaces(true)
                .setIgnoreEmptyLines(true)
                .setCommentMarker('#')
                .build();
    }

    public boolean alias(String alt, String coin)
    {
        HashMap<Long, Quote> srcQuotes = this.quotes.get(coin);
        if(srcQuotes!=null)
        {
            this.quotes.put(alt, srcQuotes);
            return true;
        }
        return false;
    }
    public int load(String coin, InputStream data) throws ParseException, IOException
    {
        return load(coin, data, null);
    }
    //adds quotes using data with a different counter coin
    public int load(String coin, InputStream data, String counterCoin) throws ParseException, IOException
    {
        final CSVParser parser = getParser(data);    
        int count = 0;

        HashMap<Long, Quote> coinQuotes = this.quotes.get(coin);
        if(coinQuotes==null)
        {
            this.quotes.put(coin, coinQuotes = new HashMap<>());
        }

        for(CSVRecord values : parser)
        {
            String rawdate = values.get(time_name);
            String rawopen = values.get(open_name);
            String rawhigh = values.get(high_name);
            String rawlow = values.get(low_name);
            String rawclose = values.get(close_name);
	
            long time;
            
            switch (timeType)
            {
                case Date:
                    time = JavaTime.epochSecond(JavaTime.parseLocalDate(rawdate, JavaTime.UTC));
                    System.out.printf("%s=%d\n",rawdate, time);
                    break;
                case DateTime:
                    time = JavaTime.epochSecond(JavaTime.parseLocalDateTime(rawdate, JavaTime.UTC));
                    break;
                default:
                    time = Long.parseLong(rawdate);
                    break;
            }
            
            BigDecimal open = Parsers.safeParseBigDecimal(rawopen,null);
            BigDecimal high = Parsers.safeParseBigDecimal(rawhigh,null);
            BigDecimal low = Parsers.safeParseBigDecimal(rawlow,null);
            BigDecimal close = Parsers.safeParseBigDecimal(rawclose,null);
            if(open==null)
            {
                continue;
            }
            //update values converting prices from counterCoin to the fiat currency 			        
            if(counterCoin!=null)
            {
                Quote quote2 = getQuote(counterCoin, time);
                if(quote2==null)
                {
                    LocalDateTime date = LocalDateTime.from(Instant.ofEpochSecond(time));
                    throw new InvalidParameterException("no data for "+counterCoin+" at "+date);
                }
                open = open.multiply(quote2.open);
                high = open.multiply(quote2.high);
                low = open.multiply(quote2.low);    
                close = open.multiply(quote2.close);
            }
            
            coinQuotes.put(time, new Quote(time, open, high, low, close));
            count++;
        }
        return count;
    }
    public Quote getQuote(String coin, LocalDate date)
    {
        return getQuote(coin, JavaTime.epochSecond(date));
    }
    public Quote getQuote(String coin, long time)
    {
        Quote quote = this.fixed.getOrDefault(coin, null);
        if(quote==null)
        {
            HashMap<Long, Quote> coinQuotes = this.quotes.get(coin);
            quote = coinQuotes!=null ? coinQuotes.get(time) : null;
        }
        return quote;
    }
    public Quote getQuote(String base, String counter, LocalDate date, int decimals)
    {
        Quote quoteBase = getQuote(base, date);
        if(counter.equalsIgnoreCase(this.fiatCoin))
        {
            return quoteBase;
        }
        Quote quoteCounter = getQuote(counter, date);
        return quoteBase.relativeQuote(quoteCounter, decimals);
    }
    
    public void fixedQuote(String coin, BigDecimal quote) throws ParseException, IOException
    {
        this.fixed.put(coin, new Quote(0, quote, quote, quote, quote));
}

    public Quote getPeriodQuote(String currency, LocalDate startAt, LocalDate stopAt)
    {
        long startEpocSecond = JavaTime.epochSecond(startAt);
        Quote quote = getQuote(currency, startEpocSecond);
        
        BigDecimal open = quote.open;
        BigDecimal high = null;
        BigDecimal low  = null;
        BigDecimal close= null;
        
        for(LocalDate day = startAt;day.compareTo(stopAt)<=0;day=day.plusDays(1))
        {
            quote = getQuote(currency, day);
            high = high!=null ? quote.high.max(high) : quote.high;
            low = low!=null ? quote.low.min(low) : quote.low;
            close = quote.close;
        }
        return new Quote(startEpocSecond, open, high, low, close);
    }
    
    public Quote getYearQuote(String currency, int year)
    {
        LocalDate startAt = JavaTime.atStartOfYear(LocalDate.ofYearDay(year, 1));
        LocalDate endAt = JavaTime.atEndOfYear(startAt);
        
        return getPeriodQuote(currency, startAt, endAt);
    }
    
    public Quote[] getQuotes(String currency, BigDecimal price, int year)
    {
        LocalDate start = JavaTime.atStartOfYear(LocalDate.ofYearDay(year, 1));
        LocalDate end = JavaTime.atEndOfYear(start);
        
        ArrayList<Quote> list = new ArrayList<>();
        
        for(LocalDate day = start;day.compareTo(end)<=0;day=day.plusDays(1))
        {
            Quote quote = getQuote(currency, day);
            if(quote.low.compareTo(price)<=0 && quote.high.compareTo(price)>=0)
            {
                list.add(quote);
            }
        }
        return list.toArray(new Quote[0]);
    }
    public Quote[] getQuotes(String currency, long startAt, long stopAt)
    {
        ArrayList<Quote> list = new ArrayList<>();
        HashMap<Long, Quote> currencyQuotes = this.quotes.get(currency);
        if(currencyQuotes==null)
        {
            return null;
        }
        for(Quote quote : currencyQuotes.values())
        {
            if(startAt<=quote.time && stopAt>=quote.time)
            {
                list.add(quote);
            }
        }
        Collections.sort(list, (x,y) -> Long.compare(x.time,y.time));
        return list.toArray(new Quote[0]);
    }
    public Quote[] getQuotes(String currency, LocalDate startAt, LocalDate stopAt)
    {
        return getQuotes(currency, JavaTime.epochSecond(startAt), JavaTime.epochSecond(stopAt));
    }
    public Quote[] getQuotes(String currency, LocalDateTime startAt, LocalDateTime stopAt)
    {
        return getQuotes(currency, JavaTime.epochSecond(startAt), JavaTime.epochSecond(stopAt));
    }
    public Quote[] getQuotes(String currency, BigDecimal price)
    {
        ArrayList<Quote> list = new ArrayList<>();
        for(Quote quote : this.quotes.get(currency).values())
        {
            if(quote.low.compareTo(price)<=0 && quote.high.compareTo(price)>=0)
            {
                list.add(quote);
            }
        }
        return list.toArray(new Quote[0]);
    }
    public static Quote[] wideSort(Quote[] items, BigDecimal price)
    {
        Arrays.sort(items, (Quote qa, Quote qb) ->
        {
            BigDecimal loA = price.subtract(qa.low).abs();
            BigDecimal hiA = price.subtract(qa.high).abs();
            
            BigDecimal loB = price.subtract(qb.low).abs();
            BigDecimal hiB = price.subtract(qb.high).abs();
        
            BigDecimal a = loA.min(hiA);
            BigDecimal b = loB.min(hiB);
            
            return -a.compareTo(b);
        });
        return items;
    }
    public static Quote[] sortByLowAvgDiff(Quote[] items, BigDecimal price)
    {
        Arrays.sort(items, (Quote qa, Quote qb) ->
        {
            BigDecimal qaDiff = price.subtract(qa.getLowAvg(8),Nums.MC16HD).abs();
            BigDecimal qbDiff = price.subtract(qb.getLowAvg(8),Nums.MC16HD).abs();            
            return qaDiff.compareTo(qbDiff);
        });
        return items;
    }
    public static Quote[] sortByLowAvg(Quote[] items, BigDecimal price)
    {
        Arrays.sort(items, (Quote qa, Quote qb) -> qa.getLowAvg(8).compareTo(qb.getLowAvg(8)));
        return items;
    }
    public static Quote[] sortByLow(Quote[] items)
    {
        Arrays.sort(items, (Quote qa, Quote qb) -> qa.low.compareTo(qb.low));
        return items;
    }
    public static Quote[] sortByHigh(Quote[] items)
    {
        Arrays.sort(items, (Quote qa, Quote qb) -> qa.high.compareTo(qb.high));
        return items;
    }
}
