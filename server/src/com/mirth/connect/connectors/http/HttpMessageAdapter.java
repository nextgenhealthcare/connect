package com.mirth.connect.connectors.http;

import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.mortbay.http.HttpRequest;
import org.mule.providers.AbstractMessageAdapter;

public class HttpMessageAdapter extends AbstractMessageAdapter {
    private HttpRequest request = null;
    private Logger logger = Logger.getLogger(this.getClass());
    
    public HttpMessageAdapter(HttpRequest request) {
        this.request = request;
    }

    public Object getPayload() {
        HttpMessageConverter converter = new HttpMessageConverter();
        
        try {
            if (getBooleanProperty("includeHeaders", false)) {
                return converter.httpRequestToXml(request);
            } else {
                if (request.getCharacterEncoding() != null) {
                    return converter.convertInputStreamToString(request.getInputStream(), request.getCharacterEncoding());
                } else {
                    return converter.convertInputStreamToString(request.getInputStream(), Charset.defaultCharset().name());
                }
            }
        } catch (Exception e) {
            logger.error("Error converting HTTP request.", e);
        }

        return null;
    }

    public byte[] getPayloadAsBytes() throws Exception {
        return ((String) getPayload()).getBytes();
    }

    public String getPayloadAsString() throws Exception {
        return (String) getPayload();
    }

}