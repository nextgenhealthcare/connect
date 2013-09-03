/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;

import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.converters.DICOMConverter;
import com.mirth.connect.server.util.MessageAttachmentUtil;

/**
 * Provides DICOM utility methods.
 */
public class DICOMUtil {
    private static Logger logger = Logger.getLogger(DICOMUtil.class);

    private DICOMUtil() {}

    /**
     * Re-attaches DICOM attachments with the header data in the connector
     * message and returns the resulting merged data as a Base64-encoded string.
     * 
     * @param messageObject
     *            The connector message to retrieve merged DICOM data for.
     * @return The merged DICOM data, Base64-encoded.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use getDICOMRawData(connectorMessage) instead.
     */
    // TODO: Remove in 3.1
    public static String getDICOMRawData(MessageObject messageObject) {
        logger.error("The getDICOMRawData(messageObject) method is deprecated and will soon be removed. Please use getDICOMRawData(connectorMessage) instead.");
        return com.mirth.connect.server.util.DICOMMessageUtil.getDICOMRawData(messageObject.getImmutableConnectorMessage());
    }

    /**
     * Re-attaches DICOM attachments with the header data in the connector
     * message and returns the resulting merged data as a Base64-encoded string.
     * 
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @return The merged DICOM data, Base64-encoded.
     */
    public static String getDICOMRawData(ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.getDICOMRawData(connectorMessage);
    }

    /**
     * Re-attaches DICOM attachments with the header data in the connector
     * message and returns the resulting merged data as a byte array.
     * 
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @return The merged DICOM data as a byte array.
     */
    public static byte[] getDICOMRawBytes(ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.getDICOMRawBytes(connectorMessage);
    }

    /**
     * Re-attaches DICOM attachments with the header data in the connector
     * message and returns the resulting merged data as a byte array.
     * 
     * @param messageObject
     *            The connector message to retrieve merged DICOM data for.
     * @return The merged DICOM data as a byte array.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use getDICOMMessage(connectorMessage) instead.
     */
    // TODO: Remove in 3.1
    public static byte[] getDICOMMessage(MessageObject messageObject) {
        logger.error("The getDICOMMessage(messageObject) method is deprecated and will soon be removed. Please use getDICOMMessage(connectorMessage) instead.");
        return com.mirth.connect.server.util.DICOMMessageUtil.getDICOMMessage(messageObject.getImmutableConnectorMessage());
    }

    /**
     * Re-attaches DICOM attachments with the header data in the connector
     * message and returns the resulting merged data as a byte array.
     * 
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @return The merged DICOM data as a byte array.
     */
    public static byte[] getDICOMMessage(ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.getDICOMMessage(connectorMessage);
    }

    /**
     * Re-attaches DICOM attachments with the header data in the connector
     * message and returns the resulting merged data as a byte array.
     * 
     * @param messageObject
     *            The connector message containing header data to merge DICOM
     *            attachments with.
     * @param attachments
     *            The DICOM attachments to merge with the header data.
     * @return The merged DICOM data as a byte array.
     * @throws XmlSerializerException
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use mergeHeaderAttachments(connectorMessage, attachments)
     *             instead.
     */
    // TODO: Remove in 3.1
    public static byte[] mergeHeaderAttachments(MessageObject messageObject, List<Attachment> attachments) throws XmlSerializerException {
        logger.error("The mergeHeaderAttachments(messageObject, attachments) method is deprecated and will soon be removed. Please use mergeHeaderAttachments(connectorMessage, attachments) instead.");
        return com.mirth.connect.server.util.DICOMMessageUtil.mergeHeaderAttachments(messageObject.getImmutableConnectorMessage(), attachments);
    }

    /**
     * Re-attaches DICOM attachments with the header data in the connector
     * message and returns the resulting merged data as a byte array.
     * 
     * @param connectorMessage
     *            The connector message containing header data to merge DICOM
     *            attachments with.
     * @param attachments
     *            The DICOM attachments to merge with the header data.
     * @return The merged DICOM data as a byte array.
     * @throws XmlSerializerException
     */
    public static byte[] mergeHeaderAttachments(ImmutableConnectorMessage connectorMessage, List<Attachment> attachments) throws XmlSerializerException {
        return com.mirth.connect.server.util.DICOMMessageUtil.mergeHeaderAttachments(connectorMessage, attachments);
    }

    /**
     * Re-attaches DICOM attachments with the given header data and returns the
     * resulting merged data as a byte array.
     * 
     * @param header
     *            The header data to merge DICOM attachments with.
     * @param attachments
     *            The DICOM attachments to merge with the header data.
     * @return The merged DICOM data as a byte array.
     * @throws IOException
     */
    public static byte[] mergeHeaderPixelData(byte[] header, List<Attachment> attachments) throws IOException {
        return com.mirth.connect.server.util.DICOMMessageUtil.mergeHeaderPixelData(header, attachments);
    }

    /**
     * Retrieves all attachments currently associated with a connector message.
     * 
     * @param messageObject
     *            The connector message to retrieve associated attachments for.
     * @return A list of attachments associated with the connector message.
     * @throws XmlSerializerException
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use getAttachments() instead.
     */
    // TODO: Remove in 3.1
    public static List<Attachment> getMessageAttachments(MessageObject messageObject) throws XmlSerializerException {
        logger.error("The DICOMUtil.getMessageAttachments(messageObject) method is deprecated and will soon be removed. Please use getAttachments() instead.");
        return MessageAttachmentUtil.getMessageAttachments(messageObject.getImmutableConnectorMessage());
    }

    /**
     * Retrieves all attachments currently associated with a connector message.
     * 
     * @param connectorMessage
     *            The connector message to retrieve associated attachments for.
     * @return A list of attachments associated with the connector message.
     * @throws XmlSerializerException
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use getAttachments() instead.
     */
    // TODO: Remove in 3.1
    public static List<Attachment> getMessageAttachments(ImmutableConnectorMessage connectorMessage) throws XmlSerializerException {
        logger.error("The DICOMUtil.getMessageAttachments(connectorMessage) method is deprecated and will soon be removed. Please use getAttachments() instead.");
        return MessageAttachmentUtil.getMessageAttachments(connectorMessage);
    }

    /**
     * Returns the number of slices in the fully-merged DICOM data associated
     * with a given connector message.
     * 
     * @param connectorMessage
     *            The connector message to retrieve DICOM data for.
     * @return The number of slices in the DICOM data.
     */
    public static int getSliceCount(ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.getSliceCount(connectorMessage);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param messageObject
     *            The connector message to retrieve merged DICOM data for.
     * @param autoThreshold
     *            If true, automatically sets the lower and upper threshold
     *            levels.
     * @return The converted image, as a Base64-encoded string.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use convertDICOM(imageType, connectorMessage, autoThreshold)
     *             instead.
     */
    // TODO: Remove in 3.1
    public static String convertDICOM(String imageType, MessageObject messageObject, boolean autoThreshold) {
        logger.error("The convertDICOM(imageType, messageObject, autoThreshold) method is deprecated and will soon be removed. Please use convertDICOM(imageType, connectorMessage, autoThreshold) instead.");
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOM(imageType, messageObject.getImmutableConnectorMessage(), 1, autoThreshold);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param autoThreshold
     *            If true, automatically sets the lower and upper threshold
     *            levels.
     * @return The converted image, as a Base64-encoded string.
     */
    public static String convertDICOM(String imageType, ImmutableConnectorMessage connectorMessage, boolean autoThreshold) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOM(imageType, connectorMessage, 1, autoThreshold);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param messageObject
     *            The connector message to retrieve merged DICOM data for.
     * @return The converted image, as a Base64-encoded string.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use convertDICOM(imageType, connectorMessage) instead.
     */
    // TODO: Remove in 3.1
    public static String convertDICOM(String imageType, MessageObject messageObject) {
        logger.error("The convertDICOM(imageType, messageObject) method is deprecated and will soon be removed. Please use convertDICOM(imageType, connectorMessage) instead.");
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOM(imageType, messageObject.getImmutableConnectorMessage(), 1, false);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @return The converted image, as a Base64-encoded string.
     */
    public static String convertDICOM(String imageType, ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOM(imageType, connectorMessage, 1, false);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates
     *            which one to use (the first slice has an index of 1).
     * @return The converted image, as a Base64-encoded string.
     */
    public static String convertDICOM(String imageType, ImmutableConnectorMessage connectorMessage, int sliceIndex) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOM(imageType, connectorMessage, sliceIndex, false);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates
     *            which one to use (the first slice has an index of 1).
     * @param autoThreshold
     *            If true, automatically sets the lower and upper threshold
     *            levels.
     * @return The converted image, as a Base64-encoded string.
     */
    public static String convertDICOM(String imageType, ImmutableConnectorMessage connectorMessage, int sliceIndex, boolean autoThreshold) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOM(imageType, connectorMessage, sliceIndex, autoThreshold);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @return The converted image, as a byte array.
     */
    public static byte[] convertDICOMToByteArray(String imageType, ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOMToByteArray(imageType, connectorMessage, 1, false);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates
     *            which one to use (the first slice has an index of 1).
     * @return The converted image, as a byte array.
     */
    public static byte[] convertDICOMToByteArray(String imageType, ImmutableConnectorMessage connectorMessage, int sliceIndex) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOMToByteArray(imageType, connectorMessage, sliceIndex, false);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates
     *            which one to use (the first slice has an index of 1).
     * @param autoThreshold
     *            If true, automatically sets the lower and upper threshold
     *            levels.
     * @return The converted image, as a byte array.
     */
    public static byte[] convertDICOMToByteArray(String imageType, ImmutableConnectorMessage connectorMessage, int sliceIndex, boolean autoThreshold) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOMToByteArray(imageType, connectorMessage, sliceIndex, autoThreshold);
    }

    /**
     * Replaces any unique attachment tokens (e.g. "${ATTACH:id}") with the
     * corresponding attachment content, and returns the full post-replacement
     * message.
     * 
     * @param messageObject
     *            The connector message associated with the attachments. The
     *            encoded data will be used as the raw message string to
     *            re-attach attachments to, if it exists. Otherwise, the
     *            connector message's raw data will be used.
     * @return The resulting message with all applicable attachment content
     *         re-inserted.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use AttachmentUtil.reAttachMessage(connectorMessage) instead.
     */
    // TODO: Remove in 3.1
    public static String reAttachMessage(MessageObject messageObject) {
        logger.error("The DICOMUtil.reAttachMessage(messageObject) method is deprecated and will soon be removed. Please use AttachmentUtil.reAttachMessage(connectorMessage) instead.");
        return MessageAttachmentUtil.reAttachMessage(messageObject.getImmutableConnectorMessage());
    }

    /**
     * Replaces any unique attachment tokens (e.g. "${ATTACH:id}") with the
     * corresponding attachment content, and returns the full post-replacement
     * message.
     * 
     * @param connectorMessage
     *            The connector message associated with the attachments. The
     *            encoded data will be used as the raw message string to
     *            re-attach attachments to, if it exists. Otherwise, the
     *            connector message's raw data will be used.
     * @return The resulting message with all applicable attachment content
     *         re-inserted.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use AttachmentUtil.reAttachMessage(connectorMessage) instead.
     */
    // TODO: Remove in 3.1
    public static String reAttachMessage(ImmutableConnectorMessage connectorMessage) {
        logger.error("The DICOMUtil.reAttachMessage(connectorMessage) method is deprecated and will soon be removed. Please use AttachmentUtil.reAttachMessage(connectorMessage) instead.");
        return MessageAttachmentUtil.reAttachMessage(connectorMessage);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * JPEG image.
     * 
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates
     *            which one to use (the first slice has an index of 1).
     * @param messageObject
     *            The connector message to retrieve merged DICOM data for.
     * @param autoThreshold
     *            If true, automatically sets the lower and upper threshold
     *            levels.
     * @return The converted JPEG image, as a byte array.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use convertDICOMToByteArray(imageType, connectorMessage,
     *             sliceIndex, autoThreshold) instead.
     */
    // TODO: Remove in 3.1
    public static byte[] dicomToJpg(int sliceIndex, MessageObject messageObject, boolean autoThreshold) {
        logger.error("The dicomToJpg(sliceIndex, messageObject, autoThreshold) method is deprecated and will soon be removed. Please use convertDICOMToByteArray(imageType, connectorMessage, sliceIndex, autoThreshold) instead.");
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOMToByteArray("jpg", messageObject.getImmutableConnectorMessage(), sliceIndex, autoThreshold);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a
     * JPEG image.
     * 
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates
     *            which one to use (the first slice has an index of 1).
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param autoThreshold
     *            If true, automatically sets the lower and upper threshold
     *            levels.
     * @return The converted JPEG image, as a byte array.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please
     *             use convertDICOMToByteArray(imageType, connectorMessage,
     *             sliceIndex, autoThreshold) instead.
     */
    // TODO: Remove in 3.1
    public static byte[] dicomToJpg(int sliceIndex, ImmutableConnectorMessage connectorMessage, boolean autoThreshold) {
        logger.error("The dicomToJpg(sliceIndex, connectorMessage, autoThreshold) method is deprecated and will soon be removed. Please use convertDICOMToByteArray(imageType, connectorMessage, sliceIndex, autoThreshold) instead.");
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOMToByteArray("jpg", connectorMessage, sliceIndex, autoThreshold);
    }

    /**
     * Converts a byte array into a dcm4che DicomObject.
     * 
     * @param bytes
     *            The binary data to convert.
     * @param decodeBase64
     *            If true, the data is assumed to be Base64-encoded.
     * @return The converted DicomObject.
     * @throws IOException
     */
    public static DicomObject byteArrayToDicomObject(byte[] bytes, boolean decodeBase64) throws IOException {
        return DICOMConverter.byteArrayToDicomObject(bytes, decodeBase64);
    }

    /**
     * Converts a dcm4che DicomObject into a byte array.
     * 
     * @param dicomObject
     *            The DicomObject to convert.
     * @return The converted byte array.
     * @throws IOException
     */
    public static byte[] dicomObjectToByteArray(DicomObject dicomObject) throws IOException {
        return DICOMConverter.dicomObjectToByteArray(dicomObject);
    }
}
