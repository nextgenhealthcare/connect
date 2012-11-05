/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.Constants;

public class ConnectorMessageQueue extends PersistedBlockingQueue<ConnectorMessage> {
    public ConnectorMessageQueue() {
        setBufferCapacity(Constants.CONNECTOR_MESSAGE_QUEUE_BUFFER_SIZE);
    }

    @Override
    public void setDataSource(PersistedBlockingQueueDataSource<ConnectorMessage> dataSource) {
        if (!(dataSource instanceof ConnectorMessageQueueDataSource)) {
            throw new InvalidDataSourceException("Data source must be an instance of ConnectorMessageQueueDataSource");
        }

        super.setDataSource(dataSource);
    }

    public class InvalidDataSourceException extends RuntimeException {
        public InvalidDataSourceException(String message) {
            super(message);
        }
    }
}
