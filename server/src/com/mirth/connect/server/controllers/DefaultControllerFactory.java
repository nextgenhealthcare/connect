/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import org.apache.log4j.Logger;

public class DefaultControllerFactory extends ControllerFactory {
    private Logger logger = Logger.getLogger(this.getClass());
    private AuthorizationController authorizationController = null;
    private EngineController engineController = null;

    public synchronized AuthorizationController createAuthorizationController() {
        if (authorizationController == null) {
            ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
            extensionController.loadExtensions();

            if (extensionController.getPluginMetaData().containsKey("User Authorization")) {
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

    public CodeTemplateController createCodeTemplateController() {
        return DefaultCodeTemplateController.create();
    }

    public ConfigurationController createConfigurationController() {
        return DefaultConfigurationController.create();
    }

    public synchronized EngineController createEngineController() {
        /*
         * Eventually, plugins will be able to specify controller classes to override,
         * see MIRTH-3351
         */
        if (engineController == null) {
            ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
            extensionController.loadExtensions();

            if (extensionController.getPluginMetaData().containsKey("Clustering")) {
                try {
                    String clusterEngineController = "com.mirth.connect.plugins.clustering.server.ClusterEngineController";
                    engineController = (EngineController) Class.forName(clusterEngineController).newInstance();
                    logger.debug("using engine controller: " + clusterEngineController);
                } catch (Exception e) {
                }
            }

            if (engineController == null) {
                engineController = DonkeyEngineController.getInstance();
                logger.debug("using default engine controller");
            }
        }

        return engineController;
    }

    public EventController createEventController() {
        return DefaultEventController.create();
    }

    public ExtensionController createExtensionController() {
        return DefaultExtensionController.create();
    }

    public MessageController createMessageController() {
        return DonkeyMessageController.create();
    }

    public MigrationController createMigrationController() {
        return DefaultMigrationController.create();
    }

    public ScriptController createScriptController() {
        return DefaultScriptController.create();
    }

    public UserController createUserController() {
        return DefaultUserController.create();
    }

}
