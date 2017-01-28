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

public abstract class FilterTransformerTypePlugin<C> extends ClientPlugin {

    public FilterTransformerTypePlugin(String name) {
        super(name);
    }

    public abstract boolean isNameEditable();

    public abstract C newObject(String variable, String mapping);

    public abstract EditorPanel<C> getPanel();

    public boolean includesScrollPane() {
        return false;
    }

    public C getDefaults() {
        return getPanel().getDefaults();
    }

    public C getProperties() {
        return getPanel().getProperties();
    }

    public void setProperties(Mode mode, boolean response, C properties) {
        getPanel().resetInvalidProperties();
        getPanel().setProperties(properties);
    }

    public String checkProperties(C properties, boolean highlight) {
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