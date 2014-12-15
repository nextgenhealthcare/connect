/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.message.batch;

import com.mirth.connect.donkey.server.channel.SourceConnector;

public abstract class BatchAdaptor {
    protected BatchAdaptorFactory factory;
    protected SourceConnector sourceConnector;
    protected BatchMessageSource batchMessageSource;
    private int batchSequenceId;
    private boolean lookAhead = true;
    private String nextMessage = null;

    public BatchAdaptor(BatchAdaptorFactory factory, SourceConnector sourceConnector, BatchMessageSource batchMessageSource) {
        this.factory = factory;
        this.sourceConnector = sourceConnector;
        this.batchMessageSource = batchMessageSource;
    }

    public String getMessage() throws BatchMessageException {
        try {
            /*
             * Currently, we always perform the lookahead
             */
            if (lookAhead) {
                String message = null;
                batchSequenceId++;
                if (batchSequenceId == 1) {
                    message = getNextMessage(batchSequenceId);
                } else {
                    message = nextMessage;
                }

                if (message != null) {
                    nextMessage = getNextMessage(batchSequenceId + 1);
                }
                return message;
            } else {
                return getNextMessage(++batchSequenceId);
            }
        } catch (Exception e) {
            if (e instanceof BatchMessageException) {
                throw (BatchMessageException) e;
            }

            throw new BatchMessageException("Failed to retrieve batch message at sequence number " + batchSequenceId, e);
        }
    }

    public boolean isLookAhead() {
        return lookAhead;
    }

    public boolean isBatchComplete() {
        return lookAhead && nextMessage == null;
    }

    public int getBatchSequenceId() {
        return batchSequenceId;
    }

    protected abstract String getNextMessage(int batchSequenceId) throws Exception;

    public abstract void cleanup() throws BatchMessageException;
}
