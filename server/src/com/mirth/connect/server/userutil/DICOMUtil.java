/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che2.data.DicomObject;

import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.model.converters.DICOMConverter;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

/**
 * Provides DICOM utility methods.
 */
public class DICOMUtil {

    private DICOMUtil() {}

    /**
     * Re-attaches DICOM attachments with the header data in the connector message and returns the
     * resulting merged data as a Base64-encoded string.
     * 
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @return The merged DICOM data, Base64-encoded.
     */
    public static String getDICOMRawData(ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.getDICOMRawData(connectorMessage);
    }

    /**
     * Re-attaches DICOM attachments with the header data in the connector message and returns the
     * resulting merged data as a byte array.
     * 
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @return The merged DICOM data as a byte array.
     */
    public static byte[] getDICOMRawBytes(ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.getDICOMRawBytes(connectorMessage);
    }

    /**
     * Re-attaches DICOM attachments with the header data in the connector message and returns the
     * resulting merged data as a byte array.
     * 
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @return The merged DICOM data as a byte array.
     */
    public static byte[] getDICOMMessage(ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.getDICOMMessage(connectorMessage);
    }

    /**
     * Re-attaches DICOM attachments with the header data in the connector message and returns the
     * resulting merged data as a Base-64 encoded String.
     * 
     * @param connectorMessage
     *            The connector message containing header data to merge DICOM attachments with.
     * @param attachments
     *            The DICOM attachments to merge with the header data.
     * @return The merged DICOM data as a Base-64 encoded String.
     * @throws MessageSerializerException
     *             If a database access error occurs, or the DICOM data could not be parsed.
     * @throws IOException
     *             If Base64 encoding failed.
     */
    public static String mergeHeaderAttachments(ImmutableConnectorMessage connectorMessage, List<Attachment> attachments) throws MessageSerializerException, IOException {
        return new String(Base64Util.encodeBase64(com.mirth.connect.server.util.DICOMMessageUtil.mergeHeaderAttachments(connectorMessage, AttachmentUtil.convertToDonkeyAttachmentList(attachments))));
    }

    /**
     * Re-attaches DICOM attachments with the given header data and returns the resulting merged
     * data as a Base-64 encoded String.
     * 
     * @param header
     *            The header data to merge DICOM attachments with.
     * @param images
     *            The DICOM attachments as byte arrays to merge with the header data.
     * @return The merged DICOM data as a Base-64 encoded String.
     * @throws IOException
     *             If Base64 encoding failed.
     */
    public static String mergeHeaderPixelData(byte[] header, List<byte[]> images) throws IOException {
        List<Attachment> attachments = new ArrayList<Attachment>();
        for (byte[] image : images) {
            Attachment attachment = new Attachment();
            attachment.setContent(image);
            attachments.add(attachment);
        }

        return new String(Base64Util.encodeBase64(com.mirth.connect.server.util.DICOMMessageUtil.mergeHeaderPixelData(header, AttachmentUtil.convertToDonkeyAttachmentList(attachments))));
    }

    /**
     * Returns the number of slices in the fully-merged DICOM data associated with a given connector
     * message.
     * 
     * @param connectorMessage
     *            The connector message to retrieve DICOM data for.
     * @return The number of slices in the DICOM data.
     */
    public static int getSliceCount(ImmutableConnectorMessage connectorMessage) {
        return com.mirth.connect.server.util.DICOMMessageUtil.getSliceCount(connectorMessage);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param autoThreshold
     *            If true, automatically sets the lower and upper threshold levels.
     * @return The converted image, as a Base64-encoded string.
     */
    public static String convertDICOM(String imageType, ImmutableConnectorMessage connectorMessage, boolean autoThreshold) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOM(imageType, connectorMessage, 1, autoThreshold);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a specified image format.
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
     * Converts merged DICOM data associated with a connector message into a specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates which one to use
     *            (the first slice has an index of 1).
     * @return The converted image, as a Base64-encoded string.
     */
    public static String convertDICOM(String imageType, ImmutableConnectorMessage connectorMessage, int sliceIndex) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOM(imageType, connectorMessage, sliceIndex, false);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates which one to use
     *            (the first slice has an index of 1).
     * @param autoThreshold
     *            If true, automatically sets the lower and upper threshold levels.
     * @return The converted image, as a Base64-encoded string.
     */
    public static String convertDICOM(String imageType, ImmutableConnectorMessage connectorMessage, int sliceIndex, boolean autoThreshold) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOM(imageType, connectorMessage, sliceIndex, autoThreshold);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a specified image format.
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
     * Converts merged DICOM data associated with a connector message into a specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates which one to use
     *            (the first slice has an index of 1).
     * @return The converted image, as a byte array.
     */
    public static byte[] convertDICOMToByteArray(String imageType, ImmutableConnectorMessage connectorMessage, int sliceIndex) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOMToByteArray(imageType, connectorMessage, sliceIndex, false);
    }

    /**
     * Converts merged DICOM data associated with a connector message into a specified image format.
     * 
     * @param imageType
     *            The image format to convert the DICOM data to (e.g. "jpg").
     * @param connectorMessage
     *            The connector message to retrieve merged DICOM data for.
     * @param sliceIndex
     *            If there are multiple slices in the DICOM data, this indicates which one to use
     *            (the first slice has an index of 1).
     * @param autoThreshold
     *            If true, automatically sets the lower and upper threshold levels.
     * @return The converted image, as a byte array.
     */
    public static byte[] convertDICOMToByteArray(String imageType, ImmutableConnectorMessage connectorMessage, int sliceIndex, boolean autoThreshold) {
        return com.mirth.connect.server.util.DICOMMessageUtil.convertDICOMToByteArray(imageType, connectorMessage, sliceIndex, autoThreshold);
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
     *             If Base64 encoding failed.
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
     *             If Base64 encoding failed.
     */
    public static byte[] dicomObjectToByteArray(DicomObject dicomObject) throws IOException {
        return DICOMConverter.dicomObjectToByteArray(dicomObject);
    }
}
