/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import org.mule.providers.AbstractMessageAdapter;

public class HttpMessageAdapter extends AbstractMessageAdapter {

    private String message = null;

    public HttpMessageAdapter(HttpRequestMessage request) {
        setMessage(request);
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

    private void setMessage(HttpRequestMessage request) {
        HttpMessageConverter converter = new HttpMessageConverter();

        if (request.isIncludeHeaders()) {
            message = converter.httpRequestToXml(request);
        } else {
            message = request.getContent();
        }
    }

}