package com.webreach.mirth.plugins;

import java.util.Map;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;

public abstract class MirthEditorPanePlugin extends ClientPlugin {

    protected MirthEditorPane parent;
    protected boolean provideOwnStepName = false;

    public MirthEditorPanePlugin(String name) {
        super(name);
    }

    public MirthEditorPanePlugin(String name, MirthEditorPane parent) {
        super(name);
        this.parent = parent;
    }

    public String getPluginName() {
        return name;
    }

    public abstract BasePanel getPanel();

    public abstract boolean isNameEditable();

    public String getNewName() {
        return new String();
    }

    public abstract String getDisplayName();

    public abstract Map<Object, Object> getData(int row);

    public abstract void setData(Map<Object, Object> data);

    public String getName() {
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
}
