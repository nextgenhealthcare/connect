/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email;

import javax.mail.Authenticator;

import org.mule.umo.provider.UMOConnector;

/**
 * Implemented by mail connectors to provide Mule with a Mail authenticator
 * object
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.7 $
 */
public interface MailConnector extends UMOConnector {
    public Authenticator getAuthenticator();

    public String getPort();

    public String getEmailSecure();

    public boolean isUseAuthentication();
}
