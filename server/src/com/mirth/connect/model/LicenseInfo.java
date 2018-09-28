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
import java.util.Set;

public class LicenseInfo implements Serializable {

    public static final String EXPIRATION_DATE_KEY = "NEXTGEN_CONNECT_LICENSE_EXPIRATION";
    public static final String WARNING_PERIOD_KEY = "NEXTGEN_CONNECT_LICENSE_WARNING_PERIOD";
    public static final String GRACE_PERIOD_KEY = "NEXTGEN_CONNECT_LICENSE_GRACE_PERIOD";
    public static final String EXTENSIONS_KEY = "NEXTGEN_CONNECT_LICENSE_EXTENSIONS";

    private Long expirationDate;
    private Long warningPeriod;
    private Long gracePeriod;
    private Set<String> extensions;

    public LicenseInfo(Long expirationDate, Long warningPeriod, Long gracePeriod, Set<String> extensions) {
        this.expirationDate = expirationDate;
        this.warningPeriod = warningPeriod;
        this.gracePeriod = gracePeriod;
        this.extensions = extensions;
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
