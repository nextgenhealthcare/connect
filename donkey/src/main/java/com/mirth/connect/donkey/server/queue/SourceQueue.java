/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.mirth.connect.donkey.model.event.MessageEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.event.MessageEvent;

public class SourceQueue extends ConnectorMessageQueue {

    @Override
    protected ConnectorMessage pollFirstValue() {
        Iterator<Entry<Long, ConnectorMessage>> iterator = buffer.entrySet().iterator();

        if (iterator.hasNext()) {
            ConnectorMessage connectorMessage = iterator.next().getValue();

            iterator.remove();

            return connectorMessage;
        }

        return null;
    }

    public synchronized ConnectorMessage poll() {
        if (size == null) {
            updateSize();
        }

        ConnectorMessage connectorMessage = null;

        if (size > 0) {
            connectorMessage = pollFirstValue();

            // if no element was received and there are elements in the database,
            // fill the buffer from the database and get the next element in the queue
            if (connectorMessage == null) {
                fillBuffer();
                connectorMessage = pollFirstValue();
            }

            // if an element was found, decrement the overall count
            if (connectorMessage != null) {
                size--;
            }
        }

        if (connectorMessage != null) {
            eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), true));
        }

        return connectorMessage;
    }

    public synchronized void decrementSize() {
        if (size != null) {
            size--;
        }

        eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), true));
    }

    public ConnectorMessage poll(long timeout, TimeUnit unit) throws InterruptedException {
        waitTimeout(timeout, unit);

        return poll();
    }

    private void waitTimeout(long timeout, TimeUnit unit) throws InterruptedException {
        if ((size == null || size == 0) && timeout > 0) {
            synchronized (timeoutLock) {
                timeoutLock.set(true);
                timeoutLock.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
            }
        }
    }
}