package de.sub.goobi.metadaten;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.sub.goobi.metadaten.MetaConvertibleDate.DateType;

public class MetaConvertibleDateTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    // A list of dates to possibly test while developing this feature.
    // TODO: Delete once all tests are implemented

    /*
        List<String> dates = new ArrayList<>();
    
        dates.add("1752-09-02");
        dates.add("1752-09-03");
        dates.add("1752-09-14");
    
        dates.add("1740-03-01");
        dates.add("1741-03-12");
        dates.add("1741-04-01");
        dates.add("1741-04-12");
    
        dates.add("1751-12-31");
        dates.add("1751-01-01");
        dates.add("1752-01-01");
        dates.add("1751-03-24");
        dates.add("1752-03-24");
        dates.add("1752-03-25");
    
        dates.add("1752-09-01");
        dates.add("1752-09-02");
        dates.add("1752-09-03");
        dates.add("1752-09-04");
        dates.add("1752-09-09");
        dates.add("1752-09-13");
        dates.add("1752-09-14");
    
        dates.add("1752-04-02");
    */

    // Here follow the actual tests. They are meant to fail until the code is implemented in working order.

    @Test
    // Dates have to be in YYYY-MM-DD format
    public void malformedDateTest() {
        MetaConvertibleDate malformedDate = new MetaConvertibleDate("15-10-1934", DateType.GREGORIAN);
        assertFalse(malformedDate.isValid());
    }

    @Test
    public void validDateTest() {
        MetaConvertibleDate malformedDate = new MetaConvertibleDate("1934-10-15", DateType.GREGORIAN);
        assertTrue(malformedDate.isValid());
    }

    @Test
    public void isBritishTest() {
        MetaConvertibleDate britishDate = new MetaConvertibleDate("1744-09-02", DateType.BRITISH);
        assertTrue(britishDate.isBritish());
    }

    @Test
    public void convertSameToSameTest() {
        MetaConvertibleDate britishDate = new MetaConvertibleDate("1752-09-02", DateType.BRITISH);
        MetaConvertibleDate stillBritishDate = britishDate.convert(DateType.BRITISH);
        assertTrue(stillBritishDate.equals(britishDate));
        assertTrue(stillBritishDate.isBritish());
    }

    @Test
    public void convertBritishToGregorianSameTest() {
        MetaConvertibleDate britishDate = new MetaConvertibleDate("1752-09-02", DateType.BRITISH);
        MetaConvertibleDate gregorianDate = britishDate.convert(DateType.GREGORIAN);
        assertTrue(gregorianDate.isGregorian());
        assertEquals("1752-09-02", gregorianDate.getDate());
    }

    // Test every possible useful conversion constellation:
    // B2G, G2B, J2G, G2J

    @Test
    public void convertBritishToGregorianTest() {
        MetaConvertibleDate britishDate = new MetaConvertibleDate("1752-09-03", DateType.BRITISH);
        MetaConvertibleDate gregorianDate = britishDate.convert(DateType.GREGORIAN);
        assertTrue(gregorianDate.isGregorian());
        assertEquals("1752-09-14", gregorianDate.getDate());
    }

    @Test
    public void convertGregorianToBritishTest() {
        MetaConvertibleDate gregorianDate = new MetaConvertibleDate("1750-09-14", DateType.GREGORIAN);
        MetaConvertibleDate britishDate = gregorianDate.convert(DateType.BRITISH);
        assertTrue(britishDate.isBritish());
        assertEquals("1750-09-03", britishDate.getDate());
    }

    @Test
    public void convertJulianToGregorianTest() {
        MetaConvertibleDate julianDate = new MetaConvertibleDate("1855-02-09", DateType.JULIAN);
        MetaConvertibleDate gregorianDate = julianDate.convert(DateType.GREGORIAN);
        assertTrue(gregorianDate.isGregorian());
        assertEquals("1855-02-21", gregorianDate.getDate());
    }

    @Test
    public void convertGregorianToJulianTest() {
        MetaConvertibleDate gregorianDate = new MetaConvertibleDate("2022-10-17", DateType.GREGORIAN);
        MetaConvertibleDate julianDate = gregorianDate.convert(DateType.JULIAN);
        assertTrue(julianDate.isJulian());
        assertEquals("2022-10-04", julianDate.getDate());
    }
}