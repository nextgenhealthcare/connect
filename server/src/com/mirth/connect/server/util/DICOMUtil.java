/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.DICOM;
import ij.process.ImageProcessor;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.converters.DICOMSerializer;
import com.mirth.connect.model.converters.SerializerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;

public class DICOMUtil {
    private static Logger logger = Logger.getLogger(AttachmentUtil.class);

    public static String getDICOMRawData(MessageObject message) {
        String mergedMessage;
        if (message.isAttachment()) {
            MessageObjectController mos = ControllerFactory.getFactory().createMessageObjectController();
            try {
                List<Attachment> attachments = null;
                if (message.getCorrelationId() != null)
                    attachments = mos.getAttachmentsByMessageId(message.getCorrelationId());
                else
                    attachments = mos.getAttachmentsByMessageId(message.getId());
                if (attachments.get(0).getType().equals("DICOM")) {
                    mergedMessage = mergeHeaderAttachments(message, attachments);
                } else {
                    mergedMessage = message.getRawData();
                }
            } catch (Exception e) {
                logger.error("Error merging DICOM data", e);
                mergedMessage = message.getRawData();
            }
        } else {
            mergedMessage = message.getRawData();
        }
        return mergedMessage;
    }

    public static byte[] getDICOMMessage(MessageObject message) {
        return new Base64().decode(getDICOMRawData(message).getBytes());
    }

    public static String mergeHeaderAttachments(MessageObject message, List<Attachment> attachments) throws SerializerException {
        ArrayList<byte[]> images = new ArrayList<byte[]>();
        Base64 base64 = new Base64();
        for (Attachment attach : attachments) {
            images.add(base64.decode(attach.getData()));
        }
        byte[] headerData;
        if (message.getEncodedDataProtocol().equals(MessageObject.Protocol.DICOM)) {
            headerData = base64.decode(message.getEncodedData().getBytes());
        } else if (message.getRawDataProtocol().equals(MessageObject.Protocol.DICOM)) {
            headerData = base64.decode(message.getRawData().getBytes());
        } else {
            return "";
        }
        return DICOMSerializer.mergeHeaderPixelData(headerData, images);

    }

    public static List<Attachment> getMessageAttachments(MessageObject message) throws SerializerException {
        return AttachmentUtil.getMessageAttachments(message);
    }

    public static String convertDICOM(String imageType, MessageObject message, boolean autoThreshold) {
        return returnOtherImageFormat(message, imageType, autoThreshold);
    }

    public static String convertDICOM(String imageType, MessageObject message) {
        return returnOtherImageFormat(message, imageType, false);
    }

    private static String returnOtherImageFormat(MessageObject message, String format, boolean autoThreshold) {
        String encodedData = getDICOMRawData(message);
        Base64 base64 = new Base64();

        // use new method for jpegs
        if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg"))
            return new String(base64.encode(dicomToJpg(1, message, autoThreshold)));
        try {
            byte[] rawImage = base64.decode(encodedData.getBytes());
            ByteArrayInputStream bis = new ByteArrayInputStream(rawImage);
            DICOM dcm = new DICOM(bis);
            dcm.run(message.getType());

            int width = dcm.getWidth();
            int height = dcm.getHeight();
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream f = new ByteArrayOutputStream();
            Graphics g = bi.createGraphics();
            g.drawImage(dcm.getImage(), 0, 0, null);
            g.dispose();
            ImageIO.write(bi, format, f);
            return new String(base64.encode(f.toByteArray()));
        } catch (IOException e) {
            logger.error("Error Converting DICOM image", e);
        }
        return "";
    }

    public static String reAttachMessage(MessageObject message) {
        return AttachmentUtil.reAttachMessage(message);
    }

    public static byte[] dicomToJpg(int sliceIndex, MessageObject message, boolean autoThreshold) {
        String encodedData = getDICOMRawData(message);
        Base64 base64 = new Base64();
        ByteArrayInputStream bis = new ByteArrayInputStream(base64.decode(encodedData.getBytes()));
        DICOM dcm = new DICOM(bis);
        dcm.run("dcm");
        if (autoThreshold) {
            ImageProcessor im = dcm.getProcessor();
            // Automatically sets the lower and upper threshold levels, where
            // 'method' must be ISODATA or ISODATA2
            im.setAutoThreshold(0, 2);
        }
        ImageStack imageStack = dcm.getImageStack();
        if (imageStack.getSize() < sliceIndex || sliceIndex < 1) {
            return null;
        }
        ImagePlus image = new ImagePlus("ImageName", imageStack.getProcessor(sliceIndex));

        return saveAsJpeg(image, 100);
    }

    private static byte[] saveAsJpeg(ImagePlus imp, int quality) {
        int width = imp.getWidth();
        int height = imp.getHeight();
        int biType = BufferedImage.TYPE_INT_RGB;
        if (imp.getProcessor().isDefaultLut())
            biType = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage bi = new BufferedImage(width, height, biType);

        try {
            Graphics g = bi.createGraphics();
            g.drawImage(imp.getImage(), 0, 0, null);
            g.dispose();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality / 100f);
            if (quality == 100)
                param.setSourceSubsampling(1, 1, 0, 0);
            IIOImage iioImage = new IIOImage(bi, null, null);
            writer.write(null, iioImage, param);
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error converting dcm file", e);
        }
        return null;
    }

}
