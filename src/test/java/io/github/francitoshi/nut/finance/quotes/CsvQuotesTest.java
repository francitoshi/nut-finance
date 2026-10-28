/*
 * Copyright (C) 2024-2026 francitoshi@gmail.com
 * SPDX-License-Identifier: GPL-3.0-or-later
 * See LICENSE file in the project root for full license text.
 */
package io.github.francitoshi.nut.finance.quotes;

import java.io.InputStream;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author franci
 */
public class CsvQuotesTest
{
    /**
     * Test of load method, of class CsvQuotes.
     */
    @Test
    public void testLoad_String_File() throws Exception
    {
        LocalDate startAt = LocalDate.of(2024, 1, 1);
        LocalDate stopAt = LocalDate.of(2024, 12, 31);
        CsvQuotes.TimeType[] types = { CsvQuotes.TimeType.Date, CsvQuotes.TimeType.DateTime, CsvQuotes.TimeType.EpochSecond};      
        String[] files = {"btc-eur-date.csv", "btc-eur-datetime.csv", "btc-eur-epochsecond.csv"};        
        CsvQuotes[] quotes = new CsvQuotes[files.length];
        
        Quote[][] q = new Quote[quotes.length][];
        
        for(int i=0;i<files.length;i++)
        {
            String base = "BTC"+i;
            InputStream csv = getClass().getResourceAsStream(files[i]);
            quotes[i] = new CsvQuotes("EUR", types[i]);
            quotes[i].load(base, csv);
            q[i] = quotes[i].getQuotes(base, startAt, stopAt);
        }
        for(int i=0;i<q.length-1;i++)
        {
            assertEquals(q[i].length,q[i+1].length);
            
            for(int j=0;j<q[i].length;j++)
            {
                assertEquals(q[i][j],q[i+1][j]);
            }
        }        
    }

}
