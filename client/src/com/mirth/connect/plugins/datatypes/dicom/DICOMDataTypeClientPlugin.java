package com.mirth.connect.plugins.datatypes.dicom;

import org.syntax.jedit.tokenmarker.TokenMarker;
import org.w3c.dom.Element;

import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.util.MessageVocabulary;
import com.mirth.connect.plugins.DataTypeClientPlugin;

public class DICOMDataTypeClientPlugin extends DataTypeClientPlugin {
    private DataTypeDelegate dataTypeDelegate = new DICOMDataTypeDelegate();

    public DICOMDataTypeClientPlugin(String name) {
        super(name);
    }

    @Override
    public String getDisplayName() {
        return "DICOM";
    }

    @Override
    public Object getBeanProperties() {
        return null;
    }

    @Override
    public AttachmentHandlerType getDefaultAttachmentHandlerType() {
        return AttachmentHandlerType.DICOM;
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
        return DICOMVocabulary.class;
    }

    @Override
    public String getTemplateString(byte[] content) throws Exception {
        content = DICOMSerializer.removePixelData(content);
        return new DICOMSerializer().toXML(new String(Base64Util.encodeBase64(content)));
    }

    @Override
    public int getMinTreeLevel() {
        return 0;
    }

    @Override
    public String getNodeText(MessageVocabulary vocabulary, Element element) {
        String description = vocabulary.getDescription(element.getAttribute("tag"));
        if (description.equals("?")) {
            description = "";
        }

        String nodeText;
        if (description != null && description.length() > 0) {
            nodeText = "tag" + element.getAttribute("tag") + " (" + description + ")";
        } else {
            nodeText = element.getNodeName();
        }

        return nodeText;
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

}
