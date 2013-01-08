package com.mirth.connect.plugins.datatypes.hl7v2;

import java.util.Map;

import com.mirth.connect.donkey.model.message.AutoResponder;
import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.plugins.DataTypeServerPlugin;

public class HL7v2DataTypeServerPlugin extends DataTypeServerPlugin {
    private DataTypeDelegate dataTypeDelegate = new HL7v2DataTypeDelegate();

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
        return new HL7v2AutoResponder(properties);
    }

    @Override
    public boolean isStripNamespaces(Map<?, ?> properties) {
        boolean stripNamespaces = false;
        
        if ((properties != null) && Boolean.parseBoolean((String) properties.get("useStrictParser"))) {
            // If the property has been set, use it, otherwise use the default true
            if (properties.get("stripNamespaces") != null) {
                stripNamespaces = Boolean.parseBoolean((String) properties.get("stripNamespaces"));
            } else {
                stripNamespaces = true;
            }
        }
        
        return stripNamespaces;
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

}
