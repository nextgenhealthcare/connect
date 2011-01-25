/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingOperation;
import javax.wsdl.PortType;
import javax.wsdl.Service;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlLoader;
import com.eviware.soapui.model.iface.Operation;
import com.mirth.connect.connectors.ConnectorService;

public class WebServiceConnectorService implements ConnectorService {
    private static Map<String, WsdlInterface> wsdlInterfaceCache = new HashMap<String, WsdlInterface>();

    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("cacheWsdlFromUrl")) {
            Map<String, String> params = (Map<String, String>) object;
            String wsdlUrl = params.get("wsdlUrl");
            URI wsdlUri = new URI(wsdlUrl);
            String username = params.get("username");
            String password = params.get("password");
            wsdlInterfaceCache.put(wsdlUrl, getWsdlInterface(wsdlUri, username, password));
        } else if (method.equals("isWsdlCached")) {
            String id = (String) object;
            return (wsdlInterfaceCache.get(id) != null);
        } else if (method.equals("getOperations")) {
            String id = (String) object;
            WsdlInterface wsdlInterface = wsdlInterfaceCache.get(id);
            return getOperations(wsdlInterface);
        } else if (method.equals("getService")) {
            String id = (String) object;
            WsdlInterface wsdlInterface = wsdlInterfaceCache.get(id);

            if (MapUtils.isNotEmpty(wsdlInterface.getWsdlContext().getDefinition().getServices())) {
                Service service = (Service) wsdlInterface.getWsdlContext().getDefinition().getServices().values().iterator().next();
                return service.getQName().toString();
            }
        } else if (method.equals("getPort")) {
            String id = (String) object;
            WsdlInterface wsdlInterface = wsdlInterfaceCache.get(id);

            if (MapUtils.isNotEmpty(wsdlInterface.getWsdlContext().getDefinition().getPortTypes())) {
                PortType portType = (PortType) wsdlInterface.getWsdlContext().getDefinition().getPortTypes().values().iterator().next();
                return portType.getQName().toString();
            }
        } else if (method.equals("generateEnvelope")) {
            Map<String, String> params = (Map<String, String>) object;
            String id = params.get("id");
            String operationName = params.get("operation");
            WsdlInterface wsdlInterface = wsdlInterfaceCache.get(id);
            return buildEnvelope(wsdlInterface, operationName);
        } else if (method.equals("getSoapAction")) {
            Map<String, String> params = (Map<String, String>) object;
            String id = params.get("id");
            String operationName = params.get("operation");
            WsdlInterface wsdlInterface = wsdlInterfaceCache.get(id);
            return wsdlInterface.getOperationByName(operationName).getAction();
        }

        return null;
    }

    private WsdlInterface getWsdlInterface(URI wsdlUrl, String username, String password) throws Exception {
        // add the username:password to the URL if using authentication
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            String hostWithCredentials = username + ":" + password + "@" + wsdlUrl.getHost();
            wsdlUrl = new URI(wsdlUrl.getScheme(), hostWithCredentials, wsdlUrl.getPath(), wsdlUrl.getQuery(), wsdlUrl.getFragment());
        }

        // create a new soapUI project
        WsdlProject wsdlProject = new WsdlProjectFactory().createNew();

        // import the WSDL interface
        WsdlLoader wsdlLoader = new UrlWsdlLoader(wsdlUrl.toURL().toString());
        WsdlInterface[] wsdlInterfaces = WsdlInterfaceFactory.importWsdl(wsdlProject, wsdlUrl.toURL().toString(), false, wsdlLoader);

        return wsdlInterfaces[0];
    }

    private List<String> getOperations(WsdlInterface wsdlInterface) {
        List<String> operations = new ArrayList<String>();

        for (Operation operation : wsdlInterface.getOperationList()) {
            operations.add(operation.getName());
        }

        return operations;
    }

    private String buildEnvelope(WsdlInterface wsdlInterface, String operationName) throws Exception {
        SoapMessageBuilder messageBuilder = wsdlInterface.getMessageBuilder();
        BindingOperation bindingOperation = wsdlInterface.getOperationByName(operationName).getBindingOperation();
        return messageBuilder.buildSoapMessageFromInput(bindingOperation, true);
    }
}
