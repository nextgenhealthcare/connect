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
import com.mirth.connect.plugins.httpauth.basic.BasicAuthenticatorProvider;
import com.mirth.connect.plugins.httpauth.custom.CustomAuthenticatorProvider;
import com.mirth.connect.plugins.httpauth.digest.DigestAuthenticatorProvider;
import com.mirth.connect.plugins.httpauth.javascript.JavaScriptAuthenticatorProvider;
import com.mirth.connect.plugins.httpauth.oauth2.OAuth2AuthenticatorProvider;

public class AuthenticatorProviderFactory {

    public static AuthenticatorProvider getAuthenticatorProvider(Connector connector, HttpAuthConnectorPluginProperties properties) throws Exception {
        switch (properties.getAuthType()) {
            case BASIC:
                return new BasicAuthenticatorProvider(connector, properties);
            case DIGEST:
                return new DigestAuthenticatorProvider(connector, properties);
            case JAVASCRIPT:
                return new JavaScriptAuthenticatorProvider(connector, properties);
            case CUSTOM:
                return new CustomAuthenticatorProvider(connector, properties);
            case OAUTH2_VERIFICATION:
                return new OAuth2AuthenticatorProvider(connector, properties);
            default:
                return null;
        }
    }
}