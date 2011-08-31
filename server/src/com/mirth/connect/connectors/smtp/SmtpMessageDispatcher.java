/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;

public class SmtpMessageDispatcher extends AbstractMessageDispatcher {
    protected SmtpConnector connector;
    private final MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private final MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private final AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private final TemplateValueReplacer replacer = new TemplateValueReplacer();
    private final ConnectorType connectorType = ConnectorType.WRITER;

    public SmtpMessageDispatcher(SmtpConnector connector) {
        super(connector);
        this.connector = connector;
    }

    @Override
    public void doDispatch(UMOEvent event) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        MessageObject mo = messageObjectController.getMessageObjectFromEvent(event);

        if (mo == null) {
            return;
        }

        try {
            Email email = null;
            
            if (connector.isHtml()) {
                email = new HtmlEmail();
            } else {
                email = new MultiPartEmail();
            }
            
            email.setDebug(true);
            email.setHostName(connector.getSmtpHost());
            email.setSmtpPort(connector.getSmtpPort());
            email.setSocketConnectionTimeout(connector.getTimeout());

            if ("SSL".equalsIgnoreCase(connector.getEncryption())) {
                email.setSSL(true);
            } else if ("TLS".equalsIgnoreCase(connector.getEncryption())) {
                email.setTLS(true);
            }

            if (connector.isAuthentication()) {
                email.setAuthentication(connector.getUsername(), connector.getPassword());
            }

            /*
             * NOTE: There seems to be a bug when calling setTo with a List
             * (throws a java.lang.ArrayStoreException), so we are using addTo
             * instead.
             */

            for (String to : replaceValuesAndSplit(connector.getTo(), mo)) {
                email.addTo(to);
            }

            for (String cc : replaceValuesAndSplit(connector.cc(), mo)) {
                email.addCc(cc);
            }

            for (String bcc : replaceValuesAndSplit(connector.getBcc(), mo)) {
                email.addBcc(bcc);
            }

            if (StringUtils.isNotBlank(connector.getReplyTo())) {
                email.addReplyTo(connector.getReplyTo());
            }

            for (Entry<String, String> header : connector.getHeaders().entrySet()) {
                email.addHeader(header.getKey(), header.getValue());
            }

            email.setFrom(connector.getFrom());
            email.setSubject(replacer.replaceValues(connector.getSubject(), mo));
            
            String body = replacer.replaceValues(connector.getBody(), mo);
            
            if (connector.isHtml()) {
                ((HtmlEmail) email).setHtmlMsg(body);
            } else {
                email.setMsg(body);    
            }

            /*
             * If the MIME type for the attachment is missing, we display a
             * warning and set the content anyway. If the MIME type is of type
             * "text" or "application/xml", then we add the content. If it is
             * anything else, we assume it should be Base64 decoded first.
             */
            for (Attachment attachment : connector.getAttachments()) {
                byte[] contentBytes;

                if (StringUtils.indexOf(attachment.getMimeType(), "/") < 0) {
                    logger.warn("valid MIME type is missing for email attachment: \"" + attachment.getName() + "\", using default of text/plain");
                    attachment.setMimeType("text/plain");
                    contentBytes = attachment.getContent().getBytes();
                } else if ("application/xml".equalsIgnoreCase(attachment.getMimeType()) || StringUtils.startsWith(attachment.getMimeType(), "text/")) {
                    logger.debug("text or XML MIME type detected for attachment \"" + attachment.getName() + "\"");
                    contentBytes = attachment.getContent().getBytes();
                } else {
                    logger.debug("binary MIME type detected for attachment \"" + attachment.getName() + "\", performing Base64 decoding");
                    contentBytes = Base64.decodeBase64(attachment.getContent());
                }

                ((MultiPartEmail) email).attach(new ByteArrayDataSource(contentBytes, attachment.getMimeType()), attachment.getName(), null);
            }

            /*
             * From the Commons Email JavaDoc: send returns
             * "the message id of the underlying MimeMessage".
             */
            String response = email.send();
            messageObjectController.setSuccess(mo, response, null);
        } catch (EmailException e) {
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_402, "Error sending email message.", e);
            messageObjectController.setError(mo, Constants.ERROR_402, "Error sending email message.", e, null);
            connector.handleException(new Exception(e));
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    /**
     * Takes a comma-separated list of email addresses and returns a String[] of
     * individual addresses with replaced values.
     * 
     * @param addresses
     *            A comma-separated list of email addresses
     * @param mo
     *            A MessageObject
     * @return A String[] of individual adresses, or an empty String[] if
     *         addresses is blank
     */
    private String[] replaceValuesAndSplit(String addresses, MessageObject mo) {
        if (StringUtils.isNotBlank(addresses)) {
            return StringUtils.split(replacer.replaceValues(addresses, mo), ",");
        } else {
            return new String[0];
        }
    }

    @Override
    public void doDispose() {

    }

    @Override
    public UMOMessage doSend(UMOEvent event) throws Exception {
        doDispatch(event);
        return event.getMessage();
    }

    @Override
    public Object getDelegateSession() throws UMOException {
        return null;
    }

    @Override
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        return null;
    }

}
