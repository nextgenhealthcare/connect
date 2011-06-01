/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.ArrayList;
import java.util.Properties;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.CodeTemplate;

public abstract class ClientPlugin {

    protected String name;
    protected Frame parent = PlatformUI.MIRTH_FRAME;

    public ClientPlugin() {
    }

    public ClientPlugin(String name) {
        this.name = name;
    }

    public ArrayList<CodeTemplate> getReferenceItems() {
        return new ArrayList<CodeTemplate>();
    }

    public String getName() {
        return name;
    }
    
    public Properties getPropertiesFromServer() throws ClientException {
        return parent.mirthClient.getPluginProperties(name);
    }

    public void setPropertiesToServer(Properties properties) throws ClientException {
        parent.mirthClient.setPluginProperties(name, properties);
    }
    
    public Object invoke(String method, Object object) throws ClientException {
        return parent.mirthClient.invokePluginMethod(name, method, object);
    }
    
    // used for starting processes in the plugin when the program is started
    public abstract void start();

    // used for stopping processes in the plugin when the program is exited
    public abstract void stop();

    // Called when establishing a new session for the user
    public abstract void reset();
}
