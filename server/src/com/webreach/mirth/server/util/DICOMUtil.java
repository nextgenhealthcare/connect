package com.webreach.mirth.server.util;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DICOMSerializer;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.server.controllers.MessageObjectController;
import ij.plugin.DICOM;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: 
 * Date: Oct 4, 2007
 * Time: 10:42:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMUtil {
    public static String getDICOMRawData(MessageObject message) {
        if(message.isAttachment()){
            MessageObjectController mos = MessageObjectController.getInstance();
            try {
                List<Attachment> attachments = null;
                if(message.getCorrelationId() != null)
                    attachments = mos.getAttachmentsByMessageId(message.getCorrelationId());
                else
                    attachments = mos.getAttachmentsByMessageId(message.getId());
                if(attachments.get(0).getType().equals("DICOM")){
                    return mergeHeaderAttachments(message, attachments);
                }
                else {
                    return message.getRawData();
                }
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
    
    public static byte[] getDICOMMessage(MessageObject message){
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            return decoder.decodeBuffer(getDICOMRawData(message));    
        }
        catch (IOException ie){
            ie.printStackTrace();
        }
        return message.getRawData().getBytes();
    }
    
    public static String mergeHeaderAttachments(MessageObject message, List<Attachment> attachments) throws IOException, SerializerException {
        ArrayList<byte[]> images = new ArrayList();
        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder encoder = new BASE64Encoder();
        Iterator<Attachment> i = attachments.iterator();
        while(i.hasNext()){
            Attachment attach = i.next();
            byte[] image = decoder.decodeBuffer(new String(attach.getData()));
            images.add(image);
        }   
        byte[] headerData;
        if(message.getRawDataProtocol().equals(MessageObject.Protocol.DICOM)) {
            headerData = decoder.decodeBuffer(message.getRawData());
        }
        else if(message.getEncodedDataProtocol().equals(MessageObject.Protocol.DICOM)) {
            headerData = decoder.decodeBuffer(message.getEncodedData());
        }
        else {
            return "";
        }
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
    public static List<Attachment> getMessageAttachments(MessageObject message) throws SerializerException {
        if(message.isAttachment()){
            MessageObjectController mos = MessageObjectController.getInstance();
            try {
                List<Attachment> attachments = null;
                if(message.getCorrelationId() != null)
                    attachments = mos.getAttachmentsByMessageId(message.getCorrelationId());
                else
                    attachments = mos.getAttachmentsByMessageId(message.getId());
                return attachments;
            }
            catch (Exception e){
                throw new SerializerException(e.getMessage());
            }
        }
        return null;
    }
    
    public static String convertDICOM(String imageType,MessageObject message){
            return returnOther(message, imageType);
    }

    public static String returnOther(MessageObject message, String format)
    {
        String encodedData = getDICOMRawData(message);
        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder base64Encoder = new BASE64Encoder();           
        try {
            byte[] rawImage = decoder.decodeBuffer(encodedData);
            ByteArrayInputStream bis = new ByteArrayInputStream(rawImage);
            DICOM dcm = new DICOM(bis);
            dcm.run(message.getType());
            int width = dcm.getWidth();
            int height = dcm.getHeight();
            BufferedImage   bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream  f  = new ByteArrayOutputStream();
            Graphics g = bi.createGraphics();
            g.drawImage(dcm.getImage(), 0, 0, null);
            g.dispose();            
            ImageIO.write(bi,format,f);
            return base64Encoder.encode(f.toByteArray());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }    
    
    public static String reAttachMessage(MessageObject message){
        String messageData = message.getEncodedData();
        MessageObjectController mos = MessageObjectController.getInstance();
        try {
            List<Attachment> list  = mos.getAttachmentsByMessageId(message.getCorrelationId());
            Iterator<Attachment> a = list.iterator();
            while(a.hasNext()){
                Attachment attachment = a.next();
                messageData = messageData.replaceAll(attachment.getAttachmentId(),new String(attachment.getData()));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return messageData;
    }
    
}
