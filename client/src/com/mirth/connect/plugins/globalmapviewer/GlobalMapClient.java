/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.globalmapviewer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.ForbiddenException;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.DashboardTabPlugin;
import com.mirth.connect.plugins.DashboardTablePlugin;
import com.mirth.connect.util.StringUtil;

public class GlobalMapClient extends DashboardTabPlugin {

    private GlobalMapPanel globalMapPanel;
    private Vector<Object> data;
    private int selectedRow;

    public GlobalMapClient(String name) {
        super(name);

        globalMapPanel = new GlobalMapPanel();
    }

    @Override
    public JComponent getTabComponent() {
        return globalMapPanel;
    }

    @Override
    public void prepareData() throws ClientException {
        prepareData(null);
    }

    @Override
    public void prepareData(List<DashboardStatus> statuses) throws ClientException {
        final Set<String> channelIds = new HashSet<String>();
        // Use this map to look up channel names from channel Ids
        final Map<String, String> channelNameMap = new HashMap<String, String>();
        /*
         * We can't just use a final String here because it's set inside the invokeAndWait call and
         * it's illegal to set a final local field of an enclosing class. So instead we just use a
         * Set and add a single element.
         */
        final Set<String> selectedServer = new HashSet<String>();

        // Determine which global channel maps to retrieve
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    Set<DashboardStatus> channelStatuses = PlatformUI.MIRTH_FRAME.dashboardPanel.getSelectedChannelStatuses();
                    if (channelStatuses != null) {
                        for (DashboardStatus channelStatus : channelStatuses) {
                            channelIds.add(channelStatus.getChannelId());
                            channelNameMap.put(channelStatus.getChannelId(), channelStatus.getName());
                        }
                    }

                    String serverId = null;
                    for (DashboardTablePlugin plugin : LoadedExtensions.getInstance().getDashboardTablePlugins().values()) {
                        serverId = plugin.getServerId();
                        if (serverId != null) {
                            break;
                        }
                    }
                    selectedServer.add(serverId);
                }

            });
        } catch (Exception e) {
        }

        try {
            data = new Vector<Object>();
            Serializer serializer = ObjectXMLSerializer.getInstance();
            Map<String, Map<String, String>> globalMaps = null;

            selectedRow = 0;
            String currentlySelectedServer = globalMapPanel.getSelectedServer();
            String currentlySelectedMap = globalMapPanel.getSelectedMap();
            String currentlySelectedVar = globalMapPanel.getSelectedVar();
            try {
                globalMaps = (Map<String, Map<String, String>>) PlatformUI.MIRTH_FRAME.mirthClient.getServlet(GlobalMapServletInterface.class).getAllMapsPost(channelIds, true);
            } catch (ClientException e) {
                if (e instanceof ForbiddenException) {
                    // Don't error. Let an empty map be processed
                    parent.alertThrowable(parent, e, false);
                } else {
                    throw e;
                }
            }

            if (globalMaps != null) {
                Map<String, Map<String, String>> sortedGlobalMaps = new TreeMap<String, Map<String, String>>();
                Map<String, String> serializedGlobalMaps = new TreeMap<String, String>();

                String selectedServerId = null;
                if (!selectedServer.isEmpty()) {
                    selectedServerId = selectedServer.iterator().next();
                }

                // Sort the maps in order of channel name for better readability
                for (Entry<String, Map<String, String>> serverEntry : globalMaps.entrySet()) {
                    if (selectedServerId != null && !selectedServerId.equals(serverEntry.getKey())) {
                        continue;
                    }

                    Map<String, String> sortedServerGlobalMaps = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

                    for (Entry<String, String> channelEntry : serverEntry.getValue().entrySet()) {
                        if (channelEntry.getKey() == null) {
                            /*
                             * Since the global map's name is null, it cannot be used as a key in
                             * the Tree Map. We also want to display it last, so we'll just store a
                             * reference to it and use it later
                             */
                            serializedGlobalMaps.put(serverEntry.getKey(), channelEntry.getValue());
                        } else {
                            sortedServerGlobalMaps.put(channelNameMap.get(channelEntry.getKey()), channelEntry.getValue());
                        }
                    }

                    sortedGlobalMaps.put(serverEntry.getKey(), sortedServerGlobalMaps);
                }

                // For each global channel map, display each of its keys alphabetically
                for (Entry<String, Map<String, String>> serverEntry : sortedGlobalMaps.entrySet()) {
                    for (Entry<String, String> channelEntry : serverEntry.getValue().entrySet()) {
                        String channelName = channelEntry.getKey();
                        String serializedMap = channelEntry.getValue();

                        Map<String, Object> sortedMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
                        sortedMap.putAll(MapUtil.deserializeMap(serializer, serializedMap));

                        for (Entry<String, Object> entry : sortedMap.entrySet()) {
                            Vector<Object> row = new Vector<Object>();
                            String entryKey = StringUtil.valueOf(entry.getKey());
                            row.add(serverEntry.getKey());
                            row.add(channelName);
                            row.add(StringUtil.valueOf(entryKey));
                            row.add(StringUtil.valueOf(entry.getValue()));

                            data.add(row);

                            if (StringUtils.equals(serverEntry.getKey(), currentlySelectedServer) && StringUtils.equals(entryKey, currentlySelectedVar) && StringUtils.equals(channelName, currentlySelectedMap)) {
                                selectedRow = data.size();
                            }
                        }
                    }
                }

                // Now we add the global map if necessary
                if (MapUtils.isNotEmpty(serializedGlobalMaps)) {
                    for (Entry<String, String> serverEntry : serializedGlobalMaps.entrySet()) {
                        Map<String, Object> sortedMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
                        sortedMap.putAll(MapUtil.deserializeMap(serializer, serverEntry.getValue()));

                        for (Entry<String, Object> entry : sortedMap.entrySet()) {

                            Vector<Object> row = new Vector<Object>();
                            String entryKey = StringUtil.valueOf(entry.getKey());
                            row.add(serverEntry.getKey());
                            row.add("<Global Map>");
                            row.add(entryKey);
                            row.add(StringUtil.valueOf(entry.getValue()));

                            data.add(row);

                            if (StringUtils.equals(serverEntry.getKey(), currentlySelectedServer) && StringUtils.equals(entryKey, currentlySelectedVar) && StringUtils.equals("<Global Map>", currentlySelectedMap)) {
                                selectedRow = data.size();
                            }
                        }
                    }
                }
            }

        } catch (ClientException e) {
            throw e;
        }
    }

    @Override
    public void update() {
        globalMapPanel.updateTable(data, selectedRow);
    }

    @Override
    public void update(List<DashboardStatus> statuses) {
        update();
    }

    @Override
    public String getPluginPointName() {
        return GlobalMapServletInterface.PLUGIN_POINT;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}
