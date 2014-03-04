/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.SerializationProperties;

public class DelimitedSerializationProperties extends SerializationProperties {

    private transient Logger logger;

    private String columnDelimiter = ",";
    private String recordDelimiter = "\\n";
    private Integer[] columnWidths = null; // list of widths: width1,width2,...,widthN
    private String quoteChar = "\"";
    private boolean escapeWithDoubleQuote = true;
    private String quoteEscapeChar = "\\";
    private String[] columnNames = null; // list of column names: name1,name2,...,nameN
    private boolean numberedRows = false;
    private boolean ignoreCR = true;

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("columnDelimiter", new DataTypePropertyDescriptor(columnDelimiter, "Column Delimiter", "If column values are delimited, enter the column delimiter that separates columns.  For example, this is a comma in a CSV file.", PropertyEditorType.STRING));
        properties.put("recordDelimiter", new DataTypePropertyDescriptor(recordDelimiter, "Record Delimiter", "Enter the character that separates each record (a message may contain multiple records).  For example, this is a newline (\\n) in a CSV file.", PropertyEditorType.STRING));
        properties.put("columnWidths", new DataTypePropertyDescriptor(toCommaSeparatedString(columnWidths), "Column Widths", "If the column values are fixed width, enter a comma separated list of fixed column widths.  By default, column values are assumed to be delimited.", PropertyEditorType.STRING));
        properties.put("quoteChar", new DataTypePropertyDescriptor(quoteChar, "Quote Character", "Enter the quote character that is used to bracket delimit column values containing embedded special characters like column delimiters, record delimiters, quote characters and/or message delimiters.    For example, this is a double quote (\") in a CSV file.", PropertyEditorType.STRING));
        properties.put("escapeWithDoubleQuote", new DataTypePropertyDescriptor(escapeWithDoubleQuote, "Double Quote Escaping", "By default, two consecutive quote characters within a quoted value are treated as an embedded quote character.  Uncheck to enable escaped quote character processing (and specify the Escape Character).", PropertyEditorType.BOOLEAN));
        properties.put("quoteEscapeChar", new DataTypePropertyDescriptor(quoteEscapeChar, "Escape Character", "Enter the character used to escape embedded quote characters.  By default, this is a back slash.  This option has no effect unless Double Quote Escaping is unchecked.", PropertyEditorType.STRING));
        properties.put("columnNames", new DataTypePropertyDescriptor(toCommaSeparatedString(columnNames), "Column Names", "To override the default column names (column1, ..., columnN), enter a comma separated list of column names.", PropertyEditorType.STRING));
        properties.put("numberedRows", new DataTypePropertyDescriptor(numberedRows, "Numbered Rows", "Check to number each row in the XML representation of the message.", PropertyEditorType.BOOLEAN));
        properties.put("ignoreCR", new DataTypePropertyDescriptor(ignoreCR, "Ignore Carriage Returns", "Ignores carriage return (\\r) characters.  These are read over and skipped without processing them.", PropertyEditorType.BOOLEAN));

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

            if (StringUtils.isNotEmpty((String) properties.get("quoteChar"))) {
                quoteChar = (String) properties.get("quoteChar");
            }

            if (properties.get("escapeWithDoubleQuote") != null) {
                escapeWithDoubleQuote = (Boolean) properties.get("escapeWithDoubleQuote");
            }

            if (StringUtils.isNotEmpty((String) properties.get("quoteEscapeChar"))) {
                quoteEscapeChar = (String) properties.get("quoteEscapeChar");
            }

            if (properties.get("columnNames") != null) {
                String names = (String) properties.get("columnNames");
                if (names.isEmpty()) {
                    columnNames = null;
                } else {
                    // Split the comma delimited list of column names and store as String[]
                    String[] columnNames = names.split(",");

                    boolean error = false;

                    for (int i = 0; i < columnNames.length; i++) {
                        if (!validXMLElementName(columnNames[i])) {
                            error = true;
                            logError("Invalid column name: " + columnNames[i] + " (must be a combination of letters, digits, periods, dashes, underscores and colons that begins with a letter, underscore or colon)");
                        }
                    }

                    if (!error) {
                        this.columnNames = columnNames;
                    }
                }
            }

            if (properties.get("numberedRows") != null) {
                numberedRows = (Boolean) properties.get("numberedRows");
            }

            if (properties.get("ignoreCR") != null) {
                ignoreCR = (Boolean) properties.get("ignoreCR");
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

    private boolean validXMLElementName(String s) {

        // Reference: http://www.w3.org/TR/REC-xml/#sec-well-formed
        //
        // Simplified requirements for a valid XML element name:
        //  o First character must be a letter, underscore or colon
        //  o Remaining characters must be letter, digit, period, dash, underscore or colon
        //
        // Note: this is not 100% complete, as it does not include tests for the so called
        //  "CombiningChar" nor "Extender".

        // Must not be null or empty string 
        if (s == null || s.length() == 0) {
            return false;
        }

        // First character must be a letter, underscore or colon
        char ch = s.charAt(0);
        if (!Character.isLetter(ch) && ch != '_' && ch != ':') {
            return false;
        }

        // Remaining characters must be letter, digit, period, dash, underscore or colon
        for (int i = 1; i < s.length(); i++) {
            ch = s.charAt(i);
            if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '.' && ch != '-' && ch != '_' && ch != ':') {
                return false;
            }
        }
        return true;
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

    public String getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(String quoteChar) {
        this.quoteChar = quoteChar;
    }

    public boolean isEscapeWithDoubleQuote() {
        return escapeWithDoubleQuote;
    }

    public void setEscapeWithDoubleQuote(boolean escapeWithDoubleQuote) {
        this.escapeWithDoubleQuote = escapeWithDoubleQuote;
    }

    public String getQuoteEscapeChar() {
        return quoteEscapeChar;
    }

    public void setQuoteEscapeChar(String quoteEscapeChar) {
        this.quoteEscapeChar = quoteEscapeChar;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public boolean isNumberedRows() {
        return numberedRows;
    }

    public void setNumberedRows(boolean numberedRows) {
        this.numberedRows = numberedRows;
    }

    public boolean isIgnoreCR() {
        return ignoreCR;
    }

    public void setIgnoreCR(boolean ignoreCR) {
        this.ignoreCR = ignoreCR;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}
}
