/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.mirth.connect.donkey.model.event.MessageEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.event.MessageEvent;

public class SourceQueue extends ConnectorMessageQueue {

    private Set<Long> checkedOut = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());

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

            /*
             * We use a while loop here to ensure that no message gets polled at the same time from
             * multiple queue threads. After calling poll() and acquiring a connector message, the
             * caller is expected to call finish to remove the message ID from the checked out set.
             */
            while (connectorMessage != null && checkedOut.contains(connectorMessage.getMessageId())) {
                connectorMessage = pollFirstValue();
            }
        }

        // if an element was found, decrement the overall count
        if (connectorMessage != null) {
            size--;
            checkedOut.add(connectorMessage.getMessageId());
            eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), true));
        }

        return connectorMessage;
    }

    public synchronized void finish(ConnectorMessage connectorMessage) {
        if (connectorMessage != null) {
            Long messageId = connectorMessage.getMessageId();

            if (buffer.containsKey(messageId)) {
                buffer.remove(messageId);
            }

            checkedOut.remove(messageId);
        }
    }

    @Override
    protected void reset() {
        checkedOut.clear();
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
        /*
         * If there are no queued messages, then we want to wait. Otherwise, it's possible that
         * multiple queue threads all have messages checked out and the buffer is full. In this case
         * we also want to wait until at least one of the messages has finished.
         */
        if ((size == null || size == 0 || checkedOut.size() == getBufferCapacity()) && timeout > 0) {
            synchronized (timeoutLock) {
                timeoutLock.set(true);
                timeoutLock.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
            }
        }
    }
}