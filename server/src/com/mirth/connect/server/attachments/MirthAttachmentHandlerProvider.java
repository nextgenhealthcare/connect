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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProvider;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.donkey.util.StringUtil;
import com.mirth.connect.server.controllers.MessageController;
import com.mirth.connect.server.util.DICOMMessageUtil;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.util.AttachmentUtil;

public abstract class MirthAttachmentHandlerProvider implements AttachmentHandlerProvider {

    private final static String PREFIX = "${";
    private final static String SUFFIX = "}";
    private final static String DELIMITER = ":";
    private final static int ATTACHMENT_ID_LENGTH = 36;
    private final static int MESSAGE_ID_MAX_LENGTH = 20; // 2^64-1 unsigned, 20 chars max
    private final static String ATTACHMENT_KEY = "ATTACH:";
    private final static String DICOM_KEY = "DICOMMESSAGE";
    private final static int KEY_DATA = 0;
    private final static int KEY_END_INDEX = 1;

    private Logger logger = Logger.getLogger(getClass());
    private MessageController messageController;

    public MirthAttachmentHandlerProvider(MessageController messageController) {
        this.messageController = messageController;
    }

    @Override
    public byte[] reAttachMessage(String raw, ConnectorMessage connectorMessage, String charsetEncoding, boolean binary, boolean reattach) {
        return reAttachMessage(raw, connectorMessage, charsetEncoding, binary, reattach, false);
    }

    public byte[] reAttachMessage(String raw, ConnectorMessage connectorMessage, String charsetEncoding, boolean binary, boolean reattach, boolean localOnly) {
        return reAttachMessage(raw, new ImmutableConnectorMessage(connectorMessage), charsetEncoding, binary, reattach, localOnly);
    }

    @Override
    public String reAttachMessage(ConnectorMessage message, boolean reattach) {
        return reAttachMessage(new ImmutableConnectorMessage(message), reattach);
    }

    public String reAttachMessage(ImmutableConnectorMessage message, boolean reattach) {
        String messageData = null;
        if (message.getEncoded() != null && message.getEncoded().getContent() != null) {
            messageData = message.getEncoded().getContent();
        } else if (message.getRaw() != null) {
            messageData = message.getRaw().getContent();
        }

        return StringUtils.newString(reAttachMessage(messageData, message, Constants.ATTACHMENT_CHARSET, false, reattach), Constants.ATTACHMENT_CHARSET);
    }

    @Override
    public String reAttachMessage(String raw, ConnectorMessage message, boolean reattach) {
        return reAttachMessage(raw, new ImmutableConnectorMessage(message), reattach);
    }

    public String reAttachMessage(String raw, ImmutableConnectorMessage message, boolean reattach) {
        return StringUtils.newString(reAttachMessage(raw, message, Constants.ATTACHMENT_CHARSET, false, reattach), Constants.ATTACHMENT_CHARSET);
    }

    public byte[] reAttachMessage(String raw, ImmutableConnectorMessage connectorMessage, String charsetEncoding, boolean binary, boolean reattach) {
        return reAttachMessage(raw, connectorMessage, charsetEncoding, binary, reattach, false);
    }

    /**
     * Replaces any unique attachment tokens (e.g. "${ATTACH:id}") with the corresponding attachment
     * content, and returns the full post-replacement message as a byte array.
     * 
     * @param raw
     *            The raw message string to replace tokens from.
     * @param connectorMessage
     *            The ConnectorMessage associated with this message, used to identify the
     *            channel/message ID.
     * @param charsetEncoding
     *            If binary mode is not used, the resulting byte array will be encoded using this
     *            charset.
     * @param binary
     *            If enabled, the raw data is assumed to be Base64 encoded. The resulting byte array
     *            will be the raw Base64 decoded bytes.
     * @param reattach
     *            If true, attachment tokens will be replaced with the actual attachment content.
     *            Otherwise, local attachment tokens will be replaced only with the corresponding
     *            expanded tokens.
     * @param localOnly
     *            If true, only local attachment tokens will be replaced, and expanded tokens will
     *            be ignored. This is for the use-case of reprocessing messages.
     * @return The resulting message as a byte array, with all applicable attachment content
     *         re-inserted.
     */
    public byte[] reAttachMessage(String raw, ImmutableConnectorMessage connectorMessage, String charsetEncoding, boolean binary, boolean reattach, boolean localOnly) {
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
            if (reattach && raw.trim().equals(PREFIX + DICOM_KEY + SUFFIX)) {
                dicomObject = DICOMMessageUtil.getDICOMRawBytes(connectorMessage);

                if (!binary) {
                    dicomObject = Base64Util.encodeBase64(dicomObject);
                }

                return dicomObject;
            }

            // Check the raw string in one pass for any attachments.
            // Stores the start and end indices to replace, along with the attachment content.
            while ((index = raw.indexOf(PREFIX, index)) != -1) {
                // Check for special DICOM token first
                if (raw.startsWith(DICOM_KEY + SUFFIX, index + PREFIX.length())) {
                    endIndex = index + PREFIX.length() + DICOM_KEY.length() + SUFFIX.length();

                    // Don't actually make any replacement but still advance the index if not reattaching
                    if (reattach) {
                        if (dicomObject == null) {
                            // Unfortunately, if the dicom data needs to appended to other base64 data, it must be done so in base64.
                            dicomObject = Base64Util.encodeBase64(DICOMMessageUtil.getDICOMRawBytes(connectorMessage));
                        }

                        Map<Integer, Object> replacementMap = new HashMap<Integer, Object>();
                        replacementMap.put(KEY_END_INDEX, endIndex);
                        replacementMap.put(KEY_DATA, dicomObject);
                        replacementObjects.put(index, replacementMap);

                        bufferSize += dicomObject.length;
                    }

                    index += endIndex - index;
                } else if (raw.startsWith(ATTACHMENT_KEY, index + PREFIX.length())) {
                    // Get the index of the actual attachment ID 
                    int idStartIndex = index + PREFIX.length() + ATTACHMENT_KEY.length();
                    // Advance the end index
                    endIndex = idStartIndex;

                    // Make sure there are enough characters left in the message for an attachment ID
                    if (endIndex + ATTACHMENT_ID_LENGTH <= raw.length()) {
                        endIndex += ATTACHMENT_ID_LENGTH;

                        // We can safely substring the attachment ID now
                        String attachmentOrChannelId = raw.substring(idStartIndex, endIndex);

                        // Make sure the next characters are the suffix
                        if (endIndex + SUFFIX.length() <= raw.length() && raw.substring(endIndex, endIndex + SUFFIX.length()).equals(SUFFIX)) {
                            // At this point we know it's a regular attachment, ${ATTACH:id}
                            // Advance the end index
                            endIndex += SUFFIX.length();

                            // A replacement is going to be made one way or the other, so initialize the replacement map
                            Map<Integer, Object> replacementMap = new HashMap<Integer, Object>();
                            replacementMap.put(KEY_END_INDEX, endIndex);

                            if (reattach) {
                                // Initialize the attachment map if necessary
                                if (attachmentMap == null) {
                                    List<Attachment> list = getMessageAttachments(connectorMessage);

                                    // Store the attachments in a map with the attachment's Id as the key
                                    attachmentMap = new HashMap<String, Attachment>();
                                    for (Attachment attachment : list) {
                                        attachmentMap.put(attachment.getId(), attachment);
                                    }
                                }

                                if (attachmentMap.containsKey(attachmentOrChannelId)) {
                                    // If the attachment is found, put the contents into the replacement map
                                    Attachment attachment = attachmentMap.get(attachmentOrChannelId);

                                    attachment.setContent(replaceOutboundAttachment(attachment.getContent()));
                                    replacementMap.put(KEY_DATA, attachment.getContent());
                                    bufferSize += attachment.getContent().length;
                                } else {
                                    // Otherwise, replace with nothing
                                    replacementMap.put(KEY_DATA, new byte[0]);
                                }
                            } else {
                                // Replace with the expanded token
                                replacementMap.put(KEY_DATA, (PREFIX + ATTACHMENT_KEY + connectorMessage.getChannelId() + DELIMITER + connectorMessage.getMessageId() + DELIMITER + attachmentOrChannelId + SUFFIX).getBytes(Constants.ATTACHMENT_CHARSET));
                            }

                            replacementObjects.put(index, replacementMap);
                        } else if (reattach && !localOnly && endIndex + DELIMITER.length() <= raw.length() && raw.substring(endIndex, endIndex + DELIMITER.length()).equals(DELIMITER)) {
                            // The suffix wasn't found, so assume this is an absolute attachment, ${ATTACH:channelId:messageId:attachmentId}
                            // Check if the next characters are the delimiter
                            // Advance the end index
                            endIndex += DELIMITER.length();

                            // The previously used attachment ID is now assumed to be the channel ID
                            String channelId = attachmentOrChannelId;

                            // Find the next delimiter without taking substrings
                            int nextDelimIndex = StringUtil.indexOf(raw, DELIMITER, endIndex, endIndex + MESSAGE_ID_MAX_LENGTH);

                            if (nextDelimIndex != -1) {
                                // If a delimiter is found, assume we have the message ID
                                String messageIdStr = raw.substring(endIndex, nextDelimIndex);
                                // Advance the end index
                                endIndex = nextDelimIndex + DELIMITER.length();

                                // Attempt to parse the message ID
                                long messageId = NumberUtils.toLong(messageIdStr);

                                // If the message ID is valid and there are enough characters for the attachment ID and suffix
                                if (messageId > 0 && endIndex + ATTACHMENT_ID_LENGTH + SUFFIX.length() <= raw.length()) {
                                    // Assume the next characters are the attachment ID
                                    attachmentOrChannelId = raw.substring(endIndex, endIndex + ATTACHMENT_ID_LENGTH);
                                    endIndex += ATTACHMENT_ID_LENGTH;

                                    // Make sure the next characters are the suffix
                                    if (raw.substring(endIndex, endIndex + SUFFIX.length()).equals(SUFFIX)) {
                                        endIndex += SUFFIX.length();

                                        // Initialize the replacement map
                                        Map<Integer, Object> replacementMap = new HashMap<Integer, Object>();
                                        replacementMap.put(KEY_END_INDEX, endIndex);

                                        // Check if the replacement token references the current message
                                        if (channelId.equals(connectorMessage.getChannelId()) && messageId == connectorMessage.getMessageId()) {
                                            // Initialize the attachment map if necessary
                                            if (attachmentMap == null) {
                                                List<Attachment> list = getMessageAttachments(connectorMessage);

                                                // Store the attachments in a map with the attachment's Id as the key
                                                attachmentMap = new HashMap<String, Attachment>();
                                                for (Attachment attachment : list) {
                                                    attachmentMap.put(attachment.getId(), attachment);
                                                }
                                            }

                                            if (attachmentMap.containsKey(attachmentOrChannelId)) {
                                                // If the attachment is found, put the contents into the replacement map
                                                Attachment attachment = attachmentMap.get(attachmentOrChannelId);

                                                attachment.setContent(replaceOutboundAttachment(attachment.getContent()));
                                                replacementMap.put(KEY_DATA, attachment.getContent());
                                                bufferSize += attachment.getContent().length;
                                            } else {
                                                // Otherwise, replace with nothing
                                                replacementMap.put(KEY_DATA, new byte[0]);
                                            }
                                        } else {
                                            // Grab the attachment using the absolute channel/message IDs
                                            Attachment attachment = getMessageAttachment(channelId, messageId, attachmentOrChannelId);

                                            if (attachmentOrChannelId.equals(attachment.getId())) {
                                                // If the attachment is found, put the contents into the replacement map
                                                attachment.setContent(replaceOutboundAttachment(attachment.getContent()));
                                                replacementMap.put(KEY_DATA, attachment.getContent());
                                                bufferSize += attachment.getContent().length;
                                            } else {
                                                // Otherwise, replace with nothing
                                                replacementMap.put(KEY_DATA, new byte[0]);
                                            }
                                        }

                                        replacementObjects.put(index, replacementMap);
                                    }
                                }
                            }
                        }
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

    public List<String> getMessageAttachmentIds(ImmutableConnectorMessage message) throws MessageSerializerException {
        return getMessageAttachmentIds(message.getChannelId(), message.getMessageId());
    }

    public List<String> getMessageAttachmentIds(String channelId, Long messageId) throws MessageSerializerException {
        List<String> attachmentIds = new ArrayList<String>();
        try {
            List<Attachment> attachments = messageController.getMessageAttachmentIds(channelId, messageId);

            for (Attachment attachment : attachments) {
                attachmentIds.add(attachment.getId());
            }
        } catch (Exception e) {
            throw new MessageSerializerException(e.getMessage());
        }
        return attachmentIds;
    }

    public List<Attachment> getMessageAttachments(ImmutableConnectorMessage message) throws MessageSerializerException {
        return getMessageAttachments(message.getChannelId(), message.getMessageId());
    }

    public List<Attachment> getMessageAttachments(ImmutableConnectorMessage message, boolean base64Decode) throws MessageSerializerException {
        return getMessageAttachments(message.getChannelId(), message.getMessageId(), base64Decode);
    }

    public List<Attachment> getMessageAttachments(String channelId, Long messageId) throws MessageSerializerException {
        return getMessageAttachments(channelId, messageId, false);
    }

    public List<Attachment> getMessageAttachments(String channelId, Long messageId, boolean base64Decode) throws MessageSerializerException {
        List<Attachment> attachments;
        try {
            attachments = messageController.getMessageAttachment(channelId, messageId);

            if (base64Decode) {
                AttachmentUtil.decodeBase64(attachments);
            }
        } catch (Exception e) {
            throw new MessageSerializerException(e.getMessage());
        }
        return attachments;
    }

    public Attachment getMessageAttachment(String channelId, Long messageId, String attachmentId) throws MessageSerializerException {
        return getMessageAttachment(channelId, messageId, attachmentId, false);
    }

    public Attachment getMessageAttachment(String channelId, Long messageId, String attachmentId, boolean base64Decode) throws MessageSerializerException {
        try {
            Attachment attachment = messageController.getMessageAttachment(channelId, attachmentId, messageId);

            if (base64Decode) {
                AttachmentUtil.decodeBase64(attachment);
            }

            return attachment;
        } catch (Exception e) {
            throw new MessageSerializerException(e.getMessage());
        }
    }
}
