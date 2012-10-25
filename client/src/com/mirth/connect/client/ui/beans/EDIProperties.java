/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.beans;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class EDIProperties extends Object implements Serializable {

    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    private PropertyChangeSupport propertySupport;

    public EDIProperties() {
        propertySupport = new PropertyChangeSupport(this);
    }
    /**
     * Holds value of property segmentDelimiter.
     */
    private String segmentDelimiter = "~";

    /**
     * Getter for property segmentDelimeter.
     * 
     * @return Value of property segmentDelimeter.
     */
    public String getSegmentDelimiter() {
        return this.segmentDelimiter;
    }

    /**
     * Setter for property segmentDelimeter.
     * 
     * @param segmentDelimeter
     *            New value of property segmentDelimeter.
     */
    public void setSegmentDelimiter(String segmentDelimiter) {
        this.segmentDelimiter = segmentDelimiter;
    }
    /**
     * Holds value of property elementDelimiter.
     */
    private String elementDelimiter = "*";

    /**
     * Getter for property elementDelimiter.
     * 
     * @return Value of property elementDelimiter.
     */
    public String getElementDelimiter() {
        return this.elementDelimiter;
    }

    /**
     * Setter for property elementDelimiter.
     * 
     * @param elementDelimiter
     *            New value of property elementDelimiter.
     */
    public void setElementDelimiter(String elementDelimiter) {
        this.elementDelimiter = elementDelimiter;
    }
    /**
     * Holds value of property subelementDelimiter.
     */
    private String subelementDelimiter = ":";

    /**
     * Getter for property subelementDelimiter.
     * 
     * @return Value of property subelementDelimiter.
     */
    public String getSubelementDelimiter() {
        return this.subelementDelimiter;
    }

    /**
     * Setter for property subelementDelimiter.
     * 
     * @param subelementDelimiter
     *            New value of property subelementDelimiter.
     */
    public void setSubelementDelimiter(String subelementDelimiter) {
        this.subelementDelimiter = subelementDelimiter;
    }
}
