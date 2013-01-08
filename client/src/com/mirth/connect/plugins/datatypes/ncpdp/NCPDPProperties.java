/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.ncpdp;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class NCPDPProperties extends Object implements Serializable {

    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    private PropertyChangeSupport propertySupport;

    public NCPDPProperties() {
        propertySupport = new PropertyChangeSupport(this);
    }
    /**
     * Holds value of property segmentDelimiter.
     */
    private String segmentDelimiter = "0x1E";

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
     * @param segmentDelimiter
     *            New value of property segmentDelimeter.
     */
    public void setSegmentDelimiter(String segmentDelimiter) {
        this.segmentDelimiter = segmentDelimiter;
    }
    /**
     * Holds value of property groupDelimiter.
     */
    private String groupDelimiter = "0x1D";

    /**
     * Getter for property elementDelimiter.
     * 
     * @return Value of property elementDelimiter.
     */
    public String getGroupDelimiter() {
        return this.groupDelimiter;
    }

    /**
     * Setter for property elementDelimiter.
     * 
     * @param groupDelimiter
     *            New value of property elementDelimiter.
     */
    public void setGroupDelimiter(String groupDelimiter) {
        this.groupDelimiter = groupDelimiter;
    }
    /**
     * Holds value of property fieldDelimiter.
     */
    private String fieldDelimiter = "0x1C";

    /**
     * Getter for property subelementDelimiter.
     * 
     * @return Value of property subelementDelimiter.
     */
    public String getFieldDelimiter() {
        return this.fieldDelimiter;
    }

    /**
     * Setter for property subelementDelimiter.
     * 
     * @param fieldDelimiter
     *            New value of property subelementDelimiter.
     */
    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }
    /**
     * Holds value of property fieldDelimiter.
     */
    private boolean useStrictValidation = false;

    /**
     * Getter for property subelementDelimiter.
     * 
     * @return Value of property subelementDelimiter.
     */
    public boolean getUseStrictValidation() {
        return this.useStrictValidation;
    }

    /**
     * Setter for property subelementDelimiter.
     * 
     * @param useStrictValidation
     *            New value of property subelementDelimiter.
     */
    public void setUseStrictValidation(boolean useStrictValidation) {
        this.useStrictValidation = useStrictValidation;
    }
}
