/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.mule.adaptors;

import java.util.Calendar;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DefaultXMLSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.UUIDGenerator;

public abstract class Adaptor {
    private Logger logger = Logger.getLogger(this.getClass());
    protected MessageObject messageObject;
    protected String source;
    protected Map properties;
    protected IXMLSerializer<String> serializer;
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();

    public MessageObject getMessage(String source, String channelId, boolean encryptData, Map properties, boolean emptyFilterAndTransformer, Map context) throws AdaptorException {
        this.source = source;
        this.properties = properties;
        this.serializer = getSerializer(properties);
        messageObject = new MessageObject();

        if ((context.get("replace") != null) && context.get("replace").equals("true")) {
            messageObject.getContext().put("replace", "true");
        }
        
        if (context.get("messageId") != null) {
            messageObject.setId((String) context.get("messageId"));
        } else {
            messageObject.setId(UUIDGenerator.getUUID());
        }

        messageObject.setServerId(configurationController.getServerId());
        messageObject.setChannelId(channelId);
        messageObject.setDateCreated(Calendar.getInstance());
        messageObject.setConnectorName("Source");
        messageObject.setEncrypted(encryptData);
        messageObject.setRawData(source);

        populateMessage(emptyFilterAndTransformer);

        messageObject.setStatus(MessageObject.Status.RECEIVED);
        // messageObjectController.updateMessage(messageObject);
        return messageObject;
    }

    /**
     * Converts a message for destination transformers
     * 
     * @throws
     */
    public MessageObject convertMessage(MessageObject incomingMessageObject, String connectorName, String channelId, boolean encryptData, Map properties, boolean emptyFilterAndTransformer) throws AdaptorException {
        // The source is the encoded data
        this.messageObject = messageObjectController.cloneMessageObjectForBroadcast(incomingMessageObject, connectorName);
        this.source = this.messageObject.getRawData();
        this.properties = properties;
        this.serializer = getSerializer(properties);
        populateMessage(emptyFilterAndTransformer);
        doConvertMessage(emptyFilterAndTransformer);
        this.messageObject.setStatus(MessageObject.Status.RECEIVED);
        return this.messageObject;
    }

    protected void handleException(Throwable e) throws AdaptorException {
        logger.warn("error adapting message", e);
        messageObjectController.setError(messageObject, Constants.ERROR_301, "Error adapting message", e, null);
        alertController.sendAlerts(messageObject.getChannelId(), Constants.ERROR_301, "Error adapting message", e);
        throw new AdaptorException(e);
    }

    protected void populateMetadataFromXML(String source) throws SerializerException {
        Map<String, String> metadata = serializer.getMetadataFromXML(source);
        messageObject.setType(metadata.get("type"));
        messageObject.setVersion(metadata.get("version"));
        messageObject.setSource(metadata.get("source"));
    }

    protected void populateMetadataFromEncoded(String source) throws SerializerException {
        Map metadata = serializer.getMetadataFromEncoded(source);
        messageObject.setType((String) metadata.get("type"));
        messageObject.setVersion((String) metadata.get("version"));
        messageObject.setSource((String) metadata.get("source"));
    }

    protected MessageObject doConvertMessage(boolean emptyFilterAndTransformer) throws AdaptorException {
        return messageObject;
    }

    public IXMLSerializer<String> getSerializer(Map properties) {
        return new DefaultXMLSerializer(properties);
    }

    protected abstract void populateMessage(boolean flag) throws AdaptorException;

}