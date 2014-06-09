/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import com.mirth.connect.client.ui.AbstractConnectorPropertiesPanel;

public abstract class ConnectorPropertiesPlugin extends ClientPlugin {

    public ConnectorPropertiesPlugin(String pluginName) {
        super(pluginName);
    }

    /**
     * Returns the title to use for the border in the connector panel (e.g. "SSL Settings").
     */
    public abstract String getSettingsTitle();

    /**
     * Returns the panel to use for getting/setting connector plugin properties.
     */
    public abstract AbstractConnectorPropertiesPanel getConnectorPropertiesPanel();

    /**
     * Returns true if the connector properties plugin supports the given connector type.
     */
    public abstract boolean isSupported(String transportName);

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}
