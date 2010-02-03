/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.mule.transformers;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.UMOEventContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.converters.DocumentSerializer;

/**
 * Transforms a database result row map into an XML string.
 *  
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
public class HttpStringToXML extends AbstractTransformer {
	private Logger logger = Logger.getLogger(HttpStringToXML.class);

	private String rootElement;
	
	public String getRootElement() {
		return rootElement;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	public HttpStringToXML() {
		super();
		// the default root element name
		setRootElement("message");
		registerSourceType(String.class);
		setReturnClass(String.class);
	}

	public Object onCall(UMOEventContext eventContext) throws Exception {
		return doTransform((HashMap) eventContext.getTransformedMessage());
	}

	public Object doTransform(Object src) {
		String request = src.toString();
		request = request.replace('?', '&');
		String[] fields = request.split("&");
		HashMap<String, String> data = new HashMap<String, String>();

		for (int i = 0; i < fields.length; i++) {
			if (fields[i].indexOf('=') > -1){
				String[] field = fields[i].split("=");
				if (field.length > 1){
					data.put(field[0], field[1]);
				}else{
					data.put(field[0], "");
				}
			}
		}
		
		try {
				// create a new document object
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				// create the root element
				Element root = document.createElement(getRootElement());
				// appent the root element to the object				
				document.appendChild(root);
				
				for (Iterator iter = data.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					Element child = document.createElement(key);
					child.appendChild(document.createTextNode(data.get(key).toString()));
					root.appendChild(child);
				}
				
				// serialize the DOM object to a String
				DocumentSerializer docSerializer = new DocumentSerializer();
				return docSerializer.toXML(document);
		} catch (Exception e) {
			logger.error(e.toString());
			
			return null;
		}
	}
}
