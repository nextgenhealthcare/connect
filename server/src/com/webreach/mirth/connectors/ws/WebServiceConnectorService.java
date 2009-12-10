package com.webreach.mirth.connectors.ws;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.server.util.FileUtil;

public class WebServiceConnectorService implements ConnectorService {
    private static Map<String, Definition> definitionCache = new HashMap<String, Definition>();

    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("cacheWsdlFromUrl")) {
            Map<String, String> params = (Map<String, String>) object;
            String id = params.get("id");
            String wsdlUrl = params.get("wsdlUrl");

            definitionCache.put(id, getDefinition(wsdlUrl));
        } else if (method.equals("cacheWsdlFromFile")) {
            Map<String, String> params = (Map<String, String>) object;
            String id = params.get("id");
            String wsdlContents = params.get("wsdlContents");

            File temp = File.createTempFile(id, ".xml");
            FileUtil.write(temp.getAbsolutePath(), false, wsdlContents);

            definitionCache.put(id, getDefinition(temp.getAbsolutePath()));
        } else if (method.equals("isWsdlCached")) {
            String id = (String) object;
            return (definitionCache.get(id) != null);
        } else if (method.equals("getOperations")) {
            String id = (String) object;
            Definition definition = definitionCache.get(id);

            if (definition != null) {
                return getOperations(definition);
            }
        } else if (method.equals("getService")) {
            String id = (String) object;
            String serviceName = null;
            Definition definition = definitionCache.get(id);

            if (definition.getServices().values().iterator().hasNext()) {
                Service service = (Service) definition.getServices().values().iterator().next();
                serviceName = service.getQName().toString();
            }

            return serviceName;
        } else if (method.equals("getPort")) {
            String id = (String) object;
            String portName = null;
            Definition definition = definitionCache.get(id);

            if (definition.getServices().values().iterator().hasNext()) {
                Service service = (Service) definition.getServices().values().iterator().next();
                if (service.getPorts().values().iterator().hasNext()) {
                    Port port = (Port) service.getPorts().values().iterator().next();
                    portName = new QName(service.getQName().getNamespaceURI(), port.getName()).toString();
                }
            }

            return portName;
        } else if (method.equals("generateEnvelope")) {
            Map<String, String> params = (Map<String, String>) object;
            String id = params.get("id");
            String operation = params.get("operation");
            Definition definition = definitionCache.get(id);
            
            /*
            List<String> imports = new ArrayList<String>();
            Types types = definition.getTypes();
            if (types != null) {
                Iterator extensibilityElementsIterator = types.getExtensibilityElements().iterator();
                while (extensibilityElementsIterator.hasNext()) {
                    Object typesElement = extensibilityElementsIterator.next();
                    if (typesElement instanceof Schema) {
                        Schema schema = (Schema) typesElement;
                        
                        Map schemaImportsMap = schema.getImports();
                        Set<String> namespaces = schemaImportsMap.keySet();
                        
                        for (String namespace : namespaces) {
                            List<SchemaImport> importsForNS = (List<SchemaImport>) schemaImportsMap.get(namespace);
                            for (SchemaImport importForNS : importsForNS) {
                                imports.add(importForNS.getSchemaLocationURI());
                            }
                        }
                    }
                }
            }*/
            
//            List<SchemaType> schemaTypes = new ArrayList<SchemaType>();
//            Parser.getAllSchemaTypes(definition, schemaTypes, new WSIFWSDLLocatorImpl(null, definition.getDocumentBaseURI(), ClassLoader.getSystemClassLoader()));
            
//            return schemaTypes.toString();
        }
        return null;
    }

    private Definition getDefinition(String wsdlUrl) throws Exception {
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        wsdlReader.setFeature("javax.wsdl.importDocuments", true);
        return wsdlReader.readWSDL(wsdlUrl);
    }

    private List<String> getOperations(Definition definition) {
        List<String> operations = new ArrayList<String>();

        for (Iterator<Service> serviceIterator = definition.getServices().values().iterator(); serviceIterator.hasNext();) {
            Service service = serviceIterator.next();

            for (Iterator<Port> portIterator = service.getPorts().values().iterator(); portIterator.hasNext();) {
                Port port = portIterator.next();

                for (Iterator<BindingOperation> iterator = port.getBinding().getBindingOperations().iterator(); iterator.hasNext();) {
                    BindingOperation operation = iterator.next();
                    operations.add(operation.getName());
                }
            }
        }

        return operations;
    }
}
