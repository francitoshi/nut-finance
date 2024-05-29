package io.nut.finance.xirr;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static io.nut.finance.xirr.NewtonRaphson.TOLERANCE;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 */
public class XirrTest {

    @Test
    public void xirr_1_year_no_growth() {
        // computes the xirr on 1 year growth of 0%
        final double xirr = new Xirr(Arrays.asList(
                new Transaction(-1000, "2010-01-01"),
                new Transaction( 1000, "2011-01-01")
            )).xirr();
        assertEquals(0, xirr, TOLERANCE);
    }

    @Test
    public void xirr_1_year_growth() {
        // computes the xirr on 1 year growth of 10%
        final double xirr = new Xirr(Arrays.asList(
                new Transaction(-1000, "2010-01-01"),
                new Transaction( 1100, "2011-01-01")
            )).xirr();
        assertEquals(0.10, xirr, TOLERANCE);
    }

    @Test
    public void xirr_1_year_decline() {
        // computes the negative xirr on 1 year decline of 10%
        final double xirr = new Xirr(
                new Transaction(-1000, "2010-01-01"),
                new Transaction(  900, "2011-01-01")
            ).xirr();
        assertEquals(-0.10, xirr, TOLERANCE);
    }

    @Test
    public void xirr_vs_spreadsheet() {
        // computes the xirr on a particular data set the same as a popular
        // spreadsheet
        final double xirr = new Xirr(
                new Transaction(-1000, "2010-01-01"),
                new Transaction(-1000, "2010-04-01"),
                new Transaction(-1000, "2010-07-01"),
                new Transaction(-1000, "2010-10-01"),
                new Transaction( 4300, "2011-01-01")
            ).xirr();
        assertEquals(0.1212676, xirr, TOLERANCE);
    }

    @Test
    public void xirr_vs_spreadsheet_reordered() {
        // gets the same answer even if the transations are out of order
        final double xirr = new Xirr(
                new Transaction(-1000, "2010-10-01"),
                new Transaction( 4300, "2011-01-01"),
                new Transaction(-1000, "2010-07-01"),
                new Transaction(-1000, "2010-01-01"),
                new Transaction(-1000, "2010-04-01")
            ).xirr();
        assertEquals(0.1212676, xirr, TOLERANCE);
    }

    @Test
    public void xirr_over_100_percent_growth() {
        // computes rates of return greater than 100%
        final double xirr = new Xirr(Arrays.asList(
                new Transaction(-1000, "2010-01-01"),
                new Transaction( 3000, "2011-01-01")
            )).xirr();
        assertEquals(2.00, xirr, TOLERANCE);
    }

    @Test
    public void xirr_total_loss() {
        // computes a rate of return of -100% on a total loss
        final double xirr = new Xirr(Arrays.asList(
                new Transaction(-1000, "2010-01-01"),
                new Transaction(    0, "2011-01-01")
            )).xirr();
        assertEquals(-1.00, xirr, TOLERANCE);
    }
    
    @Test
    public void xirr_readme_example() {
        double rate = new Xirr(
                new Transaction(-1000, "2016-01-15"),
                new Transaction(-2500, "2016-02-08"),
                new Transaction(-1000, "2016-04-17"),
                new Transaction( 5050, "2016-08-24")
            ).xirr();
        assertEquals(0.2504234710540838, rate, TOLERANCE);
    }

    @Test
    public void xirr_issue_from_node_js_version() {
        double rate = new Xirr(
                new Transaction(-10000, "2000-05-24"),
                new Transaction(3027.25, "2000-06-05"),
                new Transaction(630.68, "2001-04-09"),
                new Transaction(2018.2, "2004-02-24"),
                new Transaction(1513.62, "2005-03-18"),
                new Transaction(1765.89, "2006-02-15"),
                new Transaction(4036.33, "2007-01-10"),
                new Transaction(4036.33, "2007-11-14"),
                new Transaction(1513.62, "2008-12-17"),
                new Transaction(1513.62, "2010-01-15"),
                new Transaction(2018.16, "2011-01-14"),
                new Transaction(1513.62, "2012-02-03"),
                new Transaction(1009.08, "2013-01-18"),
                new Transaction(1513.62, "2014-01-24"),
                new Transaction(1513.62, "2015-01-30"),
                new Transaction(1765.89, "2016-01-22"),
                new Transaction(1765.89, "2017-01-20"),
                new Transaction(22421.55, "2017-06-05")
            ).xirr();
        assertEquals(0.2126861, rate, TOLERANCE);
    }

    @Test
    public void xirr_no_transactions() 
    {
        try
        {
            // throws exception when no transactions are passed
            new Xirr(Collections.emptyList()).xirr();
            fail("Expected exception for empty transaction list");
        }
        catch(IllegalArgumentException ex)
        {
        }
    }

    @Test
    public void xirr_one_transaction() 
    {
        try
        {
            // throws exception when only one transaction is passed
            new Xirr(new Transaction(-1000, "2010-01-01")).xirr();
            fail("Expected exception for only one transaction");
        }
        catch(IllegalArgumentException ex)
        {
        }
    }

    @Test
    public void xirr_same_day() throws Exception 
    {
        try
    {
            // throws an exception when all transactions are on the same day
            final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            new Xirr(
                    new Transaction(-1000, format.parse("2010-01-01 09:00")),
                    new Transaction(-1000, format.parse("2010-01-01 12:00")),
                    new Transaction( 2100, format.parse("2010-01-01 15:00"))
                ).xirr();
            fail("Expected exception for all transactions on the same day");
        }
        catch(IllegalArgumentException ex)
        {
        }
    }

    @Test
    public void xirr_all_negative() throws Exception 
    {
        try
    {
            // throws an exception when all transactions are negative
            final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            new Xirr(
                    new Transaction(-1000, "2010-01-01"),
                    new Transaction(-1000, "2010-05-01"),
                    new Transaction(-2000, "2010-09-01")
                ).xirr();
            fail("Expected exception for all transactions are negative");
        }
        catch(IllegalArgumentException ex)
        {
        }
    }

    @Test
    public void xirr_all_nonnegative() throws Exception 
    {
        try
    {
            // throws an exception when all transactions are nonnegative
            final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            new Xirr(
                    new Transaction(1000, "2010-01-01"),
                    new Transaction(1000, "2010-05-01"),
                    new Transaction(   0, "2010-09-01")
                ).xirr();
            fail("Expected exception for all transactions are nonnegative");
        }
        catch(IllegalArgumentException ex)
        {
        }
    }

}
