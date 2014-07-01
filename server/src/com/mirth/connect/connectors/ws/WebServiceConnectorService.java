/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import static com.mirth.connect.connectors.ws.WebServiceConnectorServiceMethods.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import com.eviware.soapui.model.settings.Settings;
import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class WebServiceConnectorService implements ConnectorService {
    /**
     * These nested maps fan out from the WSDL URL, to the service QName, to the port QName, to
     * either a list of operation names or a WsdlInterface object.
     */
    private static Map<String, Map<String, Map<String, List<String>>>> definitionCache = new HashMap<String, Map<String, Map<String, List<String>>>>();
    private static Map<String, Map<String, Map<String, WsdlInterface>>> wsdlInterfaceCache = new HashMap<String, Map<String, Map<String, WsdlInterface>>>();
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private Logger logger = Logger.getLogger(getClass());

    public Object invoke(String channelId, String method, Object object, String sessionsId) throws Exception {
        Map<String, Object> params = (Map<String, Object>) object;
        String wsdlUrl = replacer.replaceValues((String) params.get("wsdlUrl"), channelId);

        if (method.equals(CACHE_WSDL_FROM_URL)) {
            String username = replacer.replaceValues((String) params.get("username"), channelId);
            String password = replacer.replaceValues((String) params.get("password"), channelId);

            WsdlInterface[] wsdlInterfaces = getWsdlInterfaces(new URI(wsdlUrl), username, password);

            if (ArrayUtils.isNotEmpty(wsdlInterfaces)) {
                Definition definition = wsdlInterfaces[0].getWsdlContext().getDefinition();
                Map<String, Map<String, List<String>>> definitionServiceMap = new LinkedHashMap<String, Map<String, List<String>>>();
                Map<String, Map<String, WsdlInterface>> wsdlInterfaceServiceMap = new LinkedHashMap<String, Map<String, WsdlInterface>>();

                if (MapUtils.isNotEmpty(definition.getServices())) {
                    for (Object serviceObject : definition.getServices().values()) {
                        Service service = (Service) serviceObject;
                        logger.debug("Service: " + service.getQName().toString());
                        Map<String, List<String>> definitionPortMap = new LinkedHashMap<String, List<String>>();
                        Map<String, WsdlInterface> wsdlInterfacePortMap = new LinkedHashMap<String, WsdlInterface>();

                        if (MapUtils.isNotEmpty(service.getPorts())) {
                            for (Object portObject : service.getPorts().values()) {
                                Port port = (Port) portObject;
                                String portQName = new QName(service.getQName().getNamespaceURI(), port.getName()).toString();
                                logger.debug("    Port: " + portQName);

                                List<String> operations = new ArrayList<String>();
                                for (Object bindingOperation : port.getBinding().getBindingOperations()) {
                                    String operationName = ((BindingOperation) bindingOperation).getName();
                                    logger.debug("        Operation: " + operationName);
                                    operations.add(operationName);
                                }

                                WsdlInterface bindingInterface = null;
                                for (WsdlInterface wsdlInterface : wsdlInterfaces) {
                                    if (wsdlInterface.getBindingName().equals(port.getBinding().getQName())) {
                                        bindingInterface = wsdlInterface;
                                    }
                                }
                                logger.debug("        Interface: " + bindingInterface);
                                if (bindingInterface != null) {
                                    definitionPortMap.put(portQName, operations);
                                    wsdlInterfacePortMap.put(portQName, bindingInterface);
                                }
                            }
                        }

                        definitionServiceMap.put(service.getQName().toString(), definitionPortMap);
                        wsdlInterfaceServiceMap.put(service.getQName().toString(), wsdlInterfacePortMap);
                    }
                }

                definitionCache.put(wsdlUrl, definitionServiceMap);
                wsdlInterfaceCache.put(wsdlUrl, wsdlInterfaceServiceMap);
            } else {
                throw new Exception("Could not find any definitions in " + wsdlUrl);
            }
        } else if (method.equals(IS_WSDL_CACHED)) {
            return definitionCache.get(wsdlUrl) != null;
        } else if (method.equals(GET_DEFINITION)) {
            Map<String, Map<String, List<String>>> definition = definitionCache.get(wsdlUrl);
            if (definition == null) {
                throw new Exception("WSDL not cached for URL: " + wsdlUrl);
            }
            return definition;
        } else if (method.equals(GENERATE_ENVELOPE)) {
            return buildEnvelope(getCachedWsdlInterface(wsdlUrl, params, channelId), (String) params.get("operation"));
        } else if (method.equals(GET_SOAP_ACTION)) {
            WsdlInterface wsdlInterface = getCachedWsdlInterface(wsdlUrl, params, channelId);
            return wsdlInterface.getOperationByName((String) params.get("operation")).getAction();
        }

        return null;
    }

    private WsdlInterface getCachedWsdlInterface(String wsdlUrl, Map<String, Object> params, String channelId) throws Exception {
        String service = replacer.replaceValues((String) params.get("service"), channelId);
        String port = replacer.replaceValues((String) params.get("port"), channelId);

        Map<String, Map<String, WsdlInterface>> serviceMap = wsdlInterfaceCache.get(wsdlUrl);
        if (serviceMap == null) {
            throw new Exception("WSDL not cached for URL: " + wsdlUrl);
        }

        Map<String, WsdlInterface> portMap = serviceMap.get(service);
        if (portMap == null) {
            throw new Exception("No service \"" + service + "\" found in cached WSDL.");
        }

        if (portMap.containsKey(port)) {
            WsdlInterface wsdlInterface = portMap.get(port);
            if (wsdlInterface == null) {
                throw new Exception("No interface found for port \"" + port + "\" and service \"" + service + "\" in cached WSDL.");
            }
            return wsdlInterface;
        } else {
            throw new Exception("No port \"" + port + "\" found for service \"" + service + "\" in cached WSDL.");
        }
    }

    /*
     * Retrieves the WSDL interface from the specified URL (with optional credentials). Uses a
     * Future to execute the request in the background and timeout after 30 seconds if the server
     * could not be contacted.
     */
    private WsdlInterface[] getWsdlInterfaces(URI wsdlUrl, String username, String password) throws Exception {
        /* add the username:password to the URL if using authentication */
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            String hostWithCredentials = username + ":" + password + "@" + wsdlUrl.getHost();
            if (wsdlUrl.getPort() > -1) {
                hostWithCredentials += ":" + wsdlUrl.getPort();
            }
            wsdlUrl = new URI(wsdlUrl.getScheme(), hostWithCredentials, wsdlUrl.getPath(), wsdlUrl.getQuery(), wsdlUrl.getFragment());
        }

        SoapUI.setSoapUICore(new EmbeddedSoapUICore());
        WsdlProject wsdlProject = new WsdlProjectFactory().createNew();
        WsdlLoader wsdlLoader = new UrlWsdlLoader(wsdlUrl.toURL().toString());

        try {
            Future<WsdlInterface[]> future = importWsdlInterface(wsdlProject, wsdlUrl, wsdlLoader);
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            wsdlLoader.abort();
            throw e;
        }
    }

    private String buildEnvelope(WsdlInterface wsdlInterface, String operationName) throws Exception {
        SoapMessageBuilder messageBuilder = wsdlInterface.getMessageBuilder();
        BindingOperation bindingOperation = wsdlInterface.getOperationByName(operationName).getBindingOperation();
        return messageBuilder.buildSoapMessageFromInput(bindingOperation, true);
    }

    private Future<WsdlInterface[]> importWsdlInterface(final WsdlProject wsdlProject, final URI newWsdlUrl, final WsdlLoader wsdlLoader) {
        return executor.submit(new Callable<WsdlInterface[]>() {
            public WsdlInterface[] call() throws Exception {
                return WsdlInterfaceFactory.importWsdl(wsdlProject, newWsdlUrl.toURL().toString(), false, wsdlLoader);
            }
        });
    }

    /*
     * This is needed to prevent soapUI from disabling logging for the entire application.
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
