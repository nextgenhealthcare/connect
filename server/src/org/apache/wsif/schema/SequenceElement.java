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
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * A class to represent an &lt;element&gt; element defined within a &lt;sequence&gt; element in a schema
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class SequenceElement extends ElementType implements Serializable {
	
	static final long serialVersionUID = 1L;
		
	Hashtable attributes = new Hashtable();

    /**
     * Constructor
     * @param el The dom element for this element within a sequence
     */
    SequenceElement(Element el, String tns) {
        super(el, tns);
        getAllAttributes(el, null, attributes);
    }

    /**
     * Get the value of a specified attribute on this element
     * @param The name of the attribute
     * @return The value of the attribute or null if the attribute does not exist
     */
    public QName getXMLAttribute(String name) {
        return (QName) attributes.get(new QName(name));
    }

    /**
     * Get the value of a specified attribute on this element when the attribute name is
     * a QName
     * @param The name of the attribute
     * @return The value of the attribute or null if the attribute does not exist
     */
    QName getXMLAttribute(QName name) {
        return (QName) attributes.get(name);
    }
}
