/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

import com.mirth.connect.donkey.model.message.Status;

public class Constants {
    /**
     * Reserved channel map key for the destination metadata ids that a message is being sent
     * through
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
     * The number of milliseconds to wait for incoming messages on the source queue before timing
     * out and checking the channel state
     */
    public static final int SOURCE_QUEUE_POLL_TIMEOUT_MILLIS = 1000;

    /**
     * Response map key to be used to store the post-processor's custom response
     */
    public static final String RESPONSE_POST_PROCESSOR = "postprocessor";

    /**
     * "Respond From" key indicating that the response returned by the source connector is based on
     * whether or not a message is filtered or errored in the source filter/transformer
     */
    public static final String RESPONSE_SOURCE_TRANSFORMED = "sourcetransformed";

    /**
     * "Respond From" key indicating that the response returned by the source connector is based on
     * whether or not all destinations sent or queued the message successfully
     */
    public static final String RESPONSE_DESTINATIONS_COMPLETED = "destinationscompleted";

    /**
     * When returning a response status based on the statuses of all destinations, use this
     * precedence order in determining which status to use when the destination statuses are
     * different
     */
    public static final Status[] RESPONSE_STATUS_PRECEDENCE = new Status[] { Status.ERROR,
            Status.FILTERED, Status.SENT, Status.QUEUED, Status.PENDING, Status.TRANSFORMED,
            Status.RECEIVED };

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
