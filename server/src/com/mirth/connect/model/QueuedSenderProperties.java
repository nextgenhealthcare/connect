/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.Properties;

public class QueuedSenderProperties implements ComponentProperties
{
    public static final String USE_PERSISTENT_QUEUES = "usePersistentQueues";
    public static final String RECONNECT_INTERVAL = "reconnectMillisecs";
    public static final String ROTATE_QUEUE = "rotateQueue";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(USE_PERSISTENT_QUEUES, "0");
        properties.put(RECONNECT_INTERVAL, "10000");
        properties.put(ROTATE_QUEUE, "0");
        return properties;
    }
}
