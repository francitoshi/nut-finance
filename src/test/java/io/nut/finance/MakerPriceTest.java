/*
 * MakerPriceTest.java
 *
 *  Copyright (C) 2019-2024 francitoshi@gmail.com
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

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author franci
 */
public class MakerPriceTest
{

    BigDecimal __(String s)
    {
        return new BigDecimal(s);
    }

    /**
     * Test of getBid method, of class MakerPrice.
     */
    @Test
    public void testGetBid()
    {
        final MakerPrice INSTANCE = new MakerPrice(new BigDecimal("0.01"), 2);

        assertEquals(__("5.01"), INSTANCE.getBid(__("5.01"),__("5.02")),"1");
        assertEquals(__("5.02"), INSTANCE.getBid(__("5.02"),__("5.03")),"2");
        //jump after offset is > spread
        assertEquals(__("5.08"), INSTANCE.getBid(__("5.03"),__("5.09")),"3");
        assertEquals(__("5.08"), INSTANCE.getBid(__("5.03"),__("5.09")),"4");
        assertEquals(__("5.08"), INSTANCE.getBid(__("5.03"),__("5.09")),"5");
        assertEquals(__("5.08"), INSTANCE.getBid(__("5.03"),__("5.09")),"6");
        assertEquals(__("5.08"), INSTANCE.getBid(__("5.03"),__("5.09")),"7");
        assertEquals(__("5.22"), INSTANCE.getBid(__("5.13"),__("5.23")),"8");    
    }

    /**
     * Test of getAsk method, of class MakerPrice.
     */
    @Test
    public void testGetAsk()
    {
        final MakerPrice INSTANCE = new MakerPrice(new BigDecimal("0.01"), 2);

        assertEquals(__("5.22"), INSTANCE.getAsk(__("5.03"),__("5.23")),"1");
        assertEquals(__("5.20"), INSTANCE.getAsk(__("5.03"),__("5.22")),"2");
        assertEquals(__("5.18"), INSTANCE.getAsk(__("5.03"),__("5.21")),"3");
        assertEquals(__("5.15"), INSTANCE.getAsk(__("5.03"),__("5.19")),"4");
        assertEquals(__("5.12"), INSTANCE.getAsk(__("5.03"),__("5.17")),"5");
        assertEquals(__("5.10"), INSTANCE.getAsk(__("5.03"),__("5.16")),"6");
        //jump after offset is > spread
        assertEquals(__("5.04"), INSTANCE.getAsk(__("5.03"),__("5.15")),"7");
    }

    
}
