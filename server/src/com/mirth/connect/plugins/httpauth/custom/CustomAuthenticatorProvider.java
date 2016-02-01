/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.custom;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.plugins.httpauth.Authenticator;
import com.mirth.connect.plugins.httpauth.AuthenticatorProvider;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class CustomAuthenticatorProvider extends AuthenticatorProvider {

    private static final ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private static final TemplateValueReplacer replacer = new TemplateValueReplacer();

    public CustomAuthenticatorProvider(Connector connector, HttpAuthConnectorPluginProperties properties) {
        super(connector, properties);
    }

    @Override
    public Authenticator getAuthenticator() throws Exception {
        CustomHttpAuthProperties props = (CustomHttpAuthProperties) getProperties();

        String channelId = getConnector().getChannelId();
        String channelName = getConnector().getChannel().getName();
        String authenticatorClass = replacer.replaceValues(props.getAuthenticatorClass(), channelId, channelName);

        Map<String, String> properties = new LinkedHashMap<String, String>();
        for (Entry<String, String> entry : props.getProperties().entrySet()) {
            properties.put(replacer.replaceValues(entry.getKey(), channelId, channelName), replacer.replaceValues(entry.getValue(), channelId, channelName));
        }

        Class<?> clazz = Class.forName(authenticatorClass, true, contextFactoryController.getContextFactory(getConnector().getResourceIds()).getApplicationClassLoader());
        try {
            return (Authenticator) clazz.getConstructor(Map.class).newInstance(properties);
        } catch (Exception e) {
            return (Authenticator) clazz.newInstance();
        }
    }
}