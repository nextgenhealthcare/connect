/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.extprops;

import java.util.Properties;

public abstract class ExtensionStatusProvider implements ExtensionStatusInterface {

    protected Properties mirthProperties;
    protected LoggerWrapper logger;

    public ExtensionStatusProvider(Properties mirthProperties) {
        this.mirthProperties = mirthProperties;

        try {
            logger = new LoggerWrapper(Thread.currentThread().getContextClassLoader().loadClass("org.apache.log4j.Logger").getMethod("getLogger", Class.class).invoke(null, ExtensionStatuses.class));
        } catch (Throwable t) {
            logger = new LoggerWrapper(null);
        }
    }
}
