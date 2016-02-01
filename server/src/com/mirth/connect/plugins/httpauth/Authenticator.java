/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth;

import java.util.Map;

public abstract class Authenticator {

    protected Map<String, String> properties;

    public Authenticator() {}

    public Authenticator(Map<String, String> properties) {
        this.properties = properties;
    }

    public abstract AuthenticationResult authenticate(RequestInfo request) throws Exception;
}