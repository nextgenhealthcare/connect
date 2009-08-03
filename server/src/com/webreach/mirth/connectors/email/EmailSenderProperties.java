/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.connectors.email;

import java.util.ArrayList;
import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class EmailSenderProperties implements ComponentProperties
{
    public static final String name = "Email Sender";

    public static final String DATATYPE = "DataType";
    public static final String EMAIL_ADDRESS = "hostname";
    public static final String EMAIL_PORT = "smtpPort";
    public static final String EMAIL_SECURE = "emailSecure";
    public static final String EMAIL_USE_AUTHENTICATION = "useAuthentication";
    public static final String EMAIL_USERNAME = "username";
    public static final String EMAIL_PASSWORD = "password";
    public static final String EMAIL_TO = "toAddresses";
    public static final String EMAIL_FROM = "fromAddress";
    public static final String EMAIL_SUBJECT = "subject";
    public static final String EMAIL_BODY = "body";
    public static final String EMAIL_REPLY_TO = "replyToAddresses";
    public static final String EMAIL_CONTENT_TYPE = "contentType";
    public static final String EMAIL_ATTACHMENT_NAMES = "attachmentNames";
    public static final String EMAIL_ATTACHMENT_CONTENTS = "attachmentContents";
    public static final String EMAIL_ATTACHMENT_TYPES = "attachmentTypes";

    public Properties getDefaults()
    {
    	Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(EMAIL_ADDRESS, "");
        properties.put(EMAIL_PORT, "");
        properties.put(EMAIL_SECURE, "none");
        properties.put(EMAIL_USE_AUTHENTICATION, "1");
        properties.put(EMAIL_USERNAME, "");
        properties.put(EMAIL_PASSWORD, "");
        properties.put(EMAIL_TO, "");
        properties.put(EMAIL_FROM, "");
        properties.put(EMAIL_SUBJECT, "");
        properties.put(EMAIL_BODY, "");
        properties.put(EMAIL_CONTENT_TYPE, "text/plain");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(EMAIL_ATTACHMENT_NAMES, serializer.toXML(new ArrayList()));
        properties.put(EMAIL_ATTACHMENT_CONTENTS, serializer.toXML(new ArrayList()));
        properties.put(EMAIL_ATTACHMENT_TYPES, serializer.toXML(new ArrayList()));
        return properties;
    }

    public static String getInformation(Properties properties) {
        return "From: " + properties.getProperty(EMAIL_FROM) +
               "   To: " + properties.getProperty(EMAIL_TO) +
               "   SMTP Info: " + properties.getProperty(EMAIL_ADDRESS) + ":" + properties.getProperty(EMAIL_PORT);
    }
}
