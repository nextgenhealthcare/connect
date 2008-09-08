package com.webreach.mirth.server.controllers;

public class DefaultControllerFactory extends ControllerFactory {
    public AlertController createAlertController() {
        return DefaultAlertController.getInstance();
    }

    public ChannelController createChannelController() {
        return DefaultChannelController.getInstance();
    }

    public ChannelStatisticsController createChannelStatisticsController() {
        return DefaultChannelStatisticsController.getInstance();
    }

    public ChannelStatusController createChannelStatusController() {
        return ChannelStatusController.getInstance();
    }

    public CodeTemplateController createCodeTemplateController() {
        return DefaultCodeTemplateController.getInstance();
    }

    public ConfigurationController createConfigurationController() {
        return DefaultConfigurationController.getInstance();
    }

    public ExtensionController createExtensionController() {
        return DefaultExtensionController.getInstance();
    }

    public MessageObjectController createMessageObjectController() {
        return DefaultMessageObjectController.getInstance();
    }

    public MigrationController createMigrationController() {
        return DefaultMigrationController.getInstance();
    }

    public MonitoringController createMonitoringController() {
        return DefaultMonitoringController.getInstance();
    }

    public ScriptController createScriptContorller() {
        return DefaultScriptController.getInstance();
    }

    public EventController createEventController() {
        return DefaultEventController.getInstance();
    }

    public TemplateController createTemplateController() {
        return DefaultTemplateController.getInstance();
    }

    public UserController createUserController() {
        return DefaultUserController.getInstance();
    }

}
