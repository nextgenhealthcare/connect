/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import com.mirth.connect.server.controllers.ControllerException;

/**
 * Utility class used to create SMTPConnection object using the server's default SMTP settings.
 */
public class SMTPConnectionFactory {
    private SMTPConnectionFactory() {}

    /**
     * Creates an create SMTPConnection object using the server's default SMTP settings.
     * 
     * @return The instantiated SMTPConnection object.
     * @throws ControllerException
     */
    public static SMTPConnection createSMTPConnection() throws ControllerException {
        return new SMTPConnection(com.mirth.connect.server.util.ServerSMTPConnectionFactory.createSMTPConnection());
    }
}
