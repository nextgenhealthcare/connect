package com.webreach.mirth.server.util;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.ControllerFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Apr 9, 2009
 * Time: 2:45:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentUtil {
    public static String reAttachMessage(MessageObject message){
        String messageData = message.getEncodedData();
        if(messageData == null || messageData.equals("")){
            messageData = message.getRawData();
        }
        MessageObjectController mos = ControllerFactory.getFactory().createMessageObjectController();
        try {
            List<Attachment> list  = getMessageAttachments(message); 
            for(Attachment attachment : list){
                messageData = messageData.replaceAll(attachment.getAttachmentId(),new String(attachment.getData()));
            }
        }
        catch(Exception e){
            e.printStackTrace();
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
