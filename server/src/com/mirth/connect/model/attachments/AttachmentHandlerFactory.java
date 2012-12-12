/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.attachments;

import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.model.util.JavaScriptConstants;

public class AttachmentHandlerFactory {
    
    public static AttachmentHandler getAttachmentHandler(AttachmentHandlerProperties attachmentProperties) throws Exception {
        AttachmentHandler attachmentHandler = null;
        
        if (AttachmentHandlerType.fromString(attachmentProperties.getType()) != AttachmentHandlerType.NONE) {
            attachmentHandler = (AttachmentHandler) Class.forName(attachmentProperties.getClassName()).newInstance();
            attachmentHandler.setProperties(attachmentProperties);
        }
        
        return attachmentHandler;
    }
    
    public static AttachmentHandlerProperties getDefaults(AttachmentHandlerType type) {
        AttachmentHandlerProperties properties = new AttachmentHandlerProperties(AttachmentHandlerType.getDefaultClassName(type), type.toString());
        
        if (type == AttachmentHandlerType.REGEX) {
            properties.getProperties().put("regex.pattern", "");
            properties.getProperties().put("regex.mimetype", "");
        } else if (type == AttachmentHandlerType.JAVASCRIPT) {
            properties.getProperties().put("javascript.script", JavaScriptConstants.DEFAULT_CHANNEL_ATTACHMENT_SCRIPT);
        }
        
        return properties;
    }
}
