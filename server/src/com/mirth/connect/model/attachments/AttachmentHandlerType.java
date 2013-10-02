/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.attachments;

public enum AttachmentHandlerType {
    NONE("None"), REGEX("Regex"), DICOM("DICOM"), JAVASCRIPT("JavaScript"), CUSTOM(
            "Custom");

    private String type;

    private AttachmentHandlerType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }

    public static String getDefaultClassName(AttachmentHandlerType type) {
        if (type == NONE) {
            return null;
        } else if (type == REGEX) {
            return "com.mirth.connect.server.attachments.RegexAttachmentHandler";
        } else if (type == DICOM) {
            return "com.mirth.connect.server.attachments.DICOMAttachmentHandler";
        } else if (type == JAVASCRIPT) {
            return "com.mirth.connect.server.attachments.JavaScriptAttachmentHandler";
        } else if (type == CUSTOM) {
            return "";
        }

        return null;
    }

    public static AttachmentHandlerType fromString(String type) {
        if (type.equals("None")) {
            return NONE;
        } else if (type.equals("Regex")) {
            return REGEX;
        } else if (type.equals("DICOM")) {
            return DICOM;
        } else if (type.equals("JavaScript")) {
            return JAVASCRIPT;
        } else if (type.equals("Custom")){
            return CUSTOM;
        }
        
        return null;
    }
}
