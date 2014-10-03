/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.channel;

import java.util.concurrent.CancellationException;

public class ChannelTaskHandler {
    /**
     * This will be called when a task begins executing. Any potential exceptions that could occur
     * in this method should be handled directly within the method to avoid disrupting the task.
     */
    public void taskStarted(String channelId, Integer metaDataId) {}

    /**
     * This will be called when a task finishes executing. Any potential exceptions that could occur
     * in this method should be handled directly within the method to avoid disrupting the task.
     */
    public void taskCompleted(String channelId, Integer metaDataId) {}

    /**
     * This will be called when an error occurs in the task. Any potential exceptions that could
     * occur in this method should be handled directly within the method to avoid disrupting the
     * task.
     */
    public void taskErrored(String channelId, Integer metaDataId, Exception e) {}

    /**
     * This will be called when a task is cancelled. Any potential exceptions that could occur in
     * this method should be handled directly within the method to avoid disrupting the task.
     */
    public void taskCancelled(String channelId, Integer metaDataId, CancellationException e) {}
}
