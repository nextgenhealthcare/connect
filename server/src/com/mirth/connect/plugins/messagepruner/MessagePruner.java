/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import java.util.Calendar;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.server.util.DatabaseUtil;

public abstract class MessagePruner {
    private int retryCount;
    private boolean skipIncomplete;
    private Status[] skipStatuses;
    private Integer blockSize;
    private Logger logger = Logger.getLogger(this.getClass());

    public MessagePruner() {
        this.retryCount = 3;
        this.skipIncomplete = true;
        this.skipStatuses = new Status[] { Status.ERROR, Status.QUEUED };
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isSkipIncomplete() {
        return skipIncomplete;
    }

    public void setSkipIncomplete(boolean skipIncomplete) {
        this.skipIncomplete = skipIncomplete;
    }

    public Status[] getSkipStatuses() {
        return skipStatuses;
    }

    public void setSkipStatuses(Status[] skipStatuses) {
        this.skipStatuses = skipStatuses;
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Integer blockSize) {
        if (blockSize != null && blockSize > 0) {
            this.blockSize = blockSize;
        } else {
            this.blockSize = null;
        }
    }

    public int[] executePruner(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws InterruptedException, MessagePrunerException {
        logger.debug("Executing pruner for channel: " + channelId);

        if (messageDateThreshold == null && contentDateThreshold == null) {
            return new int[] { 0, 0 };
        }

        // the content date threshold is only used/needed if it is later than the message date threshold
        if (messageDateThreshold != null && contentDateThreshold != null && contentDateThreshold.getTimeInMillis() <= messageDateThreshold.getTimeInMillis()) {
            contentDateThreshold = null;
        }

        int retries = retryCount;

        while (true) {
            try {
                return prune(channelId, messageDateThreshold, contentDateThreshold);
            } catch (Exception e) {
                if (retries > 0) {
                    retries--;
                } else {
                    throw new MessagePrunerException("Failed to prune messages", e);
                }
            }
        }
    }

    protected abstract int[] prune(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws InterruptedException, MessagePrunerException;

    protected int runDelete(SqlSession session, String query, Map<String, Object> params, Integer limit) {
        if (!DatabaseUtil.statementExists(query)) {
            return 0;
        }

        if (limit == null) {
            return session.delete(query, params);
        }

        int deletedRows;
        int total = 0;

        do {
            deletedRows = session.delete(query, params);
            total += deletedRows;
        } while (deletedRows >= limit && deletedRows > 0);

        return total;
    }
}
