package com.mirth.connect.client.ui.alert;

import java.util.List;

import javax.swing.JPanel;

import com.mirth.connect.model.alert.AlertTrigger;

public abstract class AlertTriggerPane extends JPanel {

    public abstract List<String> getVariables();

    public abstract AlertTrigger getTrigger();

    public abstract void setTrigger(AlertTrigger trigger);

    public abstract void reset();
}
