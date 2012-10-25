/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * See: http://shrubbery.mynetgear.net/c/display/W/Routing+java.util.logging+messages+to+Log4J
 */
public class JuliToLog4JService {
    private static JuliToLog4JService instance = null;
    private Handler activeHandler;
    private List<Handler> oldHandlers = new ArrayList<Handler>();
    private Level handlerLevel = Level.ALL;
    private Level rootLevel = Level.INFO;

    private JuliToLog4JService() {

    }

    public static JuliToLog4JService getInstance() {
        synchronized (JuliToLog4JService.class) {
            if (instance == null) {
                instance = new JuliToLog4JService();
            }

            return instance;
        }
    }

    public void start() {
        try {
            JuliToLog4jHandler.getTargetLogger(JuliToLog4JService.class).info("Redirecting java.util.logging to log4j");
            Logger rootLogger = LogManager.getLogManager().getLogger("");

            // remove old handlers
            for (Handler handler : rootLogger.getHandlers()) {
                oldHandlers.add(handler);
                rootLogger.removeHandler(handler);
            }

            // add our own
            activeHandler = new JuliToLog4jHandler();
            activeHandler.setLevel(handlerLevel);
            rootLogger.addHandler(activeHandler);
            rootLogger.setLevel(rootLevel);
            // done, let's check it right away!!!

            Logger.getLogger(JuliToLog4JService.class.getName()).info("Sending java.util.logging messages to log4j");
        } catch (Exception e) {
            JuliToLog4jHandler.getTargetLogger(JuliToLog4JService.class).error("Failed to start java.util.logging to log4j service.", e);
        }
    }

    public void stop() {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.removeHandler(activeHandler);

        // Put all the old handlers back.
        for (Handler oldHandler : oldHandlers) {
            rootLogger.addHandler(oldHandler);
        }

        Logger.getLogger(JuliToLog4jHandler.class.getName()).info("Stopped java.util.logging to log4j service");
    }

    public String getHandlerLevel() {
        return handlerLevel.getName();
    }

    public void setHandlerLevel(String level) {
        final String parseThis = level.toUpperCase();

        if ("DEBUG".equalsIgnoreCase(parseThis)) {
            handlerLevel = Level.FINE;
        } else {
            handlerLevel = Level.parse(parseThis);
        }
    }
}