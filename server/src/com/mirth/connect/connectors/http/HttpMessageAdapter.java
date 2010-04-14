package com.mirth.connect.connectors.http;

import org.apache.log4j.Logger;
import org.mule.providers.AbstractMessageAdapter;

public class HttpMessageAdapter extends AbstractMessageAdapter {
    private Logger logger = Logger.getLogger(this.getClass());
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

        try {
            if (request.isIncludeHeaders()) {
                message = converter.httpRequestToXml(request);
            } else {
                message = request.getContent();
            }
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }
    }

}