/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import com.mirth.connect.donkey.util.DateParser;
import com.mirth.connect.donkey.util.DateParser.DateParserException;

public enum MetaDataColumnType {
    STRING, // string with max length = 255 characters
    LONG, // 64-bit int
    DOUBLE, BOOLEAN, DATE, TIME, // time of day with millisecond precision and no timezone information
    TIMESTAMP; // date and time of day with millisecond precision and no timezone information

    public static MetaDataColumnType fromString(String columnType) {
        // @formatter:off
        if (columnType.equals("STRING")) return STRING;
        if (columnType.equals("LONG")) return LONG;
        if (columnType.equals("DOUBLE")) return DOUBLE;
        if (columnType.equals("BOOLEAN")) return BOOLEAN;
        if (columnType.equals("DATE")) return DATE;
        if (columnType.equals("TIME")) return TIME;
        if (columnType.equals("TIMESTAMP")) return TIMESTAMP;
        // @formatter:on

        return null;
    }

    /**
     * Returns an object for a metadata value that is casted to the correct type
     * Returns null if the value is not valid for the type
     * 
     * @throws MetaDataColumnException
     */
    public Object castMetaDataFromString(String value) throws MetaDataColumnException {
        try {
            // @formatter:off
            switch (this) {
                case BOOLEAN: return Boolean.parseBoolean(value);
                case DOUBLE: return Double.parseDouble(value);
                case LONG: return Long.parseLong(value);
                case STRING: return value;
                case DATE: return new DateParser().parse(value);
                case TIME: return new DateParser().parse(value);
                case TIMESTAMP: return new DateParser().parse(value);
                default: throw new MetaDataColumnException("Unrecognized MetaDataColumnType");
            }
            // @formatter:on
        } catch (DateParserException e) {
            throw new MetaDataColumnException("Invalid value '" + value + "' given for meta data column type: " + this);
        } catch (NumberFormatException e) {
            throw new MetaDataColumnException("Invalid value '" + value + "' given for meta data column type: " + this);
        }
    }
}
