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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComponent;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardTabPlugin;
import com.mirth.connect.plugins.DashboardTablePlugin;

public class DashboardConnectorStatusClient extends DashboardTabPlugin {
    private DashboardConnectorStatusPanel dcsp;
    private static final String NO_SERVER_SELECTED = "No Server Selected";
    private static final String NO_CHANNEL_SELECTED = "No Channel Selected";
    private ConcurrentHashMap<String, Map<String, LinkedList<ConnectionLogItem>>> connectorInfoLogs;
    private int currentDashboardLogSize;
    private List<DashboardStatus> selectedStatuses;
    private boolean shouldResetLogs;
    private Map<String, Map<String, Long>> lastLogIdByServerId;
    
    private Map<String, Long> lastClusterLogIdWhenCleared;	// Maps channelId -> lastLogId

    /** Creates a new instance of DashboardConnectorStatusClient */
    public DashboardConnectorStatusClient(String name) {
        super(name);
        shouldResetLogs = true;
        lastLogIdByServerId = new ConcurrentHashMap<>();
        connectorInfoLogs = new ConcurrentHashMap<>();
        lastClusterLogIdWhenCleared = new ConcurrentHashMap<>();
        dcsp = new DashboardConnectorStatusPanel(this);
        currentDashboardLogSize = dcsp.getCurrentDashboardLogSize();
    }

    public void clearLog(String selectedChannelId) {
    	String serverId = getSelectedServerId();
    	
    	if (serverId == null) {
    		for (String srvId : lastLogIdByServerId.keySet()) {
    			clearLog(srvId, selectedChannelId);
    		}
    	} else {
    		clearLog(serverId, selectedChannelId);
    	}
    	
    	dcsp.updateTable(null);
    }
    
	private void clearLog(String serverId, String selectedChannelId) {

		if (connectorInfoLogs.containsKey(serverId)) {
			if (selectedChannelId.equals(NO_CHANNEL_SELECTED)
					&& (selectedStatuses != null && selectedStatuses.size() > 0)) {
				// One or more statuses are selected, so delete the logs for the
				// corresponding channels.
				for (DashboardStatus status : selectedStatuses) {
					clearLogAcrossServers(serverId, status.getChannelId());
				}
			} else {
				clearLogAcrossServers(serverId, selectedChannelId);
			}
		}
	}
	
	private void clearLogAcrossServers(String serverId, String channelId) {
		Map<String, LinkedList<ConnectionLogItem>> serverLog = connectorInfoLogs.get(serverId);
		
		Map<String, Long> lastLogIdByChannelId = lastLogIdByServerId.get(serverId);
		if (lastLogIdByChannelId == null) {
			lastLogIdByChannelId = new HashMap<>();
			lastLogIdByServerId.put(serverId, lastLogIdByChannelId);
		}
		Long lastLogId = lastLogIdByChannelId.get(channelId);
		
		Map<String, Long> clusterLastLogIdByChannelId = lastLogIdByServerId.get(NO_SERVER_SELECTED);
		if (clusterLastLogIdByChannelId == null) {
			clusterLastLogIdByChannelId = new HashMap<>();
			lastLogIdByServerId.put(NO_SERVER_SELECTED, clusterLastLogIdByChannelId);
		}
		Long clusterLastLogId = clusterLastLogIdByChannelId.get(channelId);
		
		LinkedList<ConnectionLogItem> logItems = null;
		if (serverLog.containsKey(channelId)) {
			logItems = serverLog.remove(channelId);
		}
		
		if (serverId.equals(NO_SERVER_SELECTED)) {
			// Update lastClusterLogIdWhenCleared. Its values are used when getting channel logs from the servlet and
			// ensures that servers will not re-fetch the same logs that we already cleared in the Cluster.
			if (clusterLastLogId != null) {
				lastClusterLogIdWhenCleared.put(channelId, clusterLastLogId);
			}
		} else {
			// Delete all of the logs, that were just removed, from the Cluster's logs
			Map<String, LinkedList<ConnectionLogItem>> clusterLog = connectorInfoLogs.get(NO_SERVER_SELECTED);
			if (clusterLog == null) {
				clusterLog = new HashMap<>();
				connectorInfoLogs.put(NO_SERVER_SELECTED, clusterLog);
			}
			
			LinkedList<ConnectionLogItem> clusterLogItems = clusterLog.get(channelId);
			
			if (clusterLogItems != null && logItems != null) {
				Set<Long> logIdsToRemove = new HashSet<>();
				for (ConnectionLogItem item : logItems) {
					logIdsToRemove.add(item.getLogId());
				}
				
				LinkedList<ConnectionLogItem> prunedClusterLogItems = new LinkedList<>();
				for (ConnectionLogItem item : clusterLogItems) {
					if (!logIdsToRemove.contains(item.getLogId())) {
						prunedClusterLogItems.add(item);
					}
				}
				
				clusterLog.put(channelId, prunedClusterLogItems);
			}

			if (lastLogId != null && (clusterLastLogId == null || lastLogId > clusterLastLogId)) {
				clusterLastLogIdByChannelId.put(channelId, lastLogId);
			}
		}
	}
    
    public void resetLogSize(int newDashboardLogSize, String selectedChannel) {

        // the log size is always set to 1000 on the server.
        // on the client side, the max size is 999.  whenever that changes, only update the client side logs. the logs on the server will always be intact.
        // Q. Does this log size affect all the channels? - Yes, it should.

        // update (refresh) log only if the new logsize got smaller.
        if (newDashboardLogSize < currentDashboardLogSize) {
            // get the currentChannelLog
            LinkedList<ConnectionLogItem> newChannelLog = getChannelLog(selectedStatuses);
            // if log size got reduced...  remove that much extra LastRows.
            synchronized (this) {
                while (newDashboardLogSize < newChannelLog.size()) {
                    newChannelLog.removeLast();
                }
            }
            
            dcsp.updateTable(newChannelLog);
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

        if (shouldResetLogs) {
            // clear out all the Dashboard Logs, and reset all the channel states to RESUMED.
            connectorInfoLogs.clear();
            dcsp.resetAllChannelStates();
            shouldResetLogs = false;
        }
        
        selectedStatuses = statuses;
        
        String serverId = getSelectedServerId();
        if (serverId == null) {
        	serverId = NO_SERVER_SELECTED;
        }
        
        if (statuses != null && statuses.size() > 0) {
        	for (DashboardStatus status : statuses) {
        		getChannelLogs(serverId, status.getChannelId());
        	}
        } else {
        	getChannelLogs(serverId, NO_CHANNEL_SELECTED);
        }
    }
    
    private void getChannelLogs(String serverId, String channelId) {
    	Map<String, Long> lastLogIdByChannelId = lastLogIdByServerId.get(serverId);
        if (lastLogIdByChannelId == null) {
        	lastLogIdByChannelId = new HashMap<>();
        	lastLogIdByServerId.put(serverId, lastLogIdByChannelId);
        }
        
        Long lastLogId = lastLogIdByChannelId.get(channelId);
        
        Long lastClusterLogId = lastClusterLogIdWhenCleared.get(channelId);
        if (lastClusterLogId != null &&
        		(lastLogId == null || lastClusterLogId > lastLogId)) {
        	lastLogId = lastClusterLogId;
        }

        //get states from server only if the client's channel log is not in the paused state.
        if (!dcsp.isPaused(channelId)) {
            LinkedList<ConnectionLogItem> connectionInfoLogsReceived = new LinkedList<>();
            try {
                if (channelId.equals(NO_CHANNEL_SELECTED)) {
                    connectionInfoLogsReceived = PlatformUI.MIRTH_FRAME.mirthClient.getServlet(DashboardConnectorStatusServletInterface.class).getAllChannelLogs(serverId.equals(NO_SERVER_SELECTED) ? null : serverId, currentDashboardLogSize, lastLogId);
                } else {
                    connectionInfoLogsReceived = PlatformUI.MIRTH_FRAME.mirthClient.getServlet(DashboardConnectorStatusServletInterface.class).getChannelLog(serverId.equals(NO_SERVER_SELECTED) ? null : serverId, channelId, currentDashboardLogSize, lastLogId);
                }
            } catch (ClientException e) {
                parent.alertThrowable(parent, e, false);
            }

            LinkedList<ConnectionLogItem> channelLog = getChannelLog(channelId);

            synchronized (this) {
                for (int i = connectionInfoLogsReceived.size() - 1; i >= 0; i--) {
                    channelLog.addFirst(connectionInfoLogsReceived.get(i));
                }
                while (channelLog.size() > currentDashboardLogSize) {
                    channelLog.removeLast();
                }

                lastLogIdByChannelId.put(channelId, channelLog.getFirst().getLogId());
            }

            Map<String, LinkedList<ConnectionLogItem>> serverLog = connectorInfoLogs.get(serverId);
            if (serverLog == null) {
            	serverLog = new HashMap<>();
            	connectorInfoLogs.put(serverId, serverLog);
            }
            serverLog.put(channelId, channelLog);
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
        
        String selectedChannelId = (statuses != null && statuses.size() == 1) ? statuses.get(0).getChannelId() : NO_CHANNEL_SELECTED;

        dcsp.setSelectedChannelId(selectedChannelId);
        dcsp.setSelectedConnectors(selectedConnectorMap);
        dcsp.updateTable(getChannelLog(statuses));
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
    
    private LinkedList<ConnectionLogItem> getChannelLog(List<DashboardStatus> statuses) {
    	if (statuses == null || statuses.size() < 1) {
    		return getChannelLog(NO_CHANNEL_SELECTED);
    	}
    	
    	// Get logs for all selected channels and return a combined list
    	List<ConnectionLogItem> items = new ArrayList<>();
    	Set<String> selectedChannelIds = new HashSet<>();
    	
    	for (DashboardStatus status : statuses) {
    		if (!selectedChannelIds.contains(status.getChannelId())) {
    			items.addAll(getChannelLog(status.getChannelId()));
    			selectedChannelIds.add(status.getChannelId());
    		}
    	}
    	
    	items.sort(new Comparator<ConnectionLogItem>() {
			@Override
			public int compare(ConnectionLogItem item1, ConnectionLogItem item2) {
				return item2.getLogId().compareTo(item1.getLogId());
			}
    	});
    	
    	if (items.size() > currentDashboardLogSize) {
    		items = items.subList(0, currentDashboardLogSize);
    	}
    	
    	LinkedList<ConnectionLogItem> linkedItems = new LinkedList<>();
    	linkedItems.addAll(items);
    	return linkedItems;
    }
    
    private LinkedList<ConnectionLogItem> getChannelLog(String channelId) {
    	String serverId = getSelectedServerId();
		if (serverId == null) {
			serverId = NO_SERVER_SELECTED;
		}
		
    	return getChannelLog(serverId, channelId);
    }
    
    private LinkedList<ConnectionLogItem> getChannelLog(String serverId, String channelId) {
		if (connectorInfoLogs.containsKey(serverId)) {
			Map<String, LinkedList<ConnectionLogItem>> serverLog = connectorInfoLogs.get(serverId);
			if (serverLog.containsKey(channelId)) {
				return serverLog.get(channelId);
			}
		}

    	return new LinkedList<>();
    }
    
    private String getSelectedServerId() {
    	for (DashboardTablePlugin plugin : LoadedExtensions.getInstance().getDashboardTablePlugins().values()) {
            if (plugin.getServerId() != null) {
            	return plugin.getServerId();
            }
        }
    	return null;
    }       
}
