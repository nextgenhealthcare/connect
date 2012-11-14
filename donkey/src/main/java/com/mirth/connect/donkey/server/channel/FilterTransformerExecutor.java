/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Encryptor;
import com.mirth.connect.donkey.server.PassthruEncryptor;
import com.mirth.connect.donkey.server.channel.components.FilterTransformer;
import com.mirth.connect.donkey.util.ThreadUtils;

public class FilterTransformerExecutor {
    private DataType inbound;
    private DataType outbound;
    private FilterTransformer filterTransformer;
    private Encryptor encryptor = new PassthruEncryptor();

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

    protected void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
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
        String encodedContent;

        if (filterTransformer != null) {
            String serializedContent;

            if (connectorMessage.getProcessedRaw() == null) {
                serializedContent = inbound.getSerializer().toXML(connectorMessage.getRaw().getContent());
            } else {
                serializedContent = inbound.getSerializer().toXML(connectorMessage.getProcessedRaw().getContent());
            }

            connectorMessage.setTransformed(new MessageContent(connectorMessage.getChannelId(), connectorMessage.getMessageId(), connectorMessage.getMetaDataId(), ContentType.TRANSFORMED, serializedContent, encryptor.encrypt(serializedContent)));
            ThreadUtils.checkInterruptedStatus();

            if (!filterTransformer.doFilterTransform(connectorMessage)) {
                connectorMessage.setStatus(Status.FILTERED);
                return;
            }

            ThreadUtils.checkInterruptedStatus();
            encodedContent = outbound.getSerializer().fromXML(connectorMessage.getTransformed().getContent());
        } else {
            if (connectorMessage.getProcessedRaw() == null) {
                encodedContent = connectorMessage.getRaw().getContent();
            } else {
                encodedContent = connectorMessage.getProcessedRaw().getContent();
            }
        }

        connectorMessage.setEncoded(new MessageContent(connectorMessage.getChannelId(), connectorMessage.getMessageId(), connectorMessage.getMetaDataId(), ContentType.ENCODED, encodedContent, encryptor.encrypt(encodedContent)));
        connectorMessage.setStatus(Status.TRANSFORMED);
    }
}
