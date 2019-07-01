/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.javascript;

import java.util.UUID;

import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.plugins.httpauth.Authenticator;
import com.mirth.connect.plugins.httpauth.AuthenticatorProvider;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptAuthenticatorProvider extends AuthenticatorProvider {

    private static final ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();

    private String scriptId;
    private volatile String contextFactoryId;

    public JavaScriptAuthenticatorProvider(Connector connector, HttpAuthConnectorPluginProperties properties) throws Exception {
        super(connector, properties);
        JavaScriptHttpAuthProperties props = (JavaScriptHttpAuthProperties) properties;

        scriptId = UUID.randomUUID().toString();
        MirthContextFactory contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());
        contextFactoryId = contextFactory.getId();
        JavaScriptUtil.compileAndAddScript(connector.getChannelId(), contextFactory, scriptId, props.getScript(), ContextType.SOURCE_RECEIVER, null, null);
    }

    String getScriptId() {
        return scriptId;
    }

    MirthContextFactory getContextFactory() throws Exception {
        MirthContextFactory contextFactory = contextFactoryController.getContextFactory(getConnector().getResourceIds());

        if (!contextFactoryId.equals(contextFactory.getId())) {
            synchronized (this) {
                contextFactory = contextFactoryController.getContextFactory(getConnector().getResourceIds());

                if (!contextFactoryId.equals(contextFactory.getId())) {
                    JavaScriptUtil.recompileGeneratedScript(contextFactory, scriptId);
                    contextFactoryId = contextFactory.getId();
                }
            }
        }

        return contextFactory;
    }

    @Override
    public Authenticator getAuthenticator() throws Exception {
        return new JavaScriptAuthenticator(this);
    }

    @Override
    public void shutdown() {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }
}