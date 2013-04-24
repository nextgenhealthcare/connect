package com.mirth.connect.model.alert;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertModel")
public class AlertModel {

    private String id;
    private String name;
    private boolean enabled;
    private AlertTrigger trigger;
    private List<AlertActionGroup> actionGroups = new ArrayList<AlertActionGroup>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AlertTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(AlertTrigger trigger) {
        this.trigger = trigger;
    }

    public List<AlertActionGroup> getActionGroups() {
        return actionGroups;
    }

    public void setActionGroups(List<AlertActionGroup> actionGroups) {
        this.actionGroups = actionGroups;
    }

}
