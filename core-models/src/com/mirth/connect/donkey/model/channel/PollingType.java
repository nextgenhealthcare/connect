/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

public enum PollingType {
    INTERVAL("Interval"), TIME("Time"), CRON("Cron");

    private String displayName;

    private PollingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PollingType fromDisplayName(String displayName) {
        for (PollingType type : PollingType.values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }

        return null;
    }
}