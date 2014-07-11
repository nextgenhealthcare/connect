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
    protected SourceConnector sourceConnector;
    protected BatchMessageSource batchMessageSource;
    private int batchId;

    public BatchAdaptor(SourceConnector sourceConnector, BatchMessageSource batchMessageSource) {
        this.sourceConnector = sourceConnector;
        this.batchMessageSource = batchMessageSource;
    }

    public String getMessage() throws BatchMessageException {
        try {
            return getNextMessage(++batchId);
        } catch (Exception e) {
            throw new BatchMessageException("Failed to retrieve batch message at sequence number " + batchId, e);
        }
    }

    public int getBatchId() {
        return batchId;
    }

    protected abstract String getNextMessage(int batchId) throws Exception;

    public abstract void cleanup() throws BatchMessageException;
}
