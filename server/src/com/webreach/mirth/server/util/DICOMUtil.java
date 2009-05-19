package com.webreach.mirth.server.util;

import ij.plugin.DICOM;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DICOMSerializer;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: 
 * Date: Oct 4, 2007
 * Time: 10:42:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMUtil {
    private static Logger logger = Logger.getLogger(AttachmentUtil.class);
    public static String getDICOMRawData(MessageObject message) {
        String mergedMessage;
        if(message.isAttachment()){
            MessageObjectController mos = ControllerFactory.getFactory().createMessageObjectController();
            try {
                List<Attachment> attachments = null;
                if(message.getCorrelationId() != null)
                    attachments = mos.getAttachmentsByMessageId(message.getCorrelationId());
                else
                    attachments = mos.getAttachmentsByMessageId(message.getId());
                if(attachments.get(0).getType().equals("DICOM")){
                    mergedMessage = mergeHeaderAttachments(message, attachments);
                }
                else {
                    mergedMessage = message.getRawData();
                }
            }
            catch (Exception e){
                logger.error("Error merging DICOM data", e);
                mergedMessage = message.getRawData();
            }
        }
        else {
            mergedMessage = message.getRawData();
        }
        return mergedMessage;
    }
    
    public static byte[] getDICOMMessage(MessageObject message){
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            return decoder.decodeBuffer(getDICOMRawData(message));    
        }
        catch (IOException ie){
            logger.error("Error getting DICOM message", ie);
        }
        return message.getRawData().getBytes();
    }
    
    public static String mergeHeaderAttachments(MessageObject message, List<Attachment> attachments) throws IOException, SerializerException {
        ArrayList<byte[]> images = new ArrayList();
        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder encoder = new BASE64Encoder();
        for(Attachment attach : attachments){
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
        return DICOMSerializer.mergeHeaderPixelData(headerData,images);

    }
    public static List<Attachment> getMessageAttachments(MessageObject message) throws SerializerException {
        return AttachmentUtil.getMessageAttachments(message);
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
            logger.error("Error Converting DICOM image", e);
        }
        return "";
    }    
    
    public static String reAttachMessage(MessageObject message){
        return AttachmentUtil.reAttachMessage(message);
    }
    
}
