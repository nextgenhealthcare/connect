/*
 * $Header: /home/projects/mule/scm/mule/providers/email/src/java/org/mule/providers/email/Pop3Connector.java,v 1.13 2005/10/23 15:29:43 holger Exp $
 * $Revision: 1.13 $
 * $Date: 2005/10/23 15:29:43 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.server.mule.providers.email;

import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.provider.UMOMessageReceiver;

import javax.mail.Authenticator;

/**
 * <code>Pop3Connector</code> is used to connect and receive mail from a pop3
 * mailbox
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.13 $
 */
public class Pop3Connector extends AbstractServiceEnabledConnector implements MailConnector
{
    public static final String MAILBOX = "INBOX";
    public static final int DEFAULT_POP3_PORT = 110;
    public static final int DEFAULT_CHECK_FREQUENCY = 60000;

    /**
     * Holds the time in milliseconds that the endpoint should wait before
     * checking a mailbox
     */
    private long checkFrequency = DEFAULT_CHECK_FREQUENCY;

    /**
     * holds a path where messages should be backed up to
     */
    private String backupFolder = null;

     /**
     * A custom authenticator to bew used on any mail sessions created with this connector
     * This will only be used if user name credendtials are set on the endpoint
     */
    private Authenticator authenticator = null;

    /**
     * Default mail port if one is not set
     */
    private int port = DEFAULT_POP3_PORT;

    /**
     * @return
     */
    public long getCheckFrequency()
    {
        return checkFrequency;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#getProtocol()
     */
    public String getProtocol()
    {
        return "pop3";
    }

    /**
     * @param l
     */
    public void setCheckFrequency(long l)
    {
        if (l < 1) {
            l = DEFAULT_CHECK_FREQUENCY;
        }
        checkFrequency = l;
    }

    /**
     * @return
     */
    public String getBackupFolder()
    {
        return backupFolder;
    }

    /**
     * @param string
     */
    public void setBackupFolder(String string)
    {
        backupFolder = string;
    }

    /* (non-Javadoc) 
     * @see org.mule.providers.UMOConnector#registerListener(javax.jms.MessageListener, java.lang.String) 
     */ 
    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception 
    {
        Object[] args = {new Long(checkFrequency), backupFolder};
        return serviceDescriptor.createMessageReceiver(this, component, endpoint, args); 
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
