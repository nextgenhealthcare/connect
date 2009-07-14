package com.webreach.mirth.connectors.mllp;

import java.util.Map;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.server.util.ConnectorUtil;

public class LLPSenderConnectorService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            Map<String, String> params = (Map<String, String>) object;
            String host = params.get(LLPSenderProperties.LLP_ADDRESS);
            int port = Integer.parseInt(params.get(LLPSenderProperties.LLP_PORT));
            int timeout = Integer.parseInt(params.get(LLPSenderProperties.LLP_SERVER_TIMEOUT));
            return ConnectorUtil.testConnection(host, port, timeout);
        }

        return null;
    }
}
