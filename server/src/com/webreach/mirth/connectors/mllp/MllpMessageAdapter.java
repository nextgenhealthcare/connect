/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.mllp;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

public class MllpMessageAdapter extends AbstractMessageAdapter {
    // ast: an HL7 message is really a String
    private String message;

    public MllpMessageAdapter(Object message) throws MessagingException {
        if (message instanceof byte[]) {
            this.message = new String((byte[]) message);
        } else if (message instanceof String) {
            this.message = (String) message;
        } else {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
    }

    public String getPayloadAsString() throws Exception {
        return message;
    }

    public byte[] getPayloadAsBytes() throws Exception {
        return message.getBytes();
    }

    // ast: select the encoding in which we whant the message
    public byte[] getPayloadAsBytes(String encoding) throws Exception {
        byte[] r;
        try {
            r = message.getBytes(encoding);
        } catch (Exception e) {
            logger.error("The charset " + encoding + " can't be used in this JVM " + e);
            r = message.getBytes();
        }
        return r;
    }

    public Object getPayload() {
        return message;
    }
}
