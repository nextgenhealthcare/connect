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
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.mule.MuleException;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;

/**
 * <code>SmtpConnector</code> is used to connect to and send data to an SMTP
 * mail server
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.11 $
 */
public class SmtpConnector extends AbstractServiceEnabledConnector implements MailConnector {
    public static final String DEFAULT_SMTP_PORT = "25";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";

    private String body;
    /**
     * Holds value of to addresses.
     */
    private String to;
    /**
     * Holds value of bcc addresses.
     */
    private String bcc;

    /**
     * Holds value of cc addresses.
     */
    private String cc;

    /**
     * Holds value of replyTo addresses.
     */
    private String replyTo;

    /**
     * determines whether a mailbox connection is active
     */
    private boolean connected = false;

    /**
     * Holds value of default subject
     */
    private String defaultSubject = "[No Subject]";

    /**
     * Holds value of the from address.
     */
    private String from;

    /**
     * Holds value of emailSecure ("tls", "ssl", "none")
     */
    private String emailSecure;

    /**
     * Holds value of property SMTP password.
     */
    private String password;

    /**
     * Holds value of property hostname for the smtp server.
     */
    private String hostname = "";

    /**
     * Holds value of property port for the smtp server.
     */
    private String smtpPort = DEFAULT_SMTP_PORT;

    /**
     * Holds value of property SMTPusername.
     */
    private String username;

    /**
     * Any custom headers to be set on messages sent using this connector
     */
    private Properties customHeaders = new Properties();
    
    private String channelId;

    /**
     * A custom authenticator to bew used on any mail sessions created with this
     * connector This will only be used if user name credendtials are set on the
     * endpoint
     */
    private Authenticator authenticator = null;

    private String contentType = DEFAULT_CONTENT_TYPE;

    private List attachmentNames = null;
    private List attachmentContents = null;
    private List attachmentTypes = null;
    private boolean useAuthentication;
    private boolean useServerSettings;

    public SmtpConnector() throws InitialisationException {
        initFromServiceDescriptor();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#createMessage(java.lang.Object)
     */
    public Object createMessage(Object message, Session session) throws Exception {
        if (message instanceof Message) {
            return message;
        }
        String body = null;
        if (message instanceof String) {
            body = (String) message;
        }
        Message msg = createMessage(getFromAddress(), null, cc, bcc, defaultSubject, body, session);
        return msg;
    }

    protected Message createMessage(String from, String to, String cc, String bcc, String subject, String body, Session session) throws MuleException {
        Message msg = new MimeMessage(session);
        try {
            // to

            InternetAddress[] toAddrs = null;
            if ((to != null) && !to.equals("")) {
                toAddrs = InternetAddress.parse(to, false);
                msg.setRecipients(Message.RecipientType.TO, toAddrs);
            } else {
                throw new MuleException(new org.mule.config.i18n.Message(Messages.X_IS_NULL, "toAddress"));
            }
            // sent date
            msg.setSentDate(Calendar.getInstance().getTime());
            // from
            if (from == null) {
                throw new IllegalArgumentException("From address must be set");
            }
            msg.setFrom(new InternetAddress(from));
            // cc
            InternetAddress[] ccAddrs = null;
            if ((cc != null) && !cc.equals("")) {
                ccAddrs = InternetAddress.parse(cc, false);
                msg.setRecipients(Message.RecipientType.CC, ccAddrs);
            }
            InternetAddress[] bccAddrs = null;
            if ((bcc != null) && !bcc.equals("")) {
                bccAddrs = InternetAddress.parse(bcc, false);
                msg.setRecipients(Message.RecipientType.BCC, bccAddrs);
            }
            // subject
            if ((subject != null) && !subject.equals("")) {
                msg.setSubject(subject);
            } else {
                msg.setSubject("(no subject)");
            }
            // todo attachments

            // create the Multipart and its parts to it
            Multipart mp = new MimeMultipart();
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setText(body);
            mp.addBodyPart(mbp);

            // add the Multipart to the message
            msg.setContent(mp);
            return msg;
        } catch (MuleException e) {
            throw e;
        } catch (MessagingException e) {
            throw new MuleException(new org.mule.config.i18n.Message(Messages.FAILED_TO_SET_PROPERTIES_ON_X, "Email message"), e);
        }
    }

    /**
     * @return
     */
    public String getFromAddress() {
        return from;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#getProtocol()
     */
    public String getProtocol() {
        return "smtp";
    }

    public boolean isConnected() {
        return this.connected;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mule.providers.UMOConnector#registerListener(javax.jms.MessageListener
     * , java.lang.String)
     */
    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
        throw new UnsupportedOperationException("Listeners cannot be registered on a SMTP endpoint");
    }

    /*
     * @see org.mule.providers.UMOConnector#start()
     */
    public void doStart() throws UMOException {
        // force connection to server
        dispatcherFactory.create(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#stop()
     */
    public void doStop() throws UMOException {
        connected = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.AbstractConnector#doDispose()
     */
    protected void doDispose() {
        try {
            doStop();
        } catch (UMOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @return
     */
    public String getBccAddresses() {
        return bcc;
    }

    /**
     * @return
     */
    public String getCcAddresses() {
        return cc;
    }

    /**
     * @return
     */
    public String getSubject() {
        return defaultSubject;
    }

    /**
     * @return
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param string
     */
    public void setBccAddresses(String string) {
        bcc = string;
    }

    /**
     * @param string
     */
    public void setCcAddresses(String string) {
        cc = string;
    }

    /**
     * @param string
     */
    public void setSubject(String string) {
        defaultSubject = string;
    }

    /**
     * @param string
     */
    public void setFromAddress(String string) {
        from = string;
    }

    /**
     * @param string
     */
    public void setHostname(String string) {
        hostname = string;
    }

    /**
     * @param string
     */
    public void setPassword(String string) {
        password = string;
    }

    /**
     * @param string
     */
    public void setUsername(String string) {
        username = string;
    }

    public String getPort() {
        return smtpPort;
    }

    public void setPort(String port) {
        this.smtpPort = port;
    }

    public String getReplyToAddresses() {
        return replyTo;
    }

    public void setReplyToAddresses(String replyTo) {
        this.replyTo = replyTo;
    }

    public Properties getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Properties customHeaders) {
        this.customHeaders = customHeaders;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getToAddresses() {
        return to;
    }

    public void setToAddresses(String to) {
        this.to = to;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public List getAttachmentNames() {
        return attachmentNames;
    }

    public void setAttachmentNames(List attachmentNames) {
        this.attachmentNames = attachmentNames;
    }

    public List getAttachmentContents() {
        return attachmentContents;
    }

    public void setAttachmentContents(List attachmentContents) {
        this.attachmentContents = attachmentContents;
    }

    public List getAttachmentTypes() {
        return attachmentTypes;
    }

    public void setAttachmentTypes(List attachmentTypes) {
        this.attachmentTypes = attachmentTypes;
    }

    public void setEmailSecure(String emailSecure) {
        this.emailSecure = emailSecure;
    }

    public String getEmailSecure() {
        return emailSecure;
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public boolean isUseServerSettings() {
        return useServerSettings;
    }

    public void setUseServerSettings(boolean useServerSettings) {
        this.useServerSettings = useServerSettings;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
