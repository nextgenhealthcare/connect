/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import static com.mirth.connect.plugins.serverlog.ServerLogServletInterface.PERMISSION_VIEW;
import static com.mirth.connect.plugins.serverlog.ServerLogServletInterface.PLUGIN_POINT;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.mirth.connect.client.core.api.util.OperationUtil;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ServerLogProvider implements ServicePlugin {

    private static long logId = 1;

    private String serverId;
    private ServerLogController logController;

    @Override
    public String getPluginPointName() {
        return PLUGIN_POINT;
    }

    private void initialize() {
        // add the new appender
        Appender arrayAppender = new ArrayAppender(this);
        Layout patternLayout = new PatternLayout("[%d]  %-5p (%c:%L): %m%n");
        arrayAppender.setLayout(patternLayout);
        patternLayout.activateOptions();
        Logger.getRootLogger().addAppender(arrayAppender);
        serverId = ControllerFactory.getFactory().createConfigurationController().getServerId();
        logController = ServerLogController.getInstance();
    }

    public void init(Properties properties) {
        initialize();
    }

    public synchronized void newServerLogReceived(String level, Date date, String threadName, String category, String lineNumber, String message, String throwableInformation) {
        logController.addLogItem(new ServerLogItem(serverId, logId, level, date, threadName, category, lineNumber, message, throwableInformation));
        logId++;
    }

    public List<ServerLogItem> getServerLogs(int fetchSize, Long lastLogId) {
        return logController.getServerLogs(fetchSize, lastLogId);
    }

    @Override
    public void start() {}

    @Override
    public void update(Properties properties) {}

    @Override
    public void stop() {}

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGIN_POINT, PERMISSION_VIEW, "Displays the contents of the Server Log on the Dashboard.", OperationUtil.getOperationNamesForPermission(PERMISSION_VIEW, ServerLogServletInterface.class), new String[] {});
        return new ExtensionPermission[] { viewPermission };
    }
}
