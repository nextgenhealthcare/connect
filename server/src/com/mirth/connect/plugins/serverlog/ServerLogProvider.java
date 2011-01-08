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

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.converters.ObjectCloner;
import com.mirth.connect.model.converters.ObjectClonerException;
import com.mirth.connect.plugins.ServerPlugin;

public class ServerLogProvider implements ServerPlugin {
    private Logger logger = Logger.getLogger(this.getClass());

    private static final String PLUGIN_NAME = "Server Log";

    private static final String GET_SERVER_LOGS = "getMirthServerLogs";
    private static final String REMOVE_SESSIONID = "removeSessionId";
    private static LinkedList<String[]> serverLogs = new LinkedList<String[]>();
    private static int LOG_SIZE = 100; // maximum number of log entries. not the
                                       // number of lines.
    private static long logId = 1;
    private ConcurrentHashMap<String, Long> lastDisplayedServerLogIdBySessionId;

    private void initialize() {
        // add the new appender
        Appender arrayAppender = new ArrayAppender();
        Layout patternLayout = new PatternLayout("[%d]  %-5p (%c:%L): %m%n");
        arrayAppender.setLayout(patternLayout);
        patternLayout.activateOptions();
        Logger.getRootLogger().addAppender(arrayAppender);

        this.lastDisplayedServerLogIdBySessionId = new ConcurrentHashMap<String, Long>();
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
                serverLogsCloned = (LinkedList<String[]>) ObjectCloner.deepCopy(serverLogs);
            } catch (ObjectClonerException oce) {
                // ignore potential OptionalDataException.
                // even if the Exception may be thrown, the logs will properly
                // be retrieved to the Dashboard next time it refreshes (by
                // default set to every second).
                // this error is purely relevant to the display only. i.e. the
                // only time/chance it may occur is when the Mirth Administrator
                // is open.
                // this error won't affect the server and any other running
                // process.
                // It is also pointless anyway since it'll be impossible to keep
                // track or view a single specific error, if there are that many
                // new errors constantly being displayed on the Dashboard.
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
                    return ObjectCloner.deepCopy(newServerLogEntries);
                } catch (ObjectClonerException oce) {
                    logger.error("Error: ServerLogProvider.java", oce);
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
                    return ObjectCloner.deepCopy(serverLogsCloned);
                } catch (ObjectClonerException oce) {
                    logger.error("Error: ServerLogProvider.java", oce);
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
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGIN_NAME, "View Server Log", "Displays the contents of the Server Log on the Dashboard.", new String[] { GET_SERVER_LOGS }, new String[] { });
        
        return new ExtensionPermission[] { viewPermission };
    }
}
