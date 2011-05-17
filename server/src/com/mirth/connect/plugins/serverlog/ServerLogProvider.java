/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.ServicePlugin;

public class ServerLogProvider implements ServicePlugin {
    public static final String PLUGINPOINT = "Server Log";
    private Logger logger = Logger.getLogger(this.getClass());
    private static final String GET_SERVER_LOGS = "getMirthServerLogs";
    private static final String REMOVE_SESSIONID = "removeSessionId";
    private static LinkedList<String[]> serverLogs = new LinkedList<String[]>();
    private static int LOG_SIZE = 100; // maximum number of log entries. not the
                                       // number of lines.
    private static long logId = 1;
    private ConcurrentHashMap<String, Long> lastDisplayedServerLogIdBySessionId;

    @Override
    public String getPluginPointName() {
        return PLUGINPOINT;
    }

    private void initialize() {
        // add the new appender
        Appender arrayAppender = new ArrayAppender();
        Layout patternLayout = new PatternLayout("[%d]  %-5p (%c:%L): %m%n");
        arrayAppender.setLayout(patternLayout);
        patternLayout.activateOptions();
        Logger.getRootLogger().addAppender(arrayAppender);
        lastDisplayedServerLogIdBySessionId = new ConcurrentHashMap<String, Long>();
    }

    public void init(Properties properties) {
        initialize();
    }

    public synchronized void newServerLogReceived(String logMessage) {
        if (serverLogs.size() == LOG_SIZE) {
            serverLogs.removeLast();
        }
        
        serverLogs.addFirst(new String[] { String.valueOf(logId), logMessage });
        logId++;
    }

    public synchronized Object invoke(String method, Object object, String sessionId) {
        if (method.equals(GET_SERVER_LOGS)) {

            // work with deep copied clone of the static server logs object in
            // order to avoid multiple threads ConcurrentModificationException.
            LinkedList<String[]> serverLogsCloned = new LinkedList<String[]>();

            try {
                serverLogsCloned = (LinkedList<String[]>) SerializationUtils.clone(serverLogs);
            } catch (SerializationException e) {
                // ignore
            }
            
            if (lastDisplayedServerLogIdBySessionId.containsKey(sessionId)) {
                // client exist with the sessionId.
                // -> only display new log entries.
                long lastDisplayedServerLogId = lastDisplayedServerLogIdBySessionId.get(sessionId);

                LinkedList<String[]> newServerLogEntries = new LinkedList<String[]>();
                // FYI, channelLog.size() will never be larger than LOG_SIZE =
                // 100.
                for (String[] aServerLog : serverLogsCloned) {
                    if (lastDisplayedServerLogId < Long.parseLong(aServerLog[0])) {
                        newServerLogEntries.addLast(aServerLog);
                    }
                }

                if (newServerLogEntries.size() > 0) {
                    // put the lastDisplayedLogId into the HashMap. index 0 is
                    // the most recent entry, and index0 of that entry contains
                    // the logId.
                    lastDisplayedServerLogIdBySessionId.put(sessionId, Long.parseLong(newServerLogEntries.get(0)[0]));
                }

                try {
                    return SerializationUtils.clone(newServerLogEntries);
                } catch (SerializationException e) {
                    logger.error(e);
                }
            } else {
                // brand new client. i.e. brand new session id, and all log
                // entries are new.
                // -> display all log entries.
                if (serverLogsCloned.size() > 0) {
                    lastDisplayedServerLogIdBySessionId.put(sessionId, Long.parseLong(serverLogsCloned.get(0)[0]));
                } else {
                    // no log exist at all. put the currentLogId-1, which is the
                    // very latest logId.
                    lastDisplayedServerLogIdBySessionId.put(sessionId, logId - 1);
                }
                
                try {
                    return SerializationUtils.clone(serverLogsCloned);
                } catch (SerializationException e) {
                    logger.error(e);
                }
            }

        } else if (method.equals(REMOVE_SESSIONID)) {
            // client shut down, or user logged out -> remove everything
            // involving this sessionId.
            if (lastDisplayedServerLogIdBySessionId.containsKey(sessionId)) {
                // client exist with the sessionId. remove it.
                lastDisplayedServerLogIdBySessionId.remove(sessionId);
                return true; // sessionId found and successfully removed.
            }
            
            return false; // no sessionId found.
        }
        
        return null;
    }

    public void start() {

    }

    public void update(Properties properties) {

    }

    public void onDeploy() {

    }

    public void stop() {

    }

    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGINPOINT, "View Server Log", "Displays the contents of the Server Log on the Dashboard.", new String[] { GET_SERVER_LOGS }, new String[] { });
        return new ExtensionPermission[] { viewPermission };
    }
}
