package com.mirth.connect.plugins.datatypes.x12;

import java.util.Map;

import com.mirth.connect.donkey.model.message.AutoResponder;
import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.DefaultAutoResponder;
import com.mirth.connect.plugins.DataTypeServerPlugin;

public class X12DataTypeServerPlugin extends DataTypeServerPlugin {
    private DataTypeDelegate dataTypeDelegate = new X12DataTypeDelegate();

    @Override
    public String getPluginPointName() {
        return dataTypeDelegate.getName();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public AutoResponder getAutoResponder(Map<?, ?> properties) {
        return new DefaultAutoResponder();
    }

    @Override
    public boolean isStripNamespaces(Map<?, ?> properties) {
        return false;
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }
}
