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
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to represent the tpo level &lt;schema&gt; element of a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class Schema implements Serializable {
	
	static final long serialVersionUID = 1L;	
	
	private String targetNamespace = "";
	private ArrayList types = new ArrayList();
	private ArrayList iai = new ArrayList();

	/**
	 * Constructor
	 * @param el The dom element for this schema
	 */	
	Schema(Element el) {
		targetNamespace = el.getAttribute("targetNamespace");
        NodeList children = el.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element subEl = (Element) child;
				String elType = subEl.getLocalName();
				if (elType.equals("complexType")) {
					types.add(new ComplexType(subEl, targetNamespace));
				} else if (elType.equals("simpleType")) {
					types.add(new SimpleType(subEl, targetNamespace));
				} else if (elType.equals("element")) {
					types.add(new ElementType(subEl, targetNamespace));	
				} else if (elType.equals("import") || elType.equals("include")) {
					// If either an import or an include is defined, we need to get
					// the referenced file so store its location (if appropriate)
					String loc = subEl.getAttribute("schemaLocation");
					if (loc != null && !loc.equals("")) {
						iai.add(loc);
					}
				} else {
					//ignore all other types
				}
			}			
		}
	}
	
	/**
	 * Get a list of all the types within this schema
	 * @return A list of SchemaType objects
	 */
	List getTypes() {
		return types;
	}

	/**
	 * Get the "targetNamespace" attribute for this schema
	 * @return The "targetNamespace" attribute
	 */	
	String getTargetNamespace() {
		return targetNamespace;
	}

	/**
	 * Get all the locations of imported/included schemas so that they can also be retrieved
	 * @return An array of all the import/include schemaLocations
	 */		
	String[] getImportsAndIncludes() {		
        return (String[]) iai.toArray(new String[iai.size()]);		
	}	
}
