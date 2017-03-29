/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

public class Permissions {
    // Alerts
    public static final String ALERTS_VIEW = "viewAlerts";
    public static final String ALERTS_MANAGE = "manageAlerts";

    // Channels
    public static final String CHANNELS_VIEW = "viewChannels";
    public static final String CHANNELS_MANAGE = "manageChannels";
    public static final String CHANNELS_CLEAR_STATS = "clearStatistics";
    public static final String CHANNELS_START_STOP = "startStopChannels";
    public static final String CHANNELS_DEPLOY_UNDEPLOY = "deployUndeployChannels";

    // Code Templates
    public static final String CODE_TEMPLATES_VIEW = "viewCodeTemplates";
    public static final String CODE_TEMPLATES_MANAGE = "manageCodeTemplates";

    // Global Scripts
    public static final String GLOBAL_SCRIPTS_VIEW = "viewGlobalScripts";
    public static final String GLOBAL_SCRIPTS_EDIT = "editGlobalScripts";

    // Messages
    public static final String MESSAGES_VIEW = "viewMessages";
    public static final String MESSAGES_REMOVE = "removeMessages";
    public static final String MESSAGES_REMOVE_RESULTS = "removeResults";
    public static final String MESSAGES_REMOVE_ALL = "removeAllMessages";
    public static final String MESSAGES_PROCESS = "processMessages";
    public static final String MESSAGES_REPROCESS = "reprocessMessages";
    public static final String MESSAGES_REPROCESS_RESULTS = "reprocessResults";
    public static final String MESSAGES_IMPORT = "importMessages";
    public static final String MESSAGES_EXPORT_SERVER = "exportMessagesServer";

    // Tags
    public static final String TAGS_VIEW = "viewTags";
    public static final String TAGS_MANAGE = "manageTags";

    // Events
    public static final String EVENTS_VIEW = "viewEvents";
    public static final String EVENTS_REMOVE = "removeEvents";

    // Users
    public static final String USERS_MANAGE = "manageUsers";

    // Extensions
    public static final String EXTENSIONS_MANAGE = "manageExtensions";

    // Server Configuration and Settings
    public static final String SERVER_CONFIGURATION_BACKUP = "backupServerConfiguration";
    public static final String SERVER_CONFIGURATION_RESTORE = "restoreServerConfiguration";
    public static final String SERVER_SETTINGS_VIEW = "viewServerSettings";
    public static final String SERVER_SETTINGS_EDIT = "editServerSettings";
    public static final String SERVER_CLEAR_LIFETIME_STATS = "clearLifetimeStats";
    public static final String SERVER_SEND_TEST_EMAIL = "sendTestEmail";

    // Configuration Map
    public static final String CONFIGURATION_MAP_VIEW = "viewConfigurationMap";
    public static final String CONFIGURATION_MAP_EDIT = "editConfigurationMap";

    // Database Tasks
    public static final String DATABASE_TASKS_VIEW = "viewDatabaseTasks";
    public static final String DATABASE_TASKS_MANAGE = "manageDatabaseTasks";

    // Resources
    public static final String RESOURCES_VIEW = "viewResources";
    public static final String RESOURCES_EDIT = "editResources";
    public static final String RESOURCES_RELOAD = "reloadResources";
}
