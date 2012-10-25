/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.converters.SerializerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import org.apache.log4j.Logger;

import java.util.List;

public class AttachmentUtil {

    private static Logger logger = Logger.getLogger(AttachmentUtil.class);

    public static String reAttachMessage(MessageObject message){
        String messageData = message.getEncodedData();
        if(messageData == null || messageData.equals("")){
            messageData = message.getRawData();
        }
        try {
            List<Attachment> list  = getMessageAttachments(message);
            if(list != null){
                for(Attachment attachment : list){
	                // backslash escaping - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4626653
                    messageData = messageData.replaceAll(attachment.getAttachmentId(), new String(attachment.getData()).replaceAll("\\\\", "\\\\\\\\"));
                }
            }
        }
        catch(Exception e){
            logger.error("Error reattaching attachments",e);
        }
        return messageData;
    }

    public static String reAttachRawMessage(MessageObject message){
        String messageData = message.getRawData();
        try {
            List<Attachment> list  = getMessageAttachments(message);
            if(list != null){
                for(Attachment attachment : list){
                    // backslash escaping - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4626653
                    messageData = messageData.replaceAll(attachment.getAttachmentId(), new String(attachment.getData()).replaceAll("\\\\", "\\\\\\\\"));
                }
            }
        }
        catch(Exception e){
            logger.error("Error reattaching attachments",e);
        }
        return messageData;
    }
    
    public static List<Attachment> getMessageAttachments(MessageObject message) throws SerializerException {
        List<Attachment> attachments = null;
        if(message.isAttachment()){
            MessageObjectController mos = ControllerFactory.getFactory().createMessageObjectController();
            try {
                if(message.getCorrelationId() != null)
                    attachments = mos.getAttachmentsByMessageId(message.getCorrelationId());
                else
                    attachments = mos.getAttachmentsByMessageId(message.getId());
            }
            catch (Exception e){
                throw new SerializerException(e.getMessage());
            }
        }
        return attachments;
    }
}
