package com.mirth.connect.connectors.http;

import java.net.URL;
import java.util.Map;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.ConnectorUtil;

public class HttpConnectorService implements ConnectorService {
    private static final int TIMEOUT = 5000;

    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            Map<String, String> params = (Map<String, String>) object;
            URL url = new URL(params.get(HttpSenderProperties.HTTP_URL));
            return ConnectorUtil.testConnection(url.getHost(), url.getPort(), TIMEOUT);
        }

        return null;
    }
}
