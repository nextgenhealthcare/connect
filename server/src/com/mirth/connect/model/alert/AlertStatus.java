/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertStatus")
public class AlertStatus {

    private String id;
    private String name;
    private boolean enabled;
    private Integer alertedCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getAlertedCount() {
        return alertedCount;
    }

    public void setAlertedCount(Integer alertedCount) {
        this.alertedCount = alertedCount;
    }
}
