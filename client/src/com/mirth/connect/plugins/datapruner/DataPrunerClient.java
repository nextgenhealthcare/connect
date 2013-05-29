/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.plugins.SettingsPanelPlugin;

public class DataPrunerClient extends SettingsPanelPlugin {

    private AbstractSettingsPanel settingsPanel = null;

    public DataPrunerClient(String name) {
        super(name);

        settingsPanel = new DataPrunerPanel("Data Pruner", this);
    }

    @Override
    public AbstractSettingsPanel getSettingsPanel() {
        return settingsPanel;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }

    @Override
    public String getPluginPointName() {
        return "Data Pruner";
    }
}
