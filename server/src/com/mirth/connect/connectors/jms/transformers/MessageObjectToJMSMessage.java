/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms.transformers;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.umo.transformer.TransformerException;

import com.mirth.connect.connectors.jms.JmsConnector;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class MessageObjectToJMSMessage extends AbstractJmsTransformer {
    private static transient Log logger = LogFactory.getLog(MessageObjectToJMSMessage.class);
    private static final long serialVersionUID = 1L;
    private JmsConnector connector;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    public MessageObjectToJMSMessage(JmsConnector connector){
    	this.connector = connector;
    }
	public Object doTransform(Object src) throws TransformerException {
		
		if (src instanceof MessageObject) {
			MessageObject messageObject = (MessageObject) src;
			if (messageObject.getStatus().equals(MessageObject.Status.FILTERED)){
				return null;
			}
			String template = replacer.replaceValues(connector.getTemplate(), messageObject);
			Message message = transformToMessage(template);
            try {
            	message.setStringProperty("MIRTH_MESSAGE_ID", messageObject.getId());
            } catch (JMSException e) {
                //Various Jms servers have slightly different rules to what can be set as an object property on the message
                //As such we have to take a hit n' hope approach
                if(logger.isDebugEnabled()) logger.debug("Unable to set property '" + encodeHeader("MIRTH_MESSAGE_ID") + "': " + e.getMessage());
            }
			return message;
		}	
		return null;
	}
}
