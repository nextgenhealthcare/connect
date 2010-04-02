/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.ws;

import org.mule.providers.AbstractMessageAdapter;

public class WebServiceMessageAdapter extends AbstractMessageAdapter {
    private String message;

    public WebServiceMessageAdapter(String message) {
        this.message = message;
    }

    public Object getPayload() {
        return message;
    }

    public byte[] getPayloadAsBytes() throws Exception {
        return message.getBytes();
    }

    public String getPayloadAsString() throws Exception {
        return message;
    }
}
