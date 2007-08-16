/*
 * $Header: /home/projects/mule/scm/mule/providers/email/src/java/org/mule/providers/email/ImapConnector.java,v 1.2 2005/10/24 10:24:27 rossmason Exp $
 * $Revision: 1.2 $
 * $Date: 2005/10/24 10:24:27 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.email;

/**
 * Receives messages from an Imap mailbox
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.2 $
 */
public class ImapConnector extends Pop3Connector {

    public static final String DEFAULT_IMAP_PORT = "143";

    public ImapConnector() {
        setPort(DEFAULT_IMAP_PORT);
    }

    public String getProtocol()
    {
        return "imap";
    }
}
