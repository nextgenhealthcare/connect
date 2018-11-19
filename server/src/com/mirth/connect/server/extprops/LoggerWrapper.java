/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.extprops;

public class LoggerWrapper {

    private Object logger;

    public LoggerWrapper(Object logger) {
        this.logger = logger;
    }

    public void error(Object message) {
        error(message, null);
    }

    public void error(Object message, Throwable t) {
        call("error", message, t);
    }

    public void warn(Object message) {
        warn(message, null);
    }

    public void warn(Object message, Throwable t) {
        call("warn", message, t);
    }

    public void debug(Object message) {
        debug(message, null);
    }

    public void debug(Object message, Throwable t) {
        call("debug", message, t);
    }

    public void trace(Object message) {
        trace(message, null);
    };

    public void trace(Object message, Throwable t) {
        call("trace", message, t);
    }

    private void call(String methodName, Object message, Throwable t) {
        if (logger != null) {
            try {
                logger.getClass().getMethod(methodName, Object.class, Throwable.class).invoke(logger, message, t);
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
        } else {
            if (message != null) {
                System.err.println(message.toString());
            }
            if (t != null) {
                t.printStackTrace();
            }
        }
    }
}
