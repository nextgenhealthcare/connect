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
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;

public class GlobalMapProvider implements ServicePlugin {
    public static final String PLUGINPOINT = "Global Maps";
    private Logger logger = Logger.getLogger(this.getClass());
    private static final String GET_GLOBAL_MAPS = "getGlobalMaps";

    @Override
    public String getPluginPointName() {
        return PLUGINPOINT;
    }

    public synchronized Object invoke(String method, Object object, String sessionId) {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

        if (method.equals(GET_GLOBAL_MAPS)) {
            Set<String> channelIds = (Set<String>) object;
            Map<String, String> serializedMaps = new HashMap<String, String>();
            for (String channelId : channelIds) {
                if (channelId == null) {
                    serializedMaps.put(null, MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalVariableStore.getInstance().getVariables())));
                } else {
                    serializedMaps.put(channelId, MapUtil.serializeMap(serializer, new HashMap<String, Object>(GlobalChannelVariableStoreFactory.getInstance().get(channelId).getVariables())));
                }
            }

            return serializedMaps;
        }

        return null;
    }

    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGINPOINT, "View Global Maps", "Displays the contents of the global map and global channel maps on the Dashboard.", new String[] { GET_GLOBAL_MAPS }, new String[] {});
        return new ExtensionPermission[] { viewPermission };
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void update(Properties properties) {}

    @Override
    public void init(Properties properties) {}
}
