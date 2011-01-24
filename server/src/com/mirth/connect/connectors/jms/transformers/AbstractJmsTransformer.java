/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.jms.transformers;

import java.util.Iterator;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.MuleProperties;
import org.mule.config.i18n.Messages;
import org.mule.impl.RequestContext;
import org.mule.impl.internal.events.ConnectionEvent;
import org.mule.impl.internal.events.ConnectionEventListener;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.manager.UMOServerEvent;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.PropertiesHelper;
import org.mule.util.compression.CompressionHelper;

import com.mirth.connect.connectors.jms.JmsMessageUtils;

/**
 * <code>AbstractJmsTransformer</code> is an abstract class the should be used
 * for all transformers where a JMS message <p/> will be the transformed or
 * transformee object. It provides services for compressing and uncompressing
 * messages.
 * 
 * @author Ross Mason
 * @version 1.2
 */

public abstract class AbstractJmsTransformer extends AbstractTransformer implements ConnectionEventListener
{
    public static final char REPLACEMENT_CHAR = '_';

    protected boolean requireNewSession = true;

    /**
     * logger used by this class
     */
    private static transient Log logger = LogFactory.getLog(AbstractJmsTransformer.class);

    private Session session = null;

    public AbstractJmsTransformer()
    {
        super();
    }

    /**
     * Transforms the object.
     * 
     * @param src The source object to transform.
     * @param session
     * @return The transformed object as an XMLMessage
     */
    public Object transform(Object src, Session session) throws TransformerException
    {
        if (session == null && this.session == null) {
            throw new TransformerException(new org.mule.config.i18n.Message("jms", 1), this);
        }
        if (session != null) {
            this.session = session;
            requireNewSession=false;
        }
        if (src == null) {
            throw new TransformerException(new org.mule.config.i18n.Message(Messages.TRANSFORM_FAILED_FROM_X_TO_X,
                                                                            "null",
                                                                            "Object"), this);
        }
        Object ret = transform(src);
        if (logger.isDebugEnabled()) {
            logger.debug("Transformed message from type: " + src.getClass().getName() + " to type: "
                    + ret.getClass().getName());
        }
        return ret;
    }

    /**
     * @param src The source data to compress
     * @return
     * @throws TransformerException
     */
    protected Message transformToMessage(Object src) throws TransformerException
    {
        try {
            // The session can be closed by the dispatcher closing so its more
            // reliable to get it from the dispatcher each time
            if (requireNewSession || getEndpoint() != null) {
                session = (Session) getEndpoint().getConnector()
                                                 .getDispatcher("transformerSession")
                                                 .getDelegateSession();
                requireNewSession = session==null;
            }

            Message msg = null;
            if (src instanceof Message) {
                msg = (Message) src;
                msg.clearProperties();
            } else {
                msg = JmsMessageUtils.getMessageForObject(src, session);
            }
            // set the event properties on the Message
            UMOEventContext ctx = RequestContext.getEventContext();
            if (ctx == null) {
                logger.warn("There is no current event context");
                return msg;
            }

            Map props = ctx.getProperties();
            props = PropertiesHelper.getPropertiesWithoutPrefix(props, "JMS");

            // FIXME: If we add the "destinations" property, then this message will be
            // ignored by channels that are not related to the original source
            // channel.
            // Bug: MIRTH-1689
            props.remove("destinations");
            
            Map.Entry entry;
            String key;
            for (Iterator iterator = props.entrySet().iterator(); iterator.hasNext();) {
                entry = (Map.Entry) iterator.next();
                key = entry.getKey().toString();
                if (MuleProperties.MULE_CORRELATION_ID_PROPERTY.equals(key)) {
                    msg.setJMSCorrelationID(entry.getValue().toString());
                }
                //We dont want to set the ReplyTo property again as it will be set using JMSReplyTo
                if(!(MuleProperties.MULE_REPLY_TO_PROPERTY.equals(key) && entry.getValue() instanceof Destination)) {
                    try {
                        msg.setObjectProperty(encodeHeader(key), entry.getValue());
                    } catch (JMSException e) {
                        //Various Jms servers have slightly different rules to what can be set as an object property on the message
                        //As such we have to take a hit n' hope approach
                        if(logger.isDebugEnabled()) logger.debug("Unable to set property '" + encodeHeader(key) + "' of type " +  entry.getValue().getClass().getName() + "': " + e.getMessage());
                    }
                }
            }

            return msg;
            // }
        } catch (Exception e) {
            throw new TransformerException(this, e);
        }
    }

    /**
     * Encode a string so that is is a valid java identifier
     * 
     * @param name
     * @return
     */
    public static String encodeHeader(String name)
    {
        StringBuffer sb = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(c)) {
                    c = REPLACEMENT_CHAR;
                }
            } else {
                if (!Character.isJavaIdentifierPart(c)) {
                    c = REPLACEMENT_CHAR;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    protected Object transformFromMessage(Message source) throws TransformerException
    {
        Object result = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Message type received is: " + source.getClass().getName());
            }
            if (source instanceof BytesMessage) {
                // If this bytes Message is not compressed it will throw a
                // NotGZipFormatException
                // It would be nice if we could check the custom JMS compression
                // property here. However
                // When jms bridging other, non-JMS-compliant message servers
                // occur, there is no guarantee that
                // Custom properties will be propagated
                byte[] bytes = JmsMessageUtils.getBytesFromMessage(source);
                if (CompressionHelper.isCompressed(bytes)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Message recieved is compressed");
                    }
                    result = CompressionHelper.uncompressByteArray(bytes);
                } else {
                    // If the message is not compressed, handle it the standard
                    // way
                    result = JmsMessageUtils.getObjectForMessage(source);
                }
            } else {
                result = JmsMessageUtils.getObjectForMessage(source);
            }
        } catch (Exception e) {
            throw new TransformerException(this, e);
        }
        return result;
    }

    public Session getSession()
    {
        return session;
    }

    public void setSession(Session session)
    {
        this.session = session;
    }

    public void onEvent(UMOServerEvent event) {
        if(event.getAction() == ConnectionEvent.CONNECTION_DISCONNECTED) {
            session = null;
            requireNewSession = true;
        }
    }
}
