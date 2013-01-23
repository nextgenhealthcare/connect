package com.mirth.connect.plugins.datatypes.delimited;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.SerializationProperties;

public class DelimitedSerializationProperties implements SerializationProperties {
    
    private transient Logger logger = Logger.getLogger(this.getClass());
    
    private String columnDelimiter = ",";
    private String recordDelimiter = "\\n";
    private int[] columnWidths = null;      // list of widths: width1,width2,...,widthN
    private String quoteChar = "\"";
    private boolean escapeWithDoubleQuote = true;
    private String quoteEscapeChar = "\\";
    private String[] columnNames = null;    // list of column names: name1,name2,...,nameN
    private boolean numberedRows = false;
    private boolean ignoreCR = true;
    
    @Override
    public Map<String, DataTypePropertyDescriptor> getProperties() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();
        
        properties.put("columnDelimiter", new DataTypePropertyDescriptor(columnDelimiter, "Column Delimiter", "If column values are delimited, enter the column delimiter that separates columns.  For example, this is a comma in a CSV file.", PropertyEditorType.STRING));
        properties.put("recordDelimiter", new DataTypePropertyDescriptor(recordDelimiter, "Record Delimiter", "Enter the character that separates each record (a message may contain multiple records).  For example, this is a newline (\\n) in a CSV file.", PropertyEditorType.STRING));
        properties.put("columnWidths", new DataTypePropertyDescriptor(columnWidths, "Column Widths", "If the column values are fixed width, enter a comma separated list of fixed column widths.  By default, column values are assumed to be delimited.", PropertyEditorType.STRING));
        properties.put("quoteChar", new DataTypePropertyDescriptor(quoteChar, "Quote Character", "Enter the quote character that is used to bracket delimit column values containing embedded special characters like column delimiters, record delimiters, quote characters and/or message delimiters.    For example, this is a double quote (\") in a CSV file.", PropertyEditorType.STRING));
        properties.put("escapeWithDoubleQuote", new DataTypePropertyDescriptor(escapeWithDoubleQuote, "Double Quote Escaping", "By default, two consecutive quote characters within a quoted value are treated as an embedded quote character.  Uncheck to enable escaped quote character processing (and specify the Escape Character).", PropertyEditorType.BOOLEAN));
        properties.put("quoteEscapeChar", new DataTypePropertyDescriptor(quoteEscapeChar, "Escape Character", "Enter the character used to escape embedded quote characters.  By default, this is a back slash.  This option has no effect unless Double Quote Escaping is unchecked.", PropertyEditorType.STRING));
        properties.put("columnNames", new DataTypePropertyDescriptor(columnNames, "Column Names", "To override the default column names (column1, ..., columnN), enter a comma separated list of column names.", PropertyEditorType.STRING));
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

            if (StringUtils.isNotEmpty((String) properties.get("columnWidths"))) {
                // Split the comma delimited list of column widths and store as int[]
                String[] temp = ((String) properties.get("columnWidths")).split(",");
                columnWidths = new int[temp.length];
                for (int i=0; i < temp.length; i++) {
                    try {
                        columnWidths[i] = Integer.parseInt(temp[i]);
                        
                        if (columnWidths[i] <= 0) {
                            logger.error("Fixed column width must be positive integer: " + columnWidths[i]);
                        }
                    }
                    catch (NumberFormatException e) {
                        columnWidths[i] = 0;
                        logger.warn("Invalid number format in Column Widths: " + temp[i]);
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
            
            if (StringUtils.isNotEmpty((String) properties.get("columnNames"))) {
                // Split the comma delimited list of column names and store as String[]
                columnNames = ((String) properties.get("columnNames")).split(",");
                
                for (int i=0; i < columnNames.length; i++) {
                    if (!validXMLElementName(columnNames[i])) {
                        logger.error("Invalid column name: " + columnNames[i] + " (must be a combination of letters, digits, periods, dashes, underscores and colons that begins with a letter, underscore or colon)");
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
        for (int i=1; i < s.length(); i++) {
            ch = s.charAt(i);
            if (!Character.isLetter(ch) && 
                    !Character.isDigit(ch) &&
                    ch != '.' &&
                    ch != '-' &&
                    ch != '_' && 
                    ch != ':') {
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

    public int[] getColumnWidths() {
        return columnWidths;
    }

    public void setColumnWidths(int[] columnWidths) {
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

}
