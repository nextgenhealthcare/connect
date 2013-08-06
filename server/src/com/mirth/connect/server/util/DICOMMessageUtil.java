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
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.ImmutableMessageContent;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.model.converters.DICOMConverter;
import com.mirth.connect.server.controllers.MessageController;

public class DICOMMessageUtil {
    private static Logger logger = Logger.getLogger(DICOMMessageUtil.class);

    public static String getDICOMRawData(ImmutableConnectorMessage message) {
        String mergedMessage = null;

        List<Attachment> attachments = MessageController.getInstance().getMessageAttachment(message.getChannelId(), message.getMessageId());

        if (attachments != null && attachments.size() > 0) {
            try {
                if (attachments.get(0).getType().equals("DICOM")) {
                    byte[] mergedMessageBytes = mergeHeaderAttachments(message, attachments);

                    // Replace the raw binary with the encoded binary to free up the memory
                    mergedMessageBytes = Base64Util.encodeBase64(mergedMessageBytes);

                    mergedMessage = StringUtils.newStringUsAscii(mergedMessageBytes);
                } else {
                    mergedMessage = message.getRaw().getContent();
                }
            } catch (Exception e) {
                logger.error("Error merging DICOM data", e);
                mergedMessage = message.getRaw().getContent();
            }
        } else {
            mergedMessage = message.getRaw().getContent();
        }

        return mergedMessage;
    }

    public static String getDICOMRawData(ConnectorMessage message) {
        return getDICOMRawData(new ImmutableConnectorMessage(message));
    }

    public static byte[] getDICOMRawBytes(ImmutableConnectorMessage message) {
        byte[] mergedMessage = null;

        List<Attachment> attachments = MessageController.getInstance().getMessageAttachment(message.getChannelId(), message.getMessageId());

        if (attachments != null && attachments.size() > 0) {
            try {
                if (attachments.get(0).getType().equals("DICOM")) {
                    mergedMessage = mergeHeaderAttachments(message, attachments);
                } else {
                    mergedMessage = Base64.decodeBase64(StringUtils.getBytesUsAscii(message.getRaw().getContent()));
                }
            } catch (Exception e) {
                logger.error("Error merging DICOM data", e);
                mergedMessage = Base64.decodeBase64(StringUtils.getBytesUsAscii(message.getRaw().getContent()));
            }
        } else {
            mergedMessage = Base64.decodeBase64(StringUtils.getBytesUsAscii(message.getRaw().getContent()));
        }

        return mergedMessage;
    }

    public static byte[] getDICOMRawBytes(ConnectorMessage message) {
        return getDICOMRawBytes(new ImmutableConnectorMessage(message));
    }

    public static byte[] getDICOMMessage(ImmutableConnectorMessage message) {
        return getDICOMRawBytes(message);
    }

    public static byte[] mergeHeaderAttachments(ImmutableConnectorMessage message, List<Attachment> attachments) throws XmlSerializerException {
        try {
            byte[] headerBytes;

            ImmutableMessageContent encoded = message.getEncoded();
            ImmutableMessageContent raw = message.getRaw();

            if (encoded != null && encoded.getContent() != null && encoded.getDataType().equals("DICOM")) {
                headerBytes = Base64.decodeBase64(StringUtils.getBytesUsAscii(encoded.getContent()));
            } else if (raw != null && raw.getContent() != null && raw.getDataType().equals("DICOM")) {
                headerBytes = Base64.decodeBase64(StringUtils.getBytesUsAscii(raw.getContent()));
            } else {
                return new byte[0];
            }

            return mergeHeaderPixelData(headerBytes, attachments);
        } catch (IOException e) {
            throw new XmlSerializerException(e);
        }
    }

    public static byte[] mergeHeaderPixelData(byte[] header, List<Attachment> attachments) throws IOException {
        // 1. read in header
        DicomObject dcmObj = DICOMConverter.byteArrayToDicomObject(header, false);

        // 2. Add pixel data to DicomObject
        if (attachments != null && !attachments.isEmpty()) {
            if (attachments.size() > 1) {
                DicomElement dicomElement = dcmObj.putFragments(Tag.PixelData, VR.OB, dcmObj.bigEndian(), attachments.size());

                for (Attachment attachment : attachments) {
                    dicomElement.addFragment(attachment.getContent());
                }

                dcmObj.add(dicomElement);
            } else {
                dcmObj.putBytes(Tag.PixelData, VR.OB, attachments.get(0).getContent());
            }
        }

        // Memory Optimization. Free the references to the data in the attachments list.
        attachments.clear();

        return DICOMConverter.dicomObjectToByteArray(dcmObj);
    }

    public static int getSliceCount(ImmutableConnectorMessage message) {
        ByteArrayInputStream bais = new ByteArrayInputStream(getDICOMRawBytes(message));

        try {
            DICOM dicom = new DICOM(bais);
            // run() is required to create the dicom object. The argument serves multiple purposes. If it is null or empty, it opens a dialog to select a dicom file.
            // Otherwise, if dicom.show() is called, it is the title of the dialog. Since we are not showing any dialogs here, we just need to pass a non-null, non-empty string.
            dicom.run("DICOM");

            return dicom.getImageStack().getSize();
        } finally {
            IOUtils.closeQuietly(bais);
        }
    }

    public static String convertDICOM(String imageType, ImmutableConnectorMessage message, int sliceIndex, boolean autoThreshold) {
        byte[] bytes = convertDICOMToByteArray(imageType, message, sliceIndex, autoThreshold);

        if (bytes != null) {
            return new String(Base64.encodeBase64Chunked(bytes));
        }

        return "";
    }

    public static byte[] convertDICOMToByteArray(String imageType, ImmutableConnectorMessage message, int sliceIndex, boolean autoThreshold) {
        if (imageType.equalsIgnoreCase("jpg") || imageType.equalsIgnoreCase("jpeg")) {
            return dicomToJpg(sliceIndex, message, autoThreshold);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(getDICOMRawBytes(message));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            DICOM dicom = new DICOM(bais);
            // run() is required to create the dicom object. The argument serves multiple purposes. If it is null or empty, it opens a dialog to select a dicom file.
            // Otherwise, if dicom.show() is called, it is the title of the dialog. Since we are not showing any dialogs here, we just need to pass a non-null, non-empty string.
            dicom.run("DICOM");

            ImageStack imageStack = dicom.getImageStack();

            if (sliceIndex >= 1 && sliceIndex <= imageStack.getSize()) {
                ImageIO.write(imageStack.getProcessor(sliceIndex).getBufferedImage(), imageType, baos);
                return baos.toByteArray();
            } else {
                logger.error("Image slice " + sliceIndex + " not found for message " + message.getMessageId() + ".");
            }
        } catch (IOException e) {
            logger.error("Error Converting DICOM image", e);
        } finally {
            IOUtils.closeQuietly(bais);
            IOUtils.closeQuietly(baos);
        }

        return null;
    }

    private static byte[] dicomToJpg(int sliceIndex, ImmutableConnectorMessage message, boolean autoThreshold) {
        ByteArrayInputStream bais = new ByteArrayInputStream(getDICOMRawBytes(message));

        try {
            DICOM dicom = new DICOM(bais);
            // run() is required to create the dicom object. The argument serves multiple purposes. If it is null or empty, it opens a dialog to select a dicom file.
            // Otherwise, if dicom.show() is called, it is the title of the dialog. Since we are not showing any dialogs here, we just need to pass a non-null, non-empty string.
            dicom.run("DICOM");

            if (autoThreshold) {
                ImageProcessor im = dicom.getProcessor();
                // Automatically sets the lower and upper threshold levels, where
                // 'method' must be ISODATA or ISODATA2
                im.setAutoThreshold(0, 2);
            }

            ImageStack imageStack = dicom.getImageStack();

            if ((imageStack.getSize() < sliceIndex) || sliceIndex < 1) {
                logger.error("Image slice " + sliceIndex + " not found for message " + message.getMessageId() + ".");
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
            logger.error("Error Converting DICOM image", e);
        } finally {
            IOUtils.closeQuietly(baos);
        }

        return null;
    }
}
