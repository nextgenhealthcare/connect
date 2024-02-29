/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

public enum Category {
    // @formatter:off
    CONVERSION("Conversion Functions"),
    LOGGING_AND_ALERTS("Logging and Alerts"),
    DATABASE("Database Functions"),
    UTILITY("Utility Functions"),
    DATE("Date Functions"),
    MESSAGE("Message Functions"),
    RESPONSE("Response Transformer"),
    MAP("Map Functions"),
    CHANNEL("Channel Functions"),
    POSTPROCESSOR("Postprocessor Functions"),
    USER_CODE("User Defined Code"),
    USER_FUNCTIONS("User Defined Functions");
    // @formatter:on

    private String value;

    private Category(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Category fromString(String value) {
        for (Category category : values()) {
            if (category.toString().equals(value)) {
                return category;
            }
        }
        return null;
    }
}