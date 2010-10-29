/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

public class DICOMMessageAdapter extends AbstractMessageAdapter {

    private byte[] message;

    public DICOMMessageAdapter(Object message) throws MessagingException {
        if (message instanceof byte[]) {
            this.message = (byte[]) message;
        } else if (message instanceof String) {
            this.message = ((String) message).getBytes();
        } else {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
    }

    public String getPayloadAsString() throws Exception {
        return new String(message);
    }

    public byte[] getPayloadAsBytes() throws Exception {
        return message;
    }

    public Object getPayload() {
        return message;
    }

}