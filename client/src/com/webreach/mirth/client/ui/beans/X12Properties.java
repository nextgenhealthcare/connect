/*
 * X12Properties.java
 *
 * Created on February 16, 2007, 4:21 PM
 */

package com.webreach.mirth.client.ui.beans;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * @author brendanh
 */
public class X12Properties extends EDIProperties implements Serializable
{
    
    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    
    private PropertyChangeSupport propertySupport;
    
    public X12Properties()
    {
        propertySupport = new PropertyChangeSupport(this);
    }

    /**
     * Holds value of property inferX12Delimiters.
     */
    private boolean inferX12Delimiters = true;

    /**
     * Getter for property inferX12Delimiters.
     * @return Value of property inferX12Delimiters.
     */
    public boolean isInferX12Delimiters()
    {
        return this.inferX12Delimiters;
    }

    /**
     * Setter for property inferX12Delimiters.
     * @param inferX12Delimiters New value of property inferX12Delimiters.
     */
    public void setInferX12Delimiters(boolean inferX12Delimiters)
    {
        this.inferX12Delimiters = inferX12Delimiters;
    }


    
}
