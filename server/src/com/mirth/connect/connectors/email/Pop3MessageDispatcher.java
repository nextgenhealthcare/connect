/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.UMOConnector;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * <code>Pop3MessageDispatcher</code> For Pop3 connections the dispatcher can
 * only be used to receive message (as opposed to listening for them). Trying to
 * send or dispatch will throw an UnsupportedOperationException.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.8 $
 */

public class Pop3MessageDispatcher extends AbstractMessageDispatcher {
    private Pop3Connector connector;

    private Folder folder;

    private Session session = null;

    private AtomicBoolean initialised = new AtomicBoolean(false);

    public Pop3MessageDispatcher(Pop3Connector connector) {
        super(connector);
        this.connector = connector;
    }

    protected void initialise(UMOEndpointURI endpoint) throws MessagingException {
        if (!initialised.get()) {
            String inbox = null;
            if (connector.getProtocol().equals("imap") && endpoint.getParams().get("folder") != null) {
                inbox = (String) endpoint.getParams().get("folder");
            } else {
                inbox = Pop3Connector.MAILBOX;
            }

            URLName url = new URLName(endpoint.getScheme(), endpoint.getHost(), endpoint.getPort(), inbox, endpoint.getUsername(), endpoint.getPassword());

            session = MailUtils.createMailSession(url, connector);
            session.setDebug(logger.isDebugEnabled());

            Store store = session.getStore(url);
            store.connect();
            folder = store.getFolder(inbox);
            if (!folder.isOpen()) {
                try {
                    // Depending on Server implementation it's not always
                    // necessary
                    // to open the folder to check it
                    // Opening folders can be exprensive!
                    // folder.open(Folder.READ_ONLY);
                    folder.open(Folder.READ_WRITE);
                } catch (MessagingException e) {
                    logger.warn("Failed to open folder: " + folder.getFullName(), e);
                }
            }
        }
    }

    /**
     * 
     * @param event
     * @throws UnsupportedOperationException
     */
    public void doDispatch(UMOEvent event) throws Exception {
        throw new UnsupportedOperationException("Cannot dispatch from a Pop3 connection");
    }

    /**
     * 
     * @param event
     * @return
     * @throws UnsupportedOperationException
     */
    public UMOMessage doSend(UMOEvent event) throws Exception {
        throw new UnsupportedOperationException("Cannot send from a Pop3 connection");
    }

    /**
     * Endpoint can be in the form of pop3://username:password@pop3.muleumo.org
     * 
     * @param endpointUri
     * @param timeout
     * @return
     * @throws Exception
     */
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        initialise(endpointUri);

        long t0 = System.currentTimeMillis();
        if (timeout < 0) {
            timeout = Long.MAX_VALUE;
        }
        do {
            int count = folder.getMessageCount();
            if (count > 0) {
                Message message = folder.getMessage(1);
                // so we don't get the same message again
                message.setFlag(Flags.Flag.DELETED, true);
                return new MuleMessage(connector.getMessageAdapter(message));
            } else if (count == -1) {
                throw new MessagingException("Cannot monitor folder: " + folder.getFullName() + " as folder is closed");
            }
            long sleep = Math.min(this.connector.getCheckFrequency(), timeout - (System.currentTimeMillis() - t0));
            if (sleep > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No results, sleeping for " + sleep);
                }
                Thread.sleep(sleep);
            } else {
                logger.debug("Timeout");
                return null;
            }
        } while (true);
    }

    public Object getDelegateSession() throws UMOException {
        return session;
    }

    public UMOConnector getConnector() {
        return connector;
    }

    public void doDispose() {
        initialised.set(false);
        // close and expunge deleted messages
        try {
            if (folder != null) {
                folder.close(true);
            }
        } catch (MessagingException e) {
            logger.error("Failed to close pop3 inbox: " + e.getMessage(), e);
        }
    }
}
