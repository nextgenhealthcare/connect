/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import java.math.BigDecimal;
import java.sql.Types;

import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.util.DateParser;

public enum MetaDataColumnType {
    STRING, NUMBER, BOOLEAN, TIMESTAMP;

    private static final BigDecimal MAX_NUMBER_VALUE = new BigDecimal(10000000000000000L);

    public static MetaDataColumnType fromString(String columnType) {
        // @formatter:off
        if (columnType.equals("STRING")) return STRING;
        if (columnType.equals("NUMBER")) return NUMBER;
        if (columnType.equals("BOOLEAN")) return BOOLEAN;
        if (columnType.equals("TIMESTAMP")) return TIMESTAMP;
        // @formatter:on

        return null;
    }

    public static MetaDataColumnType fromSqlType(int sqlType) {
        switch (sqlType) {
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
                return STRING;

            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
            case Types.REAL:
                return NUMBER;

            case Types.BOOLEAN:
            case Types.BIT:
            case Types.CHAR:
                return BOOLEAN;

            case Types.TIMESTAMP:
            case Types.TIME:
            case Types.DATE:
                return TIMESTAMP;
        }

        return null;
    }

    /**
     * Returns an object for a metadata value that is casted to the correct type
     * 
     * @throws MetaDataColumnException
     *             If an error occurred while attempting to cast the value
     */
    public Object castValue(Object value) throws MetaDataColumnException {
        if (value == null) {
            return null;
        }

        try {
            switch (this) {
                case BOOLEAN:
                    return (Boolean) new BooleanConverter().convert(Boolean.class, value);
                case NUMBER:
                    BigDecimal number = (BigDecimal) new BigDecimalConverter().convert(BigDecimal.class, value);
                    if (number.compareTo(MAX_NUMBER_VALUE) >= 0) {
                        throw new Exception("Number " + String.valueOf(number) + " is greater than or equal to the maximum allowed value of 10^16.");
                    }
                    return number;
                case STRING:
                    String string = (String) new StringConverter().convert(String.class, value);
                    if (string.length() > 255) {
                        string = StringUtils.substring(string, 0, 255);
                    }
                    return string;
                case TIMESTAMP:
                    return new DateParser().parse(value.toString());
            }
        } catch (Exception e) {
            throw new MetaDataColumnException(e);
        }

        throw new MetaDataColumnException("Unrecognized MetaDataColumnType");
    }
}
