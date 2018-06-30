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
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.converters.PluginPropertiesConverter;
import com.mirth.connect.plugins.ConnectorPropertiesPlugin;
import com.mirth.connect.plugins.httpauth.oauth2.OAuth2HttpAuthProperties;
import com.thoughtworks.xstream.XStream;

public class HttpAuthConnectorPropertiesPlugin extends ConnectorPropertiesPlugin {

    public HttpAuthConnectorPropertiesPlugin(String pluginName) {
        super(pluginName);

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        XStream xstream = serializer.getXStream();
        xstream.registerLocalConverter(OAuth2HttpAuthProperties.class, "connectorPluginProperties", new PluginPropertiesConverter(serializer.getNormalizedVersion(), xstream.getMapper()));
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
        return transportName.equals("HTTP Listener") || transportName.equals("Web Service Listener") || transportName.equals("FHIR Listener") || transportName.equals("Health Data Hub Listener");
    }

    @Override
    public boolean isConnectorPropertiesPluginSupported(String pluginPointName) {
        return false;
    }
}