/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.List;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Encryptor;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class ConnectorMessageQueueDataSource implements PersistedBlockingQueueDataSource<ConnectorMessage> {
    private DonkeyDaoFactory daoFactory;
    private String channelId;
    private int metaDataId;
    private Status status;
    private Encryptor encryptor;
    private MessageController messageController = MessageController.getInstance();
    private boolean rotate;
    private Long maxMessageId = null;
    private Long minMessageId = null;

    public ConnectorMessageQueueDataSource(String channelId, int metaDataId, Status status, boolean rotate, DonkeyDaoFactory daoFactory, Encryptor encryptor) {
        this.channelId = channelId;
        this.metaDataId = metaDataId;
        this.status = status;
        this.rotate = rotate;
        this.daoFactory = daoFactory;
        this.encryptor = encryptor;
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
    
    @Override
    public boolean wasItemRotated() {
    	return (rotate && minMessageId != null && minMessageId != 0L);
    }
    
    @Override
    public void rotateItem(Object o) {
    	this.minMessageId = ((ConnectorMessage) o).getMessageId() + 1;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public int getSize() {
        DonkeyDao dao = getDaoFactory().getDao();

        try {
        	if (rotate) {
        		minMessageId = 0L;
        		maxMessageId = dao.getConnectorMessageMaxMessageId(channelId, metaDataId, status);
        	} else {
        		minMessageId = null;
        		maxMessageId = null;
        	}
        	
            return dao.getConnectorMessageCount(channelId, metaDataId, status);
        } finally {
            dao.close();
        }
    }

    @Override
    public List<ConnectorMessage> getItems(int offset, int limit) {
        DonkeyDao dao = daoFactory.getDao();

        try {
            List<ConnectorMessage> connectorMessages = dao.getConnectorMessages(channelId, metaDataId, status, offset, limit, minMessageId, maxMessageId);
            
            if (rotate && connectorMessages.size() == 0) {
                minMessageId = 0L;
                maxMessageId = dao.getConnectorMessageMaxMessageId(channelId, metaDataId, status);
                connectorMessages = dao.getConnectorMessages(channelId, metaDataId, status, offset, limit, minMessageId, maxMessageId);
            }
            
            for (ConnectorMessage connectorMessage : connectorMessages) {
                messageController.decryptConnectorMessage(connectorMessage, encryptor);
            }
            
            return connectorMessages;
        } finally {
            dao.close();
        }
    }
}
