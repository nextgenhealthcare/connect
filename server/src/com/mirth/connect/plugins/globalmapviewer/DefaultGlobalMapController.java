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
import java.util.Map;
import java.util.Set;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;

public class DefaultGlobalMapController extends GlobalMapController {

    private static final ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    protected static final String GLOBAL_MAP_KEY = "<Global Map>";

    @Override
    public Map<String, Map<String, String>> getAllMaps(Set<String> channelIds, boolean includeGlobalMap) throws ControllerException {
        Map<String, String> serializedMaps = new HashMap<String, String>();

        if (includeGlobalMap) {
            serializedMaps.put(null, MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalVariableStore.getInstance().getVariables())));
        }

        if (channelIds == null) {
            channelIds = new HashSet<String>(GlobalChannelVariableStoreFactory.getInstance().globalChannelVariableMap.keySet());
        }

        for (String channelId : channelIds) {
            serializedMaps.put(channelId, MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables())));
        }

        Map<String, Map<String, String>> serverMap = new HashMap<String, Map<String, String>>();
        serverMap.put(configurationController.getServerId(), serializedMaps);
        return serverMap;
    }

    @Override
    public String getGlobalMap() {
        return MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalVariableStore.getInstance().getVariables()));
    }

    @Override
    public String getGlobalChannelMap(String channelId) {
        return MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables()));
    }
}
