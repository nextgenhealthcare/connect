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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class ConnectorMessageQueueDataSource {
    private DonkeyDaoFactory daoFactory;
    private String channelId;
    private String serverId;
    private int metaDataId;
    private Status status;
    private boolean rotate;
    private Long maxMessageId = null;
    private Long minMessageId = null;
    private Long rotatedMessageId = null;
    private ConcurrentHashMap<Long, Boolean> rotateThreadMap;

    public ConnectorMessageQueueDataSource(String channelId, String serverId, int metaDataId, Status status, boolean rotate, DonkeyDaoFactory daoFactory) {
        this.channelId = channelId;
        this.serverId = serverId;
        this.metaDataId = metaDataId;
        this.status = status;
        this.rotate = rotate;
        this.daoFactory = daoFactory;

        if (rotate) {
            rotateThreadMap = new ConcurrentHashMap<Long, Boolean>();
        }
    }

    public DonkeyDaoFactory getDaoFactory() {
        return daoFactory;
    }

    public void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public int getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(int metaDataId) {
        this.metaDataId = metaDataId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ConcurrentHashMap<Long, Boolean> getRotateThreadMap() {
        return rotateThreadMap;
    }

    public void setLastItem(ConnectorMessage connectorMessage) {
        /*
         * Multiple processing threads can cause the buffer to be out of order, so make sure the
         * rotation ID is always set to the highest one available.
         */
        if (rotatedMessageId == null || connectorMessage.getMessageId() >= rotatedMessageId) {
            rotatedMessageId = connectorMessage.getMessageId() + 1;
        }

        if (isQueueRotated()) {
            rotateQueue();
        }
    }

    public void rotateQueue() {
        minMessageId = rotatedMessageId;

        /*
         * Set the max message ID when the minimum message ID is set for the first time (for the
         * first message being returned to the queue). This sets an ending point for the current
         * cycle, and any new messages (with higher message IDs) will not be part of it. When the
         * queue exhausts all messages in the current cycle, it will start back at the beginning,
         * and the min/max IDs will be reset, allowing those new messages to be part of the next
         * cycle.
         */
        if (maxMessageId == null) {
            DonkeyDao dao = getDaoFactory().getDao();
            try {
                maxMessageId = dao.getConnectorMessageMaxMessageId(channelId, serverId, metaDataId, status);
            } finally {
                dao.close();
            }
        }
    }

    public boolean isQueueRotated() {
        return (rotate && minMessageId != null && minMessageId != 0);
    };

    public int getSize() {
        DonkeyDao dao = getDaoFactory().getDao();

        try {
            if (rotate) {
                minMessageId = 0L;
                rotatedMessageId = null;
            } else {
                minMessageId = null;
            }
            maxMessageId = null;

            return dao.getConnectorMessageCount(channelId, serverId, metaDataId, status);
        } finally {
            dao.close();
        }
    }

    public Map<Long, ConnectorMessage> getItems(int offset, int limit) {
        DonkeyDao dao = daoFactory.getDao();

        try {
            List<ConnectorMessage> connectorMessages = dao.getConnectorMessages(channelId, serverId, metaDataId, status, offset, limit, minMessageId, maxMessageId);

            /*
             * If rotation is on, the last query may not have returned any results because min/max
             * message IDs were set, indicating that an earlier message was returned to the queue.
             * If this is the case, reset the min/max messages IDs to start a new rotation cycle. If
             * a message is returned to the queue again, the min/max IDs will be set at that time.
             */
            if (rotate && connectorMessages.size() == 0) {
                minMessageId = 0L;
                maxMessageId = null;
                rotatedMessageId = null;
                connectorMessages = dao.getConnectorMessages(channelId, serverId, metaDataId, status, offset, limit, minMessageId, maxMessageId);

                if (connectorMessages.size() > 0) {
                    // Update the rotate map for each thread ID so destination connectors know to sleep
                    for (Long threadId : rotateThreadMap.keySet().toArray(new Long[rotateThreadMap.size()])) {
                        rotateThreadMap.put(threadId, true);
                    }
                }
            }

            Map<Long, ConnectorMessage> map = new LinkedHashMap<Long, ConnectorMessage>();

            for (ConnectorMessage connectorMessage : connectorMessages) {
                map.put(connectorMessage.getMessageId(), connectorMessage);
            }

            return map;
        } finally {
            dao.close();
        }
    }
}
