/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth;

import com.mirth.connect.donkey.server.channel.Connector;

public abstract class AuthenticatorProvider {

    private Connector connector;
    private HttpAuthConnectorPluginProperties properties;

    public AuthenticatorProvider(Connector connector, HttpAuthConnectorPluginProperties properties) {
        this.connector = connector;
        this.properties = properties;
    }

    public abstract Authenticator getAuthenticator() throws Exception;

    public void shutdown() {}

    public Connector getConnector() {
        return connector;
    }

    public HttpAuthConnectorPluginProperties getProperties() {
        return properties;
    }
}