/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;

public interface QueueHandler {

    public boolean canStartSourceQueue();

    public ConnectorMessageQueueDataSource createSourceQueueDataSource(SourceConnector connector, DonkeyDaoFactory daoFactory);

    public boolean canStartDestinationQueue(DestinationConnector connector);

    public boolean allowSendFirst(DestinationConnector connector);

    public ConnectorMessageQueueDataSource createDestinationQueueDataSource(DestinationConnector connector, DonkeyDaoFactory daoFactory);
}
