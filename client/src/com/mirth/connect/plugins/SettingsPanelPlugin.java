/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.Properties;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.AbstractSettingsPanel;

public abstract class SettingsPanelPlugin extends ClientPlugin {

    public SettingsPanelPlugin(String name) {
        this.name = name;
    }

    public abstract AbstractSettingsPanel getSettingsPanel();

    public Object invoke(String method, Object object) throws ClientException {
        return parent.mirthClient.invokePluginMethod(name, method, object);
    }

    public Properties getPropertiesFromServer() throws ClientException {
        return parent.mirthClient.getPluginProperties(name);
    }

    public void setPropertiesToServer(Properties properties) throws ClientException {
        parent.mirthClient.setPluginProperties(name, properties);
    }
}
