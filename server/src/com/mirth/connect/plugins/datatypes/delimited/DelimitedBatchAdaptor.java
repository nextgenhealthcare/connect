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

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.message.BatchAdaptor;
import com.mirth.connect.donkey.server.message.BatchMessageProcessor;
import com.mirth.connect.donkey.server.message.BatchMessageProcessorException;
import com.mirth.connect.model.datatype.SerializerProperties;

public class DelimitedBatchAdaptor implements BatchAdaptor {
	private Logger logger = Logger.getLogger(this.getClass());
	private DelimitedBatchReader delimitedBatchReader = null;
	private DelimitedSerializationProperties serializationProperties;
    private DelimitedBatchProperties batchProperties;
	
	public DelimitedBatchAdaptor(SerializerProperties properties) {
		serializationProperties = (DelimitedSerializationProperties) properties.getSerializationProperties();
        batchProperties = (DelimitedBatchProperties) properties.getBatchProperties();
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
    public String getMessage(BufferedReader in, boolean skipHeader, String batchScriptId) throws IOException, InterruptedException {

        // Allocate a batch reader if not already allocated
        if (delimitedBatchReader == null) {
            delimitedBatchReader = new DelimitedBatchReader(serializationProperties, batchProperties);
        }
        return delimitedBatchReader.getMessage(in, skipHeader, batchScriptId);
    }
	
	@Override
    public void processBatch(Reader src, BatchMessageProcessor dest) throws Exception {
        BufferedReader in = new BufferedReader(src);
        String message;
        boolean skipHeader = true;
        boolean errored = false;
        
        while ((message = getMessage(in, skipHeader, dest.getBatchScriptId())) != null) {
            try {
                if (!dest.processBatchMessage(message)) {
                    logger.warn("Batch processing stopped.");
                    return;
                }
            } catch (BatchMessageProcessorException e) {
                errored = true;
                logger.error("Error processing message in batch.", e);
            }
            
            skipHeader = false;
        }
        
        if (errored) {
            throw new BatchMessageProcessorException("Error processing message in batch.");
        }
    }
	
	public String getBatchScript() {
        return batchProperties.getBatchScript();
    }
}
