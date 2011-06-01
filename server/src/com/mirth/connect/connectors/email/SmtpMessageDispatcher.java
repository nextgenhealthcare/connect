/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email;

import java.util.Calendar;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

import org.mule.MuleException;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.provider.UMOConnector;

import com.mirth.connect.connectors.email.transformers.MessageObjectToEmailMessage;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;

/**
 * @author Ross Mason
 */
public class SmtpMessageDispatcher extends AbstractMessageDispatcher {
    private Session session;
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private SmtpConnector connector;
    private ConnectorType connectorType = ConnectorType.SENDER;

    /**
     * @param connector
     */
    public SmtpMessageDispatcher(SmtpConnector connector) {
        super(connector);
        this.connector = connector;
        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);

        String host = null;
        int port = -1;
        String username = null;
        String password = null;
        String secureType = null;

        if (connector.isUseServerSettings()) {
            try {
                ServerSettings settings = ControllerFactory.getFactory().createConfigurationController().getServerSettings();
                connector.setHostname(settings.getSmtpHost());
                connector.setSmtpPort(settings.getSmtpPort());
                connector.setUsername(settings.getSmtpUsername());
                connector.setPassword(settings.getSmtpPassword());
                connector.setUseAuthentication(settings.getSmtpAuth());
                connector.setEmailSecure(settings.getSmtpSecure());
            } catch (ControllerException e) {
                logger.error("Unable to load server properties.", e);
            }
        }

        if (connector.getUsername() != null) {
            username = replacer.replaceValues(connector.getUsername(), connector.getChannelId());
        }

        if (connector.getPassword() != null) {
            password = replacer.replaceValues(connector.getPassword(), connector.getChannelId());
        }

        if (connector.getHostname() != null) {
            host = replacer.replaceValues(connector.getHostname(), connector.getChannelId());
        }

        if (connector.getSmtpPort() != null) {
            port = Integer.parseInt(replacer.replaceValues(connector.getSmtpPort(), connector.getChannelId()));
        }

        if (connector.getEmailSecure() != null) {
            secureType = connector.getEmailSecure();
        }

        // NOTE: This is a hack. The first parameter should be the protocol
        // (SMTP, POP, etc.),
        // but since we need a way to pass in the secure type (SSL or TLS) we
        // will overload
        // the parameter since in the actual method it just gets the actual
        // protocol from the
        // connector again.
        URLName url = new URLName(secureType, host, port, null, username, password);
        session = MailUtils.createMailSession(url, connector);
        session.setDebug(logger.isDebugEnabled());
    }

    private Session createSession(String hostname, String username, String password, String port, MessageObject messageObject) {
        hostname = replacer.replaceValues(hostname, messageObject);
        username = replacer.replaceURLValues(username, messageObject);
        password = replacer.replaceURLValues(password, messageObject);
        port = replacer.replaceURLValues(port, messageObject);
        URLName url = new URLName(connector.getProtocol(), hostname, Integer.parseInt(port), null, username, password);
        Session session = MailUtils.createMailSession(url, connector);
        session.setDebug(logger.isDebugEnabled());
        return session;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#dispatch(java.lang.Object,
     * org.mule.providers.MuleEndpoint)
     */
    public void doDispatch(UMOEvent event) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        Message msg = null;

        MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
        if (messageObject == null) {
            return;
        }
        try {
            if ((connector.getUsername() != null && connector.getUsername().indexOf('$') > -1) || (connector.getHostname() != null && connector.getHostname().indexOf('$') > -1) || (connector.getPassword() != null && connector.getPassword().indexOf('$') > -1) || (connector.getPort() != null && connector.getPort().indexOf('$') > -1)) {
                // recreate session
                session = createSession(connector.getHostname(), connector.getUsername(), connector.getPassword(), connector.getPort(), messageObject);
            }
            MessageObjectToEmailMessage motoEmail = new MessageObjectToEmailMessage();
            motoEmail.setEndpoint(event.getEndpoint());

            Object data = motoEmail.transform(messageObject);

            if (!(data instanceof Message)) {
                throw new DispatchException(new org.mule.config.i18n.Message(Messages.TRANSFORM_X_UNEXPECTED_TYPE_X, data.getClass().getName(), Message.class.getName()), event.getMessage(), event.getEndpoint());
            } else {
                // Check the message for any unset data and use defaults
                msg = (Message) data;
            }

            sendMailMessage(msg);
            messageObjectController.setSuccess(messageObject, "Email successfully sent: " + connector.getToAddresses(), null);
        } catch (Exception e) {
            alertController.sendAlerts(messageObject.getChannelId(), Constants.ERROR_402, "Error sending email", e);
            messageObjectController.setError(messageObject, Constants.ERROR_402, "Error sending email", e, null);
            connector.handleException(e);
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnectorSession#getDelegateSession()
     */
    public Object getDelegateSession() throws UMOException {
        return session;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnectorSession#receive(java.lang.String,
     * org.mule.umo.UMOEvent)
     */
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        throw new UnsupportedOperationException("Cannot do a receive on an SmtpConnector");
    }

    public UMOMessage doSend(UMOEvent event) throws Exception {
        doDispatch(event);
        return event.getMessage();
    }

    protected void sendMailMessage(String to, String cc, String bcc, String subject, String body) throws MuleException, MessagingException {
        Message message = connector.createMessage(connector.getFromAddress(), to, cc, bcc, subject, body, session);
        sendMailMessage(message);
    }

    protected void sendMailMessage(Message message) throws MessagingException {
        // sent date
        message.setSentDate(Calendar.getInstance().getTime());
        Transport.send(message);
        if (logger.isDebugEnabled()) {
            StringBuffer msg = new StringBuffer();
            msg.append("Email message sent with subject'").append(message.getSubject()).append("' sent- ");
            msg.append("From: ").append(MailUtils.mailAddressesToString(message.getFrom())).append(" ");
            msg.append("To: ").append(MailUtils.mailAddressesToString(message.getRecipients(Message.RecipientType.TO))).append(" ");
            msg.append("Cc: ").append(MailUtils.mailAddressesToString(message.getRecipients(Message.RecipientType.CC))).append(" ");
            msg.append("Bcc: ").append(MailUtils.mailAddressesToString(message.getRecipients(Message.RecipientType.BCC))).append(" ");
            msg.append("ReplyTo: ").append(MailUtils.mailAddressesToString(message.getReplyTo()));

            logger.debug(msg.toString());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnectorSession#getConnector()
     */
    public UMOConnector getConnector() {
        return connector;
    }

    public void doDispose() {
        session = null;
    }
}
