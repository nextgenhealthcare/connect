/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

public class ExtendedLoginStatus extends LoginStatus {

    private String clientPluginClass;

    public ExtendedLoginStatus(Status status, String message) {
        this(status, message, null, null);
    }

    public ExtendedLoginStatus(Status status, String message, String updatedUsername, String clientPluginClass) {
        super(status, message, updatedUsername);
        this.clientPluginClass = clientPluginClass;
    }

    public String getClientPluginClass() {
        return clientPluginClass;
    }
}
