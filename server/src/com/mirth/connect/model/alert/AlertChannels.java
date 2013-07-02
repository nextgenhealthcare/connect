/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertChannels")
public class AlertChannels {

    /**
     * The default enabled setting for a channel.
     */
    private Set<Integer> newChannel = new HashSet<Integer>();
    
    /**
     * Enabled settings for individual channels and connectors
     */
    private Map<String, Set<Integer>> channels = new HashMap<String, Set<Integer>>();
    
    public void setNewChannel(boolean source, boolean destinations) {
        if (destinations) {
            newChannel.add(null);
        }
        if (source != destinations) {
            newChannel.add(0);
        }
    }
    
    public void addChannel(String channelId, boolean newConnector, Map<Integer, Boolean> connectorMap) {
        boolean matchesNewChannel = (newChannel.contains(null) == newConnector);
        
        /*
         * The new connector setting is stored in the set as null. If null is in the set, new connectors will be enabled.
         */
        Set<Integer> connectors = new HashSet<Integer>();
        if (newConnector) {
            connectors.add(null);
        }
        
        for (Entry<Integer, Boolean> entry : connectorMap.entrySet()) {
            Integer metaDataId = entry.getKey();
            boolean enabled = entry.getValue();
            
            /*
             * The rest of the values in the set are exclusions for the new connector setting. 
             * 
             * If null is in the set, then new connectors are enabled, and all other metaDataIds in the set are disabled.
             * If null is NOT in the set, then new connectors are disabled, and all other metaDataIds in the set are enabled.
             */
            if (enabled != newConnector) {
                connectors.add(metaDataId);
            }

            if (metaDataId != null && metaDataId > 0) {
                metaDataId = null;
            }
            
            if (newChannel.contains(metaDataId) != enabled) {
                matchesNewChannel = false;
            }
        }
        
        /*
         * Only add this channel's settings if it differs from the new channel settings
         */
        if (!matchesNewChannel) {
            channels.put(channelId, connectors);
        }
    }
    
    public boolean isChannelEnabled(String channelId) {
        Set<Integer> connectors;
        if (channels.containsKey(channelId)) {
            connectors = channels.get(channelId);
        } else {
            connectors = newChannel;
        }
        
        return connectors.size() > 0;
    }

    public boolean isConnectorEnabled(String channelId, Integer metaDataId) {
        Set<Integer> connectors;
        if (channels.containsKey(channelId)) {
            connectors = channels.get(channelId);
        } else {
            connectors = newChannel;
        }
        
        boolean newConnector = connectors.contains(null);
        
        if (metaDataId == null) {
            return newConnector;
        } else {
            return connectors.contains(metaDataId) ? !newConnector : newConnector;
        }
    }
}
