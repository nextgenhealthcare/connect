/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

public abstract class ControllerFactory {
    private static ControllerFactory factory;

    public static ControllerFactory getFactory() {
        synchronized (ControllerFactory.class) {
            if (factory == null) {
                factory = new DefaultControllerFactory();
            }

            return factory;
        }
    }

    public abstract AuthorizationController createAuthorizationController();

    public abstract AlertController createAlertController();

    public abstract ChannelController createChannelController();

    public abstract ChannelStatisticsController createChannelStatisticsController();

    public abstract ChannelStatusController createChannelStatusController();

    public abstract CodeTemplateController createCodeTemplateController();

    public abstract ConfigurationController createConfigurationController();

    public abstract EngineController createEngineController();

    public abstract ExtensionController createExtensionController();

    public abstract MessageObjectController createMessageObjectController();

    public abstract MigrationController createMigrationController();

    public abstract MonitoringController createMonitoringController();

    public abstract ScriptController createScriptController();

    public abstract EventController createEventController();

    public abstract TemplateController createTemplateController();

    public abstract UserController createUserController();
}
