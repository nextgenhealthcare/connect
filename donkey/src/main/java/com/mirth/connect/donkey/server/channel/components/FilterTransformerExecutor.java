/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel.components;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.util.ThreadUtils;

public class FilterTransformerExecutor {
    private DataType inbound;
    private DataType outbound;
    private FilterTransformer filterTransformer;

    public FilterTransformerExecutor(DataType inbound, DataType outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    public DataType getInbound() {
        return inbound;
    }

    public void setInbound(DataType inbound) {
        this.inbound = inbound;
    }

    public DataType getOutbound() {
        return outbound;
    }

    public void setOutbound(DataType outbound) {
        this.outbound = outbound;
    }

    public FilterTransformer getFilterTransformer() {
        return filterTransformer;
    }

    public void setFilterTransformer(FilterTransformer filterTransformer) {
        this.filterTransformer = filterTransformer;
    }

    /**
     * Takes a ConnectorMessage and runs any filtering or transforming logic
     * against it. Sets the transformed content and encoded content on
     * connectorMessage and updates it's status to either Status.FILTERED,
     * Status.TRANSFORMED or Status.ERROR
     * 
     * @return The new status that was set for connectorMessage
     *         (Status.FILTERED, Status.TRANSFORMED or Status.ERROR)
     * @throws InterruptedException
     */
    public void processConnectorMessage(ConnectorMessage connectorMessage) throws InterruptedException, DonkeyException {
        ThreadUtils.checkInterruptedStatus();
        String encodedMessage;

        if (filterTransformer != null) {
            String serializedMessage;

            if (connectorMessage.getProcessedRaw() == null) {
                serializedMessage = inbound.getSerializer().toXML(connectorMessage.getRaw().getContent());
            } else {
                serializedMessage = inbound.getSerializer().toXML(connectorMessage.getProcessedRaw().getContent());
            }

            MessageContent transformedContent = new MessageContent();
            transformedContent.setChannelId(connectorMessage.getChannelId());
            transformedContent.setMessageId(connectorMessage.getMessageId());
            transformedContent.setMetaDataId(connectorMessage.getMetaDataId());
            transformedContent.setContentType(ContentType.TRANSFORMED);
            transformedContent.setContent(serializedMessage);
            connectorMessage.setTransformed(transformedContent);

            ThreadUtils.checkInterruptedStatus();

            if (!filterTransformer.doFilterTransform(connectorMessage)) {
                connectorMessage.setStatus(Status.FILTERED);
                return;
            }

            ThreadUtils.checkInterruptedStatus();
            encodedMessage = outbound.getSerializer().fromXML(connectorMessage.getTransformed().getContent());
        } else {
            if (connectorMessage.getProcessedRaw() == null) {
                encodedMessage = connectorMessage.getRaw().getContent();
            } else {
                encodedMessage = connectorMessage.getProcessedRaw().getContent();
            }
        }

        MessageContent encodedContent = new MessageContent();
        encodedContent.setChannelId(connectorMessage.getChannelId());
        encodedContent.setMessageId(connectorMessage.getMessageId());
        encodedContent.setMetaDataId(connectorMessage.getMetaDataId());
        encodedContent.setContentType(ContentType.ENCODED);
        encodedContent.setContent(encodedMessage);

        connectorMessage.setEncoded(encodedContent);
        connectorMessage.setStatus(Status.TRANSFORMED);
    }
}
