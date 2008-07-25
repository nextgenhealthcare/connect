package com.webreach.mirth.connectors.soap;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.server.util.WebServiceReader;

public class SoapConnectorService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) {
        if (method.equals("getWebServiceDefinition")) {
            try {
                String address = (String) object;
                WebServiceReader wsReader = new WebServiceReader(address);
                return wsReader.getWSDefinition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
