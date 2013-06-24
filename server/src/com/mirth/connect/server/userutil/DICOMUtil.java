package com.mirth.connect.server.userutil;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;

import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.converters.DICOMConverter;
import com.mirth.connect.server.util.AttachmentUtil;

public class DICOMUtil {
    private static Logger logger = Logger.getLogger(DICOMUtil.class);
    
    @Deprecated
    // TODO: Remove in 3.1
    public static String getDICOMRawData(MessageObject message) {
        logger.error("The getDICOMRawData(messageObject) method is deprecated and will soon be removed. Please use getDICOMRawData(connectorMessage) instead.");
        return com.mirth.connect.server.util.DICOMUtil.getDICOMRawData(message.getImmutableConnectorMessage());
    }
    
    public static String getDICOMRawData(ImmutableConnectorMessage message) {
        return com.mirth.connect.server.util.DICOMUtil.getDICOMRawData(message);
    }

    public static byte[] getDICOMRawBytes(ImmutableConnectorMessage message) {
        return com.mirth.connect.server.util.DICOMUtil.getDICOMRawBytes(message);
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static byte[] getDICOMMessage(MessageObject message) {
        logger.error("The getDICOMMessage(messageObject) method is deprecated and will soon be removed. Please use getDICOMMessage(connectorMessage) instead.");
        return com.mirth.connect.server.util.DICOMUtil.getDICOMMessage(message.getImmutableConnectorMessage());
    }

    public static byte[] getDICOMMessage(ImmutableConnectorMessage message) {
        return com.mirth.connect.server.util.DICOMUtil.getDICOMMessage(message);
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static byte[] mergeHeaderAttachments(MessageObject message, List<Attachment> attachments) throws XmlSerializerException {
        logger.error("The mergeHeaderAttachments(messageObject, attachments) method is deprecated and will soon be removed. Please use mergeHeaderAttachments(connectorMessage, attachments) instead.");
        return com.mirth.connect.server.util.DICOMUtil.mergeHeaderAttachments(message.getImmutableConnectorMessage(), attachments);
    }

    public static byte[] mergeHeaderAttachments(ImmutableConnectorMessage message, List<Attachment> attachments) throws XmlSerializerException {
        return com.mirth.connect.server.util.DICOMUtil.mergeHeaderAttachments(message, attachments);
    }

    public static byte[] mergeHeaderPixelData(byte[] header, List<Attachment> attachments) throws IOException {
        return com.mirth.connect.server.util.DICOMUtil.mergeHeaderPixelData(header, attachments);
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static List<Attachment> getMessageAttachments(MessageObject message) throws XmlSerializerException {
        logger.error("The DICOMUtil.getMessageAttachments(messageObject) method is deprecated and will soon be removed. Please use getAttachments() instead.");
        return AttachmentUtil.getMessageAttachments(message.getImmutableConnectorMessage());
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static List<Attachment> getMessageAttachments(ImmutableConnectorMessage message) throws XmlSerializerException {
        logger.error("The DICOMUtil.getMessageAttachments(connectorMessage) method is deprecated and will soon be removed. Please use getAttachments() instead.");
        return AttachmentUtil.getMessageAttachments(message);
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static String convertDICOM(String imageType, MessageObject message, boolean autoThreshold) {
        logger.error("The convertDICOM(imageType, messageObject, autoThreshold) method is deprecated and will soon be removed. Please use convertDICOM(imageType, connectorMessage, autoThreshold) instead.");
        return com.mirth.connect.server.util.DICOMUtil.convertDICOM(imageType, message.getImmutableConnectorMessage(), autoThreshold);
    }

    public static String convertDICOM(String imageType, ImmutableConnectorMessage message, boolean autoThreshold) {
        return com.mirth.connect.server.util.DICOMUtil.convertDICOM(imageType, message, autoThreshold);
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static String convertDICOM(String imageType, MessageObject message) {
        logger.error("The convertDICOM(imageType, messageObject) method is deprecated and will soon be removed. Please use convertDICOM(imageType, connectorMessage) instead.");
        return com.mirth.connect.server.util.DICOMUtil.convertDICOM(imageType, message.getImmutableConnectorMessage());
    }

    public static String convertDICOM(String imageType, ImmutableConnectorMessage message) {
        return com.mirth.connect.server.util.DICOMUtil.convertDICOM(imageType, message);
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static String reAttachMessage(MessageObject message) {
        logger.error("The DICOMUtil.reAttachMessage(messageObject) method is deprecated and will soon be removed. Please use AttachmentUtil.reAttachMessage(connectorMessage) instead.");
        return AttachmentUtil.reAttachMessage(message.getImmutableConnectorMessage());
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static String reAttachMessage(ImmutableConnectorMessage message) {
        logger.error("The DICOMUtil.reAttachMessage(connectorMessage) method is deprecated and will soon be removed. Please use AttachmentUtil.reAttachMessage(connectorMessage) instead.");
        return AttachmentUtil.reAttachMessage(message);
    }
    
    @Deprecated
    // TODO: Remove in 3.1
    public static byte[] dicomToJpg(int sliceIndex, MessageObject message, boolean autoThreshold) {
        logger.error("The dicomToJpg(sliceIndex, messageObject, autoThreshold) method is deprecated and will soon be removed. Please use dicomToJpg(sliceIndex, connectorMessage, autoThreshold) instead.");
        return com.mirth.connect.server.util.DICOMUtil.dicomToJpg(sliceIndex, message.getImmutableConnectorMessage(), autoThreshold);
    }

    public static byte[] dicomToJpg(int sliceIndex, ImmutableConnectorMessage message, boolean autoThreshold) {
        return com.mirth.connect.server.util.DICOMUtil.dicomToJpg(sliceIndex, message, autoThreshold);
    }
    
    public static DicomObject byteArrayToDicomObject(byte[] bytes, boolean decodeBase64) throws IOException {
        return DICOMConverter.byteArrayToDicomObject(bytes, decodeBase64);
    }
    
    public static byte[] dicomObjectToByteArray(DicomObject dicomObject) throws IOException {
        return DICOMConverter.dicomObjectToByteArray(dicomObject);
    }
}
