/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.globalmapviewer;

import java.util.Map;
import java.util.Set;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.server.ExtensionLoader;

public abstract class GlobalMapController {

    private static GlobalMapController instance = null;

    public static GlobalMapController getInstance() {
        synchronized (DefaultGlobalMapController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(GlobalMapController.class);

                if (instance == null) {
                    instance = new DefaultGlobalMapController();
                }
            }

            return instance;
        }
    }

    public abstract Map<String, Map<String, String>> getAllMaps(Set<String> channelIds, boolean includeGlobalMap) throws ControllerException;

    public abstract String getGlobalMap();

    public abstract String getGlobalChannelMap(String channelId);
}
