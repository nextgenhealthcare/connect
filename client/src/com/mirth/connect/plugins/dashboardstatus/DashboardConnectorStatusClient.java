/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComponent;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.UnauthorizedException;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardPanelPlugin;

public class DashboardConnectorStatusClient extends DashboardPanelPlugin {
    private static final String DASHBOARD_SERVICE_PLUGINPOINT = "Dashboard Connector Service";
    private DashboardConnectorStatusPanel dcsp;
    private static final String REMOVE_SESSIONID = "removeSessionId";
    private static final String GET_CONNECTION_INFO_LOGS = "getConnectionInfoLogs";
    private static final String CHANNELS_DEPLOYED = "channelsDeployed";
    private static final String NO_CHANNEL_SELECTED = "No Channel Selected";
    private ConcurrentHashMap<String, LinkedList<String[]>> connectorInfoLogs;
    private int currentDashboardLogSize;

    /** Creates a new instance of DashboardConnectorStatusClient */
    public DashboardConnectorStatusClient(String name) {
        super(name);
        connectorInfoLogs = new ConcurrentHashMap<String, LinkedList<String[]>>();
        dcsp = new DashboardConnectorStatusPanel(this);
        currentDashboardLogSize = dcsp.getCurrentDashboardLogSize();
    }

    public void clearLog(String selectedChannel) {
        if (connectorInfoLogs.containsKey(selectedChannel)) {
            connectorInfoLogs.remove(selectedChannel);
        }

        if (selectedChannel.equals(NO_CHANNEL_SELECTED)) {
            // Add Channel Name column in the UI so that which logs correspond to which channel.
            dcsp.updateTable(null);
        } else {
            // No Channel Name column needed.
            dcsp.updateTable(null);
        }
    }

    public void resetLogSize(int newDashboardLogSize, String selectedChannel) {

        // the log size is always set to 1000 on the server.
        // on the client side, the max size is 999.  whenever that changes, only update the client side logs. the logs on the server will always be intact.
        // Q. Does this log size affect all the channels? - Yes, it should.

        // update (refresh) log only if the new logsize got smaller.
        if (newDashboardLogSize < currentDashboardLogSize) {
            // get the currentChannelLog
            LinkedList<String[]> newChannelLog = connectorInfoLogs.get(selectedChannel);
            // if log size got reduced...  remove that much extra LastRows.
            synchronized (this) {
                while (newDashboardLogSize < newChannelLog.size()) {
                    newChannelLog.removeLast();
                }
            }
            if (selectedChannel.equals(NO_CHANNEL_SELECTED)) {
                // Add Channel Name column in the UI so that which logs correspond to which channel.
                dcsp.updateTable(newChannelLog);
            } else {
                // No Channel Name column needed.
                dcsp.updateTable(newChannelLog);
            }
        }

        // reset currentLogSize.
        currentDashboardLogSize = newDashboardLogSize;
    }

    // used for setting actions to be called for updating when there is no status selected
    public void update() {

        // call the other function with no channel selected (null).
        update(null);

    }

    // used for setting actions to be called for updating when there is a status selected    
    public void update(List<DashboardStatus> statuses) {
        // Keep status as null if there are more than one channels selected
        DashboardStatus status = null;
        if ((statuses != null) && (statuses.size() == 1)) {
            status = statuses.get(0);
        }

        boolean channelsDeployed = false;
        try {
            channelsDeployed = (Boolean) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(DASHBOARD_SERVICE_PLUGINPOINT, CHANNELS_DEPLOYED, null);
        } catch (ClientException e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
        }

        if (channelsDeployed) {
            // clear out all the Dashboard Logs, and reset all the channel states to RESUMED.
            connectorInfoLogs.clear();
            dcsp.resetAllChannelStates();
        }

        // If there are more than one channels selected, use state: NO_CHANNEL_SELECTED
        String selectedChannel;
        if (status == null) {
            // no channel is selected.
            selectedChannel = NO_CHANNEL_SELECTED;
        } else {
            // channel is selected.
            selectedChannel = status.getName();
        }

        // If there are more than one channels selected, create an array of those names
        List<String> selectedChannels = null;
        if (statuses != null && statuses.size() > 1) {
            selectedChannels = new ArrayList<String>();
            for (int i = 0; i < statuses.size(); i++) {
                selectedChannels.add(statuses.get(i).getName());
            }
        }

        // set selectedChannel.  selectedChannels will be null unless multiple rows are selected
        dcsp.setSelectedChannel(selectedChannel);
        dcsp.setSelectedChannels(selectedChannels);

        // store this log on the client side for later use.
        // grab the channel's log from the HashMap, if not exist, create one.
        LinkedList<String[]> channelLog;
        if (connectorInfoLogs.containsKey(selectedChannel)) {
            channelLog = connectorInfoLogs.get(selectedChannel);
        } else {
            channelLog = new LinkedList<String[]>();
        }

        //get states from server only if the client's channel log is not in the paused state.
        if (!dcsp.isPaused(selectedChannel)) {
            LinkedList<String[]> connectionInfoLogsReceived = new LinkedList<String[]>();
            try {
                if (status == null) {
                    connectionInfoLogsReceived = (LinkedList<String[]>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(DASHBOARD_SERVICE_PLUGINPOINT, GET_CONNECTION_INFO_LOGS, null);
                } else {
                    connectionInfoLogsReceived = (LinkedList<String[]>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(DASHBOARD_SERVICE_PLUGINPOINT, GET_CONNECTION_INFO_LOGS, selectedChannel);
                }
            } catch (ClientException e) {
                if (e.getCause() instanceof UnauthorizedException) {
                    // Don't error. Let an empty list be processed
                } else {
                    parent.alertException(parent, e.getStackTrace(), e.getMessage());
                }
            }

            synchronized (this) {
                for (int i = connectionInfoLogsReceived.size() - 1; i >= 0; i--) {
                    while (currentDashboardLogSize <= channelLog.size()) {
                        channelLog.removeLast();
                    }
                    channelLog.addFirst(connectionInfoLogsReceived.get(i));
                }
            }
            connectorInfoLogs.put(selectedChannel, channelLog);
        }

        // call updateLogTextArea.
        if (selectedChannel.equals(NO_CHANNEL_SELECTED)) {
            // Add Channel Name column in the UI so that which logs correspond to which channel.
            dcsp.updateTable(channelLog);
        } else {
            // No Channel Name column needed.
            dcsp.updateTable(channelLog);
        }
        dcsp.adjustPauseResumeButton(selectedChannel);

    }
    
    @Override
    public JComponent getComponent() {
        return dcsp;
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
    
    // Called when establishing a new session for the user
    @Override
    public void reset() {
        clearLog(NO_CHANNEL_SELECTED);
        
        // invoke method to remove everything involving this client's sessionId.
        try {
            PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(DASHBOARD_SERVICE_PLUGINPOINT, REMOVE_SESSIONID, null);
        } catch (ClientException e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
        }
    }

    @Override
    public String getPluginPointName() {
        return "Connection Log";
    }
}
