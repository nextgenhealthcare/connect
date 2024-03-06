package com.mirth.connect.client.ui;

import java.util.Set;

import javax.swing.JPanel;

import com.mirth.connect.model.DashboardStatus;

public abstract class DashboardPanelBase extends JPanel {

    public abstract Set<DashboardStatus> getSelectedChannelStatuses();
}
