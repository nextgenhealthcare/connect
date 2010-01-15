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
