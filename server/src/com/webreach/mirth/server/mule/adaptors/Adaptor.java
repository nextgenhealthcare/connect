package com.webreach.mirth.server.mule.adaptors;

import java.util.Calendar;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DefaultXMLSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.UUIDGenerator;

public abstract class Adaptor {
	private Logger logger = Logger.getLogger(this.getClass());
	protected MessageObject messageObject;
	protected String source;
	protected Map properties;
	protected IXMLSerializer<String> serializer;
	private MessageObjectController messageObjectController = new MessageObjectController();
    private ConfigurationController configurationController = new ConfigurationController();
    
	public MessageObject getMessage(String source, String channelId, boolean encryptData, Map properties) throws AdaptorException {
		this.source = source;
        this.properties = properties;
        this.serializer = getSerializer(properties);
		messageObject = new MessageObject();
		messageObject.setId(UUIDGenerator.getUUID());
        messageObject.setServerId(configurationController.getServerId());
		messageObject.setChannelId(channelId);
		messageObject.setDateCreated(Calendar.getInstance());
		messageObject.setConnectorName("Source");
		messageObject.setEncrypted(encryptData);
		messageObject.setRawData(source);

		populateMessage();

		messageObject.setStatus(MessageObject.Status.RECEIVED);
		// messageObjectController.updateMessage(messageObject);
		return messageObject;
	}

	/**
	 * Converts a message for destination transformers
	 * @throws  
	 */
	public MessageObject convertMessage(MessageObject incomingMessageObject, String connectorName, String channelId, boolean encryptData, Map properties) throws AdaptorException {
		//The source is the encoded data
		this.messageObject = messageObjectController.cloneMessageObjectForBroadcast(incomingMessageObject, connectorName);
		this.source = this.messageObject.getRawData();
        this.properties = properties;
        this.serializer = getSerializer(properties);
		populateMessage();
		doConvertMessage();
		this.messageObject.setStatus(MessageObject.Status.RECEIVED);
		return this.messageObject;
	}

	protected void handleException(Throwable e) throws AdaptorException {
		logger.warn("error adapting message", e);
		messageObjectController.setError(messageObject, Constants.ERROR_301, "Error adapting message", e);
		throw new AdaptorException(e);
	}
	protected void populateMetadata(String source) throws SerializerException{
		Map<String, String> metadata = serializer.getMetadata();
		messageObject.setType(metadata.get("type"));
		messageObject.setVersion(metadata.get("version"));
		messageObject.setSource(metadata.get("source"));
	}
	protected MessageObject doConvertMessage() throws AdaptorException {
		return messageObject;
	}

	public IXMLSerializer<String> getSerializer(Map properties) {
		return new DefaultXMLSerializer();
	}

	protected abstract void populateMessage() throws AdaptorException;

}