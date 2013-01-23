package com.mirth.connect.plugins.datatypes.hl7v2;

import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.TokenMarker;

import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.util.MessageVocabulary;
import com.mirth.connect.plugins.DataTypeClientPlugin;

public class HL7v2DataTypeClientPlugin extends DataTypeClientPlugin {
    private DataTypeDelegate dataTypeDelegate = new HL7v2DataTypeDelegate();
    
    public HL7v2DataTypeClientPlugin(String name) {
        super(name);
    }

    @Override
    public String getDisplayName() {
        return "HL7 v2.x";
    }

    @Override
    public AttachmentHandlerType getDefaultAttachmentHandlerType() {
        return null;
    }

    @Override
    public TokenMarker getTokenMarker() {
        return new HL7TokenMarker();
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
        return HL7v2Vocabulary.class;
    }

    @Override
    public String getTemplateString(byte[] content) {
        return null;
    }

    @Override
    public int getMinTreeLevel() {
        return 2;
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

    @Override
    public DataTypeProperties getDefaultProperties() {
        return new HL7v2DataTypeProperties();
    }

}
