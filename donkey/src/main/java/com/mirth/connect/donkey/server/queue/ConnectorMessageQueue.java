/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mirth.connect.donkey.model.event.MessageEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.event.MessageEvent;

public class ConnectorMessageQueue {
    private Map<Long, ConnectorMessage> buffer = new LinkedHashMap<Long, ConnectorMessage>();
    private Integer size;
    private int bufferCapacity = 1000;
    private boolean reachedCapacity = false;
    private boolean rotate = false;
    private ConnectorMessageQueueDataSource dataSource;
    private final AtomicBoolean timeoutLock = new AtomicBoolean(false);

    private EventDispatcher eventDispatcher = Donkey.getInstance().getEventDispatcher();
    private String channelId;
    private Integer metaDataId;
    private Set<Long> checkedOut = new HashSet<Long>();
    private Set<Long> deleted = new HashSet<Long>();

    public ConnectorMessageQueue() {}

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

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    public synchronized void updateSize() {
        size = dataSource.getSize();
    }

    public synchronized void invalidate(boolean updateSize, boolean reset) {
        buffer.clear();

        if (reset) {
            checkedOut.clear();
            deleted.clear();
        }

        size = null;

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

        eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), false));
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

    public ConnectorMessage poll(long timeout, TimeUnit unit) throws InterruptedException {
        waitTimeout(timeout, unit);

        return poll();
    }

    private ConnectorMessage pollFirstValue() {
        Iterator<Entry<Long, ConnectorMessage>> iterator = buffer.entrySet().iterator();

        if (iterator.hasNext()) {
            ConnectorMessage connectorMessage = iterator.next().getValue();

            iterator.remove();

            return connectorMessage;
        }

        return null;
    }

    private void waitTimeout(long timeout, TimeUnit unit) throws InterruptedException {
        if ((size == null || size == 0) && timeout > 0) {
            synchronized (timeoutLock) {
                timeoutLock.set(true);
                timeoutLock.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
            }
        }
    }

    public synchronized ConnectorMessage acquire() {
        ConnectorMessage connectorMessage = null;

        if (size() - checkedOut.size() > 0) {
            boolean bufferFilled = false;

            do {
                if (size == null) {
                    updateSize();
                }

                if (size > 0) {
                    connectorMessage = pollFirstValue();

                    // if no element was received and there are elements in the database,
                    // fill the buffer from the database and get the next element in the queue
                    if (connectorMessage == null) {
                        if (bufferFilled) {
                            return null;
                        }

                        fillBuffer();
                        bufferFilled = true;

                        connectorMessage = pollFirstValue();
                    }

                    // if an element was found, decrement the overall count
                    if (connectorMessage != null && rotate) {
                        dataSource.setLastItem(connectorMessage);
                    }
                }
            } while (connectorMessage != null && checkedOut.contains(connectorMessage.getMessageId()));
        }

        if (connectorMessage != null) {
            checkedOut.add(connectorMessage.getMessageId());
        }

        return connectorMessage;
    }

    public synchronized boolean isCheckedOut(Long messageId) {
        boolean isCheckedOut = checkedOut.contains(messageId);

        /*
         * If the message is no longer checked out and it was previously marked as deleted, we want
         * to remove it from the deleted list as well as the buffer so that it does not get acquired
         * again.
         */
        if (!isCheckedOut && deleted.contains(messageId)) {
            deleted.remove(messageId);
            buffer.remove(messageId);
            updateSize();
        }

        return isCheckedOut;
    }

    public synchronized void markAsDeleted(Long messageId) {
        deleted.add(messageId);
    }

    public synchronized boolean releaseIfDeleted(ConnectorMessage connectorMessage) {
        if (deleted.contains(connectorMessage.getMessageId())) {
            release(connectorMessage, true);
            return true;
        }

        return false;
    }

    public synchronized void release(ConnectorMessage connectorMessage, boolean finished) {
        if (connectorMessage != null) {
            if (size != null) {
                Long messageId = connectorMessage.getMessageId();

                if (finished) {
                    size--;

                    if (buffer.containsKey(messageId)) {
                        buffer.remove(messageId);
                    }
                } else {
                    if (buffer.containsKey(messageId)) {
                        buffer.put(messageId, connectorMessage);
                    }

                    dataSource.rotateQueue();
                }
            }

            if (finished) {
                eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), true));
            }

            checkedOut.remove(connectorMessage.getMessageId());
        }
    }

    public synchronized void fillBuffer() {
        if (size == null) {
            updateSize();
        }

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
