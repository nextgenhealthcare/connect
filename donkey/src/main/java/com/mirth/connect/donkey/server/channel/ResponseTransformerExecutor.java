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
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.Encryptor;
import com.mirth.connect.donkey.server.PassthruEncryptor;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformer;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.util.ThreadUtils;

public class ResponseTransformerExecutor {
    private DataType inbound;
    private DataType outbound;
    private ResponseTransformer responseTransformer;
    private Encryptor encryptor = new PassthruEncryptor();

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
    
    protected void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

	public ResponseTransformer getResponseTransformer() {
		return responseTransformer;
	}

	public void setResponseTransformer(ResponseTransformer responseTransformer) {
		this.responseTransformer = responseTransformer;
	}
    
    public void runResponseTransformer(DonkeyDao dao, ConnectorMessage connectorMessage, Response response, boolean queueEnabled, StorageSettings storageSettings) throws InterruptedException, DonkeyException {
    	ThreadUtils.checkInterruptedStatus();
        String processedResponseContent;
        
        if (responseTransformer != null){
        	
	        // Convert the content to xml
	        String serializedContent = inbound.getSerializer().toXML(response.getMessage());
	        
	        boolean isResponseTransformedNull = connectorMessage.getResponseTransformed() == null;
	        connectorMessage.setResponseTransformed(new MessageContent(connectorMessage.getChannelId(), connectorMessage.getMessageId(), connectorMessage.getMetaDataId(), ContentType.RESPONSE_TRANSFORMED, serializedContent, "XML", encryptor.encrypt(serializedContent)));
	        	    	
	        // Perform transformation
	        try{
		        responseTransformer.doTransform(response, connectorMessage);
	        } catch (DonkeyException e){
	        	throw e;
	        } finally{
	        	  if (storageSettings.isStoreResponseTransformed()) {
	  	            ThreadUtils.checkInterruptedStatus();

	  	            if (!isResponseTransformedNull) {
	  	                dao.storeMessageContent(connectorMessage.getResponseTransformed());
	  	            } else {
	  	                dao.insertMessageContent(connectorMessage.getResponseTransformed());
	  	            }
	  	        }
	        }
	        
	        response.fixStatus(queueEnabled);
	        
	        // Convert the response transformed data to the outbound data type
	        processedResponseContent = outbound.getSerializer().fromXML(connectorMessage.getResponseTransformed().getContent());

        	setProcessedResponse(dao, response, connectorMessage, processedResponseContent, storageSettings);
	        
        } else {
	        response.fixStatus(queueEnabled);
	        
        	if (response.getMessage() != null) {
	        	// Since this condition can only occur if the inbound and outbound datatypes are the same, it is safe to pass the outbound serializer to the inbound serializer 
	            // so that it can compare/use the properties from both. The purpose of this method is to allow the optimization of not serializing, but still modifying the message in certain circumstances.
	            // It should NOT be used anywhere other than here.
	        	String content = inbound.getSerializer().transformWithoutSerializing(response.getMessage(), outbound.getSerializer());
	        	if (content != null){
	            	processedResponseContent = content;
	            	setProcessedResponse(dao, response, connectorMessage, processedResponseContent, storageSettings);
	        	}
        	}
        }
	}
    
    private void setProcessedResponse(DonkeyDao dao, Response response, ConnectorMessage connectorMessage, String processedResponseContent, StorageSettings storageSettings) throws InterruptedException{
        response.setMessage(processedResponseContent);
                
        // Store the processed response in the message
        String responseString = response.toString();
        MessageContent processedResponse = new MessageContent(connectorMessage.getChannelId(), connectorMessage.getMessageId(), connectorMessage.getMetaDataId(), ContentType.PROCESSED_RESPONSE, responseString, outbound.getType(), encryptor.encrypt(responseString));
        
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
