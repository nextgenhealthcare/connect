package com.mirth.connect.plugins.datatypes.hl7v3;

import org.syntax.jedit.tokenmarker.TokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.util.MessageVocabulary;
import com.mirth.connect.plugins.DataTypeClientPlugin;

public class HL7V3DataTypeClientPlugin extends DataTypeClientPlugin {
    private DataTypeDelegate dataTypeDelegate = new HL7V3DataTypeDelegate();

    public HL7V3DataTypeClientPlugin(String name) {
        super(name);
    }

    @Override
    public String getDisplayName() {
        return "HL7 v3.x";
    }

    @Override
    public AttachmentHandlerType getDefaultAttachmentHandlerType() {
        return null;
    }

    @Override
    public TokenMarker getTokenMarker() {
        return new XMLTokenMarker();
    }

    @Override
    public Class<? extends MessageVocabulary> getVocabulary() {
        return null;
    }

    @Override
    public String getTemplateString(byte[] content) throws Exception {
        return null;
    }

    @Override
    public int getMinTreeLevel() {
        return 0;
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
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

    @Override
    public DataTypeProperties getDefaultProperties() {
        return new HL7V3DataTypeProperties();
    }
}
