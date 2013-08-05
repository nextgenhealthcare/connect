/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

public class TaskConstants {
    // Task pane name keys
    public static final String OTHER_KEY = "other";
    public static final String GLOBAL_SCRIPT_KEY = "script";
    public static final String CODE_TEMPLATE_KEY = "codeTemplate";
    public static final String USER_KEY = "user";
    public static final String MESSAGE_KEY = "message";
    public static final String EVENT_KEY = "event";
    public static final String DASHBOARD_KEY = "dashboard";
    public static final String CHANNEL_EDIT_KEY = "channelEdit";
    public static final String CHANNEL_KEY = "channel";
    public static final String ALERT_KEY = "alert";
    public static final String ALERT_EDIT_KEY = "alertEdit";
    public static final String EXTENSIONS_KEY = "extensions";
    public static final String VIEW_KEY = "view";
    public static final String SETTINGS_KEY_PREFIX = "settings_";
    public static final String SETTINGS_SERVER_KEY = SETTINGS_KEY_PREFIX + "Server";

    // View Tasks
    public static final String VIEW_DASHBOARD = "doShowDashboard";
    public static final String VIEW_CHANNEL = "doShowChannel";
    public static final String VIEW_USERS = "doShowUsers";
    public static final String VIEW_SETTINGS = "doShowSettings";
    public static final String VIEW_ALERTS = "doShowAlerts";
    public static final String VIEW_EVENTS = "doShowEvents";
    public static final String VIEW_EXTENSIONS = "doShowExtensions";

    // Settings Tasks
    public static final String SETTINGS_REFRESH = "doRefresh";
    public static final String SETTINGS_SAVE = "doSave";
    
    // Server Settings Tasks
    public static final String SETTINGS_SERVER_BACKUP = "doBackup";
    public static final String SETTINGS_SERVER_RESTORE = "doRestore";
    public static final String SETTINGS_CLEAR_ALL_STATS = "doClearAllStats";

    // Alert Tasks
    public static final String ALERT_REFRESH = "doRefreshAlerts";
    public static final String ALERT_NEW = "doNewAlert";
    public static final String ALERT_IMPORT = "doImportAlert";
    public static final String ALERT_EXPORT_ALL = "doExportAlerts";
    public static final String ALERT_EXPORT = "doExportAlert";
    public static final String ALERT_DELETE = "doDeleteAlert";
    public static final String ALERT_EDIT = "doEditAlert";
    public static final String ALERT_ENABLE = "doEnableAlert";
    public static final String ALERT_DISABLE = "doDisableAlert";
    
    // Alert Edit Tasks
    public static final String ALERT_EDIT_SAVE = "doSaveAlerts";
    public static final String ALERT_EDIT_EXPORT = "doExportAlert";

    // Channel Tasks
    public static final String CHANNEL_REFRESH = "doRefreshChannels";
    public static final String CHANNEL_REDEPLOY_ALL = "doRedeployAll";
    public static final String CHANNEL_DEPLOY = "doDeployChannel";
    public static final String CHANNEL_EDIT_GLOBAL_SCRIPTS = "doEditGlobalScripts";
    public static final String CHANNEL_EDIT_CODE_TEMPLATES = "doEditCodeTemplates";
    public static final String CHANNEL_NEW = "doNewChannel";
    public static final String CHANNEL_IMPORT = "doImport";
    public static final String CHANNEL_EXPORT_ALL = "doExportAll";
    public static final String CHANNEL_EXPORT = "doExportChannel";
    public static final String CHANNEL_DELETE = "doDeleteChannel";
    public static final String CHANNEL_CLONE = "doCloneChannel";
    public static final String CHANNEL_EDIT = "doEditChannel";
    public static final String CHANNEL_ENABLE = "doEnableChannel";
    public static final String CHANNEL_DISABLE = "doDisableChannel";
    
    // Channel Edit Tasks
    public static final String CHANNEL_EDIT_SAVE = "doSaveChannel";
    public static final String CHANNEL_EDIT_VALIDATE = "doValidate";
    public static final String CHANNEL_EDIT_NEW_DESTINATION = "doNewDestination";
    public static final String CHANNEL_EDIT_DELETE_DESTINATION = "doDeleteDestination";
    public static final String CHANNEL_EDIT_CLONE_DESTINATION = "doCloneDestination";
    public static final String CHANNEL_EDIT_ENABLE_DESTINATION = "doEnableDestination";
    public static final String CHANNEL_EDIT_DISABLE_DESTINATION = "doDisableDestination";
    public static final String CHANNEL_EDIT_MOVE_DESTINATION_UP = "doMoveDestinationUp";
    public static final String CHANNEL_EDIT_MOVE_DESTINATION_DOWN = "doMoveDestinationDown";
    public static final String CHANNEL_EDIT_FILTER = "doEditFilter";
    public static final String CHANNEL_EDIT_TRANSFORMER = "doEditTransformer";
    public static final String CHANNEL_EDIT_RESPONSE_TRANSFORMER = "doEditResponseTransformer";
    public static final String CHANNEL_EDIT_IMPORT_CONNECTOR = "doImportConnector";
    public static final String CHANNEL_EDIT_EXPORT_CONNECTOR = "doExportConnector";
    public static final String CHANNEL_EDIT_EXPORT = "doExport";
    public static final String CHANNEL_EDIT_VALIDATE_SCRIPT = "doValidateChannelScripts";
    
    // Dashboard Tasks
    public static final String DASHBOARD_REFRESH = "doRefreshStatuses";
    public static final String DASHBOARD_FILTER = "doFilter";
    public static final String DASHBOARD_SEND_MESSAGE = "doSendMessage";
    public static final String DASHBOARD_SHOW_MESSAGES = "doShowMessages";
    public static final String DASHBOARD_REMOVE_ALL_MESSAGES = "doRemoveAllMessages";
    public static final String DASHBOARD_CLEAR_STATS = "doClearStats";
    public static final String DASHBOARD_START = "doStart";
    public static final String DASHBOARD_PAUSE = "doPause";
    public static final String DASHBOARD_STOP = "doStop";
    public static final String DASHBOARD_HALT = "doHalt";
    public static final String DASHBOARD_START_CONNECTOR = "doStartConnector";
    public static final String DASHBOARD_STOP_CONNECTOR = "doStopConnector";
    public static final String DASHBOARD_UNDEPLOY = "doUndeployChannel";
    
    // Event Tasks
    public static final String EVENT_REFRESH = "doRefreshEvents";
    public static final String EVENT_REMOVE_ALL = "doRemoveAllEvents";
    public static final String EVENT_EXPORT_ALL = "doExportAllEvents";
    
    // Message Tasks
    public static final String MESSAGE_REFRESH = "doRefreshMessages";
    public static final String MESSAGE_SEND = "doSendMessage";
    public static final String MESSAGE_IMPORT = "doImportMessages";
    public static final String MESSAGE_EXPORT = "doExportMessages";
    public static final String MESSAGE_REMOVE_ALL = "doRemoveAllMessages";
    public static final String MESSAGE_REMOVE_FILTERED = "doRemoveFilteredMessages";
    public static final String MESSAGE_REMOVE = "doRemoveMessage";
    public static final String MESSAGE_REPROCESS_FILTERED = "doReprocessFilteredMessages";
    public static final String MESSAGE_REPROCESS = "doReprocessMessage";
    public static final String MESSAGE_VIEW_IMAGE = "viewImage";
    
    // User Tasks
    public static final String USER_REFRESH = "doRefreshUser";
    public static final String USER_NEW = "doNewUser";
    public static final String USER_EDIT = "doEditUser";
    public static final String USER_DELETE = "doDeleteUser";
    
    // Code Template Tasks
    public static final String CODE_TEMPLATE_REFRESH = "doRefreshCodeTemplates";
    public static final String CODE_TEMPLATE_SAVE = "doSaveCodeTemplates";
    public static final String CODE_TEMPLATE_NEW = "doNewCodeTemplate";
    public static final String CODE_TEMPLATE_IMPORT = "doImportCodeTemplates";
    public static final String CODE_TEMPLATE_EXPORT = "doExportCodeTemplates";
    public static final String CODE_TEMPLATE_DELETE = "doDeleteCodeTemplate";
    public static final String CODE_TEMPLATE_VALIDATE = "doValidateCodeTemplate";
    
    // Global Script Tasks
    public static final String GLOBAL_SCRIPT_SAVE = "doSaveGlobalScripts";
    public static final String GLOBAL_SCRIPT_VALIDATE = "doValidateCurrentGlobalScript";
    public static final String GLOBAL_SCRIPT_IMPORT = "doImportGlobalScripts";
    public static final String GLOBAL_SCRIPT_EXPORT = "doExportGlobalScripts";
    
    // Extensions Tasks
    public static final String EXTENSIONS_REFRESH = "doRefreshExtensions";
    public static final String EXTENSIONS_CHECK_FOR_UPDATES = "doCheckForUpdates";
    public static final String EXTENSIONS_ENABLE = "doEnableExtension";
    public static final String EXTENSIONS_DISABLE = "doDisableExtension";
    public static final String EXTENSIONS_SHOW_PROPERTIES = "doShowExtensionProperties";
    public static final String EXTENSIONS_UNINSTALL = "doUninstallExtension";
    
    // Other Tasks
    public static final String OTHER_HELP = "doHelp";
    public static final String OTHER_ABOUT = "goToAbout";
    public static final String OTHER_VISIT_MIRTH = "goToMirth";
    public static final String OTHER_REPORT_ISSUE = "doReportIssue";
    public static final String OTHER_LOGOUT = "doLogout";
}
