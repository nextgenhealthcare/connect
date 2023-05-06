/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import java.io.Serializable;
import java.util.Date;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.util.Throwables;
import org.apache.logging.log4j.util.Strings;

import com.mirth.connect.server.logging.MirthLog4jFilter;

public class ArrayAppender extends AbstractAppender {
    private ServerLogProvider serverLogProvider;

    public ArrayAppender(ServerLogProvider serverLogProvider, Layout<? extends Serializable> layout) {
        super(serverLogProvider.getClass().getName(), new MirthLog4jFilter(), layout, true, Property.EMPTY_ARRAY);
        this.serverLogProvider = serverLogProvider;
    }

    @Override
    public void append(LogEvent logEvent) {
        if (getLayout() == null) {
            getHandler().error("No layout for appender " + getName(), logEvent, null);
            return;
        }

        String level = String.valueOf(logEvent.getLevel());
        Date date = new Date(logEvent.getTimeMillis());
        String threadName = logEvent.getThreadName();
        String category = logEvent.getLoggerName();
        String message = logEvent.getMessage().getFormattedMessage();

        String lineNumber = "?";
        StackTraceElement source = logEvent.getSource();
        if (source != null) {
            int line = source.getLineNumber();
            if (line >= 0) {
                lineNumber = Integer.toString(line);
            }
        }

        String throwableInformation = null;
        if (logEvent.getThrown() != null) {
            String[] completeLogTrace = Throwables.toStringList(logEvent.getThrown()).toArray(Strings.EMPTY_ARRAY);
            StringBuffer logText = new StringBuffer();
            for (String aCompleteLogTrace : completeLogTrace) {
                logText.append(aCompleteLogTrace);
            }
            throwableInformation = logText.toString();
        }

        serverLogProvider.newServerLogReceived(level, date, threadName, category, lineNumber, message, throwableInformation);
    }
}
