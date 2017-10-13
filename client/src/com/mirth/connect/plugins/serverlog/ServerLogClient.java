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

import javax.swing.JComponent;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.ForbiddenException;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardTabPlugin;
import com.mirth.connect.plugins.DashboardTablePlugin;

public class ServerLogClient extends DashboardTabPlugin {
    private ServerLogPanel serverLogPanel;
    private LinkedList<ServerLogItem> serverLogs;
    private static final ServerLogItem unauthorizedLog = new ServerLogItem("You are not authorized to view the server log.");
    private int currentServerLogSize;
    private boolean receivedNewLogs;
    private Long lastLogId;
    private String currentServerId;

    public ServerLogClient(String name) {
        super(name);
        serverLogs = new LinkedList<ServerLogItem>();
        serverLogPanel = new ServerLogPanel(this);
        currentServerLogSize = serverLogPanel.getCurrentServerLogSize();
    }

    public void clearLog() {
        serverLogs.clear();
        serverLogPanel.updateTable(null);
    }

    public void resetServerLogSize(int newServerLogSize) {

        // the log size is always set to 100 on the server.
        // on the client side, the max size is 99.  whenever that changes, only update the client side logs. the logs on the server will always be intact.
        // Q. Does this log size affect all the channels? - Yes, it should.

        // update (refresh) log only if the new logsize got smaller.
        if (newServerLogSize < currentServerLogSize) {
            // if log size got reduced...  remove that much extra LastRows.
            synchronized (this) {
                while (newServerLogSize < serverLogs.size()) {
                    serverLogs.removeLast();
                }
            }
            serverLogPanel.updateTable(serverLogs);
        }

        // reset currentServerLogSize.
        currentServerLogSize = newServerLogSize;
    }

    @Override
    public void prepareData() throws ClientException {
        receivedNewLogs = false;

        if (!serverLogPanel.isPaused()) {
            List<ServerLogItem> serverLogReceived = new ArrayList<ServerLogItem>();
            //get logs from server
            try {
                serverLogReceived = PlatformUI.MIRTH_FRAME.mirthClient.getServlet(ServerLogServletInterface.class).getServerLogs(currentServerLogSize, lastLogId);
            } catch (ClientException e) {
                if (e instanceof ForbiddenException) {
                    LinkedList<ServerLogItem> unauthorizedLogs = new LinkedList<ServerLogItem>();
                    // Add the unauthorized log message if it's not already there.
                    if (serverLogs.isEmpty() || !serverLogs.getLast().equals(unauthorizedLog)) {
                        unauthorizedLogs.add(unauthorizedLog);
                    }
                    serverLogReceived = unauthorizedLogs;
                    parent.alertThrowable(parent, e, false);
                } else {
                    throw e;
                }
            }

            if (serverLogReceived.size() > 0) {
                receivedNewLogs = true;

                ServerLogItem latestItem = serverLogReceived.get(0);
                if (latestItem.getId() != null && latestItem.getId() > 0) {
                    lastLogId = latestItem.getId();
                }

                synchronized (this) {
                    for (int i = serverLogReceived.size() - 1; i >= 0; i--) {
                        while (currentServerLogSize <= serverLogs.size()) {
                            serverLogs.removeLast();
                        }
                        serverLogs.addFirst(serverLogReceived.get(i));
                    }
                }
            }
        }
    }

    @Override
    public void prepareData(List<DashboardStatus> statuses) throws ClientException {
        prepareData();
    }

    // used for setting actions to be called for updating when there is no status selected
    @Override
    public void update() {
        boolean serverIdChanged = false;
        String serverId = null;
        for (DashboardTablePlugin plugin : LoadedExtensions.getInstance().getDashboardTablePlugins().values()) {
            serverId = plugin.getServerId();
            if (serverId != null) {
                break;
            }
        }
        if (currentServerId != serverId) {
            currentServerId = serverId;
            serverIdChanged = true;
        }

        if (!serverLogPanel.isPaused() && (receivedNewLogs || serverIdChanged)) {
            // for mirth.log, channel being selected does not matter. display either way.
            serverLogPanel.updateTable(serverLogs);
        }
    }

    // used for setting actions to be called for updating when there is a status selected
    public void update(List<DashboardStatus> statuses) {

        // Mirth Server Log is irrelevant with Channel Status.  so just call the default update() method.
        update();

    }

    @Override
    public JComponent getTabComponent() {
        return serverLogPanel;
    }

    // used for starting processes in the plugin when the program is started
    @Override
    public void start() {}

    // used for stopping processes in the plugin when the program is exited
    @Override
    public void stop() {
        reset();
    }

    // Called when establishing a new session for the user.
    @Override
    public void reset() {
        clearLog();
    }

    @Override
    public String getPluginPointName() {
        return ServerLogServletInterface.PLUGIN_POINT;
    }
}
