/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.Rule;

public abstract class FilterRulePlugin extends ClientPlugin {

    public FilterRulePlugin(String name) {
        super(name);
    }

    public abstract boolean isNameEditable();

    public boolean showValidateTask() {
        return false;
    }

    public abstract Rule newRule(String mapping);

    public abstract EditorPanel<Rule> getPanel();

    public Rule getDefaults() {
        return getPanel().getDefaults();
    }

    public Rule getProperties() {
        return getPanel().getProperties();
    }

    public void setProperties(Mode mode, Rule properties) {
        getPanel().resetInvalidProperties();
        getPanel().setProperties(properties);
    }

    public String checkProperties(Rule properties, boolean highlight) {
        getPanel().resetInvalidProperties();
        return getPanel().checkProperties(properties, highlight);
    }

    public void resetInvalidProperties() {
        getPanel().resetInvalidProperties();
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}