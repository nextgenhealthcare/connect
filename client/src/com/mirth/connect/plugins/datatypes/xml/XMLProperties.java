/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.xml;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class XMLProperties implements Serializable {

    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    private PropertyChangeSupport propertySupport;

    public XMLProperties() {
        propertySupport = new PropertyChangeSupport(this);
    }
    /**
     * Holds value of property stripNamespaces.
     */
    private boolean stripNamespaces = true;

    /**
     * Getter for property stripNamespaces.
     * @return Value of property stripNamespaces.
     */
    public boolean isStripNamespaces() {
        return this.stripNamespaces;
    }

    /**
     * Setter for property stripNamespaces.
     * @param stripNamespaces New value of property stripNamespaces.
     */
    public void setStripNamespaces(boolean stripNamespaces) {
        this.stripNamespaces = stripNamespaces;
    }
}
