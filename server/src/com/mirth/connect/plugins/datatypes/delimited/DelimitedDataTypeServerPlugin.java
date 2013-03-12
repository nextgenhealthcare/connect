package com.mirth.connect.plugins.datatypes.delimited;

import com.mirth.connect.donkey.server.message.AutoResponder;
import com.mirth.connect.donkey.server.message.BatchAdaptor;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.message.DefaultAutoResponder;

public class DelimitedDataTypeServerPlugin extends DataTypeServerPlugin {
    private DataTypeDelegate dataTypeDelegate = new DelimitedDataTypeDelegate();

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
        return new DefaultAutoResponder();
    }
    
    @Override
    public BatchAdaptor getBatchAdaptor(SerializerProperties properties) {
    	return new DelimitedBatchAdaptor(properties);
    }

    @Override
    public boolean isStripNamespaces(SerializerProperties properties) {
        return false;
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

    @Override
    public String getBatchScript(BatchAdaptor batchAdaptor) {
    	DelimitedBatchAdaptor delimitedBatchAdaptor = (DelimitedBatchAdaptor) batchAdaptor;
        return delimitedBatchAdaptor.getBatchScript();
    }
}
