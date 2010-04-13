package com.mirth.connect.connectors.http;

import org.apache.log4j.Logger;
import org.mortbay.http.HttpRequest;
import org.mule.providers.AbstractMessageAdapter;

public class HttpMessageAdapter extends AbstractMessageAdapter {
    private Logger logger = Logger.getLogger(this.getClass());
    private String message = null;

    public HttpMessageAdapter(HttpRequest request) {
        try {
            HttpMessageConverter converter = new HttpMessageConverter();
            
            if (getBooleanProperty("includeHeaders", false)) {
                message = converter.httpRequestToXml(request);
            } else {
                message = converter.convertInputStreamToString(request.getInputStream(), request.getCharacterEncoding());
            }
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }
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