/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

// ast: class for encoding information
/*
 * class CharsetEncodingInformation gets all the information we need for an
 * encoding class
 */
public class CharsetEncodingInformation {

    protected String canonicalName = "";
    protected String description = "";

    public CharsetEncodingInformation(String name, String descp) {
        this.canonicalName = name;
        this.description = descp;
    }

    public CharsetEncodingInformation(String name) {
        this.canonicalName = name;
        this.description = "";
    }

    /**
     * Overloaded method to show the description in the combo box
     */
    public String toString() {
        return new String(this.description);
    }

    /**
     * Overloaded method to show the description in the combo box
     */
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return canonicalName.equalsIgnoreCase((String) obj);
        } else if (obj instanceof CharsetEncodingInformation) {
            return canonicalName.equalsIgnoreCase(((CharsetEncodingInformation) obj).getCanonicalName());
        } else {
            return this.equals(obj);
        }
    }

    public String getCanonicalName() {
        return this.canonicalName;
    }

    public void setCanonicalName(String c) {
        this.canonicalName = c;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String d) {
        this.description = d;
    }
}
