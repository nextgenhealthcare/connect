/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.PropertyEditorType;

public class DelimitedDeserializationProperties extends DeserializationProperties {

    private transient Logger logger;

    private String columnDelimiter = ",";
    private String recordDelimiter = "\\n";
    private Integer[] columnWidths = null; // list of widths: width1,width2,...,widthN
    private String quoteToken = "\"";
    private boolean escapeWithDoubleQuote = true;
    private String quoteEscapeToken = "\\";

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("columnDelimiter", new DataTypePropertyDescriptor(columnDelimiter, "Column Delimiter", "If column values are delimited, enter the characters that separate columns.  For example, this is a comma in a CSV file.", PropertyEditorType.STRING));
        properties.put("recordDelimiter", new DataTypePropertyDescriptor(recordDelimiter, "Record Delimiter", "Enter the characters that separate each record (a message may contain multiple records).  For example, this is a newline (\\n) in a CSV file.", PropertyEditorType.STRING));
        properties.put("columnWidths", new DataTypePropertyDescriptor(toCommaSeparatedString(columnWidths), "Column Widths", "If the column values are fixed width, enter a comma separated list of fixed column widths.  By default, column values are assumed to be delimited.", PropertyEditorType.STRING));
        properties.put("quoteToken", new DataTypePropertyDescriptor(quoteToken, "Quote Token", "Enter the quote characters that are used to bracket delimit column values containing embedded special characters like column delimiters, record delimiters, quote characters and/or message delimiters.    For example, this is a double quote (\") in a CSV file.", PropertyEditorType.STRING));
        properties.put("escapeWithDoubleQuote", new DataTypePropertyDescriptor(escapeWithDoubleQuote, "Double Quote Escaping", "By default, two consecutive quote tokens within a quoted value are treated as an embedded quote token.  Uncheck to enable escaped quote token processing (and specify the Escape Tokens).", PropertyEditorType.BOOLEAN));
        properties.put("quoteEscapeToken", new DataTypePropertyDescriptor(quoteEscapeToken, "Escape Token", "Enter the characters used to escape embedded quote tokens.  By default, this is a back slash.  This option has no effect unless Double Quote Escaping is unchecked.", PropertyEditorType.STRING));

        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (StringUtils.isNotEmpty((String) properties.get("columnDelimiter"))) {
                columnDelimiter = (String) properties.get("columnDelimiter");
            }

            if (StringUtils.isNotEmpty((String) properties.get("recordDelimiter"))) {
                recordDelimiter = (String) properties.get("recordDelimiter");
            }

            if (properties.get("columnWidths") != null) {
                String widths = (String) properties.get("columnWidths");
                if (widths.isEmpty()) {
                    columnWidths = null;
                } else {
                    // Split the comma delimited list of column widths and store as int[]
                    String[] temp = widths.split(",");
                    Integer[] columnWidths = new Integer[temp.length];
                    boolean error = false;

                    for (int i = 0; i < temp.length; i++) {
                        try {
                            columnWidths[i] = Integer.parseInt(temp[i]);

                            if (columnWidths[i] <= 0) {
                                error = true;
                                logError("Fixed column width must be positive integer: " + columnWidths[i]);
                            }
                        } catch (NumberFormatException e) {
                            error = true;
                            logError("Invalid number format in Column Widths: " + temp[i]);
                        }
                    }

                    if (!error) {
                        this.columnWidths = columnWidths;
                    }
                }
            }

            if (StringUtils.isNotEmpty((String) properties.get("quoteToken"))) {
                quoteToken = (String) properties.get("quoteToken");
            }

            if (properties.get("escapeWithDoubleQuote") != null) {
                escapeWithDoubleQuote = (Boolean) properties.get("escapeWithDoubleQuote");
            }

            if (StringUtils.isNotEmpty((String) properties.get("quoteEscapeToken"))) {
                quoteEscapeToken = (String) properties.get("quoteEscapeToken");
            }
        }
    }

    private void logError(String error) {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass());
        }

        logger.error(error);
    }

    private String toCommaSeparatedString(Object[] objects) {
        StringBuilder builder = new StringBuilder();

        if (objects != null && objects.length > 0) {
            builder.append(objects[0]);

            for (int i = 1; i < objects.length; i++) {
                builder.append(",");
                builder.append(objects[i]);
            }
        }

        return builder.toString();
    }

    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;
    }

    public String getRecordDelimiter() {
        return recordDelimiter;
    }

    public void setRecordDelimiter(String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }

    public Integer[] getColumnWidths() {
        return columnWidths;
    }

    public void setColumnWidths(Integer[] columnWidths) {
        this.columnWidths = columnWidths;
    }

    public String getQuoteToken() {
        return quoteToken;
    }

    public void setQuoteToken(String quoteToken) {
        this.quoteToken = quoteToken;
    }

    public boolean isEscapeWithDoubleQuote() {
        return escapeWithDoubleQuote;
    }

    public void setEscapeWithDoubleQuote(boolean escapeWithDoubleQuote) {
        this.escapeWithDoubleQuote = escapeWithDoubleQuote;
    }

    public String getQuoteEscapeToken() {
        return quoteEscapeToken;
    }

    public void setQuoteEscapeToken(String quoteEscapeToken) {
        this.quoteEscapeToken = quoteEscapeToken;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {
        DonkeyElement childElement = element.getChildElement("quoteChar");
        if (childElement != null) {
            childElement.setNodeName("quoteToken");
        }

        childElement = element.getChildElement("quoteEscapeChar");
        if (childElement != null) {
            childElement.setNodeName("quoteEscapeToken");
        }
    }

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {}

    @Override
    public void migrate3_6_0(DonkeyElement element) {}
    
    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("columnWidths", columnWidths);
        purgedProperties.put("escapeWithDoubleQuote", escapeWithDoubleQuote);
        return purgedProperties;
    }
}
