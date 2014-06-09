/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import javax.swing.JPanel;

import com.mirth.connect.client.ui.panels.connectors.ConnectorPanel;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.model.Connector.Mode;

public abstract class AbstractConnectorPropertiesPanel extends JPanel {

    protected ConnectorPanel connectorPanel;

    /**
     * Gets all of the current data in the plugin's form.
     */
    public abstract ConnectorPluginProperties getProperties();

    /**
     * Sets all of the current data in the plugin's form to the data in the properties object
     * parameter.
     */
    public abstract void setProperties(ConnectorPluginProperties properties, Mode mode, String transportName);

    /**
     * Gets all of the default connector plugin properties
     */
    public abstract ConnectorPluginProperties getDefaults();

    /**
     * Checks to see if the properties in the plugin are all valid. Highlights fields that are not
     * valid if highlight=true.
     * 
     * @param props
     * @param highlight
     * @return
     */
    public abstract boolean checkProperties(ConnectorPluginProperties properties, Mode mode, String transportName, boolean highlight);

    /**
     * Resets the highlighting on fields that could be highlighted.
     */
    public abstract void resetInvalidProperties();

    /**
     * Sets the ConnectorPanel associated with this plugin properties-specific panel.
     * 
     * @param connectorPanel
     */
    public final void setConnectorPanel(ConnectorPanel connectorPanel) {
        this.connectorPanel = connectorPanel;
    }

    /**
     * Returns any special highlighting/etc. that should be done for the connector type in its
     * associated table. Returns null if no decoration should be done, or if not applicable.
     */
    public ConnectorTypeDecoration getConnectorTypeDecoration() {
        return null;
    }

    /**
     * Using the decoration object parameter, performs any special highlighting/etc. that should be
     * done.
     * 
     * @param connectorTypeDecoration
     */
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {}

    /**
     * Allows the connector plugin properties panel to take special actions after invoking a
     * connector service.
     * 
     * @param settingsPanel
     * @param method
     * @param response
     * @return true if the regular connector panel response logic should be used, otherwise false
     */
    public boolean handleConnectorServiceResponse(ConnectorSettingsPanel settingsPanel, String method, Object response) {
        return true;
    }
}