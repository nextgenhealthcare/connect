/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import com.mirth.connect.client.ui.AbstractSortableTreeTableNode;
import com.mirth.connect.donkey.model.message.ConnectorMessage;

public class MessageBrowserTableNode extends AbstractSortableTreeTableNode {
    private final static int NUM_STATIC_COLUMNS = 4;

    private Object[] row;
    private Long messageId;
    private Integer metaDataId;
    private Boolean active;

    public MessageBrowserTableNode(Long messageId) {
        row = new Object[NUM_STATIC_COLUMNS];

        active = false;
        row[0] = messageId;
        row[1] = "--";
        row[2] = "--";
        row[3] = null;
    }

    public MessageBrowserTableNode(ConnectorMessage connectorMessage, MessageBrowserTableModel model) {
        row = new Object[model.getColumnCount()];

        messageId = connectorMessage.getMessageId();
        metaDataId = connectorMessage.getMetaDataId();
        active = true;

        if (connectorMessage.getMetaDataId() == 0) {
            row[0] = messageId;
        } else {
            row[0] = null;
        }

        row[1] = connectorMessage.getConnectorName();
        row[2] = connectorMessage.getStatus();
        row[3] = connectorMessage.getDateCreated();

        for (int i = NUM_STATIC_COLUMNS; i < model.getColumnCount(); i++) {
            row[i] = connectorMessage.getMetaDataMap().get(model.getColumnName(i).toLowerCase());
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
}
