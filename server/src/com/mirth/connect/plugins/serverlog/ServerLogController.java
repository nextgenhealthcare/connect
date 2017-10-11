/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import java.util.List;

import com.mirth.connect.server.ExtensionLoader;

public abstract class ServerLogController {

    private static ServerLogController instance = null;

    public static ServerLogController getInstance() {
        synchronized (DefaultServerLogController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(ServerLogController.class);

                if (instance == null) {
                    instance = new DefaultServerLogController();
                }
            }

            return instance;
        }
    }

    public abstract void addLogItem(ServerLogItem logItem);

    public abstract List<ServerLogItem> getServerLogs(int fetchSize, Long lastLogId);
}
