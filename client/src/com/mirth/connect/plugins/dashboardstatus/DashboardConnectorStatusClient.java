/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComponent;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardTabPlugin;

public class DashboardConnectorStatusClient extends DashboardTabPlugin {
    private DashboardConnectorStatusPanel dcsp;
    private static final String NO_CHANNEL_SELECTED = "No Channel Selected";
    private static final int FETCH_SIZE = 999; 
    private ConcurrentHashMap<String, LinkedList<ConnectionLogItem>> connectorInfoLogs;
    private int currentDashboardLogSize;
    private String selectedChannelId;
    private boolean shouldResetLogs;
    private Map<String, Long> lastLogIdByChannelId;

    /** Creates a new instance of DashboardConnectorStatusClient */
    public DashboardConnectorStatusClient(String name) {
        super(name);
        shouldResetLogs = true;
        lastLogIdByChannelId = new ConcurrentHashMap<>();
        connectorInfoLogs = new ConcurrentHashMap<>();
        dcsp = new DashboardConnectorStatusPanel(this);
        currentDashboardLogSize = dcsp.getCurrentDashboardLogSize();
    }

    public void clearLog(String selectedChannelId) {
        if (connectorInfoLogs.containsKey(selectedChannelId)) {
            connectorInfoLogs.remove(selectedChannelId);
        }

        if (selectedChannelId.equals(NO_CHANNEL_SELECTED)) {
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
            LinkedList<ConnectionLogItem> newChannelLog = connectorInfoLogs.get(selectedChannel);
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

    @Override
    public void prepareData() throws ClientException {
        prepareData(null);
    }

    @Override
    public void prepareData(List<DashboardStatus> statuses) throws ClientException {
        // Keep status as null if there are more than one channels selected
        DashboardStatus status = null;
        if ((statuses != null) && (statuses.size() == 1)) {
            status = statuses.get(0);
        }

        if (shouldResetLogs) {
            // clear out all the Dashboard Logs, and reset all the channel states to RESUMED.
            connectorInfoLogs.clear();
            dcsp.resetAllChannelStates();
            shouldResetLogs = false;
        }

        // If there are more than one channels selected, use state: NO_CHANNEL_SELECTED
        if (status == null) {
            // no channel is selected.
            selectedChannelId = NO_CHANNEL_SELECTED;
        } else {
            // channel is selected.
            selectedChannelId = status.getChannelId();
        }
        
        Long lastLogId = lastLogIdByChannelId.get(selectedChannelId);

        //get states from server only if the client's channel log is not in the paused state.
        if (!dcsp.isPaused(selectedChannelId)) {
            LinkedList<ConnectionLogItem> connectionInfoLogsReceived = new LinkedList<>();
            try {
                if (status == null) {
                    connectionInfoLogsReceived = PlatformUI.MIRTH_FRAME.mirthClient.getServlet(DashboardConnectorStatusServletInterface.class).getAllChannelLogs(FETCH_SIZE, lastLogId);
                } else {
                    connectionInfoLogsReceived = PlatformUI.MIRTH_FRAME.mirthClient.getServlet(DashboardConnectorStatusServletInterface.class).getChannelLog(selectedChannelId, FETCH_SIZE, lastLogId);
                }
            } catch (ClientException e) {
                parent.alertThrowable(parent, e, false);
            }

            // grab the channel's log from the HashMap, if not exist, create one.
            LinkedList<ConnectionLogItem> channelLog = getChannelLog();

            synchronized (this) {
                for (int i = connectionInfoLogsReceived.size() - 1; i >= 0; i--) {
                    while (currentDashboardLogSize <= channelLog.size()) {
                        channelLog.removeLast();
                    }
                    channelLog.addFirst(connectionInfoLogsReceived.get(i));
                }
                
                lastLogIdByChannelId.put(selectedChannelId, channelLog.getFirst().getLogId());
            }
            connectorInfoLogs.put(selectedChannelId, channelLog);
        }
    }

    // used for setting actions to be called for updating when there is no status selected
    @Override
    public void update() {
        // call the other function with no channel selected (null).
        update(null);
    }

    // used for setting actions to be called for updating when there is a status selected
    @Override
    public void update(List<DashboardStatus> statuses) {
        // If there are more than one channels selected, create an array of those names
        Map<String, List<Integer>> selectedConnectorMap = null;

        if (statuses != null) {
            selectedConnectorMap = new ConcurrentHashMap<String, List<Integer>>();

            for (DashboardStatus status : statuses) {
                String channelId = status.getChannelId();
                Integer metaDataId = status.getMetaDataId();

                List<Integer> selectedConnectors = selectedConnectorMap.get(channelId);

                if (selectedConnectors == null) {
                    selectedConnectors = new ArrayList<Integer>();
                    selectedConnectorMap.put(channelId, selectedConnectors);
                }

                selectedConnectors.add(metaDataId);
            }
        }

        dcsp.setSelectedChannelId(selectedChannelId);
        dcsp.setSelectedConnectors(selectedConnectorMap);
        dcsp.updateTable(getChannelLog());
        dcsp.adjustPauseResumeButton(selectedChannelId);
    }

    @Override
    public JComponent getTabComponent() {
        return dcsp;
    }

    // used for starting processes in the plugin when the program is started
    @Override
    public void start() {}

    // used for stopping processes in the plugin when the program is exited
    @Override
    public void stop() {
        reset();
    }

    // Called when establishing a new session for the user
    @Override
    public void reset() {
        clearLog(NO_CHANNEL_SELECTED);
        
        shouldResetLogs = true;
    }

    @Override
    public String getPluginPointName() {
        return "Connection Log";
    }

    private LinkedList<ConnectionLogItem> getChannelLog() {
        if (connectorInfoLogs.containsKey(selectedChannelId)) {
            return connectorInfoLogs.get(selectedChannelId);
        } else {
            return new LinkedList<>();
        }
    }
}
