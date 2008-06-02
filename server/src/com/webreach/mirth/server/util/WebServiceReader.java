/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
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
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axis.utils.NetworkUtils;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.wsif.WSIFException;
import org.apache.wsif.schema.ComplexType;
import org.apache.wsif.schema.ElementType;
import org.apache.wsif.schema.Parser;
import org.apache.wsif.schema.SchemaType;
import org.apache.wsif.schema.SequenceElement;

import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.webreach.mirth.model.ws.WSDefinition;
import com.webreach.mirth.model.ws.WSOperation;
import com.webreach.mirth.model.ws.WSParameter;

public class WebServiceReader {
	private String address;
	private Logger logger = Logger.getLogger(this.getClass());
	public WebServiceReader(String address) {
		this.address = address;
	}

	// For complex types
	class LocReader extends WSDLReaderImpl {
		public WSDLLocator getLocator() {
			return this.loc;
		}
	}
/**
 * 
 * @param WSDLUri  URL where is the WSDL
 * 
 * This method allows the user to pass a user/pass credentials in the URL 
 * of the WSDL file
 * 
 * The code is similar to the used at axis in CommonsHTTPSender  
 * 
 * 
 */
	public String getFromURi(URI WSDLUri){
		
		
		try {
			if (WSDLUri.toURL().getProtocol().equalsIgnoreCase("FILE")) return WSDLUri.toString();
		} catch (MalformedURLException e1) {
			logger.error(" Malformed URI ["+WSDLUri+"] "+e1);
			return WSDLUri.toString();
		}
		
		HttpClient httpClient = new HttpClient();
		HttpMethod getMethod = new GetMethod(WSDLUri.toString());
		String userID=null;
		String passwd=null;	
		String info=WSDLUri.getUserInfo();
		 if ( ( info != null)) {
			    logger.debug("WSDL detected user info: "+info);
	            int sep = info.indexOf(':');	            
	            if ((sep >= 0) && (sep + 1 < info.length())) {
	                userID = info.substring(0, sep);
	                passwd = info.substring(sep + 1);
	            } else {
	                userID = info;
	            }
	     }
		 //Check for NTLM HTTP auth
		 if (userID == null) return WSDLUri.toString(); 
	            Credentials proxyCred =
	                new UsernamePasswordCredentials(userID, passwd);
	            // if the username is in the form "user\domain"
	            // then use NTCredentials instead.
	            int domainIndex = userID.indexOf("\\");	            
	            if (domainIndex > 0) {	            	
	                String domain = userID.substring(0, domainIndex);
	                if (userID.length() > domainIndex + 1) {
	                    String user = userID.substring(domainIndex + 1);
	                    proxyCred = new NTCredentials(user,
	                                    passwd,
	                                    NetworkUtils.getLocalHostname(), domain);
	                    logger.trace("Detected NTLM credentials usr ["+user+"] pass: ["+passwd+"] ln ["+NetworkUtils.getLocalHostname()+"] dm ["+domain+"]");
	                }
	            }else{
	                logger.trace("Detected User/Pass credentials usr ["+userID+"] pass: ["+passwd+"]");
	            }
	            httpClient.getState().setCredentials(AuthScope.ANY, proxyCred);
	        
		
		byte[] responseBody=null;
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			responseBody = getMethod.getResponseBody();
			if (statusCode != HttpStatus.SC_OK) {
		        logger.error("Method failed: " + getMethod.getStatusLine());
		    }else{
		    	  logger.error(new String(responseBody));
		    }		
		} catch (HttpException e) {
		      logger.error("Fatal protocol violation: " + e.getMessage());
		      return WSDLUri.toString();
		} catch (IOException e) {
			logger.error("I/O Exception "+e.getMessage());
			return WSDLUri.toString();
		}finally {
		      // Release the connection.
			getMethod.releaseConnection();
		} 
		if (responseBody!=null){
		    try {		    
		        // Create temp file.
		        File temp = File.createTempFile("pattern", ".wsdl");		    
		        // Delete temp file when program exits.
		        temp.deleteOnExit();		    
		        // Write to temp file
		        FileOutputStream fos = new FileOutputStream(temp);
		        fos.write(responseBody);
	            fos.close();
	            return temp.toURL().toString();
		    } catch (IOException e) {
		    	logger.error("I/O Exception writting wsdl to a temp file: "+ e.getMessage());
			    //e.printStackTrace();
			    return WSDLUri.toString();
		    }			
		}
		return WSDLUri.toString();
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
		//WSDLReaderImpl reader = new WSDLReaderImpl();
		WSDLReader reader= javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();				
		Definition definition = null;
		// Read in the WSDL
		try{
			definition = reader.readWSDL(getFromURi(new URI(address)));
		}catch (Exception e){
			logger.warn("Unable to read WSDL location: " + address);
			//e.printStackTrace();
			return null;
		}

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
					List extensibilityElements = bindingOperation.getExtensibilityElements();
					Object concreteOperation = null;
					for (Object element : extensibilityElements)
					{
						if (element instanceof SOAPOperation){
							concreteOperation = element;
						}
					}

					if (concreteOperation != null && concreteOperation instanceof SOAPOperation) {
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
							}else if (element instanceof SOAPHeader){
								//add the header parameter
								SOAPHeader header = (SOAPHeader)element;
								String type = header.getPart();
								
								WSParameter wsParameter = new WSParameter();
								wsParameter.setName(header.getMessage().getLocalPart());
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
								wsOperation.setHeader(wsParameter);
								wsOperation.setHeaderNamespace(header.getNamespaceURI());
							}
						}
						wsOperation.setNamespace(namespace);				
						Iterator partIterator = bindingOperation.getOperation().getInput().getMessage().getOrderedParts(null).iterator();
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
				if (seqElement.getElementType()==null) newParam.setType("anyType");
				else newParam.setType(seqElement.getElementType().getLocalPart());
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
