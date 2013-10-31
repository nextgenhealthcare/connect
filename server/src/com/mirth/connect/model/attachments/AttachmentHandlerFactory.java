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
import com.mirth.connect.server.attachments.MirthAttachmentHandler;
import com.mirth.connect.server.attachments.PassthruAttachmentHandler;

public class AttachmentHandlerFactory {
    
    public static MirthAttachmentHandler getAttachmentHandler(AttachmentHandlerProperties attachmentProperties) throws Exception {
        if (AttachmentHandlerType.fromString(attachmentProperties.getType()) != AttachmentHandlerType.NONE) {
            Class<?> attachmentHandlerClass = Class.forName(attachmentProperties.getClassName());

            if (MirthAttachmentHandler.class.isAssignableFrom(attachmentHandlerClass)) {
                MirthAttachmentHandler attachmentHandler = (MirthAttachmentHandler) Class.forName(attachmentProperties.getClassName()).newInstance();
                attachmentHandler.setProperties(attachmentProperties);
                return attachmentHandler;
            } else {
                throw new Exception(attachmentProperties.getClassName() + " does not extend " + MirthAttachmentHandler.class.getName());
            }
        }

        return new PassthruAttachmentHandler();
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
