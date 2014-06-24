package com.mirth.connect.plugins;

import javax.swing.JComponent;

public abstract class DashboardTabPlugin extends DashboardPanelPlugin {
    public DashboardTabPlugin(String name) {
        super(name);
    }

    public abstract JComponent getTabComponent();
}
