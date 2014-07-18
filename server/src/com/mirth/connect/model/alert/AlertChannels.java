/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertChannels")
public class AlertChannels implements Migratable, Purgable {

    private boolean newChannelSource = false;
    private boolean newChannelDestination = false;
    private Set<String> enabledChannels = new HashSet<String>();
    private Set<String> disabledChannels = new HashSet<String>();
    private Map<String, AlertConnectors> partialChannels = new HashMap<String, AlertConnectors>();

    public void setNewChannel(boolean source, boolean destinations) {
        newChannelSource = source;
        newChannelDestination = destinations;
    }

    public void addChannel(String channelId, Map<Integer, Boolean> connectorMap) {
        AlertConnectors connectors = new AlertConnectors();

        boolean allEnabled = true;
        boolean allDisabled = true;
        boolean matchesNewChannel = true;

        for (Entry<Integer, Boolean> entry : connectorMap.entrySet()) {
            Integer metaDataId = entry.getKey();
            boolean enabled = entry.getValue();

            if (enabled) {
                allDisabled = false;
                connectors.getEnabledConnectors().add(metaDataId);
            } else {
                allEnabled = false;
                connectors.getDisabledConnectors().add(metaDataId);
            }

            if (metaDataId == null || metaDataId > 0) {
                // To match the new channel settings, all destinations must match newChannelDestination
                if (enabled != newChannelDestination) {
                    matchesNewChannel = false;
                }
            } else {
                // To match the new channel settings, the source must match newChannelSource
                if (enabled != newChannelSource) {
                    matchesNewChannel = false;
                }
            }
        }

        // There is no need to add a channel's setting if it completely matches the new channel settings.
        if (!matchesNewChannel) {
            if (allEnabled) {
                enabledChannels.add(channelId);
            } else if (allDisabled) {
                disabledChannels.add(channelId);
            } else {
                partialChannels.put(channelId, connectors);
            }
        }
    }

    public boolean isChannelEnabled(String channelId) {
        if (enabledChannels.contains(channelId)) {
            return true;
        } else if (disabledChannels.contains(channelId)) {
            return false;
        } else if (partialChannels.containsKey(channelId)) {
            Set<Integer> enabledConnectors = partialChannels.get(channelId).getEnabledConnectors();
            return enabledConnectors.size() > 0;
        } else {
            return newChannelSource || newChannelDestination;
        }
    }

    public boolean isConnectorEnabled(String channelId, Integer metaDataId) {
        if (enabledChannels.contains(channelId)) {
            return true;
        } else if (disabledChannels.contains(channelId)) {
            return false;
        } else if (partialChannels.containsKey(channelId)) {
            Set<Integer> enabledConnectors = partialChannels.get(channelId).getEnabledConnectors();
            Set<Integer> disabledConnectors = partialChannels.get(channelId).getDisabledConnectors();

            return enabledConnectors.contains(metaDataId) || (enabledConnectors.contains(null) && !disabledConnectors.contains(metaDataId));
        } else if (metaDataId == null || metaDataId > 0) {
            return newChannelDestination;
        } else {
            return newChannelSource;
        }
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("newChannelSource", newChannelSource);
        purgedProperties.put("newChannelDestination", newChannelDestination);
        purgedProperties.put("enabledChannelsCount", enabledChannels.size());
        purgedProperties.put("disabledChannelsCount", disabledChannels.size());
        purgedProperties.put("partialChannelsCount", partialChannels.size());
        return purgedProperties;
    }
}
