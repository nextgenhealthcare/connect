/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.donkey.util.StringUtil;
import com.mirth.connect.server.controllers.MessageController;
import com.mirth.connect.server.util.DICOMMessageUtil;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

public abstract class MirthAttachmentHandler implements AttachmentHandler {
    private final static String PREFIX = "${";
    private final static String SUFFIX = "}";
    private final static int ATTACHMENT_ID_LENGTH = 36;
    private final static String ATTACHMENT_KEY = "ATTACH:";
    private final static String DICOM_KEY = "DICOMMESSAGE";
    private final static int KEY_DATA = 0;
    private final static int KEY_END_INDEX = 1;

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public byte[] reAttachMessage(String raw, ConnectorMessage connectorMessage, String charsetEncoding, boolean binary) {
        return reAttachMessage(raw, new ImmutableConnectorMessage(connectorMessage), charsetEncoding, binary);
    }

    @Override
    public String reAttachMessage(ConnectorMessage message) {
        return reAttachMessage(new ImmutableConnectorMessage(message));
    }

    public String reAttachMessage(ImmutableConnectorMessage message) {
        String messageData = null;
        if (message.getEncoded() != null && message.getEncoded().getContent() != null) {
            messageData = message.getEncoded().getContent();
        } else if (message.getRaw() != null) {
            messageData = message.getRaw().getContent();
        }

        return StringUtils.newString(reAttachMessage(messageData, message, Constants.ATTACHMENT_CHARSET, false), Constants.ATTACHMENT_CHARSET);
    }

    public String reAttachMessage(String raw, ConnectorMessage message) {
        return reAttachMessage(raw, new ImmutableConnectorMessage(message));
    }

    public String reAttachMessage(String raw, ImmutableConnectorMessage message) {
        return StringUtils.newString(reAttachMessage(raw, message, Constants.ATTACHMENT_CHARSET, false), Constants.ATTACHMENT_CHARSET);
    }

    public byte[] reAttachMessage(String raw, ImmutableConnectorMessage connectorMessage, String charsetEncoding, boolean binary) {
        try {
            Map<Integer, Map<Integer, Object>> replacementObjects = new TreeMap<Integer, Map<Integer, Object>>();
            // Determine the buffersize during the first pass for better memory performance
            int bufferSize = raw.length();
            int index = 0;
            int endIndex;
            // Initialize the objects here so only one retrieval of the attachment content is ever needed.
            byte[] dicomObject = null;
            Map<String, Attachment> attachmentMap = null;

            // Handle the special case if only a dicom message is requested. 
            // In this case we can skip any byte appending and thus do not need to base64 encode the dicom object
            // if the type is binary.
            if (raw.trim().equals(PREFIX + DICOM_KEY + SUFFIX)) {
                dicomObject = DICOMMessageUtil.getDICOMRawBytes(connectorMessage);

                if (!binary) {
                    dicomObject = Base64Util.encodeBase64(dicomObject);
                }

                return dicomObject;
            }

            // Check the raw string in one pass for any attachments.
            // Stores the start and end indices to replace, along with the attachment content.
            while ((index = raw.indexOf(PREFIX, index)) != -1) {
                if (raw.startsWith(DICOM_KEY + SUFFIX, index + PREFIX.length())) {
                    if (dicomObject == null) {
                        // Unfortunately, if the dicom data needs to appended to other base64 data, it must be done so in base64.
                        dicomObject = Base64Util.encodeBase64(DICOMMessageUtil.getDICOMRawBytes(connectorMessage));
                    }

                    endIndex = index + PREFIX.length() + DICOM_KEY.length() + SUFFIX.length();

                    Map<Integer, Object> replacementMap = new HashMap<Integer, Object>();
                    replacementMap.put(KEY_END_INDEX, endIndex);
                    replacementMap.put(KEY_DATA, dicomObject);
                    replacementObjects.put(index, replacementMap);

                    bufferSize += dicomObject.length;
                    index += endIndex - index;
                } else if (raw.startsWith(ATTACHMENT_KEY, index + PREFIX.length())) {
                    if (attachmentMap == null) {
                        List<Attachment> list = getMessageAttachments(connectorMessage);

                        // Store the attachments in a map with the attachment's Id as the key
                        attachmentMap = new HashMap<String, Attachment>();
                        for (Attachment attachment : list) {
                            attachmentMap.put(attachment.getId(), attachment);
                        }
                    }

                    int attachmentIdStartIndex = index + PREFIX.length() + ATTACHMENT_KEY.length();
                    int attachmentIdEndIndex = attachmentIdStartIndex + ATTACHMENT_ID_LENGTH;
                    endIndex = attachmentIdEndIndex + SUFFIX.length();
                    String attachmentId = raw.substring(attachmentIdStartIndex, attachmentIdStartIndex + ATTACHMENT_ID_LENGTH);

                    if (raw.substring(attachmentIdEndIndex, endIndex).equals(SUFFIX)) {
                        Map<Integer, Object> replacementMap = new HashMap<Integer, Object>();
                        replacementMap.put(KEY_END_INDEX, endIndex);

                        if (attachmentMap.containsKey(attachmentId)) {
                            Attachment attachment = attachmentMap.get(attachmentId);
                            replacementMap.put(KEY_DATA, attachment.getContent());

                            bufferSize += attachment.getContent().length;
                        } else {
                            replacementMap.put(KEY_DATA, new byte[0]);
                        }

                        replacementObjects.put(index, replacementMap);
                    }
                } else {
                    endIndex = index + PREFIX.length();
                }

                index += endIndex - index;
            }
            // Release the object pointers of the attachment content so they aren't held in memory for the entire method
            dicomObject = null;
            attachmentMap = null;

            // Initialize the stream's buffer size. The buffer size will always be slightly large than needed,
            // because the template keys are never removed from the buffer size.
            // It is not worth doing any extra calculations for the amount of memory saved. 
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);

            int segmentStartIndex = 0;
            for (Map.Entry<Integer, Map<Integer, Object>> entry : replacementObjects.entrySet()) {
                int startReplacementIndex = entry.getKey();
                int endReplacementIndex = (Integer) entry.getValue().get(KEY_END_INDEX);
                byte[] data = (byte[]) entry.getValue().get(KEY_DATA);

                // Allows the memory used by the attachments to be released at the end of the loop
                entry.getValue().clear();

                byte[] templateSegment;
                // If the data is binary, the content should be in base64, so using US-ASCII as the charset encoding should be sufficient.
                if (binary) {
                    templateSegment = StringUtils.getBytesUsAscii(raw.substring(segmentStartIndex, startReplacementIndex));
                } else {
                    templateSegment = StringUtil.getBytesUncheckedChunked(raw.substring(segmentStartIndex, startReplacementIndex), Constants.ATTACHMENT_CHARSET);
                }

                baos.write(templateSegment);
                baos.write(data);

                segmentStartIndex = endReplacementIndex;
            }

            byte[] templateSegment;
            if (binary) {
                templateSegment = StringUtils.getBytesUsAscii(raw.substring(segmentStartIndex));
            } else {
                templateSegment = StringUtil.getBytesUncheckedChunked(raw.substring(segmentStartIndex), Constants.ATTACHMENT_CHARSET);
            }

            byte[] combined;
            // If there are no attachments, don't bother writing to the output stream.
            if (segmentStartIndex == 0) {
                combined = templateSegment;
            } else {
                // Write the segment after the last replacement.
                baos.write(templateSegment);

                combined = baos.toByteArray();
                // Release the memory used by the byte array stream. ByteArrayOutputStreams do not need to be closed. 
                baos = null;
            }

            templateSegment = null;

            // If binary, the content should be in base64 so it is necessary to decode the data.
            if (binary) {
                combined = Base64Util.decodeBase64(combined);
            } else if (charsetEncoding != null && !charsetEncoding.toUpperCase().equals(Constants.ATTACHMENT_CHARSET.toUpperCase())) {
                // Convert the byte array to a string using the internal encoding.
                String combinedString = StringUtils.newString(combined, Constants.ATTACHMENT_CHARSET);
                // First release the reference to the old byte data so it can be reallocated if necessary.
                combined = null;
                // Convert the string to a byte array using the requested encoding
                combined = StringUtil.getBytesUncheckedChunked(combinedString, charsetEncoding);
            }

            return combined;
        } catch (Exception e) {
            logger.error("Error reattaching attachments", e);
            return null;
        }
    }
    
    public static boolean hasAttachmentKeys(String raw) {
        if (raw.contains(PREFIX + DICOM_KEY + SUFFIX) || raw.contains(PREFIX + ATTACHMENT_KEY)) {
            return true;
        }

        return false;
    }
    
    public static List<Attachment> getMessageAttachments(ImmutableConnectorMessage message) throws XmlSerializerException {
        List<Attachment> attachments;
        try {
            attachments = MessageController.getInstance().getMessageAttachment(message.getChannelId(), message.getMessageId());
        } catch (Exception e) {
            throw new XmlSerializerException(e.getMessage());
        }
        return attachments;
    }
}
