/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email;

/**
 * Mail properties that are sent on the MuleMessage when recieing a Mail Message
 * or can be set on the endpoint or event to be added to outgoing mail
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.1 $
 */
public interface MailProperties {
    /**
     * Event properties
     */
    public static final String CONTENT_TYPE_PROPERTY = "contentType";
    public static final String TO_ADDRESSES_PROPERTY = "toAddresses";
    public static final String CC_ADDRESSES_PROPERTY = "ccAddresses";
    public static final String BCC_ADDRESSES_PROPERTY = "bccAddresses";
    public static final String FROM_ADDRESS_PROPERTY = "fromAddress";
    public static final String REPLY_TO_ADDRESSES_PROPERTY = "replyToAddresses";
    public static final String SUBJECT_PROPERTY = "subject";
    public static final String CUSTOM_HEADERS_MAP_PROPERTY = "customHeaders";
    public static final String SENT_DATE_PROPERTY = "sentDate";
}
