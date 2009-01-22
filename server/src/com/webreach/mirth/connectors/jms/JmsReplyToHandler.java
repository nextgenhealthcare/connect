/*
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/JmsReplyToHandler.java,v 1.16 2005/09/30 15:11:12 rossmason Exp $
 * $Revision: 1.16 $
 * $Date: 2005/09/30 15:11:12 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.jms;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.mule.impl.model.AbstractComponent;
import org.mule.providers.DefaultReplyToHandler;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.transformer.UMOTransformer;

/**
 * <code>JmsReplyToHandler</code> will process a Jms replyTo or hand off to
 * the defualt replyTo handler if the replyTo is a url
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.16 $
 */
public class JmsReplyToHandler extends DefaultReplyToHandler
{
    private JmsConnector connector;

    public JmsReplyToHandler(JmsConnector connector, UMOTransformer transformer)
    {
        super(transformer);
        this.connector = connector;
    }

    public void processReplyTo(UMOEvent event, UMOMessage returnMessage, Object replyTo) throws UMOException
    {
        Destination replyToDestination = null;
        MessageProducer replyToProducer = null;
        Session session = null;
        try {
            // now we need to send the response
            if (replyTo instanceof Destination) {
                replyToDestination = (Destination) replyTo;
            }
            if (replyToDestination == null) {
                super.processReplyTo(event, returnMessage, replyTo);
                return;
            }
            Object payload = returnMessage.getPayload();
            if (getTransformer() != null) {
                getTransformer().setEndpoint(getEndpoint(event, "jms://temporary"));
                payload = getTransformer().transform(payload);
            }
            session = connector.getSession(false, replyToDestination instanceof Topic);
            Message replyToMessage = JmsMessageUtils.getMessageForObject(payload, session);

            replyToMessage.setJMSReplyTo(null);
            if (logger.isDebugEnabled()) {
                logger.debug("Sending jms reply to: " + replyToDestination + "("
                        + replyToDestination.getClass().getName() + ")");
            }
            replyToProducer = connector.getJmsSupport().createProducer(session, replyToDestination);

            // QoS support
            String ttlString = (String) event.removeProperty("TimeToLive");
            String priorityString = (String) event.removeProperty("Priority");
            String persistentDeliveryString = (String) event.removeProperty("PersistentDelivery");

            if (ttlString == null && priorityString == null && persistentDeliveryString == null) {
                connector.getJmsSupport().send(replyToProducer, replyToMessage);
            } else {
                long ttl = Message.DEFAULT_TIME_TO_LIVE;
                int priority = Message.DEFAULT_PRIORITY;
                boolean persistent = Message.DEFAULT_DELIVERY_MODE == DeliveryMode.PERSISTENT;

                if (ttlString != null)
                    ttl = Long.parseLong(ttlString);
                if (priorityString != null)
                    priority = Integer.parseInt(priorityString);
                if (persistentDeliveryString != null)
                    persistent = Boolean.valueOf(persistentDeliveryString).booleanValue();

                connector.getJmsSupport().send(replyToProducer, replyToMessage, persistent, priority, ttl);
            }

            // connector.getJmsSupport().send(replyToProducer, replyToMessage,
            // replyToDestination);
            logger.info("Reply Message sent to: " + replyToDestination);
            ((AbstractComponent) event.getComponent()).getStatistics().incSentReplyToEvent();
        } catch (Exception e) {
            throw new DispatchException(new org.mule.config.i18n.Message("jms", 8, replyToDestination.toString()),
                                        returnMessage,
                                        null, e);
        } finally {
            JmsUtils.closeQuietly(replyToProducer);
            JmsUtils.closeQuietly(session);
        }
    }
}
