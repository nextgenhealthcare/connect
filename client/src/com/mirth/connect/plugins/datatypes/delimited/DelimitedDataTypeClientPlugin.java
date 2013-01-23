package com.mirth.connect.plugins.datatypes.delimited;

import org.syntax.jedit.tokenmarker.TokenMarker;

import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.util.MessageVocabulary;
import com.mirth.connect.plugins.DataTypeClientPlugin;

public class DelimitedDataTypeClientPlugin extends DataTypeClientPlugin {
    private DataTypeDelegate dataTypeDelegate = new DelimitedDataTypeDelegate();

    public DelimitedDataTypeClientPlugin(String name) {
        super(name);
    }

    @Override
    public String getDisplayName() {
        return "Delimited Text";
    }

    @Override
    public AttachmentHandlerType getDefaultAttachmentHandlerType() {
        return null;
    }

    @Override
    public TokenMarker getTokenMarker() {
        return null;
    }

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
    public void reset() {
    }

    @Override
    public Class<? extends MessageVocabulary> getVocabulary() {
        return null;
    }

    @Override
    public String getTemplateString(byte[] content) {
        return null;
    }

    @Override
    public int getMinTreeLevel() {
        return 0;
    }
    
    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

    @Override
    public DataTypeProperties getDefaultProperties() {
        return new DelimitedDataTypeProperties();
    }

}
