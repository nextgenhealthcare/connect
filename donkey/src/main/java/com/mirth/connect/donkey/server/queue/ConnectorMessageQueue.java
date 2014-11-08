/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mirth.connect.donkey.model.event.MessageEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.event.MessageEvent;

public abstract class ConnectorMessageQueue {

    protected Map<Long, ConnectorMessage> buffer = new LinkedHashMap<Long, ConnectorMessage>();
    protected Integer size;
    protected ConnectorMessageQueueDataSource dataSource;
    protected final AtomicBoolean timeoutLock = new AtomicBoolean(false);
    protected EventDispatcher eventDispatcher = Donkey.getInstance().getEventDispatcher();
    protected String channelId;
    protected Integer metaDataId;

    private int bufferCapacity = 1000;
    private boolean reachedCapacity = false;
    private boolean invalidated = false;

    protected abstract ConnectorMessage pollFirstValue();

    protected void reset() {}

    public int getBufferSize() {
        return buffer.size();
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public synchronized void setBufferCapacity(int bufferCapacity) {
        if (bufferCapacity < this.bufferCapacity) {
            buffer.clear();
        }

        this.bufferCapacity = bufferCapacity;
    }

    public ConnectorMessageQueueDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(ConnectorMessageQueueDataSource dataSource) {
        channelId = dataSource.getChannelId();
        metaDataId = dataSource.getMetaDataId();

        this.dataSource = dataSource;
        invalidate(false, true);
    }

    public synchronized void updateSize() {
        size = dataSource.getSize();
    }

    public synchronized void invalidate(boolean updateSize, boolean reset) {
        buffer.clear();

        if (reset) {
            reset();
        }

        size = null;
        invalidated = true;

        if (updateSize) {
            eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), true));
        }
    }

    public synchronized boolean contains(ConnectorMessage connectorMessage) {
        return buffer.containsKey(connectorMessage.getMessageId());
    }

    public boolean isEmpty() {
        if (size == null) {
            updateSize();
        }

        return (size == 0);
    }

    public int size() {
        if (size == null) {
            if (dataSource == null) {
                return 0;
            }
            updateSize();
        }

        return size;
    }

    public synchronized void add(ConnectorMessage connectorMessage) {
        if (invalidated) {
            /*
             * If the buffer's size was already updated after an invalidate, then we need to
             * increment the size by one in order to account for the new message that was just
             * added, since this method is only ever called after a new message is added to the
             * database
             */
            if (size != null) {
                size++;
            }

            /*
             * If the buffer was never filled after an invalidate, we can't just insert the message
             * directly into the buffer because there could be messages that should process before
             * it. Therefore we'll just fill the buffer to resync it with the database. This method
             * can only be called after a new message was added to the database
             */
            fillBuffer();
        } else {
            if (size == null) {
                updateSize();
            }
            if (!reachedCapacity) {
                if (size < bufferCapacity && !dataSource.isQueueRotated()) {
                    buffer.put(connectorMessage.getMessageId(), connectorMessage);

                    // If there is a poll with timeout waiting, notify that an item was added to the buffer.
                    if (timeoutLock.get()) {
                        synchronized (timeoutLock) {
                            timeoutLock.notifyAll();
                            timeoutLock.set(false);
                        }
                    }
                } else {
                    reachedCapacity = true;
                }
            }
            size++;
        }

        eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), false));
    }

    public synchronized void fillBuffer() {
        if (size == null) {
            updateSize();
        }

        invalidated = false;
        buffer = dataSource.getItems(0, Math.min(bufferCapacity, size));

        if (buffer.size() == size) {
            reachedCapacity = false;
        }

        // If there is a poll with timeout waiting, notify that an item was added to the buffer.
        if (buffer.size() > 0 && timeoutLock.get()) {
            synchronized (timeoutLock) {
                timeoutLock.notifyAll();
                timeoutLock.set(false);
            }
        }
    }
}
