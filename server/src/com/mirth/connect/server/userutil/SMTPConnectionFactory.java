package com.mirth.connect.server.userutil;

import com.mirth.connect.server.controllers.ControllerException;

public class SMTPConnectionFactory {
    public static SMTPConnection createSMTPConnection() throws ControllerException {
        return new SMTPConnection(com.mirth.connect.server.util.SMTPConnectionFactory.createSMTPConnection());
    }
}
