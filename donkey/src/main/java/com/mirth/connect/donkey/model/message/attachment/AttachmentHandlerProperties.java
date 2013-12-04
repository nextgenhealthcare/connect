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

public class AttachmentHandlerProperties implements Serializable {

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
}
