/* 
 * $Header: /home/projects/mule/scm/mule/providers/email/src/java/org/mule/providers/email/transformers/EmailMessageToString.java,v 1.5 2005/10/10 14:00:15 rossmason Exp $
 * $Revision: 1.5 $
 * $Date: 2005/10/10 14:00:15 $
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

package com.webreach.mirth.connectors.email.transformers;

import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

/**
 * <code>EmailMessageToString</code> extracts a java mail Message contents and
 * returns a string.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */
public class EmailMessageToString extends AbstractTransformer
{
    public EmailMessageToString()
    {
        registerSourceType(Message.class);
        registerSourceType(String.class);
        setReturnClass(String.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transformers.AbstractTransformer#doTransform(java.lang.Object)
     */
    public Object doTransform(Object src) throws TransformerException
    {
        Message msg = (Message) src;
        try {
            //Other information about the message such as cc addresses, attachments are handled
            //By the Mail MEssage adapter.  If Transformers need access to these properties
            //they should implement the AbstractEventAwareTransformer and extract these properties
            //from the passed UMOEventContext

            // For this impl we just pass back the email content
            Object result = msg.getContent();
            if (result instanceof String) {
                return (String) result;
            } else {
                // very simplisitic, only gets first part
                MimeMultipart part = (MimeMultipart) result;
                String transMsg = (String) part.getBodyPart(0).getContent();
                return transMsg;
            }
        } catch (Exception e) {
            throw new TransformerException(this, e);
        }
    }
}
