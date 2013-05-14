/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.concurrent.TimeUnit;

import com.mirth.connect.donkey.model.event.MessageEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.event.MessageEvent;

public class ConnectorMessageQueue extends PersistedBlockingQueue<ConnectorMessage> {
    private EventDispatcher eventDispatcher = Donkey.getInstance().getEventDispatcher();
    private String channelId;
    private Integer metaDataId;

    public ConnectorMessageQueue() {
        setBufferCapacity(Constants.CONNECTOR_MESSAGE_QUEUE_BUFFER_SIZE);
    }

    @Override
    public void setDataSource(PersistedBlockingQueueDataSource<ConnectorMessage> dataSource) {
        if (dataSource instanceof ConnectorMessageQueueDataSource) {
            ConnectorMessageQueueDataSource connectorDataSource = (ConnectorMessageQueueDataSource) dataSource;
            channelId = connectorDataSource.getChannelId();
            metaDataId = connectorDataSource.getMetaDataId();

            super.setDataSource(connectorDataSource);
        } else {
            throw new InvalidDataSourceException("Data source must be an instance of ConnectorMessageQueueDataSource");
        }
    }

    public synchronized void invalidate(boolean updateSize) {
        super.invalidate();

        if (updateSize) {
            eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), true));
        }
    }

    @Override
    public synchronized boolean add(ConnectorMessage connectorMessage) {
        boolean success = super.add(connectorMessage);

        eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), false));

        return success;
    }

    @Override
    public synchronized ConnectorMessage poll() {
        ConnectorMessage connectorMessage = super.poll();

        if (connectorMessage != null) {
            eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, MessageEventType.QUEUED, (long) size(), true));
        }

        return connectorMessage;
    }

    @Override
    public ConnectorMessage poll(long timeout, TimeUnit unit) throws InterruptedException {
        waitTimeout(timeout, unit);

        return poll();
    }

    public class InvalidDataSourceException extends RuntimeException {
        public InvalidDataSourceException(String message) {
            super(message);
        }
    }
}
