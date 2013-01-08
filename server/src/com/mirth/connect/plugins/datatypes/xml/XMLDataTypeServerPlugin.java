package com.mirth.connect.plugins.datatypes.xml;

import java.util.Map;

import com.mirth.connect.donkey.model.message.AutoResponder;
import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.DefaultAutoResponder;
import com.mirth.connect.plugins.DataTypeServerPlugin;

public class XMLDataTypeServerPlugin extends DataTypeServerPlugin {
    private DataTypeDelegate dataTypeDelegate = new XMLDataTypeDelegate();

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
        boolean stripNamespaces = false;
        
        // If the property has been set, use it, otherwise use the default true
        if ((properties != null) && (properties.get("stripNamespaces") != null)) {
            stripNamespaces = Boolean.parseBoolean((String) properties.get("stripNamespaces"));
        } else {
            stripNamespaces = true;
        }
        
        return stripNamespaces;
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }
}
