/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.util.HashMap;
import java.util.Map;

public class Operations {
    // Alerts
    public static final Operation ALERT_GET = new Operation("getAlert", "Get alert", true);
    public static final Operation ALERT_UPDATE = new Operation("updateAlerts", "Update alerts", true);
    public static final Operation ALERT_REMOVE = new Operation("removeAlert", "Remove alert", true);

    // Channels
    public static final Operation CHANNEL_GET = new Operation("getChannel", "Get channel", true);
    public static final Operation CHANNEL_UPDATE = new Operation("updateChannel", "Update channel", true);
    public static final Operation CHANNEL_REMOVE = new Operation("removeChannel", "Remove channel", true);
    public static final Operation CHANNEL_GET_SUMMARY = new Operation("getChannelSummary", "Get channel summary", false);

    // Channel Statistics
    public static final Operation CHANNEL_STATS_GET = new Operation("getStatistics", "Get statistics", false);
    public static final Operation CHANNEL_STATS_CLEAR = new Operation("clearStatistics", "Clear statistics", true);

    // Channel Status
    public static final Operation CHANNEL_START = new Operation("startChannel", "Start channel", true);
    public static final Operation CHANNEL_STOP = new Operation("stopChannel", "Stop channel", true);
    public static final Operation CHANNEL_PAUSE = new Operation("pauseChannel", "Pause channel", true);
    public static final Operation CHANNEL_RESUME = new Operation("resumeChannel", "Resume channel", true);
    public static final Operation CHANNEL_GET_STATUS = new Operation("getChannelStatusList", "Get channel status list", false);

    // Code Templates
    public static final Operation CODE_TEMPLATE_GET = new Operation("getCodeTemplate", "Get code template", true);
    public static final Operation CODE_TEMPLATE_UPDATE = new Operation("updateCodeTemplates", "Update code template", true);
    public static final Operation CODE_TEMPLATE_REMOVE = new Operation("removeCodeTemplate", "Remove code template", true);

    // Configuration
    public static final Operation GLOBAL_SCRIPT_GET = new Operation("getGlobalScripts", "Get global scripts", true);
    public static final Operation GLOBAL_SCRIPT_SET = new Operation("setGlobalScripts", "Set global scripts", true);
    public static final Operation CONFIGURATION_CHARSET_ENCODINGS_GET = new Operation("getAvailableCharsetEncodings", "Get available charset encodings", false);
    public static final Operation CONFIGURATION_SERVER_SETTINGS_GET = new Operation("getServerSettings", "Get server settings", true);
    public static final Operation CONFIGURATION_SERVER_SETTINGS_SET = new Operation("setServerSettings", "Set server settings", true);
    public static final Operation CONFIGURATION_UPDATE_SETTINGS_GET = new Operation("getUpdateSettings", "Get update settings", false);
    public static final Operation CONFIGURATION_UPDATE_SETTINGS_SET = new Operation("setUpdateSettings", "Set update settings", true);
    public static final Operation CONFIGURATION_GUID_GET = new Operation("getGuid", "Get GUID", false);
    public static final Operation CONFIGURATION_DATABASE_DRIVERS_GET = new Operation("getDatabaseDrivers", "Get database drivers", false);
    public static final Operation CONFIGURATION_VERSION_GET = new Operation("getVersion", "Get version", false);
    public static final Operation CONFIGURATION_BUILD_DATE_GET = new Operation("getBuildDate", "Get build date", false);
    public static final Operation SERVER_CONFIGURATION_GET = new Operation("getServerConfiguration", "Get server configuration", true);
    public static final Operation SERVER_CONFIGURATION_SET = new Operation("setServerConfiguration", "Set server configuration", true);
    public static final Operation CONFIGURATION_SERVER_ID_GET = new Operation("getServerId", "Get server ID", false);
    public static final Operation CONFIGURATION_SERVER_TIMEZONE_GET = new Operation("getServerTimezone", "Get server timezone", false);
    public static final Operation CONFIGURATION_PASSWORD_REQUIREMENTS_GET = new Operation("getPasswordRequirements", "Get password requirements", true);
    public static final Operation CONFIGURATION_STATUS_GET = new Operation("getStatus", "Get status", true);

    // Engine
    public static final Operation CHANNEL_DEPLOY = new Operation("deployChannels", "Deploy channels", true);
    public static final Operation CHANNEL_REDEPLOY = new Operation("redeployAllChannels", "Redeploy all channels", true);
    public static final Operation CHANNEL_UNDEPLOY = new Operation("undeployChannels", "Undeploy channels", true);

    // Extensions
    public static final Operation PLUGIN_PROPERTIES_GET = new Operation("getPluginProperties", "Get plugin properties", true);
    public static final Operation PLUGIN_PROPERTIES_SET = new Operation("setPluginProperties", "Set plugin properties", true);
    public static final Operation PLUGIN_METADATA_GET = new Operation("getPluginMetaData", "Get plugin metadata", true);
    public static final Operation PLUGIN_METADATA_SET = new Operation("setPluginMetaData", "Set plugin metadata", true);
    public static final Operation CONNECTOR_METADATA_GET = new Operation("getConnectorMetaData", "Get connector metadata", true);
    public static final Operation CONNECTOR_METADATA_SET = new Operation("setConnectorMetaData", "Set connector metadata", true);
    public static final Operation PLUGIN_SERVICE_INVOKE = new Operation("invoke", "Invoke plugin service", true);
    public static final Operation CONNECTOR_SERVICE_INVOKE = new Operation("invokeConnectorService", "Invoke connector service", true);
    public static final Operation EXTENSION_INSTALL = new Operation("installExtension", "Install extension", true);
    public static final Operation EXTENSION_UNINSTALL = new Operation("uninstallExtension", "Uninstall extension", true);
    public static final Operation EXTENSION_IS_ENABLED = new Operation("isExtensionEnabled", "Check if extension is installed", true);

    // Messages
    public static final Operation MESSAGE_GET_BY_PAGE = new Operation("getMessagesByPage", "Get messages by page", false);
    public static final Operation MESSAGE_GET_BY_PAGE_LIMIT = new Operation("getMessagesByPageLimit", "Get messages by page limit", true);
    public static final Operation MESSAGE_REMOVE = new Operation("removeMessages", "Remove messages", true);
    public static final Operation MESSAGE_CLEAR = new Operation("clearMessages", "Clear messages", true);
    public static final Operation MESSAGE_PROCESS = new Operation("processMessages", "Process messages", true);
    public static final Operation MESSAGE_REPROCESS = new Operation("reprocessMessages", "Reprocess messages", true);
    public static final Operation MESSAGE_IMPORT = new Operation("importMessage", "Import message", true);
    public static final Operation MESSAGE_ATTACHMENT_GET = new Operation("getAttachment", "Get attachment", true);
    public static final Operation MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID = new Operation("getAttachmentsByMessageId", "Get attachments by message ID", false);
    public static final Operation MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID = new Operation("getAttachmentIdsByMessageId", "Get attachment IDs by message ID", false);
    public static final Operation MESSAGE_DICOM_MESSAGE_GET = new Operation("getDICOMMessage", "Get DICOM message", false);
    public static final Operation MESSAGE_CREATE_TEMP_TABLE = new Operation("createMessagesTempTable", "Create message temp table", false);
    public static final Operation MESSAGE_FILTER_TABLES_REMOVE = new Operation("removeFilterTables", "Remove filter tables", false);

    // Events
    public static final Operation EVENT_CREATE_TEMP_TABLE = new Operation("createEventTempTable", "Create event temp tables", false);
    public static final Operation EVENT_REMOVE_FILTER_TABLES = new Operation("removeEventFilterTables", "Remove event filter tables", false);
    public static final Operation EVENT_GET_BY_PAGE = new Operation("getEventsByPage", "Get events by page", false);
    public static final Operation EVENT_GET_BY_PAGE_LIMIT = new Operation("getEventsByPageLimit", "Get events by page limit", false);
    public static final Operation EVENT_REMOVE_ALL = new Operation("removeAllEvents", "Remove all events", true);
    public static final Operation EVENT_EXPORT_AND_REMOVE_REMOVE_ALL = new Operation("exportAndRemoveAllEvents", "Export and remove all events.", false);
    public static final Operation EVENT_EXPORT_ALL = new Operation("exportAllEvents", "Export all events.", true);

    // Users
    public static final Operation USER_GET = new Operation("getUser", "Get user", false);
    public static final Operation USER_UPDATE = new Operation("updateUser", "Update user", true);
    public static final Operation USER_REMOVE = new Operation("removeUser", "Remove user", true);
    public static final Operation USER_AUTHORIZE = new Operation("authorizeUser", "Authorize user", true);
    public static final Operation USER_LOGIN = new Operation("login", "Login", true);
    public static final Operation USER_LOGOUT = new Operation("logout", "Logout", true);
    public static final Operation USER_IS_USER_LOGGED_IN = new Operation("isUserLoggedIn", "Check if user is logged in", true);
    public static final Operation USER_PREFERENCES_GET = new Operation("getUserPreferences", "Get user preferences", true);
    public static final Operation USER_PREFERENCES_SET = new Operation("setUserPreference", "Set user preferences", true);

    private static Map<String, Operation> operationMap = new HashMap<String, Operation>();

    static {
        operationMap.put(ALERT_GET.getName(), ALERT_GET);
        operationMap.put(ALERT_UPDATE.getName(), ALERT_UPDATE);
        operationMap.put(ALERT_REMOVE.getName(), ALERT_REMOVE);
        operationMap.put(CHANNEL_GET.getName(), CHANNEL_GET);
        operationMap.put(CHANNEL_UPDATE.getName(), CHANNEL_UPDATE);
        operationMap.put(CHANNEL_REMOVE.getName(), CHANNEL_REMOVE);
        operationMap.put(CHANNEL_GET_SUMMARY.getName(), CHANNEL_GET_SUMMARY);
        operationMap.put(CHANNEL_STATS_GET.getName(), CHANNEL_STATS_GET);
        operationMap.put(CHANNEL_STATS_CLEAR.getName(), CHANNEL_STATS_CLEAR);
        operationMap.put(CHANNEL_START.getName(), CHANNEL_START);
        operationMap.put(CHANNEL_STOP.getName(), CHANNEL_STOP);
        operationMap.put(CHANNEL_PAUSE.getName(), CHANNEL_PAUSE);
        operationMap.put(CHANNEL_RESUME.getName(), CHANNEL_RESUME);
        operationMap.put(CHANNEL_GET_STATUS.getName(), CHANNEL_GET_STATUS);
        operationMap.put(CODE_TEMPLATE_GET.getName(), CODE_TEMPLATE_GET);
        operationMap.put(CODE_TEMPLATE_UPDATE.getName(), CODE_TEMPLATE_UPDATE);
        operationMap.put(CODE_TEMPLATE_REMOVE.getName(), CODE_TEMPLATE_REMOVE);
        operationMap.put(GLOBAL_SCRIPT_GET.getName(), GLOBAL_SCRIPT_GET);
        operationMap.put(GLOBAL_SCRIPT_SET.getName(), GLOBAL_SCRIPT_SET);
        operationMap.put(CONFIGURATION_CHARSET_ENCODINGS_GET.getName(), CONFIGURATION_CHARSET_ENCODINGS_GET);
        operationMap.put(CONFIGURATION_SERVER_SETTINGS_GET.getName(), CONFIGURATION_SERVER_SETTINGS_GET);
        operationMap.put(CONFIGURATION_SERVER_SETTINGS_SET.getName(), CONFIGURATION_SERVER_SETTINGS_SET);
        operationMap.put(CONFIGURATION_UPDATE_SETTINGS_GET.getName(), CONFIGURATION_UPDATE_SETTINGS_GET);
        operationMap.put(CONFIGURATION_UPDATE_SETTINGS_SET.getName(), CONFIGURATION_UPDATE_SETTINGS_SET);
        operationMap.put(CONFIGURATION_GUID_GET.getName(), CONFIGURATION_GUID_GET);
        operationMap.put(CONFIGURATION_DATABASE_DRIVERS_GET.getName(), CONFIGURATION_DATABASE_DRIVERS_GET);
        operationMap.put(CONFIGURATION_VERSION_GET.getName(), CONFIGURATION_VERSION_GET);
        operationMap.put(CONFIGURATION_BUILD_DATE_GET.getName(), CONFIGURATION_BUILD_DATE_GET);
        operationMap.put(SERVER_CONFIGURATION_GET.getName(), SERVER_CONFIGURATION_GET);
        operationMap.put(SERVER_CONFIGURATION_SET.getName(), SERVER_CONFIGURATION_SET);
        operationMap.put(CONFIGURATION_SERVER_ID_GET.getName(), CONFIGURATION_SERVER_ID_GET);
        operationMap.put(CONFIGURATION_SERVER_TIMEZONE_GET.getName(), CONFIGURATION_SERVER_TIMEZONE_GET);
        operationMap.put(CONFIGURATION_PASSWORD_REQUIREMENTS_GET.getName(), CONFIGURATION_PASSWORD_REQUIREMENTS_GET);
        operationMap.put(CONFIGURATION_STATUS_GET.getName(), CONFIGURATION_STATUS_GET);
        operationMap.put(CHANNEL_DEPLOY.getName(), CHANNEL_DEPLOY);
        operationMap.put(CHANNEL_REDEPLOY.getName(), CHANNEL_REDEPLOY);
        operationMap.put(CHANNEL_UNDEPLOY.getName(), CHANNEL_UNDEPLOY);
        operationMap.put(PLUGIN_PROPERTIES_GET.getName(), PLUGIN_PROPERTIES_GET);
        operationMap.put(PLUGIN_PROPERTIES_SET.getName(), PLUGIN_PROPERTIES_SET);
        operationMap.put(PLUGIN_METADATA_GET.getName(), PLUGIN_METADATA_GET);
        operationMap.put(PLUGIN_METADATA_SET.getName(), PLUGIN_METADATA_SET);
        operationMap.put(CONNECTOR_METADATA_GET.getName(), CONNECTOR_METADATA_GET);
        operationMap.put(CONNECTOR_METADATA_SET.getName(), CONNECTOR_METADATA_SET);
        operationMap.put(PLUGIN_SERVICE_INVOKE.getName(), PLUGIN_SERVICE_INVOKE);
        operationMap.put(CONNECTOR_SERVICE_INVOKE.getName(), CONNECTOR_SERVICE_INVOKE);
        operationMap.put(EXTENSION_INSTALL.getName(), EXTENSION_INSTALL);
        operationMap.put(EXTENSION_UNINSTALL.getName(), EXTENSION_UNINSTALL);
        operationMap.put(EXTENSION_IS_ENABLED.getName(), EXTENSION_IS_ENABLED);
        operationMap.put(MESSAGE_GET_BY_PAGE.getName(), MESSAGE_GET_BY_PAGE);
        operationMap.put(MESSAGE_GET_BY_PAGE_LIMIT.getName(), MESSAGE_GET_BY_PAGE_LIMIT);
        operationMap.put(MESSAGE_REMOVE.getName(), MESSAGE_REMOVE);
        operationMap.put(MESSAGE_CLEAR.getName(), MESSAGE_CLEAR);
        operationMap.put(MESSAGE_PROCESS.getName(), MESSAGE_PROCESS);
        operationMap.put(MESSAGE_REPROCESS.getName(), MESSAGE_REPROCESS);
        operationMap.put(MESSAGE_IMPORT.getName(), MESSAGE_IMPORT);
        operationMap.put(MESSAGE_ATTACHMENT_GET.getName(), MESSAGE_ATTACHMENT_GET);
        operationMap.put(MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID.getName(), MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID);
        operationMap.put(MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID.getName(), MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID);
        operationMap.put(MESSAGE_DICOM_MESSAGE_GET.getName(), MESSAGE_DICOM_MESSAGE_GET);
        operationMap.put(MESSAGE_CREATE_TEMP_TABLE.getName(), MESSAGE_CREATE_TEMP_TABLE);
        operationMap.put(MESSAGE_FILTER_TABLES_REMOVE.getName(), MESSAGE_FILTER_TABLES_REMOVE);
        operationMap.put(EVENT_CREATE_TEMP_TABLE.getName(), EVENT_CREATE_TEMP_TABLE);
        operationMap.put(EVENT_REMOVE_FILTER_TABLES.getName(), EVENT_REMOVE_FILTER_TABLES);
        operationMap.put(EVENT_GET_BY_PAGE.getName(), EVENT_GET_BY_PAGE);
        operationMap.put(EVENT_GET_BY_PAGE_LIMIT.getName(), EVENT_GET_BY_PAGE_LIMIT);
        operationMap.put(EVENT_REMOVE_ALL.getName(), EVENT_REMOVE_ALL);
        operationMap.put(USER_GET.getName(), USER_GET);
        operationMap.put(USER_UPDATE.getName(), USER_UPDATE);
        operationMap.put(USER_REMOVE.getName(), USER_REMOVE);
        operationMap.put(USER_AUTHORIZE.getName(), USER_AUTHORIZE);
        operationMap.put(USER_LOGIN.getName(), USER_LOGIN);
        operationMap.put(USER_LOGOUT.getName(), USER_LOGOUT);
        operationMap.put(USER_IS_USER_LOGGED_IN.getName(), USER_IS_USER_LOGGED_IN);
        operationMap.put(USER_PREFERENCES_GET.getName(), USER_PREFERENCES_GET);
        operationMap.put(USER_PREFERENCES_SET.getName(), USER_PREFERENCES_SET);
    }
    
    public static void addOperation(Operation operation) {
        operationMap.put(operation.getName(), operation);
    }
    
    public static Operation getOperation(String operationName) {
        return operationMap.get(operationName);
    }
}
