/* 
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/MessageRedeliveredException.java,v 1.7 2005/06/03 01:20:34 gnt Exp $
 * $Revision: 1.7 $
 * $Date: 2005/06/03 01:20:34 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package com.webreach.mirth.connectors.jms;

import org.mule.umo.MessagingException;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.7 $
 */
public class MessageRedeliveredException extends MessagingException
{
    public MessageRedeliveredException(JmsMessageAdapter jmsMessage)
    {
        super(new org.mule.config.i18n.Message("jms", 7, (jmsMessage == null ? "[null message]"
                : jmsMessage.getUniqueId())), jmsMessage);
    }
}
