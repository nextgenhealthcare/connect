/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email.transformers;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.providers.TemplateValueReplacer;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.PropertiesHelper;
import org.mule.util.TemplateParser;
import org.mule.util.Utility;

import com.mirth.connect.connectors.email.MailProperties;
import com.mirth.connect.connectors.email.MailUtils;
import com.mirth.connect.connectors.email.SmtpConnector;
import com.mirth.connect.model.MessageObject;

/**
 * <code>StringToEmailMessage</code> will convert a string to a java mail
 * Message, using the string as the contents. This implementation uses
 * properties on the transformer to determine the to and subject fields.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.8 $
 */
public class MessageObjectToEmailMessage extends AbstractEventAwareTransformer {
    
    private static final long serialVersionUID = 1L;
    /**
     * logger used by this class
     */
    protected final transient Log logger = LogFactory.getLog(getClass());
    protected TemplateParser templateParser = TemplateParser.createAntStyleParser();
    protected TemplateValueReplacer replacer = new TemplateValueReplacer();

    public MessageObjectToEmailMessage() {
        registerSourceType(MessageObject.class);
        setReturnClass(Message.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mule.transformers.AbstractTransformer#doTransform(java.lang.Object)
     */
    public Object transform(Object src, UMOEventContext context) throws TransformerException {
        String endpointAddress = endpoint.getEndpointURI().getAddress();
        SmtpConnector connector = (SmtpConnector) endpoint.getConnector();
        MessageObject messageObject = (MessageObject) src;

        String to = context.getStringProperty(MailProperties.TO_ADDRESSES_PROPERTY, connector.getToAddresses());
        to = replacer.replaceValues(to, messageObject);

        String cc = context.getStringProperty(MailProperties.CC_ADDRESSES_PROPERTY, connector.getCcAddresses());
        cc = replacer.replaceValues(cc, messageObject);

        String bcc = context.getStringProperty(MailProperties.BCC_ADDRESSES_PROPERTY, connector.getBccAddresses());
        bcc = replacer.replaceValues(bcc, messageObject);

        String from = context.getStringProperty(MailProperties.FROM_ADDRESS_PROPERTY, connector.getFromAddress());
        from = replacer.replaceValues(from, messageObject);

        String replyTo = context.getStringProperty(MailProperties.REPLY_TO_ADDRESSES_PROPERTY, connector.getReplyToAddresses());
        replyTo = replacer.replaceValues(replyTo, messageObject);

        String subject = context.getStringProperty(MailProperties.SUBJECT_PROPERTY, connector.getSubject());
        subject = replacer.replaceValues(subject, messageObject);

        String contentType = context.getStringProperty(MailProperties.CONTENT_TYPE_PROPERTY, connector.getContentType());
        contentType = replacer.replaceValues(contentType, messageObject);

        Properties headers = new Properties();

        if (connector.getCustomHeaders() != null) {
            headers.putAll(connector.getCustomHeaders());
        }

        Properties otherHeaders = (Properties) context.getProperty(MailProperties.CUSTOM_HEADERS_MAP_PROPERTY);

        if (otherHeaders != null) {
            Map props = new HashMap(MuleManager.getInstance().getProperties());
            props.putAll(context.getProperties());
            headers.putAll(templateParser.parse(props, otherHeaders));
        }

        if (logger.isDebugEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("Constucting email using:\n");
            buf.append("To: ").append(to);
            buf.append("From: ").append(from);
            buf.append("CC: ").append(cc);
            buf.append("BCC: ").append(bcc);
            buf.append("Subject: ").append(subject);
            buf.append("ReplyTo: ").append(replyTo);
            buf.append("Content type: ").append(contentType);
            buf.append("Payload type: ").append(src.getClass().getName());
            buf.append("Custom Headers: ").append(PropertiesHelper.propertiesToString(headers, false));
            logger.debug(buf.toString());
        }

        try {
            Message msg = new MimeMessage((Session) endpoint.getConnector().getDispatcher(endpointAddress).getDelegateSession());

            msg.setRecipients(Message.RecipientType.TO, MailUtils.StringToInternetAddresses(to));

            // sent date
            msg.setSentDate(Calendar.getInstance().getTime());

            if (from != null && !Utility.EMPTY_STRING.equals(from)) {
                msg.setFrom(MailUtils.StringToInternetAddresses(from)[0]);
            }

            if (cc != null && !Utility.EMPTY_STRING.equals(cc)) {
                msg.setRecipients(Message.RecipientType.CC, MailUtils.StringToInternetAddresses(cc));
            }

            if (bcc != null && !Utility.EMPTY_STRING.equals(bcc)) {
                msg.setRecipients(Message.RecipientType.BCC, MailUtils.StringToInternetAddresses(bcc));
            }

            if (replyTo != null && !Utility.EMPTY_STRING.equals(replyTo)) {
                msg.setReplyTo(MailUtils.StringToInternetAddresses(replyTo));
            }

            msg.setSubject(subject);

            Map.Entry entry;
            for (Iterator iterator = headers.entrySet().iterator(); iterator.hasNext();) {
                entry = (Map.Entry) iterator.next();
                msg.setHeader(entry.getKey().toString(), entry.getValue().toString());
            }

            setContent(src, msg, contentType, context);

            return msg;
        } catch (Exception e) {
            throw new TransformerException(this, e);
        }
    }

    protected void setContent(Object payload, Message msg, String contentType, UMOEventContext context) throws Exception {
        Multipart multipart = new MimeMultipart("mixed");

        // message
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        String body = replacer.replaceValues(((SmtpConnector) endpoint.getConnector()).getBody(), (MessageObject) payload);
        messageBodyPart.setContent(body, contentType);
        multipart.addBodyPart(messageBodyPart);

        // [attachment name, attachment content, attachment mime type]
        List<String> attachmentNames = ((SmtpConnector) endpoint.getConnector()).getAttachmentNames();
        List<String> attachmentContents = ((SmtpConnector) endpoint.getConnector()).getAttachmentContents();
        List<String> attachmentTypes = ((SmtpConnector) endpoint.getConnector()).getAttachmentTypes();

        for (int i = 0; i < attachmentNames.size(); i++) {
            String attachmentName = replacer.replaceValues(attachmentNames.get(i), (MessageObject) payload);
            String attachmentType = attachmentTypes.get(i);
            String attachmentContent = replacer.replaceValues(attachmentContents.get(i), (MessageObject) payload);
            byte[] content;

            if (attachmentType.split("/")[0].equalsIgnoreCase("text")) {
                content = attachmentContent.getBytes();
            } else {
                content = Base64.decodeBase64(attachmentContent);
            }

            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(content, attachmentType);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(attachmentName);
            multipart.addBodyPart(attachmentBodyPart);
        }

        // set the content
        msg.setContent(multipart);
    }
}
