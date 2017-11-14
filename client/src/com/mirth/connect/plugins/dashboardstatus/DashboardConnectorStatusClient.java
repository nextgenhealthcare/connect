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
    

    /** Creates a new instance of DashboardConnectorStatusClient */
    public DashboardConnectorStatusClient(String name) {
        super(name);
        shouldResetLogs = true;
        lastLogIdByServerId = new ConcurrentHashMap<>();
        connectorInfoLogs = new ConcurrentHashMap<>();
        dcsp = new DashboardConnectorStatusPanel(this);
        currentDashboardLogSize = dcsp.getCurrentDashboardLogSize();
    }

    public void clearLog(String selectedChannelId) {
    	String serverId = getSelectedServerId();
    	
    	if (serverId == null) {    		
    		for (String srvId : lastLogIdByServerId.keySet()) {
    			clearLog(srvId, selectedChannelId);
    			updateServerLastLogId(srvId, selectedChannelId);
    		}
    	} else {
    		removeLogsFromCluster(serverId, selectedChannelId);
    		clearLog(serverId, selectedChannelId);
    	}
    	
    	dcsp.updateTable(null);
    }
    
	private void clearLog(String serverId, String selectedChannelId) {
		Map<String, LinkedList<ConnectionLogItem>> serverLog = connectorInfoLogs.get(serverId);
		
		if (serverLog != null) {
			List<String> channelIds = getSelectedChannelIds(selectedChannelId);

			for (String channelId : channelIds) {
				serverLog.remove(channelId);
			}
		}
	}
	
	/*
	 * Updates the last log IDs for the server to the max of its values and the cluster's values
	 */
	private void updateServerLastLogId(String serverId, String selectedChannelId) {
		List<String> channelIds = getSelectedChannelIds(selectedChannelId);
		
		for (String channelId : channelIds) {
			Map<String, Long> clusterLastLogIds = lastLogIdByServerId.get(NO_SERVER_SELECTED);
			
			Long clusterLastLogId = null;
			if (clusterLastLogIds != null) {
				clusterLastLogId = clusterLastLogIds.get(channelId);
			}

			Map<String, Long> lastLogIds = lastLogIdByServerId.get(serverId);
			if (lastLogIds == null) {
				lastLogIds = new HashMap<>();
				lastLogIdByServerId.put(serverId, lastLogIds);
			}

			Long lastLogId = lastLogIds.get(channelId);

			if (clusterLastLogId != null && (lastLogId == null || clusterLastLogId > lastLogId)) {
				lastLogIds.put(channelId, clusterLastLogId);
			}
		}
	}
	
	private void removeLogsFromCluster(String serverId, String selectedChannelId) {
		Map<String, LinkedList<ConnectionLogItem>> serverLog = connectorInfoLogs.get(serverId);
		
		if (serverLog != null) {
			List<String> channelIds = getSelectedChannelIds(selectedChannelId);
			
			Set<Long> logIdsToRemove = new HashSet<>();
			
			for (String channelId : channelIds) {
				// We get channels logs for the Cluster, which also updates its last log IDs, before deleting logs from it.
				// This prevents us from missing out on fetching logs that we have not yet fetched.
				getChannelLogs(NO_SERVER_SELECTED, channelId);
				
				Map<String, LinkedList<ConnectionLogItem>> clusterLog = connectorInfoLogs.get(NO_SERVER_SELECTED);
				if (clusterLog == null) {
					clusterLog = new HashMap<>();
					connectorInfoLogs.put(NO_SERVER_SELECTED, clusterLog);
				}
				
				if (clusterLog.containsKey(channelId)) {
					List<ConnectionLogItem> items = serverLog.get(channelId);
					for (ConnectionLogItem item : items) {
						logIdsToRemove.add(item.getLogId());
					}

					LinkedList<ConnectionLogItem> prunedClusterLog = new LinkedList<>();
					for (ConnectionLogItem item : clusterLog.get(channelId)) {
						if (!logIdsToRemove.contains(item.getLogId())) {
							prunedClusterLog.add(item);
						}
					}

					clusterLog.put(channelId, prunedClusterLog);
				}
			}
			
			
		}
	}
	
	private List<String> getSelectedChannelIds(String selectedChannelId) {
		List<String> channelIds = new ArrayList<String>();
		
		if (selectedChannelId.equals(NO_CHANNEL_SELECTED)
				&& (selectedStatuses != null && selectedStatuses.size() > 0)) {
			for (DashboardStatus status : selectedStatuses) {
				channelIds.add(status.getChannelId());
			}
		} else {
			channelIds.add(selectedChannelId);
		}
		
		return channelIds;
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
    
    private synchronized void getChannelLogs(String serverId, String channelId) {
    	Map<String, Long> lastLogIdByChannelId = lastLogIdByServerId.get(serverId);
        if (lastLogIdByChannelId == null) {
        	lastLogIdByChannelId = new HashMap<>();
        	lastLogIdByServerId.put(serverId, lastLogIdByChannelId);
        }
        Long lastLogId = lastLogIdByChannelId.get(channelId);

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

            LinkedList<ConnectionLogItem> channelLog = getChannelLog(serverId, channelId);
			for (int i = connectionInfoLogsReceived.size() - 1; i >= 0; i--) {
				ConnectionLogItem item = connectionInfoLogsReceived.get(i);
				channelLog.addFirst(item);
				
				// Create lastLogId entries for any servers we see
				if (item.getServerId() != null && !lastLogIdByServerId.containsKey(item.getServerId())) {
					lastLogIdByServerId.put(item.getServerId(), new HashMap<>());
				}
			}
			while (channelLog.size() > currentDashboardLogSize) {
				channelLog.removeLast();
			}
			
			Map<String, LinkedList<ConnectionLogItem>> serverLog = connectorInfoLogs.get(serverId);
			if (serverLog == null) {
				serverLog = new HashMap<>();
				connectorInfoLogs.put(serverId, serverLog);
			}
			
			serverLog.put(channelId, channelLog);
			
			if (!connectionInfoLogsReceived.isEmpty()) {
				lastLogIdByChannelId.put(channelId, connectionInfoLogsReceived.getFirst().getLogId());
			}
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
    	String server = getSelectedServerId();
		return getChannelLog(server == null ? NO_SERVER_SELECTED : server, channelId);
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
