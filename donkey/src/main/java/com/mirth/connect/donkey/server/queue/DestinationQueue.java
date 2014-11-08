/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.event.MessageEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.event.MessageEvent;
import com.mirth.connect.donkey.util.MessageMaps;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.xstream.SerializerException;

public class DestinationQueue extends ConnectorMessageQueue {

    private String groupBy;
    private boolean regenerateTemplate;
    private Serializer serializer;
    private MessageMaps messageMaps;
    private Set<Long> checkedOut = new HashSet<Long>();
    private Set<Long> deleted = new HashSet<Long>();
    private boolean rotate = false;
    private int queueBuckets = 1;
    private List<Long> queueThreadIds;
    private HashFunction hashFunction;

    public DestinationQueue(String groupBy, int threadCount, boolean regenerateTemplate, Serializer serializer, MessageMaps messageMaps) {
        this.groupBy = groupBy;
        this.regenerateTemplate = regenerateTemplate;
        this.serializer = serializer;
        this.messageMaps = messageMaps;

        if (StringUtils.isNotBlank(groupBy)) {
            queueBuckets = threadCount;

            if (queueBuckets > 1) {
                queueThreadIds = new ArrayList<Long>(queueBuckets);
                hashFunction = Hashing.murmur3_32((int) System.currentTimeMillis());
            }
        }
    }

    @Override
    protected ConnectorMessage pollFirstValue() {
        Iterator<Entry<Long, ConnectorMessage>> iterator = buffer.entrySet().iterator();

        while (iterator.hasNext()) {
            ConnectorMessage connectorMessage = iterator.next().getValue();

            /*
             * If there are multiple buckets, then the first value in the buffer may not be the
             * first value for this queue thread. In this case, iterate through the buffer and find
             * the first one that matches (if any).
             */
            if (queueBuckets > 1) {
                Integer bucket = getBucket(connectorMessage);

                if (bucket < queueThreadIds.size() && queueThreadIds.get(bucket).equals(Thread.currentThread().getId())) {
                    iterator.remove();
                    return connectorMessage;
                }
            } else {
                iterator.remove();
                return connectorMessage;
            }
        }

        return null;
    }

    @Override
    protected void reset() {
        checkedOut.clear();
        deleted.clear();
        if (queueBuckets > 1) {
            queueThreadIds.clear();
        }
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    public synchronized void registerThreadId() {
        if (queueBuckets > 1) {
            queueThreadIds.add(Thread.currentThread().getId());
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

                    /*
                     * If connectorMessage is null, it may just mean that all the messages in the
                     * buffer are in buckets for other queue threads. So only go to the database for
                     * more messages and try again if the buffer is actually empty.
                     */
                    if (connectorMessage == null && buffer.size() == 0) {
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

            checkedOut.remove(connectorMessage.getMessageId());

            if (finished) {
                eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), true));
            }
        }
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

    private Integer getBucket(ConnectorMessage connectorMessage) {
        Integer bucket = connectorMessage.getQueueBucket();

        // Get the bucket if we haven't already, or if value replacement needs to be done
        if (bucket == null || regenerateTemplate) {
            String groupByVariable = groupBy;

            /*
             * If we're not regenerating connector properties, then we need to get the group by
             * variable from the actual persisted sent content, because it could be different than
             * what is being used in the currently deployed revision.
             */
            if (!regenerateTemplate) {
                try {
                    ConnectorProperties sentProperties = connectorMessage.getSentProperties();
                    if (sentProperties == null) {
                        sentProperties = serializer.deserialize(connectorMessage.getSent().getContent(), ConnectorProperties.class);
                        connectorMessage.setSentProperties(sentProperties);
                    }

                    groupByVariable = ((DestinationConnectorPropertiesInterface) sentProperties).getDestinationConnectorProperties().getThreadAssignmentVariable();
                } catch (SerializerException e) {
                }
            }

            // Calculate the 32-bit hash, then reduce it to one of the buckets
            bucket = Math.abs(hashFunction.hashUnencodedChars(String.valueOf(messageMaps.get(groupByVariable, connectorMessage))).asInt() % queueBuckets);
            connectorMessage.setQueueBucket(bucket);
        }

        return bucket;
    }
}