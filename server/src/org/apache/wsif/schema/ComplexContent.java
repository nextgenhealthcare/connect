/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 
package org.apache.wsif.schema;

import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to represent a &lt;complexContent&gt; element in a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class ComplexContent implements Serializable {
	
	static final long serialVersionUID = 1L;
		
	private Restriction restriction = null;
	private Extension extention = null;	

	/**
	 * Constructor
	 * @param el The dom element for this complexContent
	 */	
	ComplexContent(Element el, String tns) {
		NodeList children = el.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element subEl = (Element) child;
				String elType = subEl.getLocalName();
				if (elType.equals("restriction")) {
					restriction = new Restriction(subEl, tns);
					break;
				} else if (elType.equals("extension")) {
					extention = new Extension(subEl);
					break;
				}
			}			
		}	
	}
	
	/**
	 * Get the restriction element for this complexContent
	 * @return A Restriction object representing the restriction
	 */
	public Restriction getRestriction() {
		return restriction;
	}

	/**
	 * Get the extension element for this complexContent
	 * @return An Extension object representing the restriction
	 */	
	public Extension getExtension() {
		return extention;
	}	
}
