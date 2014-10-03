/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.channel;

import java.util.concurrent.CancellationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 * An implementation of ChannelTaskHandler that logs any errors which occur.
 */
public class LoggingTaskHandler extends ChannelTaskHandler {
    private Logger logger = Logger.getLogger(this.getClass());

    public void taskErrored(String channelId, Integer metaDataId, Exception e) {
        logger.error(ExceptionUtils.getStackTrace(e));
    }

    public void taskCancelled(String channelId, Integer metaDataId, CancellationException e) {
        logger.error("Task cancelled because the channel " + channelId + " was halted or removed.", e);
    }
}
