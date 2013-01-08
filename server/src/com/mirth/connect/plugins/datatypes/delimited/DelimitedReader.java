/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;

public class DelimitedReader extends SAXParser {
    private Logger logger = Logger.getLogger(this.getClass());

    private DelimitedSerializerProperties props;

    // The most recent ungotten record, and it's raw text, if any
    private ArrayList<String> ungottenRecord;
    private String ungottenRawText;
    private JavaScriptExecutor<String> jsExecutor = new JavaScriptExecutor<String>();

    public DelimitedReader(DelimitedSerializerProperties delimitedProperties) {
        props = delimitedProperties;

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
            if (props.isNumberedRows()) {
                contentHandler.startElement("", "row" + recordNo, "", null);
            } else {
                contentHandler.startElement("", "row", "", null);
            }

            // For each column
            for (int i = 0; i < record.size(); i++) {

                String columnName;
                if (props.getColumnNames() != null && i < props.getColumnNames().length) {
                    // User specified column name
                    columnName = props.getColumnNames()[i];
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
            if (props.isNumberedRows()) {
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
     * Finds the next message in the input stream and returns it.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on
     *            it require in.mark()).
     * @param skipHeader
     *            Pass true to skip the configured number of header rows,
     *            otherwise false.
     * @return The next message, or null if there are no more messages.
     * @throws IOException
     * @throws InterruptedException
     */
    public String getMessage(final BufferedReader in, final boolean skipHeader, final String batchScriptId) throws IOException, InterruptedException {
        char recDelim = props.getRecordDelimiter().charAt(0);
        int ch;

        // If skipping the header, and the option is configured
        if (skipHeader && props.getBatchSkipRecords() > 0) {

            for (int i = 0; i < props.getBatchSkipRecords(); i++) {
                while ((ch = in.read()) != -1 && ((char) ch) != recDelim) {
                }
            }
        }

        StringBuilder message = new StringBuilder();

        if (props.isBatchSplitByRecord()) {
            // Each record is treated as a message
            getRecord(in, message);
        } else if (DelimitedSerializerProperties.isSet(props.getBatchMessageDelimiter())) {
            // All records until the message delimiter (or end of input) is a
            // message.
            for (;;) {
                // Get the next record
                ArrayList<String> record = getRecord(in, message);

                if (record == null) {
                    break;
                }

                // If the next sequence of characters is the message delimiter
                String lookAhead = peekChars(in, props.getBatchMessageDelimiter().length());
                if (lookAhead.equals(props.getBatchMessageDelimiter())) {

                    // Consume it.
                    for (int i = 0; i < props.getBatchMessageDelimiter().length(); i++) {
                        ch = getChar(in, null);
                    }

                    // Append it if it is being included
                    if (props.isBatchMessageDelimiterIncluded()) {
                        message.append(props.getBatchMessageDelimiter());
                    }

                    break;
                }
            }
        } else if (DelimitedSerializerProperties.isSet(props.getBatchGroupingColumn())) {
            // Each message is a collection of records with the same value in
            // the specified column.
            // The end of the current message occurs when a transition in the
            // value of the specified
            // column occurs.

            // Prime the pump: get the first record, and save the grouping
            // column.
            ArrayList<String> record = getRecord(in, message);

            if (record != null) {

                String lastColumnValue = record.get(props.getGroupingColumnIndex());

                // Read records until the value in the grouping column changes
                // or there are no more records
                for (;;) {

                    StringBuilder recordText = new StringBuilder();
                    record = getRecord(in, recordText);

                    if (record == null) {
                        break;
                    }

                    if (!record.get(props.getGroupingColumnIndex()).equals(lastColumnValue)) {
                        ungetRecord(record, recordText.toString());
                        break;
                    }

                    message.append(recordText.toString());
                }
            }
        } else if (DelimitedSerializerProperties.isSet(props.getBatchScript())) {
            try {
                String result = jsExecutor.execute(new JavaScriptTask<String>() {
                    @Override
                    public String call() throws Exception {
                        Script compiledScript = CompiledScriptCache.getInstance().getCompiledScript(batchScriptId);

                        if (compiledScript == null) {
                            logger.error("Batch script could not be found in cache");
                            return null;
                        } else {
                            Logger scriptLogger = Logger.getLogger(ScriptController.BATCH_SCRIPT_KEY.toLowerCase());
                            
                            Scriptable scope = JavaScriptScopeUtil.getBatchProcessorScope(scriptLogger, batchScriptId, getScopeObjects(in, props, skipHeader));
                            return Context.toString(executeScript(compiledScript, scope));
                        }
                    }
                });

                if (result != null) {
                    message.append(result);
                }
            } catch (InterruptedException e) {
                throw e;
            } catch (JavaScriptExecutorException e) {
                logger.error(e.getCause());
            } catch (Throwable e) {
                logger.error(e);
            }
        } else {
            // There is no batching method configured. Treat the entire input
            // stream as the message.
            logger.warn("No batch splitting method configured (processing entire input as one message)");
            while ((ch = in.read()) != -1) {
                message.append((char) ch);
            }
        }

        String result = message.toString();
        if (result.length() == 0) {
            return null;
        } else {
            return result;
        }
    }
    
    public Map<String, Object> getScopeObjects(Reader in, DelimitedSerializerProperties props, Boolean skipHeader) {
        Map<String, Object> scopeObjects = new HashMap<String, Object>();

        // Provide the reader in the scope
        scopeObjects.put("reader", in);

        // Provide the data type properties in the scope (the ones that
        // affect parsing from delimited to XML)
        scopeObjects.put("columnDelimiter", props.getColumnDelimiter());
        scopeObjects.put("recordDelimiter", props.getRecordDelimiter());
        scopeObjects.put("columnWidths", props.getColumnWidths());
        scopeObjects.put("quoteChar", props.getQuoteChar());
        scopeObjects.put("escapeWithDoubleQuote", props.isEscapeWithDoubleQuote());
        scopeObjects.put("quoteEscapeChar", props.getQuoteEscapeChar());
        scopeObjects.put("ignoreCR", props.isIgnoreCR());
        if (skipHeader) {
            scopeObjects.put("skipRecords", props.getBatchSkipRecords());
        } else {
            scopeObjects.put("skipRecords", 0);
        }

        return scopeObjects;
    }

    /**
     * Get the next record from the input stream, and consume the record
     * delimiter, if any.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on
     *            it require in.mark()).
     * @param rawText
     *            Optional StringBuilder used to return a copy of the raw text
     *            read by this method.
     * @return The record represented as a collection of column values, or null
     *         if there is no next record.
     * @throws IOException
     */
    private ArrayList<String> getRecord(BufferedReader in, StringBuilder rawText) throws IOException {

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

        char recDelim = props.getRecordDelimiter().charAt(0);
        ArrayList<String> record = new ArrayList<String>();
        if (props.getColumnWidths() != null) {

            for (int i = 0; i < props.getColumnWidths().length; i++) {

                StringBuilder columnValue = new StringBuilder();
                for (int j = 0; j < props.getColumnWidths()[i]; j++) {

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

            String columnValue;
            while ((columnValue = getColumnValue(in, rawText)) != null) {
                record.add(columnValue);

                ch = peekChar(in);
                if (((char) ch) == recDelim) {

                    // consume record delimiter
                    ch = getChar(in, rawText);

                    break;
                }
            }
        }

        return record;
    }

    /**
     * Unget the given record. The next call to getRecord() will consume it, and
     * return it, rather than operating on the input stream.
     * 
     * @param record
     *            The record to unget.
     * @param rawText
     *            The raw text for the record to unget.
     */
    private void ungetRecord(ArrayList<String> record, String rawText) {
        ungottenRecord = record;
        ungottenRawText = rawText;
    }

    /**
     * Get the next column value from the input stream, and consume the column
     * delimiter, if any.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on
     *            it require in.mark()).
     * @param rawText
     *            Optional StringBuilder used to return a copy of the raw text
     *            read by this method.
     * @return The column value, or null if there is no next column value.
     * @throws IOException
     */
    private String getColumnValue(BufferedReader in, StringBuilder rawText) throws IOException {

        int ch;
        ch = peekChar(in);
        if (ch == -1)
            return null;

        StringBuilder columnValue = new StringBuilder();

        char colDelim = ','; // default
        if (DelimitedSerializerProperties.isSet(props.getColumnDelimiter())) {
            colDelim = props.getColumnDelimiter().charAt(0);
        }

        char recDelim = '\n'; // default
        if (DelimitedSerializerProperties.isSet(props.getRecordDelimiter())) {
            recDelim = props.getRecordDelimiter().charAt(0);
        }

        char theQuoteChar = '"'; // default
        if (DelimitedSerializerProperties.isSet(props.getQuoteChar())) {
            theQuoteChar = props.getQuoteChar().charAt(0);
        }

        char theQuoteEscapeChar = '\\'; // default
        if (DelimitedSerializerProperties.isSet(props.getQuoteEscapeChar())) {
            theQuoteEscapeChar = props.getQuoteEscapeChar().charAt(0);
        }

        // If the column value isn't quoted
        boolean inQuote = false;
        if (((char) peekChar(in)) != theQuoteChar) {
            for (;;) {
                ch = getChar(in, rawText);

                // Break on end of input and end of column
                if (ch == -1 || ((char) ch) == colDelim)
                    break;

                // Unget and break on record delimiter
                if (((char) ch) == recDelim) {
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
                    if (props.isEscapeWithDoubleQuote()) {

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
                // Break on end of column
                else if (!inQuote && ((char) ch) == colDelim) {
                    break;
                }
                // Unget and break on record delimiter
                else if (!inQuote && ((char) ch) == recDelim) {
                    ungetChar(in, rawText);
                    break;
                }

                // This character is part of the column value
                columnValue.append((char) ch);
            }
        }

        return columnValue.toString();
    }

    /**
     * This low level reader gets the next non-ignored character from the input,
     * and returns it.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on
     *            it require in.mark()).
     * @param remark
     *            Iff true, remarks the input stream after reading an ignored
     *            character.
     * @return The next non-ignored character read, or -1 if end of input
     *         stream.
     * @throws IOException
     */
    private int getNonIgnoredChar(BufferedReader in, boolean remark) throws IOException {
        int ch;

        // If configured, gobble \r
        if (props.isIgnoreCR()) {
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
     *            The input stream (it's a BufferedReader, because operations on
     *            it require in.mark()).
     * @param rawText
     *            Optional StringBuilder used to return a copy of the raw text
     *            read by this method.
     * @return The next character read from the input stream, or -1 if the end
     *         of input stream is reached.
     * @throws IOException
     */
    private int getChar(BufferedReader in, StringBuilder rawText) throws IOException {
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
     *            The input stream (it's a BufferedReader, because operations on
     *            it require in.mark()).
     * @param rawText
     *            Optional StringBuilder used to return a copy of the raw text
     *            unread by this method.
     * @throws IOException
     */
    private void ungetChar(BufferedReader in, StringBuilder rawText) throws IOException {
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
     *            The input stream (it's a BufferedReader, because operations on
     *            it require in.mark()).
     * @return The next character in the stream, or -1 if end of stream reached.
     * @throws IOException
     */
    private int peekChar(BufferedReader in) throws IOException {
        in.mark(1);
        int ch = getNonIgnoredChar(in, true);
        in.reset();
        return ch;
    }

    /**
     * Look ahead n characters in the stream, return them, but don't consume
     * them.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on
     *            it require in.mark()).
     * @param n
     *            The number of characters to read.
     * @return A string containing the next n characters without consuming them.
     *         Returns an empty string if no characters are read.
     * @throws IOException
     */
    private String peekChars(BufferedReader in, int n) throws IOException {
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
     * @return If the input string has trailing whitespace, a new string with
     *         the whitespace removed, otherwise, return s.
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
