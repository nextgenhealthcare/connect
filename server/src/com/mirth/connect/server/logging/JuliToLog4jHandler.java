/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.logging;

import org.apache.log4j.Priority;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * See: http://shrubbery.mynetgear.net/c/display/W/Routing+java.util.logging+messages+to+Log4J
 */
public class JuliToLog4jHandler extends Handler {
    public void publish(LogRecord record) {
        org.apache.log4j.Logger log4j = getTargetLogger(record.getLoggerName());
        Priority priority = toLog4j(record.getLevel());
        log4j.log(priority, toLog4jMessage(record), record.getThrown());
    }

    static Logger getTargetLogger(String loggerName) {
        return Logger.getLogger(loggerName);
    }

    public static Logger getTargetLogger(Class<?> clazz) {
        return getTargetLogger(clazz.getName());
    }

    private String toLog4jMessage(LogRecord record) {
        String message = record.getMessage();

        // Format message
        try {
            Object parameters[] = record.getParameters();
            if (parameters != null && parameters.length != 0) {
                // Check for the first few parameters ?
                if (message.indexOf("{0}") >= 0 || message.indexOf("{1}") >= 0 || message.indexOf("{2}") >= 0 || message.indexOf("{3}") >= 0) {
                    message = MessageFormat.format(message, parameters);
                }
            }
        } catch (Exception e) {
            // ignore Exception
        }

        return message;
    }

    private static org.apache.log4j.Level toLog4j(Level level) { 
        if (Level.OFF == level) {
            return org.apache.log4j.Level.OFF;
        } else if (Level.SEVERE == level) {
            return org.apache.log4j.Level.ERROR;
        } else if (Level.WARNING == level) {
            return org.apache.log4j.Level.WARN;
        } else if (Level.INFO == level) {
            return org.apache.log4j.Level.INFO;
        } else if (Level.CONFIG == level) {
            return org.apache.log4j.Level.DEBUG;
        } else if (Level.FINE == level) {
            return org.apache.log4j.Level.TRACE;
        } else if (Level.FINER == level) {
            return org.apache.log4j.Level.TRACE;
        } else if (Level.FINEST == level) {
            return org.apache.log4j.Level.TRACE;
        } else if (Level.ALL == level) {
            return org.apache.log4j.Level.ALL;
        }
        
        return org.apache.log4j.Level.INFO;
    }
    
    @Override
    public void flush() {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }
}