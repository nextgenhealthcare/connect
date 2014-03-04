/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mirth.connect.util.StringUtil;

public class DelimitedReader extends SAXParser {
    private Logger logger = Logger.getLogger(this.getClass());

    private DelimitedSerializationProperties serializationProperties;

    // The most recent ungotten record, and it's raw text, if any
    private ArrayList<String> ungottenRecord;
    private String ungottenRawText;
    private String columnDelimiter = null;
    private String recordDelimiter = null;
    private String quoteChar = null;
    private String quoteEscapeChar = null;

    public DelimitedReader(DelimitedSerializationProperties serializationProperties) {
        this.serializationProperties = serializationProperties;

        updateColumnDelimiter();
        updateRecordDelimiter();
        updateQuoteChar();
        updateQuoteEscapeChar();

        // Initially, there is no ungotten (pushed back) record
        ungottenRecord = null;
        ungottenRawText = null;
    }

    public void parse(InputSource input) throws SAXException, IOException {

        // Parsing overview
        //
        // The incoming stream is a single message which is a collection of one
        // or more records.
        // Each record is a collection of columns. Columns can be either fixed
        // width, or delimited.
        // Records are delimited.
        //
        // Assumptions
        // 1. Fixed record layout
        // Each record is assumed to contain the same number and type of
        // columns.
        // 2. Special characters are single characters. This includes: column
        // delimiter,
        // record delimiter, quote character, quote escape character.
        //
        // The following user configurable options affect the behavior of the
        // parser:
        // o columnWidths The array of fixed column widths.
        // o columnDelimiter The character that delimits (separates) the
        // columns.
        // o recordDelimiter The character that delimits (separates) each
        // record.
        // o quoteChar The character that is used to quote a column value.
        // o escapeWithDoubleQuote Iff true, embedded quotes are escaped with
        // two consecutive quotes.
        // Otherwise, the quote escape character escapes embedded quote
        // characters.
        // o quoteEscapeChar The character used to escape a quote (or itself).
        // o ignoreCR Iff true, all incoming \r characters are ignored and not
        // processed.
        //
        // The following user configurable options affect the behavior of the
        // output:
        // o columnNames A list of column names (taken from either file header,
        // or supplied by user).
        BufferedReader in = new BufferedReader(input.getCharacterStream());

        // Start the document
        String documentHead = "delimited";
        ContentHandler contentHandler = getContentHandler();
        contentHandler.startDocument();

        // Output <delimited>
        contentHandler.startElement("", documentHead, "", null);

        // While the parser gets records from the message
        ArrayList<String> record;
        int recordNo = 1;
        while ((record = getRecord(in, null)) != null) {

            // Output <rowN>
            if (serializationProperties.isNumberedRows()) {
                contentHandler.startElement("", "row" + recordNo, "", null);
            } else {
                contentHandler.startElement("", "row", "", null);
            }

            // For each column
            for (int i = 0; i < record.size(); i++) {

                String columnName;
                if (serializationProperties.getColumnNames() != null && i < serializationProperties.getColumnNames().length) {
                    // User specified column name
                    columnName = serializationProperties.getColumnNames()[i];
                } else {
                    // Default column name
                    columnName = "column" + (i + 1);
                }
                // Output <columnN>
                contentHandler.startElement("", columnName, "", null);

                // Output column value
                contentHandler.characters(record.get(i).toCharArray(), 0, record.get(i).length());

                // Output </columnN>
                contentHandler.endElement("", columnName, "");
            }

            // Output </rowN>
            if (serializationProperties.isNumberedRows()) {
                contentHandler.endElement("", "row" + recordNo, "");
            } else {
                contentHandler.endElement("", "row", "");
            }

            recordNo++;
        }

        // Output </delimited>
        contentHandler.endElement("", documentHead, "");

        // End the document
        contentHandler.endDocument();
    }

    /**
     * Get the next record from the input stream, and consume the record delimiter, if any.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on it require
     *            in.mark()).
     * @param rawText
     *            Optional StringBuilder used to return a copy of the raw text read by this method.
     * @return The record represented as a collection of column values, or null if there is no next
     *         record.
     * @throws IOException
     */
    public ArrayList<String> getRecord(BufferedReader in, StringBuilder rawText) throws IOException {

        // If there is an ungotten (pushed back) record, consume it and return
        // it, rather
        // than reading from the stream.
        if (ungottenRecord != null) {
            ArrayList<String> tempRecord = ungottenRecord;
            ungottenRecord = null;
            rawText.append(ungottenRawText);
            ungottenRawText = null;
            return tempRecord;
        }

        int ch;
        ch = peekChar(in);
        if (ch == -1)
            return null;

        char recDelim = recordDelimiter.charAt(0);
        ArrayList<String> record = new ArrayList<String>();
        if (serializationProperties.getColumnWidths() != null) {

            for (int i = 0; i < serializationProperties.getColumnWidths().length; i++) {

                StringBuilder columnValue = new StringBuilder();
                for (int j = 0; j < serializationProperties.getColumnWidths()[i]; j++) {

                    ch = getChar(in, rawText);

                    if (ch == -1 || ((char) ch) == recDelim)
                        break;

                    columnValue.append((char) ch);
                }

                record.add(ltrim(columnValue.toString()));

                if (ch == -1 || ((char) ch) == recDelim)
                    break;
            }

            // Consume trailing characters up until end of input stream or
            // record delimiter
            while (ch != -1 && ((char) ch) != recDelim) {
                ch = getChar(in, rawText);
            }
        } else {
            char colDelim = ','; // default
            if (StringUtils.isNotEmpty(columnDelimiter)) {
                colDelim = columnDelimiter.charAt(0);
            }

            for (;;) {
                String columnValue = getColumnValue(in, rawText);

                record.add(columnValue);

                ch = peekChar(in);

                if (ch == -1 || ((char) ch) == recDelim) {
                    // consume record delimiter
                    ch = getChar(in, rawText);

                    break;
                } else if (((char) ch) == colDelim) {
                    // consume column delimiter
                    ch = getChar(in, rawText);
                }
            }
        }

        return record;
    }

    /**
     * Unget the given record. The next call to getRecord() will consume it, and return it, rather
     * than operating on the input stream.
     * 
     * @param record
     *            The record to unget.
     * @param rawText
     *            The raw text for the record to unget.
     */
    public void ungetRecord(ArrayList<String> record, String rawText) {
        ungottenRecord = record;
        ungottenRawText = rawText;
    }

    /**
     * Get the next column value from the input stream, and consume the column delimiter, if any.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on it require
     *            in.mark()).
     * @param rawText
     *            Optional StringBuilder used to return a copy of the raw text read by this method.
     * @return The column value, or null if there is no next column value.
     * @throws IOException
     */
    private String getColumnValue(BufferedReader in, StringBuilder rawText) throws IOException {

        int ch;
        ch = peekChar(in);
        if (ch == -1)
            return "";

        StringBuilder columnValue = new StringBuilder();

        char colDelim = ','; // default
        if (StringUtils.isNotEmpty(columnDelimiter)) {
            colDelim = columnDelimiter.charAt(0);
        }

        char recDelim = '\n'; // default
        if (StringUtils.isNotEmpty(recordDelimiter)) {
            recDelim = recordDelimiter.charAt(0);
        }

        char theQuoteChar = '"'; // default
        if (StringUtils.isNotEmpty(quoteChar)) {
            theQuoteChar = quoteChar.charAt(0);
        }

        char theQuoteEscapeChar = '\\'; // default
        if (StringUtils.isNotEmpty(quoteEscapeChar)) {
            theQuoteEscapeChar = quoteEscapeChar.charAt(0);
        }

        // If the column value isn't quoted
        boolean inQuote = false;
        if (((char) peekChar(in)) != theQuoteChar) {
            for (;;) {
                ch = getChar(in, rawText);

                // Break on end of input and end of column
                if (ch == -1)
                    break;

                // Unget and break on record delimiter or column delimiter
                if (((char) ch) == recDelim || ((char) ch) == colDelim) {
                    ungetChar(in, rawText);
                    break;
                }

                columnValue.append((char) ch);
            }
        }
        // Column value is quoted
        else {

            inQuote = true;

            // Consume the quote
            ch = getChar(in, rawText);

            for (;;) {
                ch = getChar(in, rawText);

                // Process escaped quotes
                if (inQuote) {

                    // If the quote escape method is double quoting
                    if (serializationProperties.isEscapeWithDoubleQuote()) {

                        // If the character is a quote
                        if (((char) ch) == theQuoteChar) {

                            // If the next character is a quote
                            if (((char) peekChar(in)) == theQuoteChar) {
                                // Get the escaped quote
                                ch = getChar(in, rawText);
                                columnValue.append((char) ch);
                                continue;
                            }
                        }
                    } else {

                        // If the character is an escape
                        if (((char) ch) == theQuoteEscapeChar) {

                            // If the next character is a quote
                            if (((char) peekChar(in)) == theQuoteChar) {
                                // Get the escaped quote
                                ch = getChar(in, rawText);
                                columnValue.append((char) ch);
                                continue;
                            }
                            // Else if the next character is an escape
                            else if (((char) peekChar(in)) == theQuoteEscapeChar) {
                                // Get the escaped escape
                                ch = getChar(in, rawText);
                                columnValue.append((char) ch);
                                continue;
                            }
                        }
                    }
                }

                // If the character is a quote
                if (inQuote && ((char) ch) == theQuoteChar) {

                    // This is the ending quote. Consume it.
                    inQuote = false;
                    continue;
                }
                // Break on end of input
                else if (ch == -1) {
                    break;
                }
                // Unget and break on record delimiter or column delimiter
                else if (!inQuote && (((char) ch) == recDelim || ((char) ch) == colDelim)) {
                    ungetChar(in, rawText);
                    break;
                }

                // This character is part of the column value
                columnValue.append((char) ch);
            }
        }

        return columnValue.toString();
    }

    private void updateColumnDelimiter() {
        if (columnDelimiter == null) {

            if (StringUtils.isNotEmpty(serializationProperties.getColumnDelimiter())) {
                columnDelimiter = StringUtil.unescape(serializationProperties.getColumnDelimiter());
            }
        }
    }

    private void updateRecordDelimiter() {
        if (recordDelimiter == null) {

            if (StringUtils.isNotEmpty(serializationProperties.getRecordDelimiter())) {
                recordDelimiter = StringUtil.unescape(serializationProperties.getRecordDelimiter());
            }
        }
    }

    private void updateQuoteChar() {
        if (quoteChar == null) {

            if (StringUtils.isNotEmpty(serializationProperties.getQuoteChar())) {
                quoteChar = StringUtil.unescape(serializationProperties.getQuoteChar());
            }
        }
    }

    private void updateQuoteEscapeChar() {
        if (quoteEscapeChar == null) {

            if (StringUtils.isNotEmpty(serializationProperties.getQuoteEscapeChar())) {
                quoteEscapeChar = StringUtil.unescape(serializationProperties.getQuoteEscapeChar());
            }
        }
    }

    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    public String getRecordDelimiter() {
        return recordDelimiter;
    }

    public String getQuoteChar() {
        return quoteChar;
    }

    public String getQuoteEscapeChar() {
        return quoteEscapeChar;
    }

    /**
     * This low level reader gets the next non-ignored character from the input, and returns it.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on it require
     *            in.mark()).
     * @param remark
     *            Iff true, remarks the input stream after reading an ignored character.
     * @return The next non-ignored character read, or -1 if end of input stream.
     * @throws IOException
     */
    private int getNonIgnoredChar(BufferedReader in, boolean remark) throws IOException {
        int ch;

        // If configured, gobble \r
        if (serializationProperties.isIgnoreCR()) {
            while (((char) (ch = in.read())) == '\r') {
                if (remark) {
                    in.mark(1);
                }
            }
        } else {
            ch = in.read();
        }

        return ch;
    }

    /**
     * Get the next character from the input, and return it.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on it require
     *            in.mark()).
     * @param rawText
     *            Optional StringBuilder used to return a copy of the raw text read by this method.
     * @return The next character read from the input stream, or -1 if the end of input stream is
     *         reached.
     * @throws IOException
     */
    public int getChar(BufferedReader in, StringBuilder rawText) throws IOException {
        in.mark(1);
        int ch = getNonIgnoredChar(in, true);

        // If building up the raw text, and a character was successfully read,
        // append it to the raw text
        if (rawText != null && ch != -1) {
            rawText.append((char) ch);
        }

        return ch;
    }

    /**
     * Unget the last character read by getChar().
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on it require
     *            in.mark()).
     * @param rawText
     *            Optional StringBuilder used to return a copy of the raw text unread by this
     *            method.
     * @throws IOException
     */
    public void ungetChar(BufferedReader in, StringBuilder rawText) throws IOException {
        in.reset();

        // If building up the raw text, remove the most recently read character
        if (rawText != null) {
            rawText.deleteCharAt(rawText.length() - 1);
        }
    }

    /**
     * Look ahead one character in the stream, return it, but don't consume it.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on it require
     *            in.mark()).
     * @return The next character in the stream, or -1 if end of stream reached.
     * @throws IOException
     */
    public int peekChar(BufferedReader in) throws IOException {
        in.mark(1);
        int ch = getNonIgnoredChar(in, true);
        in.reset();
        return ch;
    }

    /**
     * Look ahead n characters in the stream, return them, but don't consume them.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on it require
     *            in.mark()).
     * @param n
     *            The number of characters to read.
     * @return A string containing the next n characters without consuming them. Returns an empty
     *         string if no characters are read.
     * @throws IOException
     */
    public String peekChars(BufferedReader in, int n) throws IOException {
        // Mark with a little extra (64 characters) in case the code skips over
        // some ignored characters (e.g. \r's)
        // Beyond 64 ignored characters may cause a problem on reset().
        in.mark(n + 64);
        StringBuilder result = new StringBuilder();
        while (n > 0) {

            int ch = getNonIgnoredChar(in, false);

            if (ch == -1) {
                break;
            }

            result.append((char) ch);
            n--;
        }
        in.reset();
        return result.toString();
    }

    /**
     * Removes trailing whitespace from a string and returns it.
     * 
     * @param s
     *            The input string.
     * @return If the input string has trailing whitespace, a new string with the whitespace
     *         removed, otherwise, return s.
     */
    private String ltrim(String s) {

        if (s.length() == 0)
            return s;

        int i = s.length() - 1;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            i--;
        }

        if (i == s.length() - 1)
            return s;
        else if (i == -1)
            return new String();
        else
            return s.substring(0, i + 1);
    }
}
