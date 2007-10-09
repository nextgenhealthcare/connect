package com.webreach.mirth.server.util;

import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DICOMSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Created by IntelliJ IDEA.
 * User: dans
 * Date: Oct 4, 2007
 * Time: 10:42:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMUtil {
    public static String getDICOMRawData(MessageObject message) {
        if(message.isAttachment()){
            MessageObjectController mos = MessageObjectController.getInstance();
            ArrayList images = new ArrayList();
            try {
                List<Attachment> attachments = null;
                if(message.getCorrelationId() != null)
                    attachments = mos.getAttachmentsByMessageId(message.getCorrelationId());
                else
                    attachments = mos.getAttachmentsByMessageId(message.getId());
                return mergeHeaderAttachments(message, attachments);
            }
            catch (Exception e){
                e.printStackTrace();
                return message.getRawData();
            }
        }
        else {
            return message.getRawData();
        }
    }
    public static String mergeHeaderAttachments(MessageObject message, List attachments) throws IOException{
        ArrayList images = new ArrayList();
        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder encoder = new BASE64Encoder();
        Iterator i = attachments.iterator();
        while(i.hasNext()){
            Attachment attach = (Attachment) i.next();
            byte[] image = decoder.decodeBuffer(new String(attach.getData()));
            images.add(image);
        }
        byte[] headerData = decoder.decodeBuffer(message.getRawData());
        if(images.size() <= 1){
            byte[] pixelData = (byte[]) images.get(0);
            String dicomString = DICOMSerializer.mergeHeaderPixelData(headerData,pixelData);
            return dicomString;
        }
        else {
            String dicomString = DICOMSerializer.mergeHeaderPixelData(headerData,images);
            return dicomString;
        }
    }
}
