/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

public class DefaultServerLogController extends ServerLogController {

    private static int LOG_SIZE = 100; // maximum number of log entries. not the number of lines.

    private static LinkedList<ServerLogItem> serverLogs = new LinkedList<ServerLogItem>();

    protected DefaultServerLogController() {}

    @Override
    public synchronized void addLogItem(ServerLogItem logItem) {
        if (serverLogs.size() == LOG_SIZE) {
            serverLogs.removeLast();
        }

        serverLogs.addFirst(logItem);
    }

    @Override
    public List<ServerLogItem> getServerLogs(int fetchSize, Long lastLogId) {
        // work with deep copied clone of the static server logs object in
        // order to avoid multiple threads ConcurrentModificationException.
        LinkedList<ServerLogItem> serverLogsCloned = new LinkedList<ServerLogItem>();

        try {
            serverLogsCloned = (LinkedList<ServerLogItem>) SerializationUtils.clone(serverLogs);
        } catch (SerializationException e) {
            // ignore
        }

        List<ServerLogItem> newServerLogEntries = new ArrayList<ServerLogItem>();

        for (ServerLogItem logItem : serverLogsCloned) {
            if (lastLogId == null || lastLogId < logItem.getId()) {
                newServerLogEntries.add(logItem);
                if (newServerLogEntries.size() >= fetchSize) {
                    break;
                }
            }
        }

        return newServerLogEntries;
    }
}
