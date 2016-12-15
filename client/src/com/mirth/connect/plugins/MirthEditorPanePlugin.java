/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.Map;

import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.model.Connector.Mode;

public abstract class MirthEditorPanePlugin extends ClientPlugin {

    protected boolean provideOwnStepName = false;

    public MirthEditorPanePlugin(String name) {
        super(name);
    }

    public abstract BasePanel getBasePanel();

    public abstract boolean isNameEditable();

    public String getNewName() {
        return new String();
    }

    public abstract Map<Object, Object> getData(int row);

    public abstract void setData(Mode mode, Map<Object, Object> data);

    public abstract String getGeneratedScript(Map<Object, Object> data);

    public String getStepName() {
        return null;
    }

    public String doValidate(Map<Object, Object> data) {
        return null;
    }

    public boolean showValidateTask() {
        return false;
    }

    public abstract String getScript(Map<Object, Object> data);

    public abstract void clearData();

    public abstract void initData();

    public boolean isProvideOwnStepName() {
        return provideOwnStepName;
    }

    public void moveStart() {}

    public void moveEnd() {}
}
