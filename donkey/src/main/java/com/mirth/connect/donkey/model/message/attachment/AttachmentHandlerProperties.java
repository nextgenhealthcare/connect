/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message.attachment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;

public class AttachmentHandlerProperties implements Serializable, Migratable, Purgable {

    private String className;
    private String type;
    private Map<String, String> properties = new HashMap<String, String>();

    public AttachmentHandlerProperties(String className, String typeName) {
        this.className = className;
        this.type = typeName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof AttachmentHandlerProperties) {
            AttachmentHandlerProperties attachmentHandlerProperties = (AttachmentHandlerProperties) object;
            if (className == null || attachmentHandlerProperties.getClassName() == null) {
                if (className == null && attachmentHandlerProperties.getClassName() == null) {
                    return true;
                }
            } else if (className.equals(attachmentHandlerProperties.getClassName()) && type.equals(attachmentHandlerProperties.getType()) && properties.equals(attachmentHandlerProperties.getProperties())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("type", type);
        return purgedProperties;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {
        DonkeyElement classNameElement = element.getChildElement("className");
        if (classNameElement != null) {
            String className = classNameElement.getTextContent();

            if (StringUtils.equals(className, "com.mirth.connect.server.attachments.DICOMAttachmentHandler")) {
                classNameElement.setTextContent("com.mirth.connect.server.attachments.dicom.DICOMAttachmentHandlerProvider");
            } else if (StringUtils.equals(className, "com.mirth.connect.server.attachments.JavaScriptAttachmentHandler")) {
                classNameElement.setTextContent("com.mirth.connect.server.attachments.javascript.JavaScriptAttachmentHandlerProvider");
            } else if (StringUtils.equals(className, "com.mirth.connect.server.attachments.PassthruAttachmentHandler")) {
                classNameElement.setTextContent("com.mirth.connect.server.attachments.passthru.PassthruAttachmentHandlerProvider");
            } else if (StringUtils.equals(className, "com.mirth.connect.server.attachments.RegexAttachmentHandler")) {
                classNameElement.setTextContent("com.mirth.connect.server.attachments.regex.RegexAttachmentHandlerProvider");
            }
        }
    }
    
    @Override
    public void migrate3_5_0(DonkeyElement element) {}
    
    @Override
    public void migrate3_6_0(DonkeyElement element) {}
}
