/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertModel")
public class AlertModel implements Migratable, Purgable {

    private String id;
    private String name;
    private boolean enabled;
    private AlertTrigger trigger;
    private List<AlertActionGroup> actionGroups;
    private Map<String, Object> properties;

    public AlertModel(AlertTrigger trigger, AlertActionGroup actionGroup) {
        id = UUID.randomUUID().toString();
        enabled = false;
        this.trigger = trigger;
        actionGroups = new ArrayList<AlertActionGroup>();
        actionGroups.add(actionGroup);
        properties = new HashMap<String, Object>();
    }

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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("id", id);
        purgedProperties.put("nameChars", PurgeUtil.countChars(name));
        purgedProperties.put("enabled", enabled);
        if (trigger instanceof Purgable) {
            purgedProperties.put("trigger", ((Purgable)trigger).getPurgedProperties());
        }
        purgedProperties.put("actionGroups", PurgeUtil.purgeList(actionGroups));
        return purgedProperties;
    }
}
