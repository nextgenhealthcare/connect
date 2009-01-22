/*
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/DefaultRedeliveryHandler.java,v 1.6 2005/10/23 15:19:36 holger Exp $
 * $Revision: 1.6 $
 * $Date: 2005/10/23 15:19:36 $
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

import java.util.Collections;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.umo.MessagingException;

/**
 * <code>DefaultRedeliveryHandler</code>
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.6 $
 */
public class DefaultRedeliveryHandler implements RedeliveryHandler
{
    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(DefaultRedeliveryHandler.class);

    private Map messages = null;

    protected JmsConnector connector;

    public DefaultRedeliveryHandler()
    {
        messages = Collections.synchronizedMap(new LRUMap(256));
    }

    /**
     * The connector associated with this handler is set before
     * <code>handleRedelivery()</code> is called
     * 
     * @param connector the connector associated with this handler
     */
    public void setConnector(JmsConnector connector)
    {
        this.connector = connector;
    }

    /**
     * process the redelivered message. If the Jms receiver should process the
     * message, it should be returned. Otherwise the connector should throw a
     * <code>MessageRedeliveredException</code> to indicate that the message
     * should be handled by the connector Exception Handler.
     * 
     * @param message
     */
    public void handleRedelivery(Message message) throws JMSException, MessagingException
    {
        if (connector.getMaxRedelivery() <= 0)
            return;

        String id = message.getJMSMessageID();
        Integer i = (Integer) messages.remove(id);
        if (i == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Message with id: " + id + " has been redelivered for the fist time");
            }
            messages.put(id, new Integer(1));
            return;
        } else if (i.intValue() == connector.getMaxRedelivery()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Message with id: " + id + " has been redelivered " + (i.intValue() + 1)
                        + " times, which exceeds the maxRedelivery setting on the connector");
            }
            JmsMessageAdapter adapter = (JmsMessageAdapter) connector.getMessageAdapter(message);
            throw new MessageRedeliveredException(adapter);

        } else {
            messages.put(id, new Integer(i.intValue() + 1));
            if (logger.isDebugEnabled()) {
                logger.debug("Message with id: " + id + " has been redelivered " + i.intValue() + " times");
            }
        }
    }
}
