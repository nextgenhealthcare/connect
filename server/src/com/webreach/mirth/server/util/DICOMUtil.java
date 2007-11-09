package com.webreach.mirth.server.util;

import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DICOMSerializer;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGCodec;
import javax.imageio.plugins.bmp.BMPImageWriteParam;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.*;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import ij.ImagePlus;
import ij.IJ;
import ij.Prefs;
import ij.io.TiffEncoder;
import ij.io.FileInfo;
import ij.io.ImageWriter;
import ij.plugin.DICOM;
import ij.plugin.JpegWriter;
import ij.plugin.BMP_Writer;

import javax.imageio.ImageIO;

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
    public static List<Attachment> getMessageAttachments(MessageObject message){
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
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public static String convertDICOM(String imageType,MessageObject message, int quality){
        if(imageType != null && (imageType.toUpperCase().equals("JPEG") || imageType.toUpperCase().equals("JPG"))){
            return getDICOMasJPEG(message, quality);
        }
        else if(imageType != null && imageType.toUpperCase().startsWith("TIF")){
            return returnAsTiff(message);
        }
        else if(imageType != null && imageType.toUpperCase().startsWith("RAW")){
            return saveRaw(message);
        }
        else if(imageType != null && imageType.toUpperCase().startsWith("BMP")){
            return returnAsBMP(message);
        }
        IJ.error("Incorrect message type selected");
        return "";
    }

    public static String getDICOMasJPEG(MessageObject message, int quality){
        //IJ.log("saveAsJpeg: "+path);
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
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(f);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
            param.setQuality((float)(quality/100.0), true);
            encoder.encode(bi, param);
            f.close();
            return base64Encoder.encode(f.toByteArray());
            //return new String(f.toByteArray());
        }
        catch (Exception e) {
           IJ.error("getDICOMasJPEG", ""+e);
        }

        return null;
    }

    public static String returnAsTiff(MessageObject message)
    {
        String encodedData = getDICOMRawData(message);
        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder base64Encoder = new BASE64Encoder();           
        try {
            byte[] rawImage = decoder.decodeBuffer(encodedData);
            ByteArrayInputStream bis = new ByteArrayInputStream(rawImage);
            DICOM dcm = new DICOM(bis);
            dcm.run(message.getType());
            TiffEncoder file = new TiffEncoder(dcm.getFileInfo());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);
            file.write(out);
            out.close();
            return base64Encoder.encode(bos.toByteArray());
        }
        catch (IOException e) {
            IJ.error("getDICOMasTIFF", ""+e);
        }
        return "";
    }

    
    public static String saveRaw(MessageObject message) {
        String encodedData = getDICOMRawData(message);
        BASE64Decoder decoder = new BASE64Decoder();
        BASE64Encoder base64Encoder = new BASE64Encoder();
        try {
            byte[] rawImage = decoder.decodeBuffer(encodedData);
            ByteArrayInputStream bis = new ByteArrayInputStream(rawImage); 
            DICOM dcm = new DICOM(bis);
            dcm.run(message.getType());
            if(dcm.getStackSize() == 1)
                return base64Encoder.encode(saveAsRaw(dcm));
            else
                return base64Encoder.encode(saveAsRawStack(dcm));
        }
        catch (Exception e) {
            IJ.error("getRaw", ""+e);
        }  
        return "";
    }
    
    /** Save the image as raw data using the specified path. */
    /** Save the image as raw data using the specified path. */
    public static byte[] saveAsRaw(ImagePlus imp) {
        FileInfo fi = imp.getFileInfo();
        fi.nImages = 1;
        fi.intelByteOrder = Prefs.intelByteOrder;
        boolean signed16Bit = false;
        short[] pixels = null;
        int n = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            signed16Bit = imp.getCalibration().isSigned16Bit();
            if (signed16Bit) {
                pixels = (short[])imp.getProcessor().getPixels();
                n = imp.getWidth()*imp.getHeight();
                for (int i=0; i<n; i++)
                    pixels[i] = (short)(pixels[i]-32768);
            }
            ImageWriter file = new ImageWriter(fi);
            file.write(out);
            out.close();
        }
        catch (IOException e) {
            IJ.error("getRaw", ""+e);            
        }
        if (signed16Bit) {
            for (int i=0; i<n; i++)
            pixels[i] = (short)(pixels[i]+32768);
        }
        return out.toByteArray();
    }

    /** Save the stack as raw data using the specified path. */
    public static byte[] saveAsRawStack(ImagePlus imp) {
        FileInfo fi = imp.getFileInfo();
        fi.intelByteOrder = Prefs.intelByteOrder;
        boolean signed16Bit = false;
        Object[] stack = null;
        int n = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            signed16Bit = imp.getCalibration().isSigned16Bit();
            if (signed16Bit) {
                stack = (Object[])fi.pixels;
                n = imp.getWidth()*imp.getHeight();
                for (int slice=0; slice<fi.nImages; slice++) {
                    short[] pixels = (short[])stack[slice];
                    for (int i=0; i<n; i++)
                        pixels[i] = (short)(pixels[i]-32768);
                }
            }
            ImageWriter file = new ImageWriter(fi);

            file.write(out);
            out.close();
        }
        catch (IOException e) {
            IJ.error("getRaw", ""+e);            
        }
        if (signed16Bit) {
            for (int slice=0; slice<fi.nImages; slice++) {
                short[] pixels = (short[])stack[slice];
                for (int i=0; i<n; i++)
                    pixels[i] = (short)(pixels[i]+32768);
            }
        }
        return out.toByteArray();
    }

    public static String returnAsBMP(MessageObject message)
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

            ImageIO.write(bi,"bmp",f);
            return base64Encoder.encode(f.toByteArray());
        }
        catch (IOException e) {
            IJ.error("returnAsBMP", ""+e);
        }
        return "";
    }    
}
