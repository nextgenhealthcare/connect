/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.attachments;

import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.model.util.JavaScriptConstants;

public enum AttachmentHandlerType {
    NONE("None"), IDENTITY("Entire Message"), REGEX("Regex"), DICOM("DICOM"), JAVASCRIPT(
            "JavaScript"), CUSTOM("Custom");

    private String type;

    private AttachmentHandlerType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }

    public AttachmentHandlerProperties getDefaultProperties() {
        AttachmentHandlerProperties properties = new AttachmentHandlerProperties(this.getDefaultClassName(), type.toString());

        if (this == AttachmentHandlerType.REGEX) {
            properties.getProperties().put("regex.pattern0", "");
            properties.getProperties().put("regex.mimetype0", "");
        } else if (this == AttachmentHandlerType.JAVASCRIPT) {
            properties.getProperties().put("javascript.script", JavaScriptConstants.DEFAULT_CHANNEL_ATTACHMENT_SCRIPT);
        }

        return properties;
    }

    public String getDefaultClassName() {
        if (this == NONE) {
            return null;
        } else if (this == IDENTITY) {
            return "com.mirth.connect.server.attachments.identity.IdentityAttachmentHandlerProvider";
        } else if (this == REGEX) {
            return "com.mirth.connect.server.attachments.regex.RegexAttachmentHandlerProvider";
        } else if (this == DICOM) {
            return "com.mirth.connect.server.attachments.dicom.DICOMAttachmentHandlerProvider";
        } else if (this == JAVASCRIPT) {
            return "com.mirth.connect.server.attachments.javascript.JavaScriptAttachmentHandlerProvider";
        } else if (this == CUSTOM) {
            return "";
        }

        return null;
    }

    public static AttachmentHandlerType fromString(String type) {
        if (type.equals("None")) {
            return NONE;
        } else if (type.equals("Entire Message")) {
            return IDENTITY;
        } else if (type.equals("Regex")) {
            return REGEX;
        } else if (type.equals("DICOM")) {
            return DICOM;
        } else if (type.equals("JavaScript")) {
            return JAVASCRIPT;
        } else if (type.equals("Custom")) {
            return CUSTOM;
        }

        return null;
    }
}
