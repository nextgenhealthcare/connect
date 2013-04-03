/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import org.apache.commons.lang.StringUtils;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformer;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.ThreadUtils;

public class ResponseTransformerExecutor {
    private DataType inbound;
    private DataType outbound;
    private ResponseTransformer responseTransformer;

    public ResponseTransformerExecutor(DataType inbound, DataType outbound) {
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

    public ResponseTransformer getResponseTransformer() {
        return responseTransformer;
    }

    public void setResponseTransformer(ResponseTransformer responseTransformer) {
        this.responseTransformer = responseTransformer;
    }

    public void runResponseTransformer(DonkeyDao dao, ConnectorMessage connectorMessage, Response response, boolean queueEnabled, StorageSettings storageSettings, Serializer serializer) throws InterruptedException, DonkeyException {
        ThreadUtils.checkInterruptedStatus();
        String processedResponseContent;

        if (isActive(response)) {
            boolean wasResponseTransformedNull = connectorMessage.getResponseTransformed() == null;
            String responseTransformedContent = null;

            // Pre-transformation setup
            switch (inbound.getSerializationType()) {
                case RAW:
                    // Only the raw/processed raw content is used for the raw serialization type, so nothing needs to be done here
                    break;

                case XML:
                default:
                    responseTransformedContent = inbound.getSerializer().toXML(response.getMessage());
                    setResponseTransformedContent(connectorMessage, responseTransformedContent, inbound.getSerializationType());
                    break;
            }

            // Perform transformation
            try {
                responseTransformedContent = responseTransformer.doTransform(response, connectorMessage);
                setResponseTransformedContent(connectorMessage, responseTransformedContent, outbound.getSerializationType());
            } catch (DonkeyException e) {
                throw e;
            } finally {
                if (storageSettings.isStoreResponseTransformed()) {
                    ThreadUtils.checkInterruptedStatus();

                    if (connectorMessage.getResponseTransformed() != null) {
                        if (!wasResponseTransformedNull) {
                            dao.storeMessageContent(connectorMessage.getResponseTransformed());
                        } else {
                            dao.insertMessageContent(connectorMessage.getResponseTransformed());
                        }
                    }
                }
            }

            connectorMessage.setResponseError(response.fixStatus(queueEnabled));

            // Post transformation: Determine what the encoded content should be set to.
            switch (outbound.getSerializationType()) {
                case RAW:
                    processedResponseContent = responseTransformedContent;
                    break;

                case XML:
                default:
                    // Convert the response transformed data to the outbound data type
                    processedResponseContent = outbound.getSerializer().fromXML(responseTransformedContent);
                    break;
            }

            setProcessedResponse(dao, response, connectorMessage, processedResponseContent, storageSettings, serializer);

        } else {
            if (StringUtils.isNotEmpty(response.getMessage())) {
                /*
                 * Since this condition can only occur if the inbound and
                 * outbound datatypes are the same, it is safe to pass the
                 * outbound serializer to the inbound serializer so that it can
                 * compare/use the properties from both. The purpose of this
                 * method is to allow the optimization of not serializing, but
                 * still modifying the message in certain circumstances. It
                 * should NOT be used anywhere other than transformers.
                 */
                String content = inbound.getSerializer().transformWithoutSerializing(response.getMessage(), outbound.getSerializer());
                // transformWithoutSerializing should return null if it has no effect.
                if (content != null) {
                    processedResponseContent = content;
                    setProcessedResponse(dao, response, connectorMessage, processedResponseContent, storageSettings, serializer);
                }
            }
        }
    }

    public boolean isActive(Response response) {
        return responseTransformer != null && (StringUtils.isNotEmpty(response.getMessage()) || inbound.getSerializationType() == SerializationType.RAW);
    }

    /**
     * 
     * @return Returns whether the response transformed message content object
     *         was null
     */
    private void setResponseTransformedContent(ConnectorMessage connectorMessage, String transformedContent, SerializationType serializationType) {
        if (connectorMessage.getResponseTransformed() == null) {
            connectorMessage.setResponseTransformed(new MessageContent(connectorMessage.getChannelId(), connectorMessage.getMessageId(), connectorMessage.getMetaDataId(), ContentType.RESPONSE_TRANSFORMED, transformedContent, serializationType.toString(), false));
        } else {
            connectorMessage.getResponseTransformed().setDataType(serializationType.toString());
            connectorMessage.getResponseTransformed().setContent(transformedContent);
        }
    }

    private void setProcessedResponse(DonkeyDao dao, Response response, ConnectorMessage connectorMessage, String processedResponseContent, StorageSettings storageSettings, Serializer serializer) throws InterruptedException {
        response.setMessage(processedResponseContent);

        // Store the processed response in the message
        String responseString = serializer.serialize(response);
        MessageContent processedResponse = new MessageContent(connectorMessage.getChannelId(), connectorMessage.getMessageId(), connectorMessage.getMetaDataId(), ContentType.PROCESSED_RESPONSE, responseString, outbound.getType(), false);

        if (storageSettings.isStoreProcessedResponse()) {
            ThreadUtils.checkInterruptedStatus();

            if (connectorMessage.getProcessedResponse() != null) {
                dao.storeMessageContent(processedResponse);
            } else {
                dao.insertMessageContent(processedResponse);
            }
        }
        connectorMessage.setProcessedResponse(processedResponse);
    }
}
