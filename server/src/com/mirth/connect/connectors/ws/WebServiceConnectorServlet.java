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
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
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
    private static final Map<String, Map<String, Map<String, Definition>>> wsdlInterfaceCache = new HashMap<String, Map<String, Map<String, Definition>>>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Logger logger = LogManager.getLogger(WebServiceConnectorServlet.class);  //log4j2.x

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
            cacheWsdlInterfaces(wsdlUrl, getDefinition(wsdlUrl, properties, channelId));
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
            return buildEnvelope(getCachedDefinition(wsdlUrl, service, port, channelId, channelName), operation, buildOptional);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public String getSoapAction(String channelId, String channelName, String wsdlUrl, String username, String password, String service, String port, String operation) {
        try {
            wsdlUrl = getWsdlUrl(channelId, channelName, wsdlUrl, username, password);
            Definition definition = getCachedDefinition(wsdlUrl, service, port, channelId, channelName);
            String soapOp = "";
            for (Object serviceObject : definition.getServices().values()) {
                Service defService = (Service) serviceObject;
                if (MapUtils.isNotEmpty(defService.getPorts())) {
                    for (Object portObject : defService.getPorts().values()) {
                        Port defPort = (Port) portObject;
                        List extensions = defPort.getExtensibilityElements();                    
                        if (extensions != null) {
                            for (int i = 0; i < extensions.size(); i++) {
                                ExtensibilityElement extElement = (ExtensibilityElement) extensions.get(i);
                                if (defPort.getName().equals(port)) {
                                    if (extElement instanceof SOAPOperation) {
                                        soapOp = ((SOAPOperation) extElement).toString();
                                    } else if (extElement instanceof SOAP12Operation) {
                                        soapOp = ((SOAP12Operation) extElement).toString();
                                    }
                                    return soapOp;
                                }
                            }
                        }
                    }
                }
            }
            return null;
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

    private Definition getCachedDefinition(String wsdlUrl, String service, String port, String channelId, String channelName) throws Exception {
        service = replacer.replaceValues(service, channelId, channelName);
        port = replacer.replaceValues(port, channelId, channelName);

        Map<String, Map<String, Definition>> serviceMap = wsdlInterfaceCache.get(wsdlUrl);
        if (serviceMap == null) {
            throw new Exception("WSDL not cached for URL: " + wsdlUrl);
        }

        Map<String, Definition> portMap = serviceMap.get(service);
        if (portMap == null) {
            throw new Exception("No service \"" + service + "\" found in cached WSDL.");
        }

        if (portMap.containsKey(port)) {
            Definition definition = portMap.get(port);
            if (definition == null) {
                throw new Exception("No interface found for port \"" + port + "\" and service \"" + service + "\" in cached WSDL.");
            }
            return definition;
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
    private Definition getDefinition(String wsdlUrl, WebServiceDispatcherProperties props, String channelId) throws Exception {
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        int timeout = NumberUtils.toInt(props.getSocketTimeout());
        return importWsdlInterfaces(wsdlFactory, wsdlUrl, wsdlReader, timeout);
    }

    public Definition importWsdlInterfaces(final WSDLFactory wsdlFactory, final String wsdlUrl, final WSDLReader wsdlReader, int timeout) throws Exception {
        try {
            Future<Definition> future = executor.submit(new Callable<Definition>() {
                public Definition call() throws Exception {
                    return wsdlReader.readWSDL(null, wsdlUrl);
                }
            });

            if (timeout > 0) {
                timeout = Math.min(timeout, MAX_TIMEOUT);
            } else {
                timeout = MAX_TIMEOUT;
            }
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (e instanceof TimeoutException) {
                e = new Exception("WSDL import operation timed out");
            }
            throw e;
        }
    }
      
    public void cacheWsdlInterfaces(String wsdlUrl, Definition definition) throws Exception {
        if (definition != null) {
            DefinitionServiceMap definitionServiceMap = new DefinitionServiceMap();
            Map<String, Map<String, Definition>> wsdlInterfaceServiceMap = new LinkedHashMap<String, Map<String, Definition>>();

            if (MapUtils.isNotEmpty(definition.getServices())) {
                for (Object serviceObject : definition.getServices().values()) {
                    Service service = (Service) serviceObject;
                    logger.debug("Service: " + service.getQName().toString());
                    DefinitionPortMap definitionPortMap = new DefinitionPortMap();
                    Map<String, Definition> wsdlInterfacePortMap = new LinkedHashMap<String, Definition>();

                    if (MapUtils.isNotEmpty(service.getPorts())) {
                        for (Object portObject : service.getPorts().values()) {
                            Port port = (Port) portObject;
                            String portQName = new QName(service.getQName().getNamespaceURI(), port.getName()).toString();
                            logger.debug("    Port: " + service);

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

                            List<String> actions = new ArrayList<String>();
                            if (port.getBinding().getBindingOperations() != null) {
                                for (Object bindOperationObject : port.getBinding().getBindingOperations()) {
                                    logger.debug("        Interface: " + bindOperationObject);
                                    List extensions = port.getExtensibilityElements();
                                    if (extensions != null) {
                                        for (int i = 0; i < extensions.size(); i++) {
                                            ExtensibilityElement extElement = (ExtensibilityElement) extensions.get(i);
                                            if (extElement instanceof SOAPOperation) {
                                                SOAPOperation soapOp = (SOAPOperation) extElement;
                                                actions.add(soapOp.toString());
                                            } else if (extElement instanceof SOAP12Operation) {
                                                SOAP12Operation soapOp = (SOAP12Operation) extElement;
                                                actions.add(soapOp.toString());
                                            }
                                        }
                                    }
                                }
                                definitionPortMap.getMap().put(portQName, new PortInformation(operations, actions, locationURI));
                                wsdlInterfacePortMap.put(portQName, definition);   
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

    private String buildEnvelope(Definition definition, String operationName, boolean buildOptional) throws Exception {
        SoapMessageBuilder messageBuilder = ((WsdlInterface) definition).getMessageBuilder();
        BindingOperation bindingOperation = ((WsdlInterface) definition).getOperationByName(operationName).getBindingOperation();
        return messageBuilder.buildSoapMessageFromInput(bindingOperation, buildOptional);
    }

    /*
     * This is needed to prevent soapUI from disabling logging for the entire application.
     */
    private static class EmbeddedSoapUICore extends DefaultSoapUICore {
        @Override
        protected void initLog() {
            log = org.apache.log4j.Logger.getLogger(DefaultSoapUICore.class); //still uses log4j1 using bridge
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