/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.webreach.mirth.connectors.jms;

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.mule.config.MuleProperties;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;
import org.mule.umo.provider.UniqueIdNotSupportedException;

/**
 * <code>JmsMessageAdapter</code> allows a <code>MuleEvent</code> to access
 * the properties and payload of a JMS Message in a uniform way. The
 * JmsMessageAdapter expects a message of type <i>javax.jms.Message</i> and
 * will throw an IllegalArgumentException if the source message type is not
 * compatible. The JmsMessageAdapter should be suitable for all JMS Connector
 * implementations.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.12 $
 */
public class JmsMessageAdapter extends AbstractMessageAdapter {

	private Message message = null;

	public JmsMessageAdapter(Object message) throws MessagingException {
		setMessage(message);
	}

	/**
	 * Converts the message implementation into a String representation
	 * 
	 * @return String representation of the message
	 * @throws Exception
	 *             Implemetation may throw an endpoint specific exception
	 */
	public String getPayloadAsString() throws Exception {
		return new String(getPayloadAsBytes());
	}

	/**
	 * Converts the message implementation into a String representation
	 * 
	 * @return String representation of the message
	 * @throws Exception
	 *             Implemetation may throw an endpoint specific exception
	 */
	public byte[] getPayloadAsBytes() throws Exception {
		return JmsMessageUtils.getBytesFromMessage(message);
	}

	/**
	 * @return the current message
	 */
	public Object getPayload() {
		return message;
	}

	/**
	 * @param message
	 *            new value for the message
	 */
	private void setMessage(Object message) throws MessagingException {
		if (message instanceof Message) {
			this.message = (Message) message;
		} else {
			throw new MessageTypeNotSupportedException(message, getClass());
		}

		try {
			if (this.message.getJMSCorrelationID() != null)
				properties.put(JmsConstants.JMS_CORRELATION_ID, this.message.getJMSCorrelationID().toString());
		} catch (JMSException e) {
		}
		try {
			Integer mode = new Integer(this.message.getJMSDeliveryMode());
			if (mode != null)
				properties.put(JmsConstants.JMS_DELIVERY_MODE, mode.toString());
		} catch (JMSException e) {
		}
		try {
			if (this.message.getJMSDestination() != null)
				properties.put(JmsConstants.JMS_DESTINATION, this.message.getJMSDestination().toString());
		} catch (JMSException e) {
		}

		try {
			Long expiration = new Long(this.message.getJMSExpiration());
			if (expiration != null)
				properties.put(JmsConstants.JMS_EXPIRATION, expiration.toString());
		} catch (JMSException e) {
		}

		try {
			if (this.message.getJMSMessageID() != null)
				properties.put(JmsConstants.JMS_MESSAGE_ID, this.message.getJMSMessageID().toString());
		} catch (JMSException e) {
		}

		try {
			Integer priority = new Integer(this.message.getJMSPriority());
			if (priority != null)
				properties.put(JmsConstants.JMS_PRIORITY, priority.toString());
		} catch (JMSException e) {
		}

		try {
			Boolean delivered = Boolean.valueOf(this.message.getJMSRedelivered());
			if (delivered != null)
				properties.put(JmsConstants.JMS_REDELIVERED, delivered.toString());
		} catch (JMSException e) {
		}
		try {
			if (this.message.getJMSReplyTo() != null)
				properties.put(JmsConstants.JMS_REPLY_TO, this.message.getJMSReplyTo().toString());
		} catch (JMSException e) {
		}
		try {
			Long timestamp = new Long(this.message.getJMSTimestamp());
			if (timestamp != null)
				properties.put(JmsConstants.JMS_TIMESTAMP, timestamp.toString());
		} catch (JMSException e) {
		}
		try {
			if (this.message.getJMSType() != null)
				properties.put(JmsConstants.JMS_TYPE, this.message.getJMSType().toString());
		} catch (JMSException e) {
		}

		try {
			Enumeration e = this.message.getPropertyNames();

			String key;
			while (e.hasMoreElements()) {
				key = (String) e.nextElement();
				try {
					properties.put(key, this.message.getObjectProperty(key));
				} catch (JMSException e1) {
				}
			}
		} catch (JMSException e1) {
		}
	}

	public String getUniqueId() throws UniqueIdNotSupportedException {
		return (String) properties.get(JmsConstants.JMS_MESSAGE_ID);
	}

	/**
	 * Sets a correlationId for this message. The correlation Id can be used by
	 * components in the system to manage message relations <p/> transport
	 * protocol. As such not all messages will support the notion of a
	 * correlationId i.e. tcp or file. In this situation the correlation Id is
	 * set as a property of the message where it's up to developer to keep the
	 * association with the message. For example if the message is serialised to
	 * xml the correlationId will be available in the message.
	 * 
	 * @param id
	 *            the Id reference for this relationship
	 */
	public void setCorrelationId(String id) {
		properties.put(JmsConstants.JMS_CORRELATION_ID, id);
	}

	/**
	 * Sets a correlationId for this message. The correlation Id can be used by
	 * components in the system to manage message relations. <p/> The
	 * correlationId is associated with the message using the underlying
	 * transport protocol. As such not all messages will support the notion of a
	 * correlationId i.e. tcp or file. In this situation the correlation Id is
	 * set as a property of the message where it's up to developer to keep the
	 * association with the message. For example if the message is serialised to
	 * xml the correlationId will be available in the message.
	 * 
	 * @return the correlationId for this message or null if one hasn't been set
	 */
	public String getCorrelationId() {
		return (String) properties.get(JmsConstants.JMS_CORRELATION_ID);
	}

	/**
	 * Sets a replyTo address for this message. This is useful in an
	 * asynchronous environment where the caller doesn't wait for a response and
	 * the response needs to be routed somewhere for further processing. The
	 * value of this field can be any valid endpointUri url.
	 * 
	 * @param replyTo
	 *            the endpointUri url to reply to
	 */
	public void setReplyTo(Object replyTo) {
		if (replyTo instanceof Destination) {
			setProperty(JmsConstants.JMS_REPLY_TO, replyTo);
		} else {
			super.setReplyTo(replyTo);
		}
	}

	/**
	 * Sets a replyTo address for this message. This is useful in an
	 * asynchronous environment where the caller doesn't wait for a response and
	 * the response needs to be routed somewhere for further processing. The
	 * value of this field can be any valid endpointUri url.
	 * 
	 * @return the endpointUri url to reply to or null if one has not been set
	 */
	public Object getReplyTo() {
		Object replyTo = properties.get(JmsConstants.JMS_REPLY_TO);
		if (replyTo == null) {
			replyTo = properties.get(MuleProperties.MULE_REPLY_TO_PROPERTY);
		}
		return replyTo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.UMOMessageAdapter#setProperty(java.lang.Object,
	 *      java.lang.Object)
	 */
	public void setProperty(Object key, Object value) {
		// if ("JMSReplyTo".equals(key)) {
		// setReplyTo(value);
		// }
		super.setProperty(key, value);
	}
}
