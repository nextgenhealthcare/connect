/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.oauth2;

import com.mirth.connect.connectors.http.DefaultHttpConfiguration;
import com.mirth.connect.connectors.http.HttpConfiguration;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.plugins.httpauth.Authenticator;
import com.mirth.connect.plugins.httpauth.AuthenticatorProvider;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class OAuth2AuthenticatorProvider extends AuthenticatorProvider {

    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    private HttpConfiguration configuration;

    public OAuth2AuthenticatorProvider(Connector connector, HttpAuthConnectorPluginProperties properties) {
        super(connector, properties);
        try {
            configuration = (HttpConfiguration) Class.forName(configurationController.getProperty("HTTP", "httpConfigurationClass")).newInstance();
        } catch (Exception e) {
            configuration = new DefaultHttpConfiguration();
        }
    }

    HttpConfiguration getHttpConfiguration() {
        return configuration;
    }

    @Override
    public Authenticator getAuthenticator() throws Exception {
        return new OAuth2Authenticator(this);
    }
}