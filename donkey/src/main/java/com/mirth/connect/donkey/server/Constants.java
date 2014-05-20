/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

public class Constants {
    /**
     * Reserved channel map key for the destination metadata ids that a message is being sent
     * through
     */
    public static final String DESTINATION_META_DATA_IDS_KEY = "mirth_destinations";

    /**
     * The number of milliseconds to wait for incoming messages on the source queue before timing
     * out and checking the channel state
     */
    public static final int SOURCE_QUEUE_POLL_TIMEOUT_MILLIS = 1000;

    /**
     * The number of milliseconds to wait when a runtime exception occurs on the source queue before
     * trying the message again
     */
    public static final int SOURCE_QUEUE_ERROR_SLEEP_TIME = 1000;

    /**
     * The number of milliseconds to wait when a destination queue is empty before peeking again
     */
    public static final int DESTINATION_QUEUE_EMPTY_SLEEP_TIME = 200;

    /**
     * Thread priority level for the event handler
     */
    public static final int EVENT_HANDLER_THREAD_PRIORITY = 2;

    /**
     * Interval in milliseconds between writing events to the database
     */
    public static final int EVENT_HANDLER_WRITE_INTERVAL_MILLIS = 5000;

    /**
     * The number of messages to buffer in memory for connector message queues
     */
    public static final int DEFAULT_QUEUE_BUFFER_SIZE = 1000;

    /**
     * The charset to use when converting attachment Strings to byte arrays for storage in the
     * database and reattaching them.
     */
    public static final String ATTACHMENT_CHARSET = "UTF-8";
}
