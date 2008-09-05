package com.webreach.mirth.server.controllers;

import java.util.Properties;

import com.webreach.mirth.util.PropertyLoader;

public abstract class ControllerFactory {
    private static ControllerFactory factory;

    public static ControllerFactory getFactory() {
        synchronized (ControllerFactory.class) {
            if (factory == null) {
                Properties mirthProperties = PropertyLoader.loadProperties("mirth");
                String factoryClassName = mirthProperties.getProperty("controllerfactory");

                if (factoryClassName != null) {
                    try {
                        factory = (ControllerFactory) Class.forName(factoryClassName).newInstance();
                    } catch (Exception e) {
                        // couldn't find a factory
                    }
                }

                factory = new DefaultControllerFactory();
            }

            return factory;
        }
    }

    public abstract AlertController createAlertController();

    public abstract ChannelController createChannelController();

    public abstract ChannelStatisticsController createChannelStatisticsController();

    public abstract ChannelStatusController createChannelStatusController();

    public abstract CodeTemplateController createCodeTemplateController();

    public abstract ConfigurationController createConfigurationController();

    public abstract ExtensionController createExtensionController();

    public abstract MessageObjectController createMessageObjectController();

    public abstract MigrationController createMigrationController();

    public abstract MonitoringController createMonitoringController();

    public abstract ScriptController createScriptContorller();

    public abstract SystemLogger createSystemLogger();

    public abstract TemplateController createTemplateController();

    public abstract UserController createUserController();
}
