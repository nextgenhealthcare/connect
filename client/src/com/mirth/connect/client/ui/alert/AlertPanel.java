package com.mirth.connect.client.ui.alert;

import java.util.List;

import javax.swing.JPanel;

import com.mirth.connect.model.alert.AlertStatus;

public abstract class AlertPanel extends JPanel {

    public abstract void updateAlertTable(List<AlertStatus> alertStatusList);

    public abstract List<String> getSelectedAlertIds();

    public abstract void setSelectedAlertIds(List<String> alertIds);

}
