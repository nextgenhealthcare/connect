package com.mirth.connect.plugins.datatypes.hl7v2;

import com.mirth.connect.donkey.model.message.AutoResponder;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
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
    public AutoResponder getAutoResponder(SerializationProperties serializationProperties, ResponseGenerationProperties generationProperties) {
        return new HL7v2AutoResponder(serializationProperties, generationProperties);
    }

    @Override
    public boolean isStripNamespaces(SerializerProperties properties) {
        boolean stripNamespaces = false;
        
        HL7v2SerializationProperties hl7Properties = (HL7v2SerializationProperties) properties.getSerializationProperties();
        
        if (hl7Properties.isUseStrictParser() && hl7Properties.isStripNamespaces()) {
            stripNamespaces = true;
        }
        
        return stripNamespaces;
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

}
