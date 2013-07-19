/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

public class ArrayAppender extends AppenderSkeleton {
    private ServerLogProvider serverLogProvider;

    public ArrayAppender(ServerLogProvider serverLogProvider) {
        this.serverLogProvider = serverLogProvider;
    }

    protected void append(LoggingEvent loggingEvent) {
        if (this.layout == null) {
            errorHandler.error("No layout for appender " + name, null, ErrorCode.MISSING_LAYOUT);
            return;
        }

        // get the complete stack trace, if applicable.
        if (loggingEvent.getThrowableStrRep() != null) {
            String[] completeLogTrace = loggingEvent.getThrowableStrRep();
            StringBuffer logText = new StringBuffer();
            for (String aCompleteLogTrace : completeLogTrace) {
                logText.append(aCompleteLogTrace);
            }
            // pass the new log message to ServerLogProvider.
            serverLogProvider.newServerLogReceived(this.layout.format(loggingEvent) + logText.toString());
        } else {
            // pass the new log message to ServerLogProvider.
            serverLogProvider.newServerLogReceived(this.layout.format(loggingEvent));
        }
    }

    public boolean requiresLayout() {
        return true;
    }

    public void close() {
        // clean up and set the boolean 'closed' to true to indiciate that this
        // appender has been shut down.
        serverLogProvider = null;
        closed = true;
    }
}
