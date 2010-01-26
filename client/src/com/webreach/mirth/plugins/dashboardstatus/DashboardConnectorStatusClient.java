/*
 * DashboardConnectorStatusClient.java
 *
 * Created on October 10, 2007, 3:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.plugins.dashboardstatus;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.plugins.DashboardPanelPlugin;

public class DashboardConnectorStatusClient extends DashboardPanelPlugin {

    private DashboardConnectorStatusPanel dcsp;
    private static final String REMOVE_SESSIONID = "removeSessionId";
    private static final String GET_CONNECTION_INFO_LOGS = "getConnectionInfoLogs";
    private static final String CHANNELS_DEPLOYED = "channelsDeployed";
    private static final String SERVER_PLUGIN_NAME = "Dashboard Status Column Server";
    private static final String NO_CHANNEL_SELECTED = "No Channel Selected";
    private ConcurrentHashMap<String, LinkedList<String[]>> connectorInfoLogs;
    private int currentDashboardLogSize;

    
    /** Creates a new instance of DashboardConnectorStatusClient */
    public DashboardConnectorStatusClient(String name)
    {
        super(name);
        connectorInfoLogs = new ConcurrentHashMap<String, LinkedList<String[]>>();
        dcsp = new DashboardConnectorStatusPanel(this);
        currentDashboardLogSize = dcsp.getCurrentDashboardLogSize();
        setComponent(dcsp);
    }

    public void clearLog(String selectedChannel) {
        if (connectorInfoLogs.containsKey(selectedChannel)) {
            connectorInfoLogs.remove(selectedChannel);
        }

        if (selectedChannel.equals(NO_CHANNEL_SELECTED)) {
            // Add Channel Name column in the UI so that which logs correspond to which channel.
            dcsp.updateTable(null, false);
        } else {
            // No Channel Name column needed.
            dcsp.updateTable(null, true);
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
            synchronized(this) {
                while (newDashboardLogSize < newChannelLog.size()) {
                    newChannelLog.removeLast();
                }
            }
            if (selectedChannel.equals(NO_CHANNEL_SELECTED)) {
                // Add Channel Name column in the UI so that which logs correspond to which channel.
                dcsp.updateTable(newChannelLog, false);
            } else {
                // No Channel Name column needed.
                dcsp.updateTable(newChannelLog, true);
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
    public void update(List<ChannelStatus> statuses) {

        // TODO: Temporary hack until the dashboard connector status client can monitor multiple statuses
        ChannelStatus status = null;
        if ((statuses != null) && (statuses.size() > 0)) {
            status = statuses.get(0);
        }
        
        boolean channelsDeployed = false;
        try {
            channelsDeployed = (Boolean) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, CHANNELS_DEPLOYED, null);
        } catch (ClientException e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());            
        }

        if (channelsDeployed) {
            // clear out all the Dashboard Logs, and reset all the channel states to RESUMED.
            connectorInfoLogs.clear();
            dcsp.resetAllChannelStates();
        }

        String selectedChannel;
        if (status == null) {
            // no channel is selected.
            selectedChannel = NO_CHANNEL_SELECTED;
        } else {
            // channel is selected.
            selectedChannel = status.getName();
        }

        // set selectedChannel.
        dcsp.setSelectedChannel(selectedChannel);

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
                    connectionInfoLogsReceived = (LinkedList<String[]>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, GET_CONNECTION_INFO_LOGS, null);
                } else {
                    connectionInfoLogsReceived = (LinkedList<String[]>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, GET_CONNECTION_INFO_LOGS, selectedChannel);
                }
            } catch (ClientException e) {
                parent.alertException(parent, e.getStackTrace(), e.getMessage());
            }

            synchronized(this) {
                for (int i = connectionInfoLogsReceived.size()-1; i >= 0; i--) {
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
            dcsp.updateTable(channelLog, false);
        } else {
            // No Channel Name column needed.
            dcsp.updateTable(channelLog, true);
        }
        dcsp.adjustPauseResumeButton(selectedChannel);

    }

    // used for starting processes in the plugin when the program is started
    public void start() {

    }
    
    // used for stopping processes in the plugin when the program is exited
    public void stop() {
        // invoke method to remove everything involving this client's sessionId.
        try {
            PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, REMOVE_SESSIONID, null);
        } catch (ClientException e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
        }
    }

}
