package com.webreach.mirth.server.controllers;

public class DefaultControllerFactory extends ControllerFactory {
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
