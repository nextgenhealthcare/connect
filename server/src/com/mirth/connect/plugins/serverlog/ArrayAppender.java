/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import java.util.Date;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

import com.mirth.connect.server.logging.MirthLog4jFilter;

public class ArrayAppender extends AppenderSkeleton {
    private ServerLogProvider serverLogProvider;

    public ArrayAppender(ServerLogProvider serverLogProvider) {
        this.serverLogProvider = serverLogProvider;
        addFilter(new MirthLog4jFilter());
    }

    protected void append(LoggingEvent loggingEvent) {
        if (this.layout == null) {
            errorHandler.error("No layout for appender " + name, null, ErrorCode.MISSING_LAYOUT);
            return;
        }

        String level = String.valueOf(loggingEvent.getLevel());
        Date date = new Date(loggingEvent.getTimeStamp());
        String threadName = loggingEvent.getThreadName();
        String category = loggingEvent.getLoggerName();
        String lineNumber = loggingEvent.getLocationInformation().getLineNumber();
        String message = String.valueOf(loggingEvent.getMessage());

        String throwableInformation = null;
        if (loggingEvent.getThrowableStrRep() != null) {
            String[] completeLogTrace = loggingEvent.getThrowableStrRep();
            StringBuffer logText = new StringBuffer();
            for (String aCompleteLogTrace : completeLogTrace) {
                logText.append(aCompleteLogTrace);
            }
            throwableInformation = logText.toString();
        }

        serverLogProvider.newServerLogReceived(level, date, threadName, category, lineNumber, message, throwableInformation);
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
