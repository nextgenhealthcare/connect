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
import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class MirthLog4jFilter extends Filter {

    @Override
    public int decide(LoggingEvent event) {
        // This check is done to filter out SAXParser warnings introduced in 7u40 (MIRTH-3548)
        if (event.getLevel().equals(Level.ERROR) && event.getLoggerName().equals(LOGGER_NAME)) {
            String msg = event.getRenderedMessage();

            if (StringUtils.isNotBlank(msg)) {
                if (StringUtils.equals(msg, "Compiler warnings:") || StringUtils.contains(msg, "Feature 'http://javax.xml.XMLConstants/feature/secure-processing' is not recognized.") || StringUtils.contains(msg, "Property 'http://javax.xml.XMLConstants/property/accessExternalDTD' is not recognized.") || StringUtils.contains(msg, "Property 'http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit' is not recognized.")) {
                    return Filter.DENY;
                }
            }
        }

        return Filter.NEUTRAL;
    }
}