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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.wsdl.BindingOperation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlLoader;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.settings.Settings;
import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class WebServiceConnectorService implements ConnectorService {
    private static Map<String, WsdlInterface> wsdlInterfaceCache = new HashMap<String, WsdlInterface>();
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public Object invoke(String channelId, String method, Object object, String sessionsId) throws Exception {
        if (method.equals("cacheWsdlFromUrl")) {
            Map<String, String> params = (Map<String, String>) object;
            String wsdlUrl = replacer.replaceValues(params.get("wsdlUrl"), channelId);
            URI wsdlUri = new URI(wsdlUrl);
            String username = replacer.replaceValues(params.get("username"), channelId);
            String password = replacer.replaceValues(params.get("password"), channelId);

            WsdlInterface wsdlInterface = getWsdlInterface(wsdlUri, username, password);
            if (wsdlInterface != null) {
                wsdlInterfaceCache.put(wsdlUrl, getWsdlInterface(wsdlUri, username, password));
            } else {
                throw new Exception("Could not find any definitions in " + wsdlUri);
            }
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

            if (MapUtils.isNotEmpty(wsdlInterface.getWsdlContext().getDefinition().getServices())) {
                Service service = (Service) wsdlInterface.getWsdlContext().getDefinition().getServices().values().iterator().next();
                Port port = (Port) service.getPorts().values().iterator().next();
                QName qName = new QName(service.getQName().getNamespaceURI(), port.getName());
                return qName.toString();
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

    /*
     * Retrieves the WSDL interface from the specified URL (with optional
     * credentials). Uses a Future to execute the request in the background and
     * timeout after 30 seconds if the server could not be contacted.
     */
    private WsdlInterface getWsdlInterface(URI wsdlUrl, String username, String password) throws Exception {
        /* add the username:password to the URL if using authentication */
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            String hostWithCredentials = username + ":" + password + "@" + wsdlUrl.getHost();
            if (wsdlUrl.getPort() > -1) {
                hostWithCredentials += ":" + wsdlUrl.getPort();
            }
            wsdlUrl = new URI(wsdlUrl.getScheme(), hostWithCredentials, wsdlUrl.getPath(), wsdlUrl.getQuery(), wsdlUrl.getFragment());
        }

        // 
        SoapUI.setSoapUICore(new EmbeddedSoapUICore());
        WsdlProject wsdlProject = new WsdlProjectFactory().createNew();
        WsdlLoader wsdlLoader = new UrlWsdlLoader(wsdlUrl.toURL().toString());

        try {
            Future<WsdlInterface> future = importWsdlInterface(wsdlProject, wsdlUrl, wsdlLoader);
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            wsdlLoader.abort();
            throw e;
        }
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

    private Future<WsdlInterface> importWsdlInterface(final WsdlProject wsdlProject, final URI newWsdlUrl, final WsdlLoader wsdlLoader) {
        return executor.submit(new Callable<WsdlInterface>() {
            public WsdlInterface call() throws Exception {
                WsdlInterface[] wsdlInterfaces = WsdlInterfaceFactory.importWsdl(wsdlProject, newWsdlUrl.toURL().toString(), false, wsdlLoader);
                return wsdlInterfaces.length > 0 ? wsdlInterfaces[0] : null;
            }
        });
    }

    /*
     * This is needed to prevent soapUI from disabling logging for the entire
     * application.
     */
    private class EmbeddedSoapUICore extends DefaultSoapUICore {
        @Override
        protected void initLog() {
            log = Logger.getLogger(DefaultSoapUICore.class);
        }

        @Override
        public Settings getSettings() {
            if (log == null) {
                initLog();
            }

            return super.getSettings();
        }
    }
}
