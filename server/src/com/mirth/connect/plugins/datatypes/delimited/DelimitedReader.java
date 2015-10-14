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
    private String quoteToken = null;
    private String quoteEscapeToken = null;

    public DelimitedReader(DelimitedSerializationProperties serializationProperties) {
        this.serializationProperties = serializationProperties;

        updateColumnDelimiter();
        updateRecordDelimiter();
        updateQuoteToken();
        updateQuoteEscapeToken();

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
        // Each record is assumed to contain the same number and type of
        // columns.
        //
        // The following user configurable options affect the behavior of the
        // parser:
        // o columnWidths The array of fixed column widths.
        // o columnDelimiter The characters that delimit (separate) the
        // columns.
        // o recordDelimiter The characters that delimit (separate) each
        // record.
        // o quoteToken The characters that are used to quote a column value.
        // o escapeWithDoubleQuote Iff true, embedded quotes are escaped with
        // two consecutive quotes.
        // Otherwise, the quote escape characters escape embedded quote
        // characters.
        // o quoteEscapeToken The characters used to escape a quote (or itself).
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

        String recDelim = recordDelimiter;
        String lookAhead = "";
        ArrayList<String> record = new ArrayList<String>();

        // If column widths are set, separate and get each column's value based off of its set width
        if (serializationProperties.getColumnWidths() != null) {

            // Iterate through the list of column widths
            for (int i = 0; i < serializationProperties.getColumnWidths().length; i++) {

                // Read through the stream at the set length for this column width
                StringBuilder columnValue = new StringBuilder();
                lookAhead = peekChars(in, recDelim.length());
                for (int j = 0; j < serializationProperties.getColumnWidths()[i]; j++) {

                    // If the next characters are the record delimiter
                    if (lookAhead.equals(recDelim)) {
                        break;
                    }

                    // Break on end of input
                    ch = getChar(in, rawText);
                    if (ch == -1) {
                        break;
                    }

                    columnValue.append((char) ch);
                    lookAhead = peekChars(in, recDelim.length());
                }

                // Add column value to the record
                record.add(ltrim(columnValue.toString()));

                // Break on end of input or record delimiter
                if (ch == -1 || lookAhead.equals(recDelim)) {
                    break;
                }
            }

            // Consume trailing characters, if any, up until end of input stream or
            // record delimiter
            while (ch != -1 && !lookAhead.equals(recDelim)) {
                ch = getChar(in, rawText);
                lookAhead = peekChars(in, recDelim.length());
            }

            // Consume record delimiter
            if (lookAhead.equals(recDelim)) {
                for (int i = 0; i < recDelim.length(); i++) {
                    ch = getChar(in, rawText);
                }
            }
        } else {
            String colDelim = ","; // default
            if (StringUtils.isNotEmpty(columnDelimiter)) {
                colDelim = columnDelimiter;
            }

            for (;;) {
                String columnValue = getColumnValue(in, rawText);

                record.add(columnValue);

                // Break at end of input
                ch = peekChar(in);
                if (ch == -1) {
                    ch = getChar(in, rawText);
                    break;
                }

                // Consume record delimiter and break
                lookAhead = peekChars(in, recDelim.length());
                if (lookAhead.equals(recDelim)) {
                    for (int i = 0; i < recDelim.length(); i++) {
                        ch = getChar(in, rawText);
                    }
                    break;
                }

                // Consume column delimiter
                lookAhead = peekChars(in, colDelim.length());
                if (lookAhead.equals(colDelim)) {
                    for (int i = 0; i < colDelim.length(); i++) {
                        ch = getChar(in, rawText);
                    }
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

        // Return empty string if input stream is empty
        int ch;
        ch = peekChar(in);
        if (ch == -1)
            return "";

        StringBuilder columnValue = new StringBuilder();

        String colDelim = ","; // default
        if (StringUtils.isNotEmpty(columnDelimiter)) {
            colDelim = columnDelimiter;
        }

        String recDelim = "\\n"; // default
        if (StringUtils.isNotEmpty(recordDelimiter)) {
            recDelim = recordDelimiter;
        }

        String theQuoteToken = "\""; // default
        if (StringUtils.isNotEmpty(quoteToken)) {
            theQuoteToken = quoteToken;
        }

        String theQuoteEscapeToken = "\\"; // default
        if (StringUtils.isNotEmpty(quoteEscapeToken)) {
            theQuoteEscapeToken = quoteEscapeToken;
        }

        // If the column value isn't quoted
        boolean inQuote = false;
        String lookAhead = peekChars(in, theQuoteToken.length());
        if (!lookAhead.equals(theQuoteToken)) {
            for (;;) {
                // Break on record delimiter
                lookAhead = peekChars(in, recDelim.length());
                if (lookAhead.equals(recDelim)) {
                    break;
                }

                // Break on column delimiter
                lookAhead = peekChars(in, colDelim.length());
                if (lookAhead.equals(colDelim)) {
                    break;
                }

                ch = getChar(in, rawText);

                // Break on end of input and end of column
                if (ch == -1)
                    break;

                columnValue.append((char) ch);
            }
        }
        // Column value is quoted
        else {

            inQuote = true;

            // Get the quote token
            for (int i = 0; i < theQuoteToken.length(); i++) {
                ch = getChar(in, rawText);
            }

            for (;;) {
                // Process escaped quotes
                if (inQuote) {
                    // If the quote escape method is double quoting
                    if (serializationProperties.isEscapeWithDoubleQuote()) {

                        // Then check if the next few characters are two quote tokens
                        lookAhead = peekChars(in, theQuoteToken.length() * 2);
                        if (lookAhead.equals(theQuoteToken + theQuoteToken)) {
                            // If so, consume the first one
                            for (int i = 0; i < theQuoteToken.length(); i++) {
                                getChar(in, rawText);
                            }

                            // And add the second one (the escaped quote)
                            for (int i = 0; i < theQuoteToken.length(); i++) {
                                ch = getChar(in, rawText);
                                columnValue.append((char) ch);
                            }
                            continue;
                        }
                    } else {
                        // First check if the next few characters are an escaped quote token
                        lookAhead = peekChars(in, theQuoteEscapeToken.length() + theQuoteToken.length());
                        if (lookAhead.equals(theQuoteEscapeToken + theQuoteToken)) {
                            // Consume the escape token
                            for (int i = 0; i < theQuoteEscapeToken.length(); i++) {
                                getChar(in, rawText);
                            }

                            // And add the escaped quote token
                            for (int i = 0; i < theQuoteToken.length(); i++) {
                                ch = getChar(in, rawText);
                                columnValue.append((char) ch);
                            }
                            continue;
                        }

                        // If not, then check if the next few characters are an escaped escape token
                        lookAhead = peekChars(in, theQuoteEscapeToken.length() * 2);
                        if (lookAhead.equals(theQuoteEscapeToken + theQuoteEscapeToken)) {
                            // Consume the escape token
                            for (int i = 0; i < theQuoteEscapeToken.length(); i++) {
                                getChar(in, rawText);
                            }

                            // And add the escaped escape token
                            for (int i = 0; i < theQuoteEscapeToken.length(); i++) {
                                ch = getChar(in, rawText);
                                columnValue.append((char) ch);
                            }
                            continue;
                        }
                    }
                }

                // If the next characters are a quote token
                lookAhead = peekChars(in, theQuoteToken.length());
                if (inQuote && lookAhead.equals(theQuoteToken)) {
                    // This is the ending quote token. Get it.
                    for (int i = 0; i < theQuoteToken.length(); i++) {
                        ch = getChar(in, rawText);
                    }
                    inQuote = false;
                    continue;
                }

                // Break on record delimiter
                lookAhead = peekChars(in, recDelim.length());
                if (!inQuote && lookAhead.equals(recDelim)) {
                    break;
                }

                // Break on column delimiter
                lookAhead = peekChars(in, colDelim.length());
                if (!inQuote && lookAhead.equals(colDelim)) {
                    break;
                }

                // Get the next character. If this point has been reached, 
                // then the next character is not part of 
                // a delimiter, quote, or escape token.
                ch = getChar(in, rawText);
                // Break on end of input
                if (ch == -1) {
                    break;
                }

                // Add the next character to the column value
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

    private void updateQuoteToken() {
        if (quoteToken == null) {

            if (StringUtils.isNotEmpty(serializationProperties.getQuoteToken())) {
                quoteToken = StringUtil.unescape(serializationProperties.getQuoteToken());
            }
        }
    }

    private void updateQuoteEscapeToken() {
        if (quoteEscapeToken == null) {

            if (StringUtils.isNotEmpty(serializationProperties.getQuoteEscapeToken())) {
                quoteEscapeToken = StringUtil.unescape(serializationProperties.getQuoteEscapeToken());
            }
        }
    }

    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    public String getRecordDelimiter() {
        return recordDelimiter;
    }

    public String getQuoteToken() {
        return quoteToken;
    }

    public String getQuoteEscapeToken() {
        return quoteEscapeToken;
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
