/*
 * $Header: /home/projects/mule/scm/mule/providers/email/src/java/org/mule/providers/email/MailConnector.java,v 1.7 2005/10/10 14:00:15 rossmason Exp $
 * $Revision: 1.7 $
 * $Date: 2005/10/10 14:00:15 $
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

import org.mule.umo.provider.UMOConnector;

import javax.mail.Authenticator;

/**
 * Implemented by  mail connectors to provide Mule with a Mail authenticator object
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.7 $
 */
public interface MailConnector extends UMOConnector
 {
    public Authenticator getAuthenticator();

    public int getPort();
}
