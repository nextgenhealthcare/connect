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

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.ForbiddenException;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.DashboardTabPlugin;
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
                }

            });
        } catch (Exception e) {
        }

        try {
            data = new Vector<Object>();
            Serializer serializer = ObjectXMLSerializer.getInstance();
            Map<String, String> globalMaps = null;

            selectedRow = 0;
            String currentlySelectedMap = globalMapPanel.getSelectedMap();
            String currentlySelectedVar = globalMapPanel.getSelectedVar();
            try {
                globalMaps = (Map<String, String>) PlatformUI.MIRTH_FRAME.mirthClient.getServlet(GlobalMapServletInterface.class).getAllMapsPost(channelIds, true);
            } catch (ClientException e) {
                if (e instanceof ForbiddenException) {
                    // Don't error. Let an empty map be processed
                    parent.alertThrowable(parent, e, false);
                } else {
                    throw e;
                }
            }

            if (globalMaps != null) {
                Map<String, String> sortedGlobalMaps = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
                String serializedGlobalMap = null;
                // Sort the maps in order of channel name for better readability
                for (Entry<String, String> channelEntry : globalMaps.entrySet()) {
                    if (channelEntry.getKey() == null) {
                        /*
                         * Since the global map's name is null, it cannot be used as a key in the
                         * Tree Map. We also want to display it last, so we'll just store a
                         * reference to it and use it later
                         */
                        serializedGlobalMap = channelEntry.getValue();
                    } else {
                        sortedGlobalMaps.put(channelNameMap.get(channelEntry.getKey()), channelEntry.getValue());
                    }
                }

                // For each global channel map, display each of its keys alphabetically
                for (Entry<String, String> channelEntry : sortedGlobalMaps.entrySet()) {
                    String channelName = channelEntry.getKey();
                    String serializedMap = channelEntry.getValue();

                    Map<String, Object> sortedMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
                    sortedMap.putAll(MapUtil.deserializeMap(serializer, serializedMap));

                    for (Entry<String, Object> entry : sortedMap.entrySet()) {
                        Vector<Object> row = new Vector<Object>();
                        String entryKey = StringUtil.valueOf(entry.getKey());
                        row.add(channelName);
                        row.add(StringUtil.valueOf(entryKey));
                        row.add(StringUtil.valueOf(entry.getValue()));

                        data.add(row);

                        if (StringUtils.equals(entryKey, currentlySelectedVar) && StringUtils.equals(channelName, currentlySelectedMap)) {
                            selectedRow = data.size();
                        }
                    }
                }

                // Now we add the global map if necessary
                if (serializedGlobalMap != null) {
                    Map<String, Object> sortedMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
                    sortedMap.putAll(MapUtil.deserializeMap(serializer, serializedGlobalMap));

                    for (Entry<String, Object> entry : sortedMap.entrySet()) {

                        Vector<Object> row = new Vector<Object>();
                        String entryKey = StringUtil.valueOf(entry.getKey());
                        row.add("<Global Map>");
                        row.add(entryKey);
                        row.add(StringUtil.valueOf(entry.getValue()));

                        data.add(row);

                        if (StringUtils.equals(entryKey, currentlySelectedVar) && StringUtils.equals("<Global Map>", currentlySelectedMap)) {
                            selectedRow = data.size();
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
