package com.webreach.mirth.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.xml.WSDLLocator;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFException;
import org.apache.wsif.schema.ComplexType;
import org.apache.wsif.schema.ElementType;
import org.apache.wsif.schema.Parser;
import org.apache.wsif.schema.SchemaType;
import org.apache.wsif.schema.SequenceElement;

import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.webreach.mirth.model.WSDefinition;
import com.webreach.mirth.model.WSOperation;
import com.webreach.mirth.model.WSParameter;

public class WebServiceReader {
	private String address;

	public WebServiceReader(String address) {
		this.address = address;
	}

	// For complex types
	class LocReader extends WSDLReaderImpl {
		public WSDLLocator getLocator() {
			return this.loc;
		}
	}

	public Map<String, SchemaType> typesToMap(List typeList) {
		HashMap<String, SchemaType> map = new HashMap<String, SchemaType>();
		Iterator<SchemaType> it = typeList.iterator();
		while (it.hasNext()) {
			SchemaType element = it.next();
			map.put(element.getTypeName().getLocalPart(), element);
		}
		return map;

	}

	public WSDefinition getWSDefinition() throws Exception {
		WSDefinition wsDefinition = new WSDefinition();
		WSDLReaderImpl reader = new WSDLReaderImpl();

		// Read in the WSDL
		Definition definition = reader.readWSDL(address);

		// Parse the WSDL (and any imports) for type definitions
		LocReader lreader = new LocReader();
		List types = new ArrayList();
		try {
			Parser.getAllSchemaTypes(definition, types, lreader.getLocator());
		} catch (WSIFException e) {

		}

		Map<String, SchemaType> schemaTypes = typesToMap(types);
		wsDefinition.setComplexTypes(schemaTypes);

		Iterator<Service> serviceIter = definition.getServices().values().iterator();
		while (serviceIter.hasNext()) {
			Service service = serviceIter.next();
			Iterator<Port> portIter = service.getPorts().values().iterator();
			while (portIter.hasNext()) {
				Port port = portIter.next();
				Iterator extIter = port.getExtensibilityElements().iterator();
				String endpointURI = "";
				// loop through the extensions to get the location address
				while (extIter.hasNext()) {
					Object extelement = extIter.next();
					if (extelement instanceof SOAPAddress) {
						endpointURI = ((SOAPAddress) extelement).getLocationURI();
						wsDefinition.setServiceEndpointURI(endpointURI);
					}
				}
				Binding binding = port.getBinding();
				Iterator bindingOperationIter = binding.getBindingOperations().iterator();

				while (bindingOperationIter.hasNext()) {
					BindingOperation bindingOperation = (BindingOperation) bindingOperationIter.next();
					Object concreteOperation = bindingOperation.getExtensibilityElements().get(0);
					// wsDefinition.setServiceEndpoint(soapOperation.getSoapActionURI());
					if (concreteOperation instanceof SOAPOperation) {
						WSOperation wsOperation = new WSOperation();
						SOAPOperation soapOperation = (SOAPOperation) concreteOperation;
						wsOperation.setSoapActionURI(soapOperation.getSoapActionURI());
						wsOperation.setName(bindingOperation.getName());
						Iterator extElements = bindingOperation.getBindingInput().getExtensibilityElements().iterator();
						String namespace = bindingOperation.getOperation().getInput().getMessage().getQName().getNamespaceURI();
						while (extElements.hasNext()) {
							ExtensibilityElement element = (ExtensibilityElement) extElements.next();
							if (element instanceof SOAPBody) {
								if (((SOAPBody) element).getNamespaceURI() != null && ((SOAPBody) element).getNamespaceURI().length() > 0) {
									namespace = ((SOAPBody) element).getNamespaceURI();
									break;
								}
							}
						}
						wsOperation.setNamespace(namespace);
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
							wsParameter.setComplex(false);
							// If we have a schemaType, we're dealing with a
							// complex type
							Object schemaType = schemaTypes.get(type);
							if (schemaType != null) {
								wsParameter.setSchemaType((SchemaType) schemaType);
								wsParameter.setComplex(true);
								buildParams(schemaTypes, wsParameter);
							}
							wsOperation.getParameters().add(wsParameter);
						}
						wsDefinition.getOperations().put(wsOperation.getName(), wsOperation);
					}
				}
			}
		}

		return wsDefinition;
	}

	private void buildParams(Map<String, SchemaType> schemaTypes, WSParameter parameter) {
		if (parameter.getSchemaType() != null) {
			// loop through each param of the complex type
			SchemaType schemaType = parameter.getSchemaType();
			buildComplexTypes(schemaTypes, parameter, schemaType);

		}
	}

	private void buildComplexTypes(Map<String, SchemaType> schemaTypes, WSParameter parameter, SchemaType schemaType) {
		if (schemaType.isElement()) {
			ElementType elementType = (ElementType) schemaType;
			Iterator it = elementType.getChildren().iterator();
			while (it.hasNext()) {
				schemaType = (SchemaType) it.next();
				buildComplexTypes(schemaTypes, parameter, schemaType);
			}
		} else if (schemaType.isComplex()) {
			ComplexType complexType = (ComplexType) schemaType;
			SequenceElement[] elements = complexType.getSequenceElements();
			for (int i = 0; i < elements.length; i++) {
				SequenceElement seqElement = elements[i];
				WSParameter newParam = new WSParameter();
				// if we have a complex type, set the param name
				newParam.setName(seqElement.getTypeName().getLocalPart());
				newParam.setType(seqElement.getElementType().getLocalPart());
				// Can we have multiple?
				QName maxOccurs = seqElement.getXMLAttribute("maxOccurs");
				if (maxOccurs != null)
					newParam.setMaxOccurs(maxOccurs.getLocalPart());
				QName minOccurs = seqElement.getXMLAttribute("minOccurs");
				if (minOccurs != null)
					newParam.setMinOccurs(minOccurs.getLocalPart());
				QName nillable = seqElement.getXMLAttribute("nillable");
				if (nillable != null)
					newParam.setNillable(Boolean.parseBoolean(nillable.getLocalPart()));
				// setup sub parameter list
				SchemaType subSchema = schemaTypes.get(newParam.getType());
				newParam.setSchemaType(subSchema);
				// when we have no subschema, we have a simple type

				parameter.getChildParameters().add(newParam);
				DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(newParam);
				newParam.setComplex(false);
				if (subSchema != null && subSchema.isComplex()) {
					newParam.setComplex(true);
					buildParams(schemaTypes, newParam);
				} else {

				}

			}

		}
	}
}
