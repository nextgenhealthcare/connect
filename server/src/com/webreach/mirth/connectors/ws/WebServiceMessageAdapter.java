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
