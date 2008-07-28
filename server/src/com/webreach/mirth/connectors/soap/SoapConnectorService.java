package com.webreach.mirth.connectors.soap;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.server.util.WebServiceReader;

public class SoapConnectorService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("getWebServiceDefinition")) {
            try {
                String address = (String) object;
                WebServiceReader wsReader = new WebServiceReader(address);
                return wsReader.getWSDefinition();
            } catch (Exception e) {
                throw new Exception("Could not retrieve web service definitions.", e);
            }
        }

        return null;
    }
}
