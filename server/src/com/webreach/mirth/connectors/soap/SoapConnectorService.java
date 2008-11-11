package com.webreach.mirth.connectors.soap;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.server.util.WebServiceReader;

public class SoapConnectorService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {

        boolean isURL = true;
        if (method.equals("getWebServiceDefinition")) {
            isURL = true;
        } else if (method.equals("getWebServiceDefinitionFromFile")) {
            isURL = false;
        }

        try {
            String wsdlSource = (String) object;
            WebServiceReader wsReader = new WebServiceReader(wsdlSource, isURL);
            return wsReader.getWSDefinition();
        } catch (Exception e) {
            throw new Exception("Could not retrieve web service definitions.", e);
        }
        
    }
}
