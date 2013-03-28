/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.AbstractSortableTreeTableNode;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;

public class MessageBrowserTableNode extends AbstractSortableTreeTableNode {
    private final static int NUM_STATIC_COLUMNS = 10;

    private Object[] row;
    private Long messageId;
    private Integer metaDataId;
    private Boolean active;
    private Boolean processed;

    public MessageBrowserTableNode(Message message) {
        row = new Object[NUM_STATIC_COLUMNS];

        active = false;
        processed = message.isProcessed();
        row[0] = message.getMessageId();
        row[1] = "--";
        row[2] = "--";
        row[3] = null;
        row[4] = null;
        row[5] = null;
        row[6] = null;
        row[7] = null;
        row[8] = "--";
        row[9] = message.getImportId();
    }

    public MessageBrowserTableNode(Message message, int metaDataId, MessageBrowserTableModel model) {
        row = new Object[model.getColumnCount()];
        messageId = message.getMessageId();
        this.metaDataId = metaDataId;

        ConnectorMessage connectorMessage = message.getConnectorMessages().get(this.metaDataId);

        active = true;
        processed = message.isProcessed();

        if (connectorMessage.getMetaDataId() == 0) {
            row[0] = message.getMessageId();
            row[9] = message.getImportId();

            if (StringUtils.isEmpty(connectorMessage.getResponseErrors())) {
                row[7] = connectorMessage.getSendAttempts() > 0 ? "SENT" : "--";
            } else {
                row[7] = "ERROR";
            }
        } else {
            row[0] = null;
            row[7] = "--";
            row[9] = null;
        }

        row[1] = connectorMessage.getConnectorName();
        row[2] = connectorMessage.getStatus();
        row[3] = connectorMessage.getReceivedDate();
        row[4] = connectorMessage.getSendAttempts();
        row[5] = connectorMessage.getSendDate();
        row[6] = connectorMessage.getResponseDate();
        row[8] = message.getServerId();

        for (int i = NUM_STATIC_COLUMNS; i < model.getColumnCount(); i++) {
            row[i] = connectorMessage.getMetaDataMap().get(model.getColumnName(i).toUpperCase());
        }
    }

    @Override
    public Object getValueAt(int column) {
        return row[column];
    }

    @Override
    public int getColumnCount() {
        return row.length;
    }

    public Long getMessageId() {
        return messageId;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public Boolean isNodeActive() {
        return active;
    }

    public Boolean isProcessed() {
        return processed;
    }
}
