/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReader;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReceiver;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;

public class ER7BatchAdaptor extends BatchAdaptor {
    private Logger logger = Logger.getLogger(this.getClass());

    private Pattern lineBreakPattern;
    private String segmentDelimiter;

    private Scanner scanner;
    private String previousLine;

    public ER7BatchAdaptor(SourceConnector sourceConnector, BatchMessageSource batchMessageSource) {
        super(sourceConnector, batchMessageSource);
    }

    public Pattern getLineBreakPattern() {
        return lineBreakPattern;
    }

    public void setLineBreakPattern(Pattern lineBreakPattern) {
        this.lineBreakPattern = lineBreakPattern;
    }

    public String getSegmentDelimiter() {
        return segmentDelimiter;
    }

    public void setSegmentDelimiter(String segmentDelimiter) {
        this.segmentDelimiter = segmentDelimiter;
    }

    @Override
    public void cleanup() throws BatchMessageException {
        if (scanner != null) {
            scanner.close();
        }

        previousLine = null;
    }

    @Override
    protected String getNextMessage(int batchSequenceId) throws Exception {
        if (batchMessageSource instanceof BatchMessageReader) {
            if (batchSequenceId == 1) {
                BatchMessageReader batchMessageReader = (BatchMessageReader) batchMessageSource;
                scanner = new Scanner(batchMessageReader.getReader());
                scanner.useDelimiter(lineBreakPattern);
                previousLine = null;
            }
            return getMessageFromReader();
        } else if (batchMessageSource instanceof BatchMessageReceiver) {
            return getMessageFromReceiver((BatchMessageReceiver) batchMessageSource);
        }

        return null;
    }

    private String getMessageFromReceiver(BatchMessageReceiver batchMessageReceiver) throws Exception {
        byte[] bytes = null;

        if (batchMessageReceiver.canRead()) {
            try {
                bytes = batchMessageReceiver.readBytes();
            } finally {
                batchMessageReceiver.readCompleted();
            }

            if (bytes != null) {
                return batchMessageReceiver.getStringFromBytes(bytes);
            }
        }
        return null;
    }

    private String getMessageFromReader() throws Exception {
        // TODO: The values of these parameters should come from the protocol
        // properties passed to processBatch
        // TODO: src is a character stream, not a byte stream
        byte startOfMessage = (byte) 0x0B;
        byte endOfMessage = (byte) 0x1C;

        StringBuilder message = new StringBuilder();
        if (StringUtils.isNotBlank(previousLine)) {
            message.append(previousLine);
            message.append(segmentDelimiter);
        }

        while (scanner.hasNext()) {
            String line = StringUtils.remove(StringUtils.remove(scanner.next(), (char) startOfMessage), (char) endOfMessage).trim();

            if ((line.length() == 0) || line.equals((char) endOfMessage) || line.startsWith("MSH")) {
                if (message.length() > 0) {
                    previousLine = line;
                    return message.toString();
                }

                while ((line.length() == 0) && scanner.hasNext()) {
                    line = scanner.next();
                }

                if (line.length() > 0) {
                    message.append(line);
                    message.append(segmentDelimiter);
                }
            } else if (line.startsWith("FHS") || line.startsWith("BHS") || line.startsWith("BTS") || line.startsWith("FTS")) {
                // ignore batch headers
            } else {
                message.append(line);
                message.append(segmentDelimiter);
            }
        }

        /*
         * MIRTH-2058: Now that the file has been completely read, make sure to process anything
         * remaining in the message buffer. There could have been lines read in that were not closed
         * with an EOM.
         */
        if (message.length() > 0) {
            previousLine = null;
            return message.toString();
        }

        return null;
    }
}
