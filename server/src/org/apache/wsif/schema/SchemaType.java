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
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ibm.wsdl.util.xml.DOMUtils;

/**
 * Super class of both ComplexType and SimpleType
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public abstract class SchemaType implements Serializable {
	
	/**
	 * Get a flag to indicate if this type is a complexType
	 * @return The boolean flag
	 */
	public boolean isComplex() {
		return false;
	}

	/**
	 * Get a flag to indicate if this type is a simpleType
	 * @return The boolean flag
	 */
	public boolean isSimple() {
		return false;
	}

	/**
	 * Get a flag to indicate if this type is an element type
	 * @return The boolean flag
	 */
	public boolean isElement() {
		return false;
	}

	/**
	 * Get a flag to indicate if this type represents an array
	 * @return The boolean flag
	 */
	public boolean isArray() {
		return false;
	}

	/**
	 * Get the type of the elements in the array represented by this type (if applicable)
	 * @return The type
	 */	
	public QName getArrayType() {
		return null;
	}

	/**
	 * Get the dimension of the array represented by this type (if applicable)
	 * @return The dimension
	 */
	public int getArrayDimension() {
		return 0;
	}

	/**
	 * Get the "name" attribute of this type
	 * @return The type's name
	 */		
	public QName getTypeName() {
		return null;
    }

	/**
	 * Get a the direct children (SimpleType or ComplexType only) for this element
	 * @return The children
	 */		
	public List getChildren() {
		return null;
    }

	/**
	 * Get a specified attribute from a given dom element
	 * @param element The dom element
	 * @param attr The name of the attribute to retrieve
	 * @return The attribute value or null is the attriute does not exist
	 */	
    protected static QName getAttributeQName(Element element, String attr) {

        if (element == null || attr == null)
            throw new IllegalArgumentException(
                "Argument to 'getAttrQName' " + "cannot be null.");

        String name = DOMUtils.getAttribute(element, attr);

        if (name == null)
            return null;

        int index = name.lastIndexOf(":");
        String prefix = null;

        if (index != -1) {
            prefix = name.substring(0, index);
            name = name.substring(index + 1);
        }
        String uri = DOMUtils.getNamespaceURIFromPrefix(element, prefix);

        return new QName(uri, name);
    }

	/**
	 * Get a specified attribute from a given dom element
	 * @param element The dom element
	 * @param attr The name of the attribute to retrieve
	 * @param tns The targetNamespace used in resolving the attribute value
	 * @return The attribute value or null is the attriute does not exist
	 */	
    protected static QName getAttributeQName(Element element, String attr, String tns) {

        if (element == null || attr == null)
            throw new IllegalArgumentException(
                "Argument to 'getAttrQName' " + "cannot be null.");

        String name = DOMUtils.getAttribute(element, attr);

        if (name == null)
            return null;

        int index = name.lastIndexOf(":");
        String prefix = null;

        if (index != -1) {
            prefix = name.substring(0, index);
            name = name.substring(index + 1);
        }
        
        String uri = null;
        if (prefix != null) {
        	uri = DOMUtils.getNamespaceURIFromPrefix(element, prefix);
        } else {
        	uri = tns;
        }

        return new QName(uri, name);
    }

	/**
	 * Get a specified attribute from a given dom element when the attribute name is a QName
	 * @param element The dom element
	 * @param attr The name of the attribute to retrieve
	 * @return The attribute value or null is the attriute does not exist
	 */	    
    protected static QName getAttributeQName(Element element, QName attr) {

        if (element == null || attr == null)
            throw new IllegalArgumentException(
                "Argument to 'getAttrQName' " + "cannot be null.");
                
		String ns = attr.getNamespaceURI();
		String lp = attr.getLocalPart();
        String name = DOMUtils.getAttributeNS(element, ns, lp);

        if (name == null)
            return null;

        int index = name.lastIndexOf(":");
        String prefix = null;

        if (index != -1) {
            prefix = name.substring(0, index);
            name = name.substring(index + 1);
        }
        String uri = DOMUtils.getNamespaceURIFromPrefix(element, prefix);

        return new QName(uri, name);

    }

	/**
	 * Get all the attributes from a given dom element
	 * @param element The dom element
	 * @param tns The targetNamespace used in resolving the attribute value
	 * @param attributes A map to populate with the attributes
	 * @return The map of QName pairs (attribute name -> attribute value) for all the element's attributes
	 */	      
    protected static void getAllAttributes(Element el, String tns, Map attributes) {
        NamedNodeMap atts = el.getAttributes();
        if (atts != null) {
            for (int a = 0; a < atts.getLength(); a++) {
                Node attribute = atts.item(a);
                String ln = attribute.getLocalName();
                String ns = attribute.getNamespaceURI();
                
                String name = "";
                if (ns != null) {
                	name = DOMUtils.getAttributeNS(el, ns, ln);
                } else {
                	name = DOMUtils.getAttribute(el, ln);
                }
                
                int index = name.lastIndexOf(":");
                String prefix = null;

                if (index != -1) {
                    prefix = name.substring(0, index);
                    name = name.substring(index + 1);
                }

                String uri = null;
                if (prefix != null || tns == null) {
                    uri = DOMUtils.getNamespaceURIFromPrefix(el, prefix);
                } else {
                    uri = tns;
                }
				attributes.put(new QName(ns, ln) ,new QName(uri, name));                
            }
        }    	
    }		
}
