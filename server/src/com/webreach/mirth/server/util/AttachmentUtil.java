package com.webreach.mirth.server.util;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
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
