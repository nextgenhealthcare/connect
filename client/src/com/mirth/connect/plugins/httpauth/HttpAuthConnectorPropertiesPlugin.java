/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth;

import com.mirth.connect.client.ui.AbstractConnectorPropertiesPanel;
import com.mirth.connect.plugins.ConnectorPropertiesPlugin;

public class HttpAuthConnectorPropertiesPlugin extends ConnectorPropertiesPlugin {

    public HttpAuthConnectorPropertiesPlugin(String pluginName) {
        super(pluginName);
    }

    @Override
    public String getPluginPointName() {
        return HttpAuthConnectorPluginProperties.PLUGIN_POINT;
    }

    @Override
    public String getSettingsTitle() {
        return "HTTP Authentication";
    }

    @Override
    public AbstractConnectorPropertiesPanel getConnectorPropertiesPanel() {
        return new HttpAuthConnectorPropertiesPanel();
    }

    @Override
    public boolean isSupported(String transportName) {
        return transportName.equals("HTTP Listener") || transportName.equals("Web Service Listener") || transportName.equals("FHIR Listener");
    }

    @Override
    public boolean isConnectorPropertiesPluginSupported(String pluginPointName) {
        return false;
    }
}