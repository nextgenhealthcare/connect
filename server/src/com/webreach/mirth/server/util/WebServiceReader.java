package com.webreach.mirth.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.xml.WSDLLocator;
import javax.xml.namespace.QName;


import org.apache.xmlbeans.impl.soap.SOAPElement;

import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.webreach.mirth.model.WSDefinition;
import com.webreach.mirth.model.WSOperation;
import com.webreach.mirth.model.WSParameter;

public class WebServiceReader {
	private String address;

	public WebServiceReader(String address) {
		this.address = address;
	}
	/* For complex types
	class LocReader extends WSDLReaderImpl{
		public WSDLLocator getLocator(){
			return this.loc;
		}
	}
	public Map typesToMap(List typeList){
		HashMap<String,SchemaType> map = new HashMap<String,SchemaType>();
		Iterator<SchemaType> it = typeList.iterator();
		while(it.hasNext()){
			SchemaType element = it.next();
			map.put(element.getTypeName().getLocalPart(), element);
		}
		return map;
		
	}
	*/
	public WSDefinition getWSDefinition() throws Exception {
		WSDefinition wsDefinition = new WSDefinition();
		WSDLReaderImpl reader = new WSDLReaderImpl();

		Definition definition = reader.readWSDL(address);
		/*For complex types
		LocReader lreader = new LocReader();
		//For complex type mappings, use the following
		List types = new ArrayList();
		try {
			Map map = new HashMap();
			Parser.getAllSchemaTypes(definition, types, lreader.getLocator());
		} catch (WSIFException e) {
			
		}
		Map schemaTypes = typesToMap(types)
		*/
		Iterator<Service> serviceIter = definition.getServices().values().iterator();
		while (serviceIter.hasNext()){
			Service service = serviceIter.next();
			Iterator<Port> portIter = service.getPorts().values().iterator();
			while (portIter.hasNext()){
				Port port = portIter.next();
				Iterator extIter = port.getExtensibilityElements().iterator();
				String endpointURI = "";
				//loop through the extensions to get the location address
				while (extIter.hasNext()){
					Object extelement = extIter.next();
					if (extelement instanceof SOAPAddress){
						endpointURI = ((SOAPAddress)extelement).getLocationURI();
					}
				}
				Binding binding = port.getBinding();
				Iterator bindingOperationIter = binding.getBindingOperations().iterator();

				while (bindingOperationIter.hasNext()) {
					BindingOperation bindingOperation = (BindingOperation) bindingOperationIter.next();
					Object concreteOperation = bindingOperation.getExtensibilityElements().get(0);
					//wsDefinition.setServiceEndpoint(soapOperation.getSoapActionURI());
					if (concreteOperation instanceof SOAPOperation){
						WSOperation wsOperation = new WSOperation();
						SOAPOperation soapOperation = (SOAPOperation)concreteOperation;
						if (endpointURI.equals("")){
							
						}else{
							wsOperation.setEndpointURI(endpointURI);
						}
						
						wsOperation.setName(bindingOperation.getName());

						Iterator partIterator = bindingOperation.getOperation().getInput().getMessage().getParts().values().iterator();

						while (partIterator.hasNext()) {
							Part part = (Part) partIterator.next();
							
							QName typeName = part.getTypeName();
							if (typeName == null)
								typeName = part.getElementName();
								
							String type = new String();

							if (typeName != null) {
								type = typeName.getLocalPart();
							}

							WSParameter wsParameter = new WSParameter();
							wsParameter.setName(part.getName());
							wsParameter.setType(type);

							wsOperation.getParameters().add(wsParameter);
						}

						wsDefinition.getOperations().add(wsOperation);
					}else{
						
					}
					
				}
			}
		}

		return wsDefinition;
	}
}
