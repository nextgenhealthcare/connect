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
import java.util.List;

import javax.swing.JComponent;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.UnauthorizedException;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.plugins.DashboardPanelPlugin;

public class ServerLogClient extends DashboardPanelPlugin {

    private ServerLogPanel serverLogPanel;
    private LinkedList<String[]> serverLogs;
    private static final String[] unauthorizedLog = new String[] { "0", "You are not authorized to view the server log." };
    private static final String GET_SERVER_LOGS = "getMirthServerLogs";
    private static final String REMOVE_SESSIONID = "removeSessionId";
    private static final String SERVER_PLUGIN_NAME = "Server Log";
    private int currentServerLogSize;

    public ServerLogClient(String name) {
        super(name);
        serverLogs = new LinkedList<String[]>();
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

    // used for setting actions to be called for updating when there is no status selected
    public void update() {
        if (!serverLogPanel.isPaused()) {
            LinkedList<String[]> serverLogReceived = new LinkedList<String[]>();
            //get logs from server
            try {
                serverLogReceived = (LinkedList<String[]>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, GET_SERVER_LOGS, null);
            } catch (ClientException e) {
                if (e.getCause() instanceof UnauthorizedException) {
                    LinkedList<String[]> unauthorizedLogs = new LinkedList<String[]>();
                    // Add the unauthorized log message if it's not already there.
                    if (serverLogs.isEmpty() || !serverLogs.getLast().equals(unauthorizedLog)) {
                        unauthorizedLogs.add(unauthorizedLog);
                    }
                    serverLogReceived = unauthorizedLogs;
                } else {
                    parent.alertException(parent, e.getStackTrace(), e.getMessage());
                }
            }

            if (serverLogReceived.size() > 0) {
                synchronized (this) {
                    for (int i = serverLogReceived.size() - 1; i >= 0; i--) {
                        while (currentServerLogSize <= serverLogs.size()) {
                            serverLogs.removeLast();
                        }
                        serverLogs.addFirst(serverLogReceived.get(i));
                    }
                }

                // for mirth.log, channel being selected does not matter. display either way.
                serverLogPanel.updateTable(serverLogs);
            }
        }
    }

    // used for setting actions to be called for updating when there is a status selected
    public void update(List<ChannelStatus> statuses) {

        // Mirth Server Log is irrelevant with Channel Status.  so just call the default update() method.
        update();

    }
    
    @Override
    public JComponent getComponent() {
        return serverLogPanel;
    }

    // used for starting processes in the plugin when the program is started
    @Override
    public void start() {
    }
    
    // used for stopping processes in the plugin when the program is exited
    @Override
    public void stop() {
        reset();
    }

    // Called when establishing a new session for the user.
    @Override
    public void reset() {
        clearLog();
        
        // invoke method to remove everything involving this client's sessionId.
        try {
            // FYI, method below returns a boolean value.
            // returned 'true' - sessionId found and removed.
            // returned 'false' - sessionId not found. - should never be this case.
            // either way, the sessionId is gone.
            PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, REMOVE_SESSIONID, null);
        } catch (ClientException e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
        }
    }

    @Override
    public String getPluginPointName() {
        return "Server Log";
    }
}
