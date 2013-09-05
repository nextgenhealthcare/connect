/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.Calendar;

import org.apache.commons.lang3.builder.ToStringStyle;

public class CalendarToStringStyle extends ToStringStyle {
    public CalendarToStringStyle() {
        super();
        this.setUseShortClassName(true);
        this.setUseIdentityHashCode(false);
    }
    
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (value instanceof Calendar) {
            value = String.format("%1$tY-%1$tm-%1$td", value);
        }

        buffer.append(value);
    }
    
    public static CalendarToStringStyle instance() {
        return new CalendarToStringStyle();
    }
}
