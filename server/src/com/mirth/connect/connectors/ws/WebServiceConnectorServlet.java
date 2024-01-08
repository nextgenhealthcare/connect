/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
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
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.ibm.wsdl.extensions.schema.SchemaImportImpl;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.connectors.ws.DefinitionServiceMap.DefinitionPortMap;
import com.mirth.connect.connectors.ws.DefinitionServiceMap.PortInformation;
import com.mirth.connect.connectors.ws.SchemaType.SchemaTypeElement;
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
    private static final Logger logger = LogManager.getLogger(WebServiceConnectorServlet.class); //log4j2.x

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
            return soapOp;
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
        String prefix = "ws";
        String isQualified = "";
        String isSchemaImportQualified = "";
        

        // add the name space
        String urlString = definition.getTargetNamespace();
        Map<String, String> nameSpaceMap = definition.getNamespaces();
        Iterator<Map.Entry<String, String>> iteratorNameSpaces = nameSpaceMap.entrySet().iterator();
        while (iteratorNameSpaces.hasNext()) {
            Map.Entry<String, String> entry = iteratorNameSpaces.next();
            String url = entry.getValue().toString();
            if (url.equals(urlString)) {
                if (!StringUtils.isEmpty(entry.getKey())) {
                    prefix = entry.getKey();
                }
                break;
            }
        }

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        // Retrieve different parts
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();

        // Extract headers
        SOAPHeader soapHeader = soapEnvelope.getHeader();

        // Extract body
        SOAPBody soapBody = soapEnvelope.getBody();

        // Add elements
        SOAPFactory soapFactory = SOAPFactory.newInstance();

        soapEnvelope.addNamespaceDeclaration(prefix, definition.getTargetNamespace());

        Name bodyName = soapFactory.createName(operationName, prefix, "");
        SOAPBodyElement bodyElement = soapBody.addBodyElement(bodyName);

        // Map of element names and their types
        List<String> elementTags = new ArrayList<String>();
        Map<String, List<String>> elementTypes = new HashMap<>();

        List<?> extensibilityElements = definition.getTypes().getExtensibilityElements();
        for (Object extensibilityElement : extensibilityElements) {
            if (extensibilityElement instanceof Schema) {
                Schema schema = (Schema) extensibilityElement;
                
                // if we have an imported schema, we will need to get the import instead of using the schema above
                if (schema.getImports() != null && !schema.getImports().isEmpty()) {
                    Iterator<Map.Entry<String, Vector>> schemaIt = schema.getImports().entrySet().iterator();
                    Map.Entry<String, Vector> schemaEntry = schemaIt.next();
                    schema = ((SchemaImport) schemaEntry.getValue().get(0)).getReferencedSchema();
                    isSchemaImportQualified = schema.getElement().getAttribute("elementFormDefault");
                    urlString = schema.getElement().getAttribute("targetNamespace");
                    for (int i = 0; i < schema.getElement().getAttributes().getLength(); i++) {
                        Node schemaNode = schema.getElement().getAttributes().item(i);
                        if (schemaNode.getNamespaceURI() != null) {
                            String url = schemaNode.getNodeValue();
                            if (url.equals(urlString)) {
                                prefix = schemaNode.getLocalName();
                                break;
                            }
                        }
                    }
                } else {
                    isQualified = schema.getElement().getAttribute("elementFormDefault");
                }
                
                // An element can specify a type name or it can define its type using child nodes
                // Complex (non-primitive) types can be defined in a separate node or as a child nodes of the element
                // go through all main elements
                for (int i = 1; i < schema.getElement().getChildNodes().getLength(); i += 2) {
                    Node node = schema.getElement().getChildNodes().item(i);
                    String name = node.getAttributes().getNamedItem("name") != null ? node.getAttributes().getNamedItem("name").getNodeValue() : null;
                    
                    // go through child nodes of all elements related to operationName
                    if (name.equals(operationName)) {
                        while (node.getChildNodes().getLength() != 0) {
                            int j = node.getChildNodes().getLength();
                            for (int k = 1; k < j; k += 2) {
                                // get last node for the name of the tag(s) - this should be the sequence
                                String tagName = node.getChildNodes().item(k).getAttributes().getNamedItem("name") != null ? node.getChildNodes().item(k).getAttributes().getNamedItem("name").getNodeValue() : null;
                                if (!StringUtils.isEmpty(tagName)) {
                                    elementTags.add(tagName);
                                }
                            }
                            if (!elementTags.isEmpty()) {
                                elementTypes.put(name, elementTags);
                            }
                            // get the next child of this child
                            node = node.getChildNodes().item(1);
                        }
                    }
                }
                break;
            }
        }
        
        // Add arguments to body
        if (elementTypes.get(operationName) != null) {
            for (String element : elementTypes.get(operationName)) {
                Name childName = null;
                if (isSchemaImportQualified.equals("qualified") || isQualified.equals("qualified")) {
                    childName = soapFactory.createName(element, prefix, urlString);
                } else {
                    childName = soapFactory.createName(element);
                }
                SOAPElement childElement = bodyElement.addChildElement(childName);
                childElement.addTextNode("?");
            }
        }

        // Get SOAP message as a String
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        soapMessage.writeTo(out);
        String envelope = new String(out.toByteArray());

        // Pretty print the string
        Source xmlInput = new StreamSource(new StringReader(envelope));
        StringWriter stringWriter = new StringWriter();
        StreamResult xmlOutput = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 2);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(xmlInput, xmlOutput);

        return xmlOutput.getWriter().toString();
    }
}