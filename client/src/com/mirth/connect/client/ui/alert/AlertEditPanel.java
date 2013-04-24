package com.mirth.connect.client.ui.alert;

import javax.swing.JPanel;

import com.mirth.connect.model.alert.AlertModel;

public abstract class AlertEditPanel extends JPanel {
    
    public abstract void updateVariableList();

    public abstract void addAlert();
    
    public abstract void editAlert(AlertModel alertModel);
    
    public abstract boolean saveAlert();
}
