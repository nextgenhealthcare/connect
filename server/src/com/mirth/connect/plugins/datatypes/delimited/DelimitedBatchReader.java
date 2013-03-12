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

import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.util.StringUtil;

public class DelimitedBatchReader {
	private Logger logger = Logger.getLogger(this.getClass());
	private DelimitedReader reader = null;
	private DelimitedSerializationProperties serializationProperties;
	private DelimitedBatchProperties batchProperties;
	private Integer groupingColumnIndex;
	private String batchMessageDelimiter = null;
	private JavaScriptExecutor<String> jsExecutor = new JavaScriptExecutor<String>();
	
	public DelimitedBatchReader(SerializationProperties serializationProperties, BatchProperties batchProperties) {
		reader = new DelimitedReader((DelimitedSerializationProperties) serializationProperties);
		this.serializationProperties = (DelimitedSerializationProperties) serializationProperties;
		this.batchProperties = (DelimitedBatchProperties) batchProperties;
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
        char recDelim = reader.getRecordDelimiter().charAt(0);
        int ch;

        // If skipping the header, and the option is configured
        if (skipHeader && batchProperties.getBatchSkipRecords() > 0) {

            for (int i = 0; i < batchProperties.getBatchSkipRecords(); i++) {
                while ((ch = in.read()) != -1 && ((char) ch) != recDelim) {
                }
            }
        }

        StringBuilder message = new StringBuilder();

        if (batchProperties.isBatchSplitByRecord()) {
            // Each record is treated as a message
            reader.getRecord(in, message);
        } else if (StringUtils.isNotEmpty(batchProperties.getBatchMessageDelimiter())) {
            if (batchMessageDelimiter == null) {
                batchMessageDelimiter = StringUtil.unescape(batchProperties.getBatchMessageDelimiter());
            }
            // All records until the message delimiter (or end of input) is a
            // message.
            for (;;) {
                // Get the next record
                ArrayList<String> record = reader.getRecord(in, message);

                if (record == null) {
                    break;
                }

                // If the next sequence of characters is the message delimiter
                String lookAhead = reader.peekChars(in, batchMessageDelimiter.length());
                if (lookAhead.equals(batchMessageDelimiter)) {

                    // Consume it.
                    for (int i = 0; i < batchMessageDelimiter.length(); i++) {
                        ch = reader.getChar(in, null);
                    }

                    // Append it if it is being included
                    if (batchProperties.isBatchMessageDelimiterIncluded()) {
                        message.append(batchMessageDelimiter);
                    }

                    break;
                }
            }
        } else if (StringUtils.isNotEmpty(batchProperties.getBatchGroupingColumn())) {
            // Each message is a collection of records with the same value in
            // the specified column.
            // The end of the current message occurs when a transition in the
            // value of the specified
            // column occurs.

            // Prime the pump: get the first record, and save the grouping
            // column.
            ArrayList<String> record = reader.getRecord(in, message);

            if (record != null) {
                
                if (groupingColumnIndex == null) {
                    updateGroupingColumnIndex(batchProperties.getBatchGroupingColumn(), serializationProperties.getColumnNames());
                }

                String lastColumnValue = record.get(groupingColumnIndex);

                // Read records until the value in the grouping column changes
                // or there are no more records
                for (;;) {

                    StringBuilder recordText = new StringBuilder();
                    record = reader.getRecord(in, recordText);

                    if (record == null) {
                        break;
                    }

                    if (!record.get(groupingColumnIndex).equals(lastColumnValue)) {
                    	reader.ungetRecord(record, recordText.toString());
                        break;
                    }

                    message.append(recordText.toString());
                }
            }
        } else if (StringUtils.isNotEmpty(batchProperties.getBatchScript())) {
            try {
                final int batchSkipRecords = batchProperties.getBatchSkipRecords();
                String result = jsExecutor.execute(new JavaScriptTask<String>() {
                    @Override
                    public String call() throws Exception {
                        Script compiledScript = CompiledScriptCache.getInstance().getCompiledScript(batchScriptId);

                        if (compiledScript == null) {
                            logger.error("Batch script could not be found in cache");
                            return null;
                        } else {
                            Logger scriptLogger = Logger.getLogger(ScriptController.BATCH_SCRIPT_KEY.toLowerCase());
                            
                            Scriptable scope = JavaScriptScopeUtil.getBatchProcessorScope(scriptLogger, batchScriptId, getScopeObjects(in, serializationProperties, skipHeader, batchSkipRecords));
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
    
    private Map<String, Object> getScopeObjects(Reader in, DelimitedSerializationProperties props, Boolean skipHeader, Integer batchSkipRecords) {
        Map<String, Object> scopeObjects = new HashMap<String, Object>();

        // Provide the reader in the scope
        scopeObjects.put("reader", in);

        // Provide the data type properties in the scope (the ones that
        // affect parsing from delimited to XML)
        scopeObjects.put("columnDelimiter", reader.getColumnDelimiter());
        scopeObjects.put("recordDelimiter", reader.getRecordDelimiter());
        scopeObjects.put("columnWidths", props.getColumnWidths());
        scopeObjects.put("quoteChar", reader.getQuoteChar());
        scopeObjects.put("escapeWithDoubleQuote", props.isEscapeWithDoubleQuote());
        scopeObjects.put("quoteEscapeChar", reader.getQuoteEscapeChar());
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
                    int index = batchGroupingColumn.length()-1;
                    int len=0;
                    while (index >= 0 && Character.isDigit(batchGroupingColumn.charAt(index))) {
                        index--;
                        len++;
                    }
                    if (len > 0) {
                        try {
                            groupingColumnIndex = Integer.valueOf(
                                    batchGroupingColumn.substring(
                                            batchGroupingColumn.length()-len, 
                                            batchGroupingColumn.length())) - 1;
                        }
                        catch (NumberFormatException e) {
                            logger.warn("Invalid number format in Split Batch by Grouping Column (defaulting to first column): " + 
                                    batchGroupingColumn.substring(
                                            batchGroupingColumn.length()-len, 
                                            batchGroupingColumn.length()));
                        }
                    }
                    else {
                        logger.warn("Unknown batch grouping column (defaulting to first column): " + batchGroupingColumn);
                    }
                }
                else {
    
                    // Try to find the grouping column name in the user specified column names
                    int i;
                    for (i=0; i < columnNames.length; i++) {
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
