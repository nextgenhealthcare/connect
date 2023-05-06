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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;

public class DefaultGlobalMapController extends GlobalMapController {

    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    protected static final String GLOBAL_MAP_KEY = "<Global Map>";
    
    private static Serializer serializer = ObjectXMLSerializer.getInstance();
    private static Logger logger = LogManager.getLogger(DefaultGlobalMapController.class);

    @Override
    public Map<String, Map<String, Map<String, String>>> getAllMaps(Set<String> channelIds, boolean includeGlobalMap) throws ControllerException {
        Map<String, Map<String, String>> localMaps = new HashMap<>();
        
        if (includeGlobalMap) {
        	Map<String, String> globalMap = new HashMap<>();
        	for (Entry<String, Object> globalMapVariable : GlobalVariableStore.getInstance().getVariables().entrySet()) {
        		globalMap.put(globalMapVariable.getKey(), serializeGlobalMapVariableValue(globalMapVariable));
        	}
            localMaps.put(null, globalMap);
        }

        if (channelIds == null) {
            channelIds = new HashSet<String>(GlobalChannelVariableStoreFactory.getInstance().globalChannelVariableMap.keySet());
        }

        for (String channelId : channelIds) {
        	Map<String, String> channelGlobalMap = new HashMap<>();
        	for (Entry<String, Object> globalMapVariable :GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables().entrySet()) {
        		channelGlobalMap.put(globalMapVariable.getKey(), serializeGlobalMapVariableValue(globalMapVariable));
        	}
            localMaps.put(channelId, channelGlobalMap);
        }

        Map<String, Map<String, Map<String, String>>> serverMap = new HashMap<>();
        serverMap.put(configurationController.getServerId(), localMaps);
        return serverMap;
    }

    @Override
    public Map<String, String> getGlobalMap() {
    	Map<String, String> globalMap = new HashMap<>();
    	for (Entry<String, Object> globalMapVariable : GlobalVariableStore.getInstance().getVariables().entrySet()) {
    		globalMap.put(globalMapVariable.getKey(), serializeGlobalMapVariableValue(globalMapVariable));
    	}
        return globalMap;
    }

    @Override
    public Map<String, String> getGlobalChannelMap(String channelId) {
    	Map<String, String> globalChannelMap = new HashMap<>();
    	for (Entry<String, Object> globalMapVariable : GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables().entrySet()) {
    		globalChannelMap.put(globalMapVariable.getKey(), serializeGlobalMapVariableValue(globalMapVariable));
    	}
        return globalChannelMap;
    }
    
    protected String serializeGlobalMapVariableValue(Entry<String, Object> globalMapVariable) {
    	String serializedValue = "";
		try {
			serializedValue = serializer.serialize(globalMapVariable.getValue());
		} catch (Exception e) {
			logger.warn("Non-serializable value found in map, converting value to string with key: " + globalMapVariable.getKey());
			serializedValue = String.valueOf((globalMapVariable.getValue() == null) ? "" : globalMapVariable.getValue().toString());
		}
		return serializedValue;
    }
}
