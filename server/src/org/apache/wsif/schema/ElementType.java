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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to represent a top level &lt;element&gt; element in a schema
 *
 * @author Owen Burroughs <owenb@apache.org>
 */
public class ElementType extends SchemaType implements Serializable {
    
    static final long serialVersionUID = 1L;
    
    private String name = "";
    private QName typeName = null;
    private QName elementType = null;
    private List childTypes = new ArrayList();
    private boolean nillable = false;
    
    /**
     * Constructor
     * @param el The dom element for this element
     */
    ElementType(Element el, String tns) {
        // jgreif Webalo, Inc. -- incorrect to use tns as default namespace
        // for type or ref attribute value !
        //elementType = getAttributeQName(el, "type", tns);
        elementType = getAttributeQName(el, "type");
        typeName = getAttributeQName(el, "name", tns);
        // jgreif Webalo, Inc. -- ref attr may appear rather than name attr
        if (typeName == null) {
            typeName = getAttributeQName(el, "ref");
        }
        
        QName nillableAttr = getAttributeQName(el, "nillable", null);
        String stTrue = "true";
        if (nillableAttr != null && stTrue.equals(nillableAttr.getLocalPart())) {
            nillable = true;
        }
        
        // If the element has no name, we cannot map it. Don't do any more processing
        // of this type
        if (typeName == null) return;
        
        name = typeName.getLocalPart();
        NodeList children = el.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element subEl = (Element) child;
                String elType = subEl.getLocalName();
                if (elType.equals("complexType")) {
                    childTypes.add(new ComplexType(subEl, tns));
                } else if (elType.equals("simpleType")) {
                    childTypes.add(new SimpleType(subEl, tns));
                } else if (elType.equals("element")) {
                    childTypes.add(new ElementType(subEl, tns));
                } else {
                    //ignore all other types
                }
            }
        }
    }
    
    /**
     * @see SchemaType#isElement()
     */
    public boolean isElement() {
        return true;
    }
    
    /**
     * @see SchemaType#getTypeName()
     */
    public QName getTypeName() {
        return typeName;
    }
    
    public QName getElementType() {
        return elementType;
    }
    
    public boolean isNillable() {
        return nillable;
    }
    
    /**
     * @see SchemaType#getChildren()
     */
    public List getChildren() {
        return childTypes;
    }
}
