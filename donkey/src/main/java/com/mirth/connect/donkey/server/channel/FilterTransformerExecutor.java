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
        String content;
        String encodedContent;
        
        // If there is no processed raw content (no preprocessor step), use the raw instead.
        if (connectorMessage.getProcessedRaw() == null) {
        	content = connectorMessage.getRaw().getContent();
        } else {
        	content = connectorMessage.getProcessedRaw().getContent();
        }
        
        if (filterTransformer != null) {
            // Convert the content to xml
            String serializedContent = inbound.getSerializer().toXML(content);

            connectorMessage.setTransformed(new MessageContent(connectorMessage.getChannelId(), connectorMessage.getMessageId(), connectorMessage.getMetaDataId(), ContentType.TRANSFORMED, serializedContent, "XML", encryptor.encrypt(serializedContent)));
            ThreadUtils.checkInterruptedStatus();

            // Perform the filter and transformation
            if (!filterTransformer.doFilterTransform(connectorMessage)) {
                connectorMessage.setStatus(Status.FILTERED);
                return;
            }

            ThreadUtils.checkInterruptedStatus();
            // Convert the transformed content to the outbound data type
            encodedContent = outbound.getSerializer().fromXML(connectorMessage.getTransformed().getContent());
        } else {
            // Since this condition can only occur if the inbound and outbound datatypes are the same, it is safe to pass the outbound serializer to the inbound serializer 
            // so that it can compare/use the properties from both. The purpose of this method is to allow the optimization of not serializing, but still modifying the message in certain circumstances.
            // It should NOT be used anywhere other than here.
        	content = inbound.getSerializer().transformWithoutSerializing(content, outbound.getSerializer());
        	
            encodedContent = content;
        }

        connectorMessage.setEncoded(new MessageContent(connectorMessage.getChannelId(), connectorMessage.getMessageId(), connectorMessage.getMetaDataId(), ContentType.ENCODED, encodedContent, outbound.getType(), encryptor.encrypt(encodedContent)));
        connectorMessage.setStatus(Status.TRANSFORMED);
    }
}
