/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DelimitedProperties {
    private Logger logger = Logger.getLogger(this.getClass());
    private String columnDelimiter = ",";
    private String recordDelimiter = "\n";
    private int[] columnWidths = null; // list of widths:
                                       // width1,width2,...,widthN
    private String quoteChar = "\"";
    private boolean escapeWithDoubleQuote = true;
    private String quoteEscapeChar = "\\";
    private String[] columnNames = null; // list of column names:
                                         // name1,name2,...,nameN
    private boolean numberedRows = false;
    private int batchSkipRecords = 0;
    private boolean batchSplitByRecord = true;
    private String batchMessageDelimiter = "";
    private boolean batchMessageDelimiterIncluded = false;
    private String batchGroupingColumn = "";
    private String batchScript = "";
    private boolean ignoreCR = true;
    private String batchScriptId = null;

    // Denormalized properties
    private int groupingColumnIndex;

    public static Map<String, String> getDefaultProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("columnDelimiter", ",");
        properties.put("recordDelimiter", "\n");
        properties.put("columnWidths", "");
        properties.put("quoteChar", "\"");
        properties.put("escapeWithDoubleQuote", "true");
        properties.put("quoteEscapeChar", "\\");
        properties.put("columnNames", "");
        properties.put("numberedRows", "false");
        properties.put("batchSkipRecords", "0");
        properties.put("batchSplitByRecord", "true");
        properties.put("batchMessageDelimiter", "");
        properties.put("batchMessageDelimiterIncluded", "false");
        properties.put("batchGroupingColumn", "");
        properties.put("batchScript", "");
        properties.put("ignoreCR", "true");
        return properties;
    }

    /**
     * Checks whether the specified property value is set.
     * 
     * @param s
     * @return true iff the specified property value is not null, and is set to
     *         a non-empty value.
     */
    public static boolean isSet(String s) {
        return (s != null && s.length() > 0);
    }

    public DelimitedProperties(Map<String, String> delimitedProperties) {
        Map<String, String> theProperties = delimitedProperties;

        if (theProperties == null) {
            theProperties = getDefaultProperties();
        }

        if (isSet((theProperties.get("columnDelimiter")))) {
            columnDelimiter = unescape(theProperties.get("columnDelimiter"));
        }

        if (isSet(theProperties.get("recordDelimiter"))) {
            recordDelimiter = unescape(theProperties.get("recordDelimiter"));
        }

        if (isSet(theProperties.get("columnWidths"))) {
            // Split the comma delimited list of column widths and store as
            // int[]
            String[] temp = (theProperties.get("columnWidths")).split(",");
            columnWidths = new int[temp.length];
            for (int i = 0; i < temp.length; i++) {
                try {
                    columnWidths[i] = Integer.parseInt(temp[i]);

                    if (columnWidths[i] <= 0) {
                        logger.error("Fixed column width must be positive integer: " + columnWidths[i]);
                    }
                } catch (NumberFormatException e) {
                    columnWidths[i] = 0;
                    logger.warn("Invalid number format in Column Widths: " + temp[i]);
                }
            }
        }

        if (isSet(theProperties.get("quoteChar"))) {
            quoteChar = unescape(theProperties.get("quoteChar"));
        }

        if (isSet(theProperties.get("escapeWithDoubleQuote"))) {
            escapeWithDoubleQuote = Boolean.valueOf(theProperties.get("escapeWithDoubleQuote"));
        }

        if (isSet(theProperties.get("quoteEscapeChar"))) {
            quoteEscapeChar = unescape(theProperties.get("quoteEscapeChar"));
        }

        if (isSet(theProperties.get("columnNames"))) {
            // Split the comma delimited list of column names and store as
            // String[]
            columnNames = (theProperties.get("columnNames")).split(",");

            for (int i = 0; i < columnNames.length; i++) {
                if (!validXMLElementName(columnNames[i])) {
                    logger.error("Invalid column name: " + columnNames[i] + " (must be a combination of letters, digits, periods, dashes, underscores and colons that begins with a letter, underscore or colon)");
                }
            }
        }

        if (isSet(theProperties.get("numberedRows"))) {
            numberedRows = Boolean.valueOf(theProperties.get("numberedRows"));
        }

        if (isSet(theProperties.get("batchSkipRecords"))) {
            // Store an int
            try {
                batchSkipRecords = Integer.parseInt(theProperties.get("batchSkipRecords"));
            } catch (NumberFormatException e) {
                logger.warn("Invalid number format in Number of Header Records: " + theProperties.get("batchSkipRecords"));
            }
        }

        int countSetBatchOptions = 0;
        if (isSet(theProperties.get("batchSplitByRecord"))) {
            batchSplitByRecord = Boolean.valueOf(theProperties.get("batchSplitByRecord"));
            if (batchSplitByRecord) {
                countSetBatchOptions++;
            }
        }

        if (isSet(theProperties.get("batchMessageDelimiter"))) {
            batchMessageDelimiter = unescape(theProperties.get("batchMessageDelimiter"));
            countSetBatchOptions++;
        }

        if (isSet(theProperties.get("batchMessageDelimiterIncluded"))) {
            batchMessageDelimiterIncluded = Boolean.valueOf(theProperties.get("batchMessageDelimiterIncluded"));
        }

        if (isSet(theProperties.get("batchGroupingColumn"))) {
            batchGroupingColumn = theProperties.get("batchGroupingColumn");
            countSetBatchOptions++;
        }

        if (isSet(theProperties.get("batchScript"))) {
            batchScript = theProperties.get("batchScript");
            countSetBatchOptions++;
        }

        if (isSet(theProperties.get("batchScriptId"))) {
            batchScriptId = theProperties.get("batchScriptId");
        }

        if (countSetBatchOptions > 1) {
            logger.warn("Multiple batch splitting options are set");
        }

        if (isSet(theProperties.get("ignoreCR"))) {
            ignoreCR = Boolean.valueOf(theProperties.get("ignoreCR"));
        }

        // Default
        groupingColumnIndex = -1;

        // If there is a batch grouping column name
        if (isSet(batchGroupingColumn)) {

            // If we can't resolve the grouping column name, it'll default to
            // the first column (index=0)
            groupingColumnIndex = 0;

            // If there are no user specified column names
            if (columnNames == null) {

                // Try to parse the index from the end of a default column name
                // e.g. "column24" => index = 23
                int index = batchGroupingColumn.length() - 1;
                int len = 0;
                while (index >= 0 && Character.isDigit(batchGroupingColumn.charAt(index))) {
                    index--;
                    len++;
                }
                if (len > 0) {
                    try {
                        groupingColumnIndex = Integer.valueOf(batchGroupingColumn.substring(batchGroupingColumn.length() - len, batchGroupingColumn.length())) - 1;
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid number format in Split Batch by Grouping Column (defaulting to first column): " + batchGroupingColumn.substring(batchGroupingColumn.length() - len, batchGroupingColumn.length()));
                    }
                } else {
                    logger.warn("Unknown batch grouping column (defaulting to first column): " + batchGroupingColumn);
                }
            } else {

                // Try to find the grouping column name in the user specified
                // column names
                int i;
                for (i = 0; i < columnNames.length; i++) {
                    if (columnNames[i].equals(batchGroupingColumn)) {
                        groupingColumnIndex = i;
                        break;
                    }
                }

                if (i == columnNames.length) {
                    logger.warn("Unknown batch grouping column (defaulting to first column): " + batchGroupingColumn);
                }
            }
        }
    }

    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;
    }

    public int[] getColumnWidths() {
        return columnWidths;
    }

    public void setColumnWidths(int[] columnWidths) {
        this.columnWidths = columnWidths;
    }

    public String getRecordDelimiter() {
        return recordDelimiter;
    }

    public void setRecordDelimiter(String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }

    public String getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(String quoteChar) {
        this.quoteChar = quoteChar;
    }

    public String getQuoteEscapeChar() {
        return quoteEscapeChar;
    }

    public void setQuoteEscapeChar(String quoteEscapeChar) {
        this.quoteEscapeChar = quoteEscapeChar;
    }

    public String getBatchMessageDelimiter() {
        return batchMessageDelimiter;
    }

    public void setBatchMessageDelimiter(String batchMessageDelimiter) {
        this.batchMessageDelimiter = batchMessageDelimiter;
    }

    public String getBatchGroupingColumn() {
        return batchGroupingColumn;
    }

    public void setBatchGroupingColumn(String batchGroupingColumn) {
        this.batchGroupingColumn = batchGroupingColumn;
    }

    public String getBatchScript() {
        return batchScript;
    }

    public void setBatchScript(String batchScript) {
        this.batchScript = batchScript;
    }

    public int getBatchSkipRecords() {
        return batchSkipRecords;
    }

    public void setBatchSkipRecords(int batchSkipRecords) {
        this.batchSkipRecords = batchSkipRecords;
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

    public int getGroupingColumnIndex() {
        return groupingColumnIndex;
    }

    public void setGroupingColumnIndex(int groupingColumnIndex) {
        this.groupingColumnIndex = groupingColumnIndex;
    }

    public boolean isIgnoreCR() {
        return ignoreCR;
    }

    public void setIgnoreCR(boolean ignoreCR) {
        this.ignoreCR = ignoreCR;
    }

    public boolean isEscapeWithDoubleQuote() {
        return escapeWithDoubleQuote;
    }

    public void setEscapeWithDoubleQuote(boolean escapeWithDoubleQuote) {
        this.escapeWithDoubleQuote = escapeWithDoubleQuote;
    }

    public boolean isBatchMessageDelimiterIncluded() {
        return batchMessageDelimiterIncluded;
    }

    public void setBatchMessageDelimiterIncluded(boolean batchMessageDelimiterIncluded) {
        this.batchMessageDelimiterIncluded = batchMessageDelimiterIncluded;
    }

    public boolean isBatchSplitByRecord() {
        return batchSplitByRecord;
    }

    public void setBatchSplitByRecord(boolean batchSplitByRecord) {
        this.batchSplitByRecord = batchSplitByRecord;
    }

    public String getBatchScriptId() {
        return batchScriptId;
    }

    public void setBatchScriptId(String batchScriptId) {
        this.batchScriptId = batchScriptId;
    }

    // Four ways to specify character values and string values
    // 1. Literal
    // 2. Quoted literal (turns off escape processing except for standard escape
    // sequences)
    // 3. Standard escape sequences (e.g. \n, \r, \t)
    // 4. Hex notation (e.g. 0xyy)
    private String unescape(String s) {

        // If not set, return the string
        if (!isSet(s)) {
            return s;
        }

        // If the value is bracket delimited in double quotes, remove the quotes
        // and treat the rest as a literal
        if (s.length() >= 2 && s.substring(0, 1).equals("\"") && s.substring(s.length() - 1, s.length()).equals("\"")) {
            return s.substring(1, s.length() - 1);
        }

        // Standard escape sequence substitutions for non-printable characters
        // (excludes printable characters: \ " ')
        s = s.replace("\\b", "\b");
        s = s.replace("\\t", "\t");
        s = s.replace("\\n", "\n");
        s = s.replace("\\f", "\f");
        s = s.replace("\\r", "\r");

        // Substitute hex sequences with single character (e.g. 0x0a -> \n)
        int n = 0;
        while ((n = s.indexOf("0x", n)) != -1 && s.length() >= n + 4) {
            char ch;
            try {
                ch = (char) Integer.parseInt(s.substring(n + 2, n + 4), 16);
            } catch (NumberFormatException e) {
                n += 2;
                continue;
            }
            if (n + 4 < s.length()) {
                s = s.substring(0, n) + ch + s.substring(n + 4);
            } else {
                s = s.substring(0, n) + ch;
                break;
            }

            n++;
        }

        return s;
    }

    private boolean validXMLElementName(String s) {

        // Reference: http://www.w3.org/TR/REC-xml/#sec-well-formed
        //
        // Simplified requirements for a valid XML element name:
        // o First character must be a letter, underscore or colon
        // o Remaining characters must be letter, digit, period, dash,
        // underscore or colon
        //
        // Note: this is not 100% complete, as it does not include tests for the
        // so called
        // "CombiningChar" nor "Extender".

        // Must not be null or empty string
        if (s == null || s.length() == 0) {
            return false;
        }

        // First character must be a letter, underscore or colon
        char ch = s.charAt(0);
        if (!Character.isLetter(ch) && ch != '_' && ch != ':') {
            return false;
        }

        // Remaining characters must be letter, digit, period, dash, underscore
        // or colon
        for (int i = 1; i < s.length(); i++) {
            ch = s.charAt(i);
            if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '.' && ch != '-' && ch != '_' && ch != ':') {
                return false;
            }
        }
        return true;
    }
}
