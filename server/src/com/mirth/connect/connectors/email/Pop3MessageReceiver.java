/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeMessage;

import org.mule.MuleManager;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.PollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.Startable;
import org.mule.umo.lifecycle.Stoppable;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.routing.RoutingException;
import org.mule.util.UUID;
import org.mule.util.Utility;

import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;

/**
 * <code>Pop3MessageReceiver</code> polls a pop3 mailbox for messages removes
 * the messages and routes them as events into Mule.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.7 $
 */

public class Pop3MessageReceiver extends PollingMessageReceiver implements MessageCountListener, Startable, Stoppable {
    private Folder folder = null;
    private String backupFolder = null;
    protected Session session;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();

    public Pop3MessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, Long checkFrequency, String backupFolder) throws InitialisationException {
        super(connector, component, endpoint, checkFrequency);

        if ("".equals(backupFolder)) {
            this.backupFolder = MuleManager.getConfiguration().getWorkingDirectory() + "/mail/" + folder.getName();
        } else {
            this.backupFolder = backupFolder;
        }
        if (backupFolder != null && !this.backupFolder.endsWith(File.separator)) {
            this.backupFolder += File.separator;
        }
    }

    public void doConnect() throws Exception {
        String inbox = null;
        if (connector.getProtocol().equalsIgnoreCase("imap")) {
            inbox = endpoint.getEndpointURI().getPath();
            if (inbox.length() == 0) {
                inbox = Pop3Connector.MAILBOX;
            } else {
                inbox = inbox.substring(1);
            }
        } else {
            inbox = Pop3Connector.MAILBOX;
        }

        URLName url = new URLName(endpoint.getEndpointURI().getScheme(), endpoint.getEndpointURI().getHost(), endpoint.getEndpointURI().getPort(), inbox, endpoint.getEndpointURI().getUsername(), endpoint.getEndpointURI().getPassword());

        session = MailUtils.createMailSession(url, (MailConnector) connector);
        session.setDebug(logger.isDebugEnabled());

        Store store = session.getStore(url);
        store.connect();
        folder = store.getFolder(inbox);
    }

    public void doDisconnect() throws Exception {
        if (folder != null)
            folder.close(true);
    }

    public void doStop() {
        folder.removeMessageCountListener(this);
    }

    public void doStart() throws UMOException {
        super.doStart();
        folder.addMessageCountListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.mail.event.MessageCountListener#messagesAdded(javax.mail.event.
     * MessageCountEvent)
     */
    public void messagesAdded(MessageCountEvent event) {
        Message messages[] = event.getMessages();
        UMOMessage message = null;
        for (int i = 0; i < messages.length; i++) {
            try {
                if (!messages[i].getFlags().contains(Flags.Flag.DELETED)) {
                    MimeMessage mimeMessage = new MimeMessage((MimeMessage) messages[i]);
                    storeMessage(mimeMessage);
                    message = new MuleMessage(connector.getMessageAdapter(mimeMessage));

                    // Mark as deleted
                    messages[i].setFlag(Flags.Flag.DELETED, true);
                    routeMessage(message, endpoint.isSynchronous());
                }
            } catch (UMOException e) {
                handleException(e);
            } catch (Exception e) {
                handleException(new RoutingException(new org.mule.config.i18n.Message(Messages.ROUTING_ERROR), message, endpoint, e));
            }
        }
    }

    protected UMOMessage handleUnacceptedFilter(UMOMessage message) {
        super.handleUnacceptedFilter(message);
        if (message.getPayload() instanceof Message) {
            Message msg = (Message) message.getPayload();
            try {
                msg.setFlag(Flags.Flag.DELETED, endpoint.isDeleteUnacceptedMessages());
            } catch (MessagingException e) {
                logger.error("failled to set message deleted: " + e.getMessage(), e);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.mail.event.MessageCountListener#messagesRemoved(javax.mail.event
     * .MessageCountEvent)
     */
    public void messagesRemoved(MessageCountEvent event) {
        if (logger.isDebugEnabled()) {
            Message messages[] = event.getMessages();
            for (int i = 0; i < messages.length; i++) {
                try {
                    logger.debug("Message removed: " + messages[i].getSubject());
                } catch (MessagingException ignore) {
                }
            }
        }
    }

    /**
     * @return
     */
    public Folder getFolder() {
        return folder;
    }

    /**
     * @param folder
     */
    public synchronized void setFolder(Folder folder) {
        if (folder == null)
            throw new IllegalArgumentException("Mail folder cannot be null");
        this.folder = folder;
        synchronized (this.folder) {
            if (!this.folder.isOpen()) {
                try {
                    // Depending on Server implementation it's not always
                    // necessary
                    // to open the folder to check it
                    // Opening folders can be exprensive!
                    // folder.open(Folder.READ_ONLY);
                    this.folder.open(Folder.READ_WRITE);
                } catch (MessagingException e) {
                    logger.warn("Failed to open folder: " + folder.getFullName(), e);
                }
                // try
                // {
                // folder.close(true);
                // }
                // catch (MessagingException me)
                // {
                // logger.warn("Failed to close folder: " +
                // folder.getFullName(), me);
                // }
            }
        }
    }

    /**
     * Helper method for testing which stores a copy of the message locally as
     * the POP3
     * <p/>
     * message will be deleted from the server
     * 
     * @param msg
     *            the message to store
     * @throws IOException
     *             If a failiure happens writing the message
     * @throws MessagingException
     *             If a failiure happens reading the message
     */
    private void storeMessage(Message msg) throws IOException, MessagingException {
        if (backupFolder != null) {
            String filename = msg.getFileName();
            if (filename == null) {
                filename = msg.getFrom()[0].toString() + "[" + new UUID().getUUID() + "]";
            }
            filename = Utility.prepareWinFilename(filename);
            filename = backupFolder + filename + ".msg";
            logger.debug("Writing message to: " + filename);
            File f = Utility.createFile(filename);
            FileWriter fw = new FileWriter(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            msg.writeTo(baos);
            fw.write(new String(baos.toByteArray()));
            baos.close();
            fw.close();
        }
    }

    public void poll() {
        try {
            try {
                if (!folder.isOpen()) {
                    folder.open(Folder.READ_WRITE);
                }
            } catch (Exception e) {
                // ignore
            }

            int count = folder.getMessageCount();
            if (count > 0) {
                Message[] messages = folder.getMessages();
                MessageCountEvent event = new MessageCountEvent(folder, MessageCountEvent.ADDED, true, messages);
                messagesAdded(event);
            } else if (count == -1) {
                throw new MessagingException("Cannot monitor folder: " + folder.getFullName() + " as folder is closed");
            }
            folder.close(true); // close and expunge deleted messages
        } catch (MessagingException e) {
            alertController.sendAlerts(((Pop3Connector) connector).getChannelId(), Constants.ERROR_413, null, e);
            handleException(e);
        }
    }

    protected void doDispose() {
        super.doDispose();
        if (folder != null)
            folder.removeMessageCountListener(this);

        try {
            if (folder != null)
                folder.close(false);
        } catch (Exception e) {
            logger.error("Failed to close pop3  inbox: " + e.getMessage());
        }
    }

}
