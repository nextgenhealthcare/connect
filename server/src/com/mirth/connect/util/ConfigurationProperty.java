/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.Serializable;

public class ConfigurationProperty implements Serializable {

    private String value;
    private String comment;

    public ConfigurationProperty(String value, String comment) {
        this.value = value;
        this.comment = comment;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName()).append('[');
        builder.append("value=").append(value).append(", ");
        builder.append("comment=").append(comment).append(']');
        return builder.toString();
    }
}
