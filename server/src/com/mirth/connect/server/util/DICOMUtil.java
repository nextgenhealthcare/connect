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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.MessageObject;
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
                
                if (message.getCorrelationId() != null) {
                    attachments = mos.getAttachmentsByMessageId(message.getCorrelationId());
                } else {
                    attachments = mos.getAttachmentsByMessageId(message.getId());
                }
                    
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
        return Base64.decodeBase64(getDICOMRawData(message).getBytes());
    }

    public static String mergeHeaderAttachments(MessageObject message, List<Attachment> attachments) throws SerializerException {
        try {
            List<byte[]> images = new ArrayList<byte[]>();

            for (Attachment attachment : attachments) {
                images.add(Base64.decodeBase64(attachment.getData()));
            }

            byte[] headerBytes;

            if (message.getEncodedDataProtocol().equals(MessageObject.Protocol.DICOM) && message.getEncodedData() != null) {
                headerBytes = Base64.decodeBase64(message.getEncodedData().getBytes());
            } else if (message.getRawDataProtocol().equals(MessageObject.Protocol.DICOM) && message.getRawData() != null) {
                headerBytes = Base64.decodeBase64(message.getRawData().getBytes());
            } else {
                return StringUtils.EMPTY;
            }

            return mergeHeaderPixelData(headerBytes, images);
        } catch (IOException e) {
            throw new SerializerException(e);
        }
    }

    public static String mergeHeaderPixelData(byte[] header, List<byte[]> images) throws IOException {
        // 1. read in header
        DicomObject dcmObj = byteArrayToDicomObject(header);

        // 2. Add pixel data to DicomObject
        if (images != null && !images.isEmpty()) {
            if (images.size() > 1) {
                DicomElement dicomElement = dcmObj.putFragments(Tag.PixelData, VR.OB, dcmObj.bigEndian(), images.size());

                for (byte[] image : images) {
                    dicomElement.addFragment(image);
                }

                dcmObj.add(dicomElement);
            } else {
                dcmObj.putBytes(Tag.PixelData, VR.OB, images.get(0));
            }
        }

        return new String(Base64.encodeBase64Chunked(dicomObjectToByteArray(dcmObj)));
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
        // use new method for jpegs
        if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
            return new String(Base64.encodeBase64Chunked(dicomToJpg(1, message, autoThreshold)));
        }

        byte[] rawImage = Base64.decodeBase64(getDICOMRawData(message).getBytes());
        ByteArrayInputStream bais = new ByteArrayInputStream(rawImage);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            DICOM dicom = new DICOM(bais);
            dicom.run(message.getType());
            BufferedImage image = new BufferedImage(dicom.getWidth(), dicom.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics graphics = image.createGraphics();
            graphics.drawImage(dicom.getImage(), 0, 0, null);
            graphics.dispose();
            ImageIO.write(image, format, baos);
            return new String(Base64.encodeBase64Chunked(baos.toByteArray()));
        } catch (IOException e) {
            logger.error("Error Converting DICOM image", e);
        } finally {
            IOUtils.closeQuietly(bais);
            IOUtils.closeQuietly(baos);
        }

        return StringUtils.EMPTY;
    }

    public static String reAttachMessage(MessageObject message) {
        return AttachmentUtil.reAttachMessage(message);
    }

    public static byte[] dicomToJpg(int sliceIndex, MessageObject message, boolean autoThreshold) {
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decodeBase64(getDICOMRawData(message)));

        try {
            DICOM dicom = new DICOM(bais);
            dicom.run("dcm");

            if (autoThreshold) {
                ImageProcessor im = dicom.getProcessor();
                // Automatically sets the lower and upper threshold levels, where
                // 'method' must be ISODATA or ISODATA2
                im.setAutoThreshold(0, 2);
            }

            ImageStack imageStack = dicom.getImageStack();

            if ((imageStack.getSize() < sliceIndex) || sliceIndex < 1) {
                return null;
            }

            ImagePlus image = new ImagePlus("ImageName", imageStack.getProcessor(sliceIndex));
            return saveAsJpeg(image, 100);
        } finally {
            IOUtils.closeQuietly(bais);
        }
    }

    private static byte[] saveAsJpeg(ImagePlus imagePlug, int quality) {
        int imageType = BufferedImage.TYPE_INT_RGB;
        
        if (imagePlug.getProcessor().isDefaultLut()) {
            imageType = BufferedImage.TYPE_BYTE_GRAY;
        }
            
        BufferedImage bufferedImage = new BufferedImage(imagePlug.getWidth(), imagePlug.getHeight(), imageType);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Graphics graphics = bufferedImage.createGraphics();
            graphics.drawImage(imagePlug.getImage(), 0, 0, null);
            graphics.dispose();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality / 100f);
            
            if (quality == 100) {
                param.setSourceSubsampling(1, 1, 0, 0);
            }
                
            IIOImage iioImage = new IIOImage(bufferedImage, null, null);
            writer.write(null, iioImage, param);
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error converting dcm file", e);
        } finally {
            IOUtils.closeQuietly(baos);
        }
        
        return null;
    }

    public static DicomObject byteArrayToDicomObject(byte[] bytes) throws IOException {
        DicomObject basicDicomObject = new BasicDicomObject();
        DicomInputStream dis = null;

        try {
            dis = new DicomInputStream(new ByteArrayInputStream(bytes));
            dis.readDicomObject(basicDicomObject, -1);
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(dis);
        }

        return basicDicomObject;
    }

    public static byte[] dicomObjectToByteArray(DicomObject dicomObject) throws IOException {
        BasicDicomObject basicDicomObject = (BasicDicomObject) dicomObject;
        DicomOutputStream dos = null;

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            dos = new DicomOutputStream(bos);

            if (basicDicomObject.fileMetaInfo().isEmpty()) {
                // Create ACR/NEMA Dump
                dos.writeDataset(basicDicomObject, TransferSyntax.ImplicitVRLittleEndian);
            } else {
                // Create DICOM File
                dos.writeDicomFile(basicDicomObject);
            }

            return bos.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(dos);
        }
    }
}
