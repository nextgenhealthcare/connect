package com.mirth.connect.plugins.datatypes.ncpdp;

import org.syntax.jedit.tokenmarker.NCPDPTokenMarker;
import org.syntax.jedit.tokenmarker.TokenMarker;

import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.util.MessageVocabulary;
import com.mirth.connect.plugins.DataTypeClientPlugin;

public class NCPDPDataTypeClientPlugin extends DataTypeClientPlugin {
    private DataTypeDelegate dataTypeDelegate = new NCPDPDataTypeDelegate();

    public NCPDPDataTypeClientPlugin(String name) {
        super(name);
    }

    @Override
    public String getDisplayName() {
        return "NCPDP";
    }

    @Override
    public Object getBeanProperties() {
        return new NCPDPProperties();
    }

    @Override
    public AttachmentHandlerType getDefaultAttachmentHandlerType() {
        return null;
    }

    @Override
    public TokenMarker getTokenMarker() {
        return new NCPDPTokenMarker();
    }

    @Override
    public Class<? extends MessageVocabulary> getVocabulary() {
        return NCPDPVocabulary.class;
    }

    @Override
    public String getTemplateString(byte[] content) throws Exception {
        return null;
    }

    @Override
    public int getMinTreeLevel() {
        return 2;
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
}
