/*
 *  CsvQuotesTest.java
 *
 *  Copyright (c) 2024 francitoshi@gmail.com
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
