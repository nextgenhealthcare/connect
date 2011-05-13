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
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;

public abstract class ClientPlugin {

    protected String pluginName;
    protected Frame parent = PlatformUI.MIRTH_FRAME;

    public ClientPlugin(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * The name of the plugin, which could contain many plugin points.
     * 
     * @return
     */
    public String getPluginName() {
        return pluginName;
    }

    public Properties getPropertiesFromServer() throws ClientException {
        return parent.mirthClient.getPluginProperties(pluginName);
    }

    public void setPropertiesToServer(Properties properties) throws ClientException {
        parent.mirthClient.setPluginProperties(pluginName, properties);
    }

    public Object invoke(String method, Object object) throws ClientException {
        return parent.mirthClient.invokePluginMethod(pluginName, method, object);
    }

    // Each plugin point a plugin implements should define its own name
    public abstract String getPluginPointName();

    // used for starting processes in the plugin when the program is started
    public abstract void start();

    // used for stopping processes in the plugin when the program is exited
    public abstract void stop();

    // Called when establishing a new session for the user
    public abstract void reset();
}
