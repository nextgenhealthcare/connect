/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.io.Reader;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.message.BatchAdaptor;
import com.mirth.connect.donkey.server.message.BatchMessageProcessor;
import com.mirth.connect.donkey.server.message.BatchMessageProcessorException;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.util.StringUtil;

public class ER7BatchAdaptor implements BatchAdaptor {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private Pattern segmentDelimiterPattern = null;
	
	public ER7BatchAdaptor(SerializerProperties properties) {
		HL7v2SerializationProperties serializationProperties = (HL7v2SerializationProperties) properties.getSerializationProperties();
        
        if (serializationProperties != null) {
            segmentDelimiterPattern = Pattern.compile(StringUtil.unescape(serializationProperties.getSegmentDelimiter()));
        }
	}

	@Override
	public void processBatch(Reader src, BatchMessageProcessor dest) throws Exception {
		// TODO: The values of these parameters should come from the protocol
        // properties passed to processBatch
        // TODO: src is a character stream, not a byte stream
        byte startOfMessage = (byte) 0x0B;
        byte endOfMessage = (byte) 0x1C;
        byte endOfRecord = (byte) 0x0D;

        Scanner scanner = null;
        try {
            scanner = new Scanner(src);
            scanner.useDelimiter(segmentDelimiterPattern);
            StringBuilder message = new StringBuilder();
            char data[] = { (char) startOfMessage, (char) endOfMessage };
            boolean errored = false;
    
            while (scanner.hasNext()) {
                String line = scanner.next().replaceAll(new String(data, 0, 1), "").replaceAll(new String(data, 1, 1), "").trim();
    
                if ((line.length() == 0) || line.equals((char) endOfMessage) || line.startsWith("MSH")) {
                    if (message.length() > 0) {
                        try {
                            if (!dest.processBatchMessage(message.toString())) {
                                logger.warn("Batch processing stopped.");
                                return;
                            }
                        } catch (Exception e) {
                            errored = true;
                            logger.error("Error processing message in batch.", e);
                        }
    
                        message = new StringBuilder();
                    }
    
                    while ((line.length() == 0) && scanner.hasNext()) {
                        line = scanner.next();
                    }
    
                    if (line.length() > 0) {
                        message.append(line);
                        message.append((char) endOfRecord);
                    }
                } else if (line.startsWith("FHS") || line.startsWith("BHS") || line.startsWith("BTS") || line.startsWith("FTS")) {
                    // ignore batch headers
                } else {
                    message.append(line);
                    message.append((char) endOfRecord);
                }
            }
    
            /*
             * MIRTH-2058: Now that the file has been completely read, make sure to
             * process
             * anything remaining in the message buffer. There could have been lines
             * read in that were not closed with an EOM.
             */
            if (message.length() > 0) {
                try {
                    dest.processBatchMessage(message.toString());
                } catch (Exception e) {
                    errored = true;
                    logger.error("Error processing message in batch.", e);
                }
            }
    
            if (errored) {
                throw new BatchMessageProcessorException("Error processing message in batch.");
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
	}

}
