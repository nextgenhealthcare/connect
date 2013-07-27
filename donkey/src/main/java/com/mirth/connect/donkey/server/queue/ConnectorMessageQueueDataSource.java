/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public ConnectorMessageQueueDataSource(String channelId, String serverId, int metaDataId, Status status, boolean rotate, DonkeyDaoFactory daoFactory) {
        this.channelId = channelId;
        this.serverId = serverId;
        this.metaDataId = metaDataId;
        this.status = status;
        this.rotate = rotate;
        this.daoFactory = daoFactory;
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

    public void setLastItem(ConnectorMessage connectorMessage) {
        rotatedMessageId = connectorMessage.getMessageId() + 1;

        if (isQueueRotated()) {
            rotateQueue();
        }
    }

    public void rotateQueue() {
        minMessageId = rotatedMessageId;
    }

    public boolean isQueueRotated() {
        return (rotate && minMessageId != null && minMessageId != 0);
    };

    public int getSize() {
        DonkeyDao dao = getDaoFactory().getDao();

        try {
            if (rotate) {
                minMessageId = 0L;
                maxMessageId = dao.getConnectorMessageMaxMessageId(channelId, serverId, metaDataId, status);
            } else {
                minMessageId = null;
                maxMessageId = null;
            }

            return dao.getConnectorMessageCount(channelId, serverId, metaDataId, status);
        } finally {
            dao.close();
        }
    }

    public Map<Long, ConnectorMessage> getItems(int offset, int limit) {
        DonkeyDao dao = daoFactory.getDao();

        try {
            List<ConnectorMessage> connectorMessages = dao.getConnectorMessages(channelId, serverId, metaDataId, status, offset, limit, minMessageId, maxMessageId);

            if (rotate && connectorMessages.size() == 0) {
                minMessageId = 0L;
                maxMessageId = dao.getConnectorMessageMaxMessageId(channelId, serverId, metaDataId, status);
                connectorMessages = dao.getConnectorMessages(channelId, serverId, metaDataId, status, offset, limit, minMessageId, maxMessageId);
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
