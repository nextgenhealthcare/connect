/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class LicenseInfo implements Serializable {

    public static final LicenseInfo INSTANCE = new LicenseInfo();

    private boolean activated;
    private boolean online;
    private Long expirationDate;
    private Long warningPeriod;
    private Long gracePeriod;
    private Set<String> extensions = new HashSet<String>();

    public LicenseInfo() {}

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getWarningPeriod() {
        return warningPeriod;
    }

    public void setWarningPeriod(Long warningPeriod) {
        this.warningPeriod = warningPeriod;
    }

    public Long getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(Long gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public Set<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(Set<String> extensions) {
        this.extensions = extensions;
    }
}
