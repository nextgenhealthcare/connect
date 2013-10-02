/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import org.apache.commons.lang3.ObjectUtils;


public class IgnoredComponent {
    public final static String COMPONENT_NAME_VERSION_SEPARATOR = ":";
    private String name;
    private String version;

    public IgnoredComponent(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String toString() {
        return name + COMPONENT_NAME_VERSION_SEPARATOR + version;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof IgnoredComponent)) {
            return false;
        }

        IgnoredComponent ignoredComponent = (IgnoredComponent) that;

        return ObjectUtils.equals(this.getName(), ignoredComponent.getName()) && ObjectUtils.equals(this.getVersion(), ignoredComponent.getVersion());
    }
}
