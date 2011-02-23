/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import org.apache.log4j.Logger;

public class DefaultControllerFactory extends ControllerFactory {
    private Logger logger = Logger.getLogger(this.getClass());
    private AuthorizationController authorizationController = null;

    public AuthorizationController createAuthorizationController() {
        if (authorizationController == null) {
            ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
            if (extensionController.getPluginMetaData().containsKey("User Authorization Plugin")) {
                try {
                    String serverAuthorizationController = "com.mirth.connect.plugins.auth.server.SecureAuthorizationController";
                    authorizationController = (AuthorizationController) Class.forName(serverAuthorizationController).newInstance();
                    logger.debug("using authorization controller: " + serverAuthorizationController);
                } catch (Exception e) {
                    // could not instantiate controller, using default
                    authorizationController = DefaultAuthorizationController.create();
                    logger.debug("using default authorization controller");
                }
            } else {
                authorizationController = DefaultAuthorizationController.create();
                logger.debug("using default authorization controller");
            }
        }

        return authorizationController;
    }

    public AlertController createAlertController() {
        return DefaultAlertController.create();
    }

    public ChannelController createChannelController() {
        return DefaultChannelController.create();
    }

    public ChannelStatisticsController createChannelStatisticsController() {
        return DefaultChannelStatisticsController.create();
    }

    public ChannelStatusController createChannelStatusController() {
        return DefaultChannelStatusController.create();
    }

    public CodeTemplateController createCodeTemplateController() {
        return DefaultCodeTemplateController.create();
    }

    public ConfigurationController createConfigurationController() {
        return DefaultConfigurationController.create();
    }

    public EngineController createEngineController() {
        return MuleEngineController.create();
    }

    public ExtensionController createExtensionController() {
        return DefaultExtensionController.create();
    }

    public MessageObjectController createMessageObjectController() {
        return DefaultMessageObjectController.create();
    }

    public MigrationController createMigrationController() {
        return DefaultMigrationController.create();
    }

    public MonitoringController createMonitoringController() {
        return DefaultMonitoringController.create();
    }

    public ScriptController createScriptController() {
        return DefaultScriptController.create();
    }

    public EventController createEventController() {
        return DefaultEventController.create();
    }

    public TemplateController createTemplateController() {
        return DefaultTemplateController.create();
    }

    public UserController createUserController() {
        return DefaultUserController.create();
    }

}
