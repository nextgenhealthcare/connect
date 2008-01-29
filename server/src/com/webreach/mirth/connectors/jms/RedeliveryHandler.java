/*
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/RedeliveryHandler.java,v 1.4 2005/06/03 01:20:34 gnt Exp $
 * $Revision: 1.4 $
 * $Date: 2005/06/03 01:20:34 $
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

import javax.jms.JMSException;
import javax.jms.Message;

import org.mule.umo.MessagingException;

/**
 * <code>RedeliveryHandler</code> is used to control how redelivered messages
 * are processed by a connector. Typically, a messsage will be re-tried once or
 * twice before throwing an exception. Then the Exception Strategy on the
 * connector can be used to forward the message to a jms or log the failure.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */
public interface RedeliveryHandler
{

    /**
     * The connector associated with this handler is set before
     * <code>handleRedelivery()</code> is called
     * 
     * @param connector the connector associated with this handler
     */
    public void setConnector(JmsConnector connector);

    /**
     * process the redelivered message. If the Jms receiver should process the
     * message, it should be returned. Otherwise the connector should throw a
     * <code>MessageRedeliveredException</code> to indicate that the message
     * should be handled by the connector Exception Handler.
     * 
     * @param message
     * @throws JMSException if properties cannot be read from the JMSMessage
     * @throws MessageRedeliveredException should be thrown if the message
     *             should be handled by the connection exception handler
     * @throws MessagingException if there is a problem reading or proessing the
     *             message
     */
    public void handleRedelivery(Message message) throws JMSException, MessageRedeliveredException, MessagingException;
}
