package com.webreach.mirth.connectors.mllp;

import java.util.Map;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.server.util.ConnectorUtil;

public class LLPListenerConnectorService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            Map<String, String> params = (Map<String, String>) object;
            String host = params.get(LLPListenerProperties.LLP_ADDRESS);
            int port = Integer.parseInt(params.get(LLPListenerProperties.LLP_PORT));
            int timeout = Integer.parseInt(params.get(LLPListenerProperties.LLP_RECEIVE_TIMEOUT));
            return ConnectorUtil.testConnection(host, port, timeout);
        }

        return null;
    }
}
