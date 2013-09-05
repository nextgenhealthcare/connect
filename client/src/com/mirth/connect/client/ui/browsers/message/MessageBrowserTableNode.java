/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import com.mirth.connect.client.ui.AbstractSortableTreeTableNode;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;

public class MessageBrowserTableNode extends AbstractSortableTreeTableNode {
    private Object[] row;
    private Long messageId;
    private Integer metaDataId;
    private Boolean active;
    private Boolean processed;

    public MessageBrowserTableNode(int staticColumnCount, Message message) {
        row = new Object[staticColumnCount];

        active = false;
        processed = message.isProcessed();
        row[MessageBrowser.ID_COLUMN] = message.getMessageId();
        row[MessageBrowser.CONNECTOR_COLUMN] = null;
        row[MessageBrowser.STATUS_COLUMN] = null;
        row[MessageBrowser.ORIGINAL_RECEIVED_DATE_COLUMN] = null;
        row[MessageBrowser.RECEIVED_DATE_COLUMN] = null;
        row[MessageBrowser.SEND_ATTEMPTS_COLUMN] = null;
        row[MessageBrowser.SEND_DATE_COLUMN] = null;
        row[MessageBrowser.RESPONSE_DATE_COLUMN] = null;
        row[MessageBrowser.ERRORS_COLUMN] = null;
        row[MessageBrowser.SERVER_ID_COLUMN] = null;
        row[MessageBrowser.ORIGINAL_ID_COLUMN] = message.getOriginalId();
        row[MessageBrowser.IMPORT_ID_COLUMN] = message.getImportId();
        row[MessageBrowser.IMPORT_CHANNEL_ID_COLUMN] = message.getImportChannelId();
        row[MessageBrowser.ORIGINAL_SERVER_ID_COLUMN] = null;
    }

    public MessageBrowserTableNode(int staticColumnCount, Message message, int metaDataId, MessageBrowserTableModel model) {
        row = new Object[model.getColumnCount()];
        messageId = message.getMessageId();
        this.metaDataId = metaDataId;

        ConnectorMessage connectorMessage = message.getConnectorMessages().get(this.metaDataId);

        active = true;
        processed = message.isProcessed();

        row[MessageBrowser.ID_COLUMN] = connectorMessage.getMetaDataId() == 0 ? message.getMessageId() : null;
        row[MessageBrowser.CONNECTOR_COLUMN] = connectorMessage.getConnectorName();
        row[MessageBrowser.STATUS_COLUMN] = connectorMessage.getStatus();
        row[MessageBrowser.ORIGINAL_RECEIVED_DATE_COLUMN] = message.getReceivedDate();
        row[MessageBrowser.RECEIVED_DATE_COLUMN] = connectorMessage.getReceivedDate();
        row[MessageBrowser.SEND_ATTEMPTS_COLUMN] = connectorMessage.getSendAttempts();
        row[MessageBrowser.SEND_DATE_COLUMN] = connectorMessage.getSendDate();
        row[MessageBrowser.RESPONSE_DATE_COLUMN] = connectorMessage.getResponseDate();
        row[MessageBrowser.ERRORS_COLUMN] = getErrorString(connectorMessage);
        row[MessageBrowser.SERVER_ID_COLUMN] = connectorMessage.getServerId();
        row[MessageBrowser.ORIGINAL_ID_COLUMN] = connectorMessage.getMetaDataId() == 0 ? message.getOriginalId() : null;
        row[MessageBrowser.IMPORT_ID_COLUMN] = connectorMessage.getMetaDataId() == 0 ? message.getImportId() : null;
        row[MessageBrowser.IMPORT_CHANNEL_ID_COLUMN] = connectorMessage.getMetaDataId() == 0 ? message.getImportChannelId() : null;
        row[MessageBrowser.ORIGINAL_SERVER_ID_COLUMN] = message.getServerId();

        for (int i = staticColumnCount; i < model.getColumnCount(); i++) {
            row[i] = connectorMessage.getMetaDataMap().get(model.getColumnName(i).toUpperCase());
        }
    }

    private String getErrorString(ConnectorMessage connectorMessage) {
        String error = null;

        if (connectorMessage.containsError(ContentType.PROCESSING_ERROR)) {
            error = "Processing";
        }

        if (connectorMessage.containsError(ContentType.RESPONSE_ERROR)) {
            if (error != null) {
                return "Multiple";
            }

            error = "Response";
        }

        if (connectorMessage.containsError(ContentType.POSTPROCESSOR_ERROR)) {
            if (error != null) {
                return "Multiple";
            }

            error = "Postprocessor";
        }

        return error;
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
