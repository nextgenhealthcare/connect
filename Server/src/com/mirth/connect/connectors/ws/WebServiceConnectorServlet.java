/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.connectors.ws.DefinitionServiceMap.DefinitionPortMap;
import com.mirth.connect.connectors.ws.DefinitionServiceMap.PortInformation;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.util.ConnectorUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ConnectionTestResponse;

public class WebServiceConnectorServlet extends MirthServlet implements WebServiceConnectorServletInterface {

    protected static final int MAX_TIMEOUT = 300000;
    protected static final TemplateValueReplacer replacer = new TemplateValueReplacer();

    /**
     * These nested maps fan out from the WSDL URL, to the service QName, to the port QName, to
     * either a list of operation names or a WsdlInterface object.
     */
    private static final Map<String, DefinitionServiceMap> definitionCache = new HashMap<String, DefinitionServiceMap>();
    private static final Map<String, Map<String, Map<String, WsdlInterface>>> wsdlInterfaceCache = new HashMap<String, Map<String, Map<String, WsdlInterface>>>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Logger logger = Logger.getLogger(WebServiceConnectorServlet.class);

    static {
        SoapUI.setSoapUICore(new EmbeddedSoapUICore());
    }

    public WebServiceConnectorServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public Object cacheWsdlFromUrl(String channelId, String channelName, WebServiceDispatcherProperties properties) {
        try {
            String wsdlUrl = getWsdlUrl(channelId, channelName, properties.getWsdlUrl(), properties.getUsername(), properties.getPassword());
            cacheWsdlInterfaces(wsdlUrl, getWsdlInterfaces(wsdlUrl, properties, channelId));
            return null;
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public boolean isWsdlCached(String channelId, String channelName, String wsdlUrl, String username, String password) {
        try {
            return definitionCache.get(getWsdlUrl(channelId, channelName, wsdlUrl, username, password)) != null;
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public DefinitionServiceMap getDefinition(String channelId, String channelName, String wsdlUrl, String username, String password) {
        try {
            wsdlUrl = getWsdlUrl(channelId, channelName, wsdlUrl, username, password);
            DefinitionServiceMap definition = definitionCache.get(wsdlUrl);
            if (definition == null) {
                throw new Exception("WSDL not cached for URL: " + wsdlUrl);
            }
            return definition;
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public String generateEnvelope(String channelId, String channelName, String wsdlUrl, String username, String password, String service, String port, String operation, boolean buildOptional) {
        try {
            wsdlUrl = getWsdlUrl(channelId, channelName, wsdlUrl, username, password);
            return buildEnvelope(getCachedWsdlInterface(wsdlUrl, service, port, channelId, channelName), operation, buildOptional);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public String getSoapAction(String channelId, String channelName, String wsdlUrl, String username, String password, String service, String port, String operation) {
        try {
            wsdlUrl = getWsdlUrl(channelId, channelName, wsdlUrl, username, password);
            WsdlInterface wsdlInterface = getCachedWsdlInterface(wsdlUrl, service, port, channelId, channelName);
            return wsdlInterface.getOperationByName(operation).getAction();
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public ConnectionTestResponse testConnection(String channelId, String channelName, WebServiceDispatcherProperties properties) {
        try {
            // Test the Location URI first if populated. Otherwise test the WSDL URL
            if (StringUtils.isNotBlank(properties.getLocationURI())) {
                return testConnection(channelId, channelName, properties.getLocationURI());
            } else if (StringUtils.isNotBlank(properties.getWsdlUrl())) {
                return testConnection(channelId, channelName, properties.getWsdlUrl());
            } else {
                throw new Exception("Both WSDL URL and Location URI are blank. At least one must be populated in order to test connection.");
            }
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    protected ConnectionTestResponse testConnection(String channelId, String channelName, String urlString) throws Exception {
        URL url = new URL(replacer.replaceValues(urlString, channelId, channelName));
        int port = url.getPort();
        // If no port was provided, default to port 80 or 443.
        return ConnectorUtil.testConnection(url.getHost(), (port == -1) ? (StringUtils.equalsIgnoreCase(url.getProtocol(), "https") ? 443 : 80) : port, MAX_TIMEOUT);
    }

    protected String getWsdlUrl(String channelId, String channelName, String wsdlUrl, String username, String password) throws Exception {
        wsdlUrl = replacer.replaceValues(wsdlUrl, channelId, channelName);
        username = replacer.replaceValues(username, channelId, channelName);
        password = replacer.replaceValues(password, channelId, channelName);
        return getURIWithCredentials(new URI(wsdlUrl), username, password).toURL().toString();
    }

    private WsdlInterface getCachedWsdlInterface(String wsdlUrl, String service, String port, String channelId, String channelName) throws Exception {
        service = replacer.replaceValues(service, channelId, channelName);
        port = replacer.replaceValues(port, channelId, channelName);

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

    public URI getURIWithCredentials(URI wsdlUrl, String username, String password) throws URISyntaxException {
        /* add the username:password to the URL if using authentication */
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            String hostWithCredentials = username + ":" + password + "@" + wsdlUrl.getHost();
            if (wsdlUrl.getPort() > -1) {
                hostWithCredentials += ":" + wsdlUrl.getPort();
            }
            wsdlUrl = new URI(wsdlUrl.getScheme(), hostWithCredentials, wsdlUrl.getPath(), wsdlUrl.getQuery(), wsdlUrl.getFragment());
        }

        return wsdlUrl;
    }

    /*
     * Retrieves the WSDL interface from the specified URL (with optional credentials). Uses a
     * Future to execute the request in the background and timeout after 30 seconds if the server
     * could not be contacted.
     */
    private WsdlInterface[] getWsdlInterfaces(String wsdlUrl, WebServiceDispatcherProperties props, String channelId) throws Exception {
        WsdlProject wsdlProject = new WsdlProjectFactory().createNew();
        WsdlLoader wsdlLoader = new UrlWsdlLoader(wsdlUrl);
        int timeout = NumberUtils.toInt(props.getSocketTimeout());
        return importWsdlInterfaces(wsdlProject, wsdlUrl, wsdlLoader, timeout);
    }

    public WsdlInterface[] importWsdlInterfaces(final WsdlProject wsdlProject, final String wsdlUrl, final WsdlLoader wsdlLoader, int timeout) throws Exception {
        try {
            Future<WsdlInterface[]> future = executor.submit(new Callable<WsdlInterface[]>() {
                public WsdlInterface[] call() throws Exception {
                    return WsdlInterfaceFactory.importWsdl(wsdlProject, wsdlUrl, false, wsdlLoader);
                }
            });

            if (timeout > 0) {
                timeout = Math.min(timeout, MAX_TIMEOUT);
            } else {
                timeout = MAX_TIMEOUT;
            }
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            wsdlLoader.abort();
            if (e instanceof TimeoutException) {
                e = new Exception("WSDL import operation timed out");
            }
            throw e;
        }
    }

    public void cacheWsdlInterfaces(String wsdlUrl, WsdlInterface[] wsdlInterfaces) throws Exception {
        if (ArrayUtils.isNotEmpty(wsdlInterfaces)) {
            Definition definition = wsdlInterfaces[0].getWsdlContext().getDefinition();
            DefinitionServiceMap definitionServiceMap = new DefinitionServiceMap();
            Map<String, Map<String, WsdlInterface>> wsdlInterfaceServiceMap = new LinkedHashMap<String, Map<String, WsdlInterface>>();

            if (MapUtils.isNotEmpty(definition.getServices())) {
                for (Object serviceObject : definition.getServices().values()) {
                    Service service = (Service) serviceObject;
                    logger.debug("Service: " + service.getQName().toString());
                    DefinitionPortMap definitionPortMap = new DefinitionPortMap();
                    Map<String, WsdlInterface> wsdlInterfacePortMap = new LinkedHashMap<String, WsdlInterface>();

                    if (MapUtils.isNotEmpty(service.getPorts())) {
                        for (Object portObject : service.getPorts().values()) {
                            Port port = (Port) portObject;
                            String portQName = new QName(service.getQName().getNamespaceURI(), port.getName()).toString();
                            logger.debug("    Port: " + portQName);

                            String locationURI = null;
                            for (Object element : port.getExtensibilityElements()) {
                                if (element instanceof SOAPAddress) {
                                    locationURI = ((SOAPAddress) element).getLocationURI();
                                } else if (element instanceof SOAP12Address) {
                                    locationURI = ((SOAP12Address) element).getLocationURI();
                                } else if (element instanceof HTTPAddress) {
                                    locationURI = ((HTTPAddress) element).getLocationURI();
                                } else if (element instanceof HTTPOperation) {
                                    locationURI = ((HTTPOperation) element).getLocationURI();
                                }
                            }

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
                                List<String> actions = new ArrayList<String>();

                                for (String operation : operations) {
                                    actions.add(bindingInterface.getOperationByName(operation).getAction());
                                }

                                definitionPortMap.getMap().put(portQName, new PortInformation(operations, actions, locationURI));
                                wsdlInterfacePortMap.put(portQName, bindingInterface);
                            }
                        }
                    }

                    definitionServiceMap.getMap().put(service.getQName().toString(), definitionPortMap);
                    wsdlInterfaceServiceMap.put(service.getQName().toString(), wsdlInterfacePortMap);
                }
            }

            definitionCache.put(wsdlUrl, definitionServiceMap);
            wsdlInterfaceCache.put(wsdlUrl, wsdlInterfaceServiceMap);
        } else {
            throw new Exception("Could not find any definitions in " + wsdlUrl);
        }
    }

    private String buildEnvelope(WsdlInterface wsdlInterface, String operationName, boolean buildOptional) throws Exception {
        SoapMessageBuilder messageBuilder = wsdlInterface.getMessageBuilder();
        BindingOperation bindingOperation = wsdlInterface.getOperationByName(operationName).getBindingOperation();
        return messageBuilder.buildSoapMessageFromInput(bindingOperation, buildOptional);
    }

    /*
     * This is needed to prevent soapUI from disabling logging for the entire application.
     */
    private static class EmbeddedSoapUICore extends DefaultSoapUICore {
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