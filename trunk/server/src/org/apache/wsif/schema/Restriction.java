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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to represent a &lt;restriction&gt; element in a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class Restriction implements Serializable {
	
	static final long serialVersionUID = 1L;
		
    QName base = null;
    ArrayList attributes = new ArrayList();
    ArrayList sequenceElements = new ArrayList();

	/**
	 * Constructor
	 * @param el The dom element for this restriction
	 */
    Restriction(Element el, String tns) {
        base = SchemaType.getAttributeQName(el, "base");
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("attribute")) {
                    attributes.add(new Attribute(subEl, tns));
                } else if (elType.equals("sequence")) {
                    parseSequenceElements(subEl, tns);
                }
            }
        }
    }

	/**
	 * Get the "base" attribute for this restriction
	 * @return The "base" attribute
	 */
    public QName getBase() {
        return base;
    }

	/**
	 * Get all the &lt;attribute&gt; elements within this restriction
	 * @return The &lt;attribute&gt; elements
	 */
    public Attribute[] getAttributes() {
        return (Attribute[]) attributes.toArray(
            new Attribute[attributes.size()]);
    }

	/**
	 * Get all the &lt;element&gt; elements within a sequence within this restriction
	 * @return The &lt;element&gt; elements within the sequnce
	 */
    public SequenceElement[] getSequenceElements() {
        return (SequenceElement[]) sequenceElements.toArray(
            new SequenceElement[sequenceElements.size()]);
    }

    private void parseSequenceElements(Element el, String tns) {
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("element")) {
                    sequenceElements.add(new SequenceElement(subEl, tns));
                }
            }
        }
    }
}
