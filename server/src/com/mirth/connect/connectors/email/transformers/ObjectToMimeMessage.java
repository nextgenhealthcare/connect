/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email.transformers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.mule.providers.email.transformers.StringToEmailMessage;
import org.mule.transformers.simple.SerializableToByteArray;
import org.mule.umo.UMOEventContext;
import org.mule.umo.transformer.TransformerException;

/**
 * Transforms a javax.mail.Message to a UMOMEssage and supports attachments
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.2 $
 */
public class ObjectToMimeMessage extends StringToEmailMessage {

    protected void setContent(Object payload, Message msg, String contentType, UMOEventContext context)
            throws Exception {
        if (context.getMessage().getAttachmentNames().size() > 0) {
            MimeMultipart multipart = new MimeMultipart("mixed"); // The contenttype must be multipart/mixed
            multipart.addBodyPart(getPayloadBodyPart(payload, contentType));
            for (Iterator it = context.getMessage().getAttachmentNames().iterator(); it.hasNext();) {
                String name = (String) it.next();
                BodyPart part = getBodyPartForAttachment(context.getMessage().getAttachment(name), name);
                multipart.addBodyPart(part);
            }
            // the payload must be set to the constructed MimeMultipart message
            payload = multipart;
            // the ContentType of the message to be sent, must be the multipart
            contentType = multipart.getContentType();
            // content type
        }
        // now the message will contain the multipart payload, and the multipart contentType
        super.setContent(payload, msg, contentType, context);
    }

    protected BodyPart getBodyPartForAttachment(DataHandler handler, String name) throws MessagingException {
        BodyPart part = new MimeBodyPart();
        part.setDataHandler(handler);
        part.setDescription(name);
        return part;
    }

    protected BodyPart getPayloadBodyPart(Object payload, String contentType) throws MessagingException, TransformerException, IOException {

        DataHandler handler = null;
        if(payload instanceof String) {
            handler = new DataHandler(new PlainTextDataSource(contentType, payload.toString()));
        } else if(payload instanceof Serializable) {
            handler = new DataHandler(new ByteArrayDataSource((byte[])new SerializableToByteArray().transform(payload), contentType));
        }else if(payload instanceof byte[]) {
            handler = new DataHandler(new ByteArrayDataSource((byte[])payload, contentType));
        } else {
            throw new IllegalArgumentException();
        }
        BodyPart part = new MimeBodyPart();
        part.setDataHandler(handler);
        part.setDescription("Payload");
        return part;
    }
}
