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
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReader;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReceiver;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.plugins.datatypes.delimited.DelimitedBatchProperties.SplitType;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.util.StringUtil;

public class DelimitedBatchAdaptor extends BatchAdaptor {
    private Logger logger = Logger.getLogger(this.getClass());
    private DelimitedSerializationProperties serializationProperties;
    private DelimitedBatchProperties batchProperties;
    private DelimitedReader delimitedReader = null;
    private BufferedReader bufferedReader;
    private boolean skipHeader;
    private Integer groupingColumnIndex;
    private String batchMessageDelimiter = null;

    public DelimitedBatchAdaptor(SourceConnector sourceConnector, BatchMessageSource batchMessageSource) {
        super(sourceConnector, batchMessageSource);
    }

    public DelimitedSerializationProperties getSerializationProperties() {
        return serializationProperties;
    }

    public void setSerializationProperties(DelimitedSerializationProperties serializationProperties) {
        this.serializationProperties = serializationProperties;
    }

    public DelimitedBatchProperties getBatchProperties() {
        return batchProperties;
    }

    public void setBatchProperties(DelimitedBatchProperties batchProperties) {
        this.batchProperties = batchProperties;
    }

    public DelimitedReader getDelimitedReader() {
        return delimitedReader;
    }

    public void setDelimitedReader(DelimitedReader delimitedReader) {
        this.delimitedReader = delimitedReader;
    }

    @Override
    public void cleanup() throws BatchMessageException {}

    @Override
    protected String getNextMessage(int batchSequenceId) throws Exception {
        if (batchMessageSource instanceof BatchMessageReader) {
            if (batchSequenceId == 1) {
                BatchMessageReader batchMessageReader = (BatchMessageReader) batchMessageSource;
                bufferedReader = new BufferedReader(batchMessageReader.getReader());
                skipHeader = true;
            }
            return getMessageFromReader();
        } else if (batchMessageSource instanceof BatchMessageReceiver) {
            return getMessageFromReceiver();
        }

        return null;
    }

    private String getMessageFromReader() throws Exception {
        String message = getMessage(bufferedReader, skipHeader);
        skipHeader = false;
        return message;
    }

    private String getMessageFromReceiver() throws Exception {
        return null;
    }

    /**
     * Finds the next message in the input stream and returns it.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on it require
     *            in.mark()).
     * @param skipHeader
     *            Pass true to skip the configured number of header rows, otherwise false.
     * @return The next message, or null if there are no more messages.
     * @throws IOException
     * @throws InterruptedException
     */
    private String getMessage(final BufferedReader in, final boolean skipHeader) throws Exception {
        char recDelim = delimitedReader.getRecordDelimiter().charAt(0);
        int ch;

        // If skipping the header, and the option is configured
        if (skipHeader && batchProperties.getBatchSkipRecords() > 0) {

            for (int i = 0; i < batchProperties.getBatchSkipRecords(); i++) {
                while ((ch = in.read()) != -1 && ((char) ch) != recDelim) {
                }
            }
        }

        StringBuilder message = new StringBuilder();
        SplitType splitOption = batchProperties.getSplitType();

        if (splitOption == SplitType.Record) {
            // Each record is treated as a message
            delimitedReader.getRecord(in, message);
        } else if (splitOption == SplitType.Delimiter) {
            if (StringUtils.isEmpty(batchProperties.getBatchMessageDelimiter())) {
                throw new BatchMessageException("No batch message delimiter was set.");
            }

            if (batchMessageDelimiter == null) {
                batchMessageDelimiter = StringUtil.unescape(batchProperties.getBatchMessageDelimiter());
            }
            // All records until the message delimiter (or end of input) is a
            // message.
            for (;;) {
                // Get the next record
                ArrayList<String> record = delimitedReader.getRecord(in, message);

                if (record == null) {
                    break;
                }

                // If the next sequence of characters is the message delimiter
                String lookAhead = delimitedReader.peekChars(in, batchMessageDelimiter.length());
                if (lookAhead.equals(batchMessageDelimiter)) {

                    // Consume it.
                    for (int i = 0; i < batchMessageDelimiter.length(); i++) {
                        ch = delimitedReader.getChar(in, null);
                    }

                    // Append it if it is being included
                    if (batchProperties.isBatchMessageDelimiterIncluded()) {
                        message.append(batchMessageDelimiter);
                    }

                    break;
                }
            }
        } else if (splitOption == SplitType.Grouping_Column) {
            if (StringUtils.isEmpty(batchProperties.getBatchGroupingColumn())) {
                throw new BatchMessageException("No batch grouping column was set.");
            }

            // Each message is a collection of records with the same value in
            // the specified column.
            // The end of the current message occurs when a transition in the
            // value of the specified
            // column occurs.

            // Prime the pump: get the first record, and save the grouping
            // column.
            ArrayList<String> record = delimitedReader.getRecord(in, message);

            if (record != null) {

                if (groupingColumnIndex == null) {
                    updateGroupingColumnIndex(batchProperties.getBatchGroupingColumn(), serializationProperties.getColumnNames());
                }

                String lastColumnValue = record.get(groupingColumnIndex);

                // Read records until the value in the grouping column changes
                // or there are no more records
                for (;;) {

                    StringBuilder recordText = new StringBuilder();
                    record = delimitedReader.getRecord(in, recordText);

                    if (record == null) {
                        break;
                    }

                    if (!record.get(groupingColumnIndex).equals(lastColumnValue)) {
                        delimitedReader.ungetRecord(record, recordText.toString());
                        break;
                    }

                    message.append(recordText.toString());
                }
            }
        } else if (splitOption == SplitType.JavaScript) {
            if (StringUtils.isEmpty(batchProperties.getBatchScript())) {
                throw new BatchMessageException("No batch script was set.");
            }

            try {
                final int batchSkipRecords = batchProperties.getBatchSkipRecords();
                String result = JavaScriptUtil.execute(new JavaScriptTask<String>() {
                    @Override
                    public String call() throws Exception {
                        String batchScriptId = ScriptController.getScriptId(ScriptController.BATCH_SCRIPT_KEY, sourceConnector.getChannelId());
                        Script compiledScript = CompiledScriptCache.getInstance().getCompiledScript(batchScriptId);

                        if (compiledScript == null) {
                            logger.error("Batch script could not be found in cache");
                            return null;
                        } else {
                            Logger scriptLogger = Logger.getLogger(ScriptController.BATCH_SCRIPT_KEY.toLowerCase());

                            try {
                                Scriptable scope = JavaScriptScopeUtil.getBatchProcessorScope(scriptLogger, batchScriptId, getScopeObjects(in, serializationProperties, skipHeader, batchSkipRecords));
                                return (String) Context.jsToJava(executeScript(compiledScript, scope), String.class);
                            } finally {
                                Context.exit();
                            }
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
            throw new BatchMessageException("No valid batch splitting method configured");
        }

        String result = message.toString();
        if (result.length() == 0) {
            return null;
        } else {
            return result;
        }
    }

    private Map<String, Object> getScopeObjects(Reader in, DelimitedSerializationProperties props, Boolean skipHeader, Integer batchSkipRecords) {
        Map<String, Object> scopeObjects = new HashMap<String, Object>();

        // Provide the reader in the scope
        scopeObjects.put("reader", in);

        // Provide the data type properties in the scope (the ones that
        // affect parsing from delimited to XML)
        scopeObjects.put("columnDelimiter", delimitedReader.getColumnDelimiter());
        scopeObjects.put("recordDelimiter", delimitedReader.getRecordDelimiter());
        scopeObjects.put("columnWidths", props.getColumnWidths());
        scopeObjects.put("quoteChar", delimitedReader.getQuoteChar());
        scopeObjects.put("escapeWithDoubleQuote", props.isEscapeWithDoubleQuote());
        scopeObjects.put("quoteEscapeChar", delimitedReader.getQuoteEscapeChar());
        scopeObjects.put("ignoreCR", props.isIgnoreCR());
        if (skipHeader) {
            scopeObjects.put("skipRecords", batchSkipRecords);
        } else {
            scopeObjects.put("skipRecords", 0);
        }

        return scopeObjects;
    }

    private void updateGroupingColumnIndex(String batchGroupingColumn, String[] columnNames) {
        if (groupingColumnIndex == null) {
            // Default
            groupingColumnIndex = -1;

            // If there is a batch grouping column name
            if (StringUtils.isNotEmpty(batchGroupingColumn)) {

                // If we can't resolve the grouping column name, it'll default to the first column (index=0)
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

                    // Try to find the grouping column name in the user specified column names
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
    }
}
