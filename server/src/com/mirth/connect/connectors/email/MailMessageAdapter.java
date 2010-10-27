/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;

import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

/**
 * <code>MailMessageAdapter</code> is a wrapper for a javax.mail.Message.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.8 $
 */
public class MailMessageAdapter extends AbstractMessageAdapter {

    private Part messagePart = null;
    private byte[] contentBuffer;

    public MailMessageAdapter(Object message) throws MessagingException {
        setMessage(message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayload()
     */
    public Object getPayload() {
        return messagePart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayloadAsBytes()
     */
    public byte[] getPayloadAsBytes() throws Exception {
        if (contentBuffer == null) {
            String contentType = messagePart.getContentType();

            if (contentType.startsWith("text/")) {
                getPayloadAsString();
            } else {
                InputStream is = messagePart.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024 * 32];
                int len = 0;
                while ((len = is.read(buf)) > -1) {
                    baos.write(buf, 0, len);
                }
                contentBuffer = baos.toByteArray();
            }
        }
        return contentBuffer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayloadAsString()
     */
    public String getPayloadAsString() throws Exception {
        if (contentBuffer == null) {
            String contentType = messagePart.getContentType();

            if (contentType.startsWith("text/")) {
                InputStream is = messagePart.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuffer buffer = new StringBuffer();
                String line = reader.readLine();
                buffer.append(line);

                while (line != null) {
                    line = reader.readLine();
                    buffer.append(line);
                }
                contentBuffer = buffer.toString().getBytes();
            } else {
                contentBuffer = getPayloadAsBytes();
            }
        }
        return new String(contentBuffer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#setMessage(java.lang.Object)
     */
    private void setMessage(Object message) throws MessagingException {
        Message msg;
        if (message instanceof Message) {
            msg = (Message) message;
        } else {
            throw new MessageTypeNotSupportedException(message, MailMessageAdapter.class);
        }

        try {
            Object content = msg.getContent();

            if (content instanceof Multipart) {
                this.messagePart = ((Multipart) content).getBodyPart(0);
                logger.debug("Received Multipart message");
                Part part;
                String name;
                for (int i = 1; i < ((Multipart) content).getCount(); i++) {
                    part = ((Multipart) content).getBodyPart(i);
                    name = part.getFileName();
                    if (name == null)
                        name = String.valueOf(i - 1);
                    addAttachment(name, part.getDataHandler());
                }
            } else {
                messagePart = msg;
            }

            // Set message attrributes as properties
            setProperty(MailProperties.TO_ADDRESSES_PROPERTY, MailUtils.mailAddressesToString(msg.getRecipients(Message.RecipientType.TO)));
            setProperty(MailProperties.CC_ADDRESSES_PROPERTY, MailUtils.mailAddressesToString(msg.getRecipients(Message.RecipientType.CC)));
            setProperty(MailProperties.BCC_ADDRESSES_PROPERTY, MailUtils.mailAddressesToString(msg.getRecipients(Message.RecipientType.BCC)));
            setProperty(MailProperties.REPLY_TO_ADDRESSES_PROPERTY, MailUtils.mailAddressesToString(msg.getReplyTo()));
            setProperty(MailProperties.FROM_ADDRESS_PROPERTY, MailUtils.mailAddressesToString(msg.getFrom()));
            setProperty(MailProperties.SUBJECT_PROPERTY, msg.getSubject());
            setProperty(MailProperties.CONTENT_TYPE_PROPERTY, msg.getContentType());
            setProperty(MailProperties.SENT_DATE_PROPERTY, msg.getSentDate());

            for (Enumeration e = msg.getAllHeaders(); e.hasMoreElements();) {
                Header h = (Header) e.nextElement();
                properties.put(h.getName(), h.getValue());
            }

        } catch (Exception e) {
            throw new MessagingException(new org.mule.config.i18n.Message(Messages.FAILED_TO_CREATE_X, "Message Adapter"), e);
        }
    }
}
