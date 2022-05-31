/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.logging;

import static com.mirth.connect.server.logging.LogOutputStream.LOGGER_NAME;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class MirthLog4jFilter extends AbstractFilter {

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object... params) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, params).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg, final Throwable t) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg, final Throwable t) {
        return decide(level, logger.getName(), msg.getFormattedMessage());
    }

    @Override
    public Result filter(final LogEvent event) {
        return decide(event.getLevel(), event.getLoggerName(), event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0, p1).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0, p1, p2).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2, final Object p3) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0, p1, p2, p3).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0, p1, p2, p3, p4).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0, p1, p2, p3, p4, p5).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0, p1, p2, p3, p4, p5, p6).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0, p1, p2, p3, p4, p5, p6, p7).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0, p1, p2, p3, p4, p5, p6, p7, p8).getFormattedMessage());
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        return decide(level, logger.getName(), logger.getMessageFactory().newMessage(msg, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9).getFormattedMessage());
    }

    private Result decide(Level level, String loggerName, String msg) {
        // This check is done to filter out SAXParser warnings introduced in 7u40 (MIRTH-3548)
        if (Level.ERROR.equals(level) && LOGGER_NAME.equals(loggerName)) {
            if (StringUtils.isNotBlank(msg)) {
                if (StringUtils.equals(msg, "Compiler warnings:") || StringUtils.contains(msg, "Feature 'http://javax.xml.XMLConstants/feature/secure-processing' is not recognized.") || StringUtils.contains(msg, "Property 'http://javax.xml.XMLConstants/property/accessExternalDTD' is not recognized.") || StringUtils.contains(msg, "Property 'http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit' is not recognized.")) {
                    return Result.DENY;
                }
            }
        }

        return Result.NEUTRAL;
    }
}