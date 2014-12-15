/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.ErrorMessageBuilder;
import com.mirth.connect.util.MirthSSLUtil;

public class SmtpDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private SmtpDispatcherProperties connectorProperties;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private final TemplateValueReplacer replacer = new TemplateValueReplacer();

    private String charsetEncoding;
    private String protocols;
    private String cipherSuites;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (SmtpDispatcherProperties) getConnectorProperties();

        // TODO remove hardcoded HL7v2 reference?
        this.charsetEncoding = CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding(), System.getProperty("ca.uhn.hl7v2.llp.charset"));
        protocols = StringUtils.join(MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsClientProtocols()), ' ');
        cipherSuites = StringUtils.join(MirthSSLUtil.getEnabledHttpsCipherSuites(configurationController.getHttpsCipherSuites()), ' ');
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {}

    @Override
    public void onStart() throws ConnectorTaskException {}

    @Override
    public void onStop() throws ConnectorTaskException {}

    @Override
    public void onHalt() throws ConnectorTaskException {}

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        SmtpDispatcherProperties smtpDispatcherProperties = (SmtpDispatcherProperties) connectorProperties;

        // Replace all values in connector properties
        smtpDispatcherProperties.setSmtpHost(replacer.replaceValues(smtpDispatcherProperties.getSmtpHost(), connectorMessage));
        smtpDispatcherProperties.setSmtpPort(replacer.replaceValues(smtpDispatcherProperties.getSmtpPort(), connectorMessage));
        smtpDispatcherProperties.setTimeout(replacer.replaceValues(smtpDispatcherProperties.getTimeout(), connectorMessage));

        if (smtpDispatcherProperties.isAuthentication()) {
            smtpDispatcherProperties.setUsername(replacer.replaceValues(smtpDispatcherProperties.getUsername(), connectorMessage));
            smtpDispatcherProperties.setPassword(replacer.replaceValues(smtpDispatcherProperties.getPassword(), connectorMessage));
        }

        smtpDispatcherProperties.setTo(replacer.replaceValues(smtpDispatcherProperties.getTo(), connectorMessage));
        smtpDispatcherProperties.setCc(replacer.replaceValues(smtpDispatcherProperties.getCc(), connectorMessage));
        smtpDispatcherProperties.setBcc(replacer.replaceValues(smtpDispatcherProperties.getBcc(), connectorMessage));
        smtpDispatcherProperties.setReplyTo(replacer.replaceValues(smtpDispatcherProperties.getReplyTo(), connectorMessage));

        smtpDispatcherProperties.setHeaders(replacer.replaceValuesInMap(smtpDispatcherProperties.getHeaders(), connectorMessage));

        smtpDispatcherProperties.setFrom(replacer.replaceValues(smtpDispatcherProperties.getFrom(), connectorMessage));
        smtpDispatcherProperties.setSubject(replacer.replaceValues(smtpDispatcherProperties.getSubject(), connectorMessage));

        smtpDispatcherProperties.setBody(replacer.replaceValues(smtpDispatcherProperties.getBody(), connectorMessage));

        for (Attachment attachment : smtpDispatcherProperties.getAttachments()) {
            attachment.setName(replacer.replaceValues(attachment.getName(), connectorMessage));
            attachment.setMimeType(replacer.replaceValues(attachment.getMimeType(), connectorMessage));
            attachment.setContent(replacer.replaceValues(attachment.getContent(), connectorMessage));
        }
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        SmtpDispatcherProperties smtpDispatcherProperties = (SmtpDispatcherProperties) connectorProperties;
        String responseData = null;
        String responseError = null;
        String responseStatusMessage = null;
        Status responseStatus = Status.QUEUED;

        String info = "From: " + smtpDispatcherProperties.getFrom() + " To: " + smtpDispatcherProperties.getTo() + " SMTP Info: " + smtpDispatcherProperties.getSmtpHost() + ":" + smtpDispatcherProperties.getSmtpPort();
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.WRITING, info));

        try {
            Email email = null;

            if (smtpDispatcherProperties.isHtml()) {
                email = new HtmlEmail();
            } else {
                email = new MultiPartEmail();
            }

            email.setCharset(charsetEncoding);

            email.setHostName(smtpDispatcherProperties.getSmtpHost());

            try {
                email.setSmtpPort(Integer.parseInt(smtpDispatcherProperties.getSmtpPort()));
            } catch (NumberFormatException e) {
                // Don't set if the value is invalid
            }

            try {
                int timeout = Integer.parseInt(smtpDispatcherProperties.getTimeout());
                email.setSocketTimeout(timeout);
                email.setSocketConnectionTimeout(timeout);
            } catch (NumberFormatException e) {
                // Don't set if the value is invalid
            }

            if ("SSL".equalsIgnoreCase(smtpDispatcherProperties.getEncryption())) {
                email.setSSLOnConnect(true);
                email.setSslSmtpPort(smtpDispatcherProperties.getSmtpPort());
            } else if ("TLS".equalsIgnoreCase(smtpDispatcherProperties.getEncryption())) {
                email.setStartTLSEnabled(true);
            }

            if (smtpDispatcherProperties.isAuthentication()) {
                email.setAuthentication(smtpDispatcherProperties.getUsername(), smtpDispatcherProperties.getPassword());
            }

            // These have to be set after the authenticator, so that a new mail session isn't created
            email.getMailSession().getProperties().setProperty("mail.smtp.ssl.protocols", protocols);
            email.getMailSession().getProperties().setProperty("mail.smtp.ssl.ciphersuites", cipherSuites);

            /*
             * NOTE: There seems to be a bug when calling setTo with a List (throws a
             * java.lang.ArrayStoreException), so we are using addTo instead.
             */

            for (String to : StringUtils.split(smtpDispatcherProperties.getTo(), ",")) {
                email.addTo(to);
            }

            // Currently unused
            for (String cc : StringUtils.split(smtpDispatcherProperties.getCc(), ",")) {
                email.addCc(cc);
            }

            // Currently unused
            for (String bcc : StringUtils.split(smtpDispatcherProperties.getBcc(), ",")) {
                email.addBcc(bcc);
            }

            // Currently unused
            for (String replyTo : StringUtils.split(smtpDispatcherProperties.getReplyTo(), ",")) {
                email.addReplyTo(replyTo);
            }

            for (Entry<String, String> header : smtpDispatcherProperties.getHeaders().entrySet()) {
                email.addHeader(header.getKey(), header.getValue());
            }

            email.setFrom(smtpDispatcherProperties.getFrom());
            email.setSubject(smtpDispatcherProperties.getSubject());

            AttachmentHandler attachmentHandler = getAttachmentHandler();

            String body = attachmentHandler.reAttachMessage(smtpDispatcherProperties.getBody(), connectorMessage);

            if (StringUtils.isNotEmpty(body)) {
                if (smtpDispatcherProperties.isHtml()) {
                    ((HtmlEmail) email).setHtmlMsg(body);
                } else {
                    email.setMsg(body);
                }
            }

            /*
             * If the MIME type for the attachment is missing, we display a warning and set the
             * content anyway. If the MIME type is of type "text" or "application/xml", then we add
             * the content. If it is anything else, we assume it should be Base64 decoded first.
             */
            for (Attachment attachment : smtpDispatcherProperties.getAttachments()) {
                String name = attachment.getName();
                String mimeType = attachment.getMimeType();
                String content = attachment.getContent();

                byte[] bytes;

                if (StringUtils.indexOf(mimeType, "/") < 0) {
                    logger.warn("valid MIME type is missing for email attachment: \"" + name + "\", using default of text/plain");
                    attachment.setMimeType("text/plain");
                    bytes = attachmentHandler.reAttachMessage(content, connectorMessage, charsetEncoding, false);
                } else if ("application/xml".equalsIgnoreCase(mimeType) || StringUtils.startsWith(mimeType, "text/")) {
                    logger.debug("text or XML MIME type detected for attachment \"" + name + "\"");
                    bytes = attachmentHandler.reAttachMessage(content, connectorMessage, charsetEncoding, false);
                } else {
                    logger.debug("binary MIME type detected for attachment \"" + name + "\", performing Base64 decoding");
                    bytes = attachmentHandler.reAttachMessage(content, connectorMessage, null, true);
                }

                ((MultiPartEmail) email).attach(new ByteArrayDataSource(bytes, mimeType), name, null);
            }

            /*
             * From the Commons Email JavaDoc: send returns
             * "the message id of the underlying MimeMessage".
             */
            responseData = email.send();
            responseStatus = Status.SENT;
            responseStatusMessage = "Email sent successfully.";
        } catch (Exception e) {
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), connectorMessage.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error sending email message", e));
            responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error sending email message", e);
            responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error sending email message", e);

            // TODO: Exception handling
//            connector.handleException(new Exception(e));
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError);
    }
}
