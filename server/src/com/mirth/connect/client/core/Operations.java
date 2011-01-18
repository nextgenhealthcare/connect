/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

public class Operations {
    // Alerts
    public static final String ALERT_GET = "getAlert";
    public static final String ALERT_UPDATE = "updateAlerts";
    public static final String ALERT_REMOVE = "removeAlert";
    
    // Channels
    public static final String CHANNEL_GET = "getChannel";
    public static final String CHANNEL_UPDATE = "updateChannel";
    public static final String CHANNEL_REMOVE = "removeChannel";
    public static final String CHANNEL_GET_SUMMARY = "getChannelSummary";
    
    // Channel Statistics
    public static final String CHANNEL_STATS_GET = "getStatistics";
    public static final String CHANNEL_STATS_CLEAR = "clearStatistics";
    
    // Channel Status
    public static final String CHANNEL_START = "startChannel";
    public static final String CHANNEL_STOP = "stopChannel";
    public static final String CHANNEL_PAUSE = "pauseChannel";
    public static final String CHANNEL_RESUME = "resumeChannel";
    public static final String CHANNEL_GET_STATUS = "getChannelStatusList";
    
    // Code Templates
    public static final String CODE_TEMPLATE_GET = "getCodeTemplate";
    public static final String CODE_TEMPLATE_UPDATE = "updateCodeTemplates";
    public static final String CODE_TEMPLATE_REMOVE = "removeCodeTemplate";
    
    // Configuration
    public static final String GLOBAL_SCRIPT_GET = "getGlobalScripts";
    public static final String GLOBAL_SCRIPT_SET = "setGlobalScripts";
    public static final String CONFIGURATION_CHARSET_ENCODINGS_GET = "getAvailableCharsetEncodings";
    public static final String CONFIGURATION_SERVER_PROPERTIES_GET = "getServerProperties";
    public static final String CONFIGURATION_SERVER_PROPERTIES_SET = "setServerProperties";
    public static final String CONFIGURATION_GUID_GET = "getGuid";
    public static final String CONFIGURATION_DATABASE_DRIVERS_GET = "getDatabaseDrivers";
    public static final String CONFIGURATION_VERSION_GET = "getVersion";
    public static final String CONFIGURATION_BUILD_DATE_GET = "getBuildDate";
    public static final String SERVER_CONFIGURATION_GET = "getServerConfiguration";
    public static final String SERVER_CONFIGURATION_SET = "setServerConfiguration";
    public static final String CONFIGURATION_SERVER_ID_GET = "getServerId";
    public static final String CONFIGURATION_SERVER_TIMEZONE_GET = "getServerTimezone";
    public static final String CONFIGURATION_PASSWORD_REQUIREMENTS_GET = "getPasswordRequirements";
    public static final String CONFIGURATION_STATUS_GET = "getStatus";
    
    // Engine
    public static final String CHANNEL_DEPLOY = "deployChannels";
    public static final String CHANNEL_REDEPLOY = "redeployAllChannels";
    public static final String CHANNEL_UNDEPLOY = "undeployChannels";
    
    // Extensions
    public static final String PLUGIN_PROPERTIES_GET = "getPluginProperties";
    public static final String PLUGIN_PROPERTIES_SET = "setPluginProperties";
    public static final String PLUGIN_METADATA_GET = "getPluginMetaData";
    public static final String PLUGIN_METADATA_SET = "setPluginMetaData";
    public static final String CONNECTOR_METADATA_GET = "getConnectorMetaData";
    public static final String CONNECTOR_METADATA_SET = "setConnectorMetaData";
    public static final String PLUGIN_SERVICE_INVOKE = "invoke";
    public static final String CONNECTOR_SERVICE_INVOKE = "invokeConnectorService";
    public static final String EXTENSION_INSTALL = "installExtension";
    public static final String EXTENSION_UNINSTALL = "uninstallExtension";
    public static final String EXTENSION_IS_ENABLED = "isExtensionEnabled";
    
    // Messages
    public static final String MESSAGE_GET_BY_PAGE = "getMessagesByPage";
    public static final String MESSAGE_GET_BY_PAGE_LIMIT = "getMessagesByPageLimit";
    public static final String MESSAGE_REMOVE = "removeMessages";
    public static final String MESSAGE_CLEAR = "clearMessages";
    public static final String MESSAGE_PROCESS = "processMessages";
    public static final String MESSAGE_REPROCESS = "reprocessMessages";
    public static final String MESSAGE_IMPORT = "importMessage";
    public static final String MESSAGE_ATTACHMENT_GET = "getAttachment";
    public static final String MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID = "getAttachmentsByMessageId";
    public static final String MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID = "getAttachmentIdsByMessageId";
    public static final String MESSAGE_DICOM_MESSAGE_GET = "getDICOMMessage";
    public static final String MESSAGE_CREATE_TEMP_TABLE = "createMessagesTempTable";
    public static final String MESSAGE_FILTER_TABLES_REMOVE = "removeFilterTables";
    
    // Events
    public static final String EVENT_CREATE_TEMP_TABLE = "createEventTempTable";
    public static final String EVENT_REMOVE_FILTER_TABLES = "removeEventFilterTables";
    public static final String EVENT_GET_BY_PAGE = "getEventsByPage";
    public static final String EVENT_GET_BY_PAGE_LIMIT = "getEventsByPageLimit";
    public static final String EVENT_CLEAR = "clearSystemEvents";
    
    // Users
    public static final String USER_GET = "getUser";
    public static final String USER_UPDATE = "updateUser";
    public static final String USER_REMOVE = "removeUser";
    public static final String USER_AUTHORIZE = "authorizeUser";
    public static final String USER_LOGIN = "login";
    public static final String USER_LOGOUT = "logout";
    public static final String USER_IS_USER_LOGGED_IN = "isUserLoggedIn";
    public static final String USER_PREFERENCES_GET = "getUserPreferences";
    public static final String USER_PREFERENCES_SET = "setUserPreference";

}
