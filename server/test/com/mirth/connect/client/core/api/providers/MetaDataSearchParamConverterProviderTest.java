/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.ws.rs.ext.ParamConverter;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.client.core.api.providers.MetaDataSearchParamConverterProvider.MetaDataSearch;
import com.mirth.connect.model.filters.elements.MetaDataSearchOperator;

public class MetaDataSearchParamConverterProviderTest {

    private static ParamConverter<MetaDataSearch> converter;

    @BeforeClass
    public static void setup() {
        converter = new MetaDataSearchParamConverterProvider().getConverter(MetaDataSearch.class, null, null);
    }

    @Test
    public void testFromStringEquals() {
        MetaDataSearch search = converter.fromString("COLUMN = VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.EQUAL, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringEqualsWithValueSpaces() {
        MetaDataSearch search = converter.fromString("COLUMN = VALUE WITH SPACES");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.EQUAL, search.getOperator());
        assertEquals("VALUE WITH SPACES", search.getValue());
    }

    @Test
    public void testFromStringEqualsWithExtraSpaces() {
        MetaDataSearch search = converter.fromString("     COLUMN     =     VALUE     WITH SPACES     ");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.EQUAL, search.getOperator());
        assertEquals("    VALUE     WITH SPACES     ", search.getValue());
    }

    @Test
    public void testFromStringInvalidColumn() {
        MetaDataSearch search = converter.fromString("INVALID COLUMN = VALUE WITH SPACES");
        assertNull(search);
    }

    @Test
    public void testFromStringEqualsWithOperatorInValue() {
        MetaDataSearch search = converter.fromString("COLUMN = VALUE = SPACES");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.EQUAL, search.getOperator());
        assertEquals("VALUE = SPACES", search.getValue());
    }

    @Test
    public void testFromStringEqualsWithOtherOperatorInValue() {
        MetaDataSearch search = converter.fromString("COLUMN = VALUE CONTAINS SPACES");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.EQUAL, search.getOperator());
        assertEquals("VALUE CONTAINS SPACES", search.getValue());
    }

    @Test
    public void testFromStringEqualsWithNull() {
        MetaDataSearch search = converter.fromString(null);
        assertNull(search);
    }

    @Test
    public void testFromStringEqualsInvalidString() {
        MetaDataSearch search = converter.fromString("COLUMN=VALUE");
        assertNull(search);
    }

    @Test
    public void testFromStringEqualsInvalidOperator() {
        MetaDataSearch search = converter.fromString("COLUMN EQUALS VALUE");
        assertNull(search);
    }

    @Test
    public void testFromStringEqualsInvalidColumnName() {
        MetaDataSearch search = converter.fromString("\t = VALUE");
        assertNull(search);
    }

    @Test
    public void testFromStringEqualsInvalidValue() {
        MetaDataSearch search = converter.fromString("COLUMN =");
        assertNull(search);
    }

    @Test
    public void testFromStringNotEquals() {
        MetaDataSearch search = converter.fromString("COLUMN != VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.NOT_EQUAL, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringLessThan() {
        MetaDataSearch search = converter.fromString("COLUMN < VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.LESS_THAN, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringLessThanOrEqual() {
        MetaDataSearch search = converter.fromString("COLUMN <= VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.LESS_THAN_OR_EQUAL, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringGreaterThan() {
        MetaDataSearch search = converter.fromString("COLUMN > VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.GREATER_THAN, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringGreaterThanOrEqual() {
        MetaDataSearch search = converter.fromString("COLUMN >= VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.GREATER_THAN_OR_EQUAL, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringContains() {
        MetaDataSearch search = converter.fromString("COLUMN CONTAINS VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.CONTAINS, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringContainsLowerCase() {
        MetaDataSearch search = converter.fromString("COLUMN contains VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.CONTAINS, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringDoesNotContain() {
        MetaDataSearch search = converter.fromString("COLUMN DOES NOT CONTAIN VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.DOES_NOT_CONTAIN, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringStartsWith() {
        MetaDataSearch search = converter.fromString("COLUMN STARTS WITH VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.STARTS_WITH, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringDoesNotStartWith() {
        MetaDataSearch search = converter.fromString("COLUMN DOES NOT START WITH VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.DOES_NOT_START_WITH, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringEndsWith() {
        MetaDataSearch search = converter.fromString("COLUMN ENDS WITH VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.ENDS_WITH, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testFromStringDoesNotEndWith() {
        MetaDataSearch search = converter.fromString("COLUMN DOES NOT END WITH VALUE");
        assertNotNull(search);
        assertEquals("COLUMN", search.getColumnName());
        assertEquals(MetaDataSearchOperator.DOES_NOT_END_WITH, search.getOperator());
        assertEquals("VALUE", search.getValue());
    }

    @Test
    public void testToStringEquals() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.EQUAL, "VALUE");
        assertEquals("COLUMN = VALUE", converter.toString(search));
    }

    @Test
    public void testToStringEqualsWithValueSpaces() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.EQUAL, "VALUE WITH SPACES");
        assertEquals("COLUMN = VALUE WITH SPACES", converter.toString(search));
    }

    @Test
    public void testToStringEqualsWithExtraSpaces() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.EQUAL, "    VALUE    WITH     SPACES    ");
        assertEquals("COLUMN =     VALUE    WITH     SPACES    ", converter.toString(search));
    }

    @Test
    public void testToStringEqualsWithNull() {
        assertNull(converter.toString(null));
    }

    @Test
    public void testToStringNotEqual() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.NOT_EQUAL, "VALUE");
        assertEquals("COLUMN != VALUE", converter.toString(search));
    }

    @Test
    public void testToStringLessThan() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.LESS_THAN, "VALUE");
        assertEquals("COLUMN < VALUE", converter.toString(search));
    }

    @Test
    public void testToStringLessThanOrEqual() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.LESS_THAN_OR_EQUAL, "VALUE");
        assertEquals("COLUMN <= VALUE", converter.toString(search));
    }

    @Test
    public void testToStringGreaterThan() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.GREATER_THAN, "VALUE");
        assertEquals("COLUMN > VALUE", converter.toString(search));
    }

    @Test
    public void testToStringGreaterThanOrEqual() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.GREATER_THAN_OR_EQUAL, "VALUE");
        assertEquals("COLUMN >= VALUE", converter.toString(search));
    }

    @Test
    public void testToStringContains() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.CONTAINS, "VALUE");
        assertEquals("COLUMN CONTAINS VALUE", converter.toString(search));
    }

    @Test
    public void testToStringDoesNotContain() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.DOES_NOT_CONTAIN, "VALUE");
        assertEquals("COLUMN DOES NOT CONTAIN VALUE", converter.toString(search));
    }

    @Test
    public void testToStringStartsWith() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.STARTS_WITH, "VALUE");
        assertEquals("COLUMN STARTS WITH VALUE", converter.toString(search));
    }

    @Test
    public void testToStringDoesNotStartWith() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.DOES_NOT_START_WITH, "VALUE");
        assertEquals("COLUMN DOES NOT START WITH VALUE", converter.toString(search));
    }

    @Test
    public void testToStringEndsWith() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.ENDS_WITH, "VALUE");
        assertEquals("COLUMN ENDS WITH VALUE", converter.toString(search));
    }

    @Test
    public void testToStringDoesNotEndWith() {
        MetaDataSearch search = new MetaDataSearch("COLUMN", MetaDataSearchOperator.DOES_NOT_END_WITH, "VALUE");
        assertEquals("COLUMN DOES NOT END WITH VALUE", converter.toString(search));
    }
}