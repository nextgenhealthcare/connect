package com.webreach.mirth.client.ui.beans;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class XMLProperties implements Serializable
{
    
    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    
    private PropertyChangeSupport propertySupport;
    
    public XMLProperties()
    {
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
    public boolean isStripNamespaces()
    {
        return this.stripNamespaces;
    }

    /**
     * Setter for property stripNamespaces.
     * @param stripNamespaces New value of property stripNamespaces.
     */
    public void setStripNamespaces(boolean stripNamespaces)
    {
        this.stripNamespaces = stripNamespaces;
    }
    
}
