/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

public class Constants {
    /**
     * Reserved channel map key for the destination metadata ids that a message
     * is being sent through
     */
    public static final String DESTINATION_META_DATA_IDS_KEY = "donkey.destinations";

    /**
     * Reserved channel map key for attachments?
     */
    public static final String ATTACHMENTS_KEY = "donkey.attachments"; // TODO: is this still used? can it be removed?

    /**
     * Path to Donkey
     */
    public static final String DIR_DONKEY = ".donkey";

    /**
     * Path to attachments
     */
    public static final String DIR_ATTACHMENTS = "attachments";

    /**
     * The number of milliseconds to wait for incoming messages on the source
     * queue before timing
     * out and checking the channel state
     */
    public static final int SOURCE_QUEUE_POLL_TIMEOUT_MILLIS = 1000;

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
    public static final int CONNECTOR_MESSAGE_QUEUE_BUFFER_SIZE = 100;
}
