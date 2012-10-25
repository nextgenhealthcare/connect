/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.beans;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class DelimitedProperties implements Serializable {

    private PropertyChangeSupport propertySupport;

    public DelimitedProperties() {
        propertySupport = new PropertyChangeSupport(this);
    }
    /**
     * Holds value of property columnDelimiter.
     */
    private String columnDelimiter = ",";

    /**
     * Getter for property columnDelimiter.
     * @return Value of property columnDelimiter.
     */
    public String getColumnDelimiter() {
        return this.columnDelimiter;
    }

    /**
     * Setter for property columnDelimiter.
     * @param columnDelimiter New value of property columnDelimiter.
     */
    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;
    }
    /**
     * Holds value of property recordDelimiter.
     */
    private String recordDelimiter = "\\n";

    /**
     * Getter for property recordDelimiter.
     * @return Value of property recordDelimiter.
     */
    public java.lang.String getRecordDelimiter() {
        return this.recordDelimiter;
    }

    /**
     * Setter for property recordDelimiter.
     * @param recordDelimiter New value of property recordDelimiter.
     */
    public void setRecordDelimiter(java.lang.String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }
    /**
     * Holds value of property columnWidths.
     */
    private String columnWidths = "";

    /**
     * Getter for property columnWidths.
     * @return Value of property columnWidths.
     */
    public String getColumnWidths() {
        return this.columnWidths;
    }

    /**
     * Setter for property columnWidths.
     * @param columnWidths New value of property columnWidths.
     */
    public void setColumnWidths(String columnWidths) {
        this.columnWidths = columnWidths;
    }
    /**
     * Holds value of property quoteChar.
     */
    private String quoteChar = "\"";

    /**
     * Getter for property quoteChar.
     * @return Value of property quoteChar.
     */
    public String getQuoteChar() {
        return this.quoteChar;
    }

    /**
     * Setter for property quoteChar.
     * @param quoteChar New value of property quoteChar.
     */
    public void setQuoteChar(String quoteChar) {
        this.quoteChar = quoteChar;
    }
    /**
     * Holds value of property escapeWithDoubleQuote.
     */
    private boolean escapeWithDoubleQuote = true;

    /**
     * Getter for property escapeWithDoubleQuote.
     * @return Value of property escapeWithDoubleQuote.
     */
    public boolean isEscapeWithDoubleQuote() {
        return this.escapeWithDoubleQuote;
    }

    /**
     * Setter for property escapeWithDoubleQuote.
     * @param escapeWithDoubleQuote New value of property escapeWithDoubleQuote.
     */
    public void setEscapeWithDoubleQuote(boolean escapeWithDoubleQuote) {
        this.escapeWithDoubleQuote = escapeWithDoubleQuote;
    }
    /**
     * Holds value of property quoteEscapeChar.
     */
    private String quoteEscapeChar = "\\";

    /**
     * Getter for property quoteEscapeChar.
     * @return Value of property quoteEscapeChar.
     */
    public String getQuoteEscapeChar() {
        return this.quoteEscapeChar;
    }

    /**
     * Setter for property quoteEscapeChar.
     * @param quoteEscapeChar New value of property quoteEscapeChar.
     */
    public void setQuoteEscapeChar(String quoteEscapeChar) {
        this.quoteEscapeChar = quoteEscapeChar;
    }
    /**
     * Holds value of property columnNames.
     */
    private String columnNames = "";

    /**
     * Getter for property columnNames.
     * @return Value of property columnNames.
     */
    public String getColumnNames() {
        return this.columnNames;
    }

    /**
     * Setter for property columnNames.
     * @param columnNames New value of property columnNames.
     */
    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }
    /**
     * Holds value of property batchSkipRecords.
     */
    private String batchSkipRecords = "0";

    /**
     * Getter for property batchSkipRecords.
     * @return Value of property batchSkipRecords.
     */
    public String getBatchSkipRecords() {
        return this.batchSkipRecords;
    }

    /**
     * Setter for property batchSkipRecords.
     * @param batchSkipRecords New value of property batchSkipRecords.
     */
    public void setBatchSkipRecords(String batchSkipRecords) {
        this.batchSkipRecords = batchSkipRecords;
    }
    /**
     * Holds value of property batchMessageDelimiter.
     */
    private String batchMessageDelimiter = "";

    /**
     * Getter for property batchMessageDelimiter.
     * @return Value of property batchMessageDelimiter.
     */
    public String getBatchMessageDelimiter() {
        return this.batchMessageDelimiter;
    }

    /**
     * Setter for property batchMessageDelimiter.
     * @param batchMessageDelimiter New value of property batchMessageDelimiter.
     */
    public void setBatchMessageDelimiter(String batchMessageDelimiter) {
        this.batchMessageDelimiter = batchMessageDelimiter;
    }
    /**
     * Holds value of property batchMessageDelimiterIncluded.
     */
    private boolean batchMessageDelimiterIncluded = false;

    /**
     * Getter for property batchMessageDelimiterIncluded.
     * @return Value of property batchMessageDelimiterIncluded.
     */
    public boolean isBatchMessageDelimiterIncluded() {
        return this.batchMessageDelimiterIncluded;
    }

    /**
     * Setter for property batchMessageDelimiterIncluded.
     * @param batchMessageDelimiterIncluded New value of property batchMessageDelimiterIncluded.
     */
    public void setBatchMessageDelimiterIncluded(boolean batchMessageDelimiterIncluded) {
        this.batchMessageDelimiterIncluded = batchMessageDelimiterIncluded;
    }
    /**
     * Holds value of property batchGroupingColumn.
     */
    private String batchGroupingColumn = "";

    /**
     * Getter for property batchGroupingColumn.
     * @return Value of property batchGroupingColumn.
     */
    public String getBatchGroupingColumn() {
        return this.batchGroupingColumn;
    }

    /**
     * Setter for property batchGroupingColumn.
     * @param batchGroupingColumn New value of property batchGroupingColumn.
     */
    public void setBatchGroupingColumn(String batchGroupingColumn) {
        this.batchGroupingColumn = batchGroupingColumn;
    }
    /**
     * Holds value of property batchScript.
     */
    private String batchScript = "";

    /**
     * Getter for property batchScript.
     * @return Value of property batchScript.
     */
    public String getBatchScript() {
        return this.batchScript;
    }

    /**
     * Setter for property batchScript.
     * @param batchScript New value of property batchScript.
     */
    public void setBatchScript(String batchScript) {
        this.batchScript = batchScript;
    }
    /**
     * Holds value of property ignoreCR.
     */
    private boolean ignoreCR = true;

    /**
     * Getter for property removeCR.
     * @return Value of property removeCR.
     */
    public boolean isIgnoreCR() {
        return this.ignoreCR;
    }

    /**
     * Setter for property removeCR.
     * @param removeCR New value of property removeCR.
     */
    public void setIgnoreCR(boolean ignoreCR) {
        this.ignoreCR = ignoreCR;
    }
    /**
     * Holds value of property batchSplitByRecord.
     */
    private boolean batchSplitByRecord = true;

    /**
     * Getter for property batchSplitByRecord.
     * @return Value of property batchSplitByRecord.
     */
    public boolean isBatchSplitByRecord() {
        return this.batchSplitByRecord;
    }

    /**
     * Setter for property batchSplitByRecord.
     * @param batchSplitByRecord New value of property batchSplitByRecord.
     */
    public void setBatchSplitByRecord(boolean batchSplitByRecord) {
        this.batchSplitByRecord = batchSplitByRecord;
    }
    /**
     * Holds value of property numberedRows.
     */
    private boolean numberedRows;

    /**
     * Getter for property numberedRows.
     * @return Value of property numberedRows.
     */
    public boolean isNumberedRows() {
        return this.numberedRows;
    }

    /**
     * Setter for property numberedRows.
     * @param numberedRows New value of property numberedRows.
     */
    public void setNumberedRows(boolean numberedRows) {
        this.numberedRows = numberedRows;
    }
}
