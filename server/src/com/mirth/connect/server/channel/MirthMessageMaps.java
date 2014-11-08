/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.channel;

import java.util.Map;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.util.MessageMaps;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.util.GlobalChannelVariableStore;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;

public class MirthMessageMaps extends MessageMaps {

    private GlobalChannelVariableStore globalChannelMap;
    private GlobalVariableStore globalMap;
    private ConfigurationController configurationController;

    public MirthMessageMaps(String channelId) {
        globalChannelMap = GlobalChannelVariableStoreFactory.getInstance().get(channelId);
        globalMap = GlobalVariableStore.getInstance();
        configurationController = ConfigurationController.getInstance();
    }

    @Override
    public Object get(String key, ConnectorMessage connectorMessage) {
        return get(key, connectorMessage, true);
    }

    @Override
    public Object get(String key, ConnectorMessage connectorMessage, boolean includeResponseMap) {
        Object value = super.get(key, connectorMessage, includeResponseMap);

        // Check the server-only maps if nothing was found in the model maps
        if (value == null) {
            if (globalChannelMap != null && globalChannelMap.containsKey(key)) {
                value = globalChannelMap.get(key);
            } else if (globalMap != null && globalMap.containsKey(key)) {
                value = globalMap.get(key);
            } else {
                /*
                 * Only get the configuration map from the controller if it's needed because it is
                 * volatile and retrieving it is more expensive.
                 */
                Map<String, String> configurationMap = configurationController.getConfigurationMap();
                if (configurationMap != null) {
                    value = configurationMap.get(key);
                }
            }
        }

        return value;
    }
}