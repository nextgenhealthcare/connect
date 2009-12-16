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

import org.apache.commons.io.FileUtils;

import com.webreach.mirth.connectors.ConnectorService;

public class WebServiceConnectorService implements ConnectorService {
    private static Map<String, Definition> definitionCache = new HashMap<String, Definition>();

    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("cacheWsdlFromUrl")) {
            Map<String, String> params = (Map<String, String>) object;
            String id = params.get("id");
            String wsdlUrl = params.get("wsdlUrl");
            String username = params.get("username");
            String password = params.get("password");

            definitionCache.put(id, getDefinition(wsdlUrl, username, password));
        } else if (method.equals("cacheWsdlFromFile")) {
            Map<String, String> params = (Map<String, String>) object;
            String id = params.get("id");
            String wsdlContents = params.get("wsdlContents");

            File tempFile = File.createTempFile(id, ".wsdl");
            tempFile.deleteOnExit();

            FileUtils.writeStringToFile(tempFile, wsdlContents);

            definitionCache.put(id, getDefinition(tempFile.getAbsolutePath(), null, null));
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
            SoapEnvelopeGenerator envelopeGenerator = new SoapEnvelopeGenerator(definitionCache.get(id));
            return envelopeGenerator.generateEnvelopeForOperation(operation);
        }

        return null;
    }

    private Definition getDefinition(String wsdlUrl, String username, String password) throws Exception {
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        wsdlReader.setFeature("javax.wsdl.importDocuments", true);
        return wsdlReader.readWSDL(new AuthWsdlLocator(wsdlUrl, username, password));
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
