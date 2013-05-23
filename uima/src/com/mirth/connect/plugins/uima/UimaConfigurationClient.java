package com.mirth.connect.plugins.uima;

import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.plugins.SettingsPanelPlugin;

public class UimaConfigurationClient extends SettingsPanelPlugin {
    private AbstractSettingsPanel settingsPanel = null;

    public UimaConfigurationClient(String name) {
        super(name);
        settingsPanel = new UimaConfigurationPanel("UIMA Configuration", this);
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
        return "UIMA Service";
    }
}
