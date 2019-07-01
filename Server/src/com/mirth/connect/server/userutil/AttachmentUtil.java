/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.controllers.UnsupportedDataTypeException;
import com.mirth.connect.server.attachments.MirthAttachmentHandlerProvider;
import com.mirth.connect.server.attachments.passthru.PassthruAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

/**
 * Provides utility methods for creating, retrieving, and re-attaching message attachments.
 */
public class AttachmentUtil {
    private static EngineController engineController = ControllerFactory.getFactory().createEngineController();

    private AttachmentUtil() {}

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
     * @return The resulting message as a byte array, with all applicable attachment content
     *         re-inserted.
     */
    public static byte[] reAttachMessage(String raw, ImmutableConnectorMessage connectorMessage, String charsetEncoding, boolean binary) {
        return getAttachmentHandlerProvider(connectorMessage.getChannelId()).reAttachMessage(raw, connectorMessage, charsetEncoding, binary, true);
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
     *            be ignored.
     * @return The resulting message as a byte array, with all applicable attachment content
     *         re-inserted.
     */
    public static byte[] reAttachMessage(String raw, ImmutableConnectorMessage connectorMessage, String charsetEncoding, boolean binary, boolean reattach, boolean localOnly) {
        return getAttachmentHandlerProvider(connectorMessage.getChannelId()).reAttachMessage(raw, connectorMessage, charsetEncoding, binary, reattach, localOnly);
    }

    /**
     * Replaces any unique attachment tokens (e.g. "${ATTACH:id}") with the corresponding attachment
     * content, and returns the full post-replacement message.
     * 
     * @param connectorMessage
     *            The ConnectorMessage associated with this message, used to identify the
     *            channel/message ID. The message string will be either the encoded or raw content.
     * @return The resulting message with all applicable attachment content re-inserted.
     */
    public static String reAttachMessage(ImmutableConnectorMessage connectorMessage) {
        return getAttachmentHandlerProvider(connectorMessage.getChannelId()).reAttachMessage(connectorMessage, true);
    }

    /**
     * Replaces any unique attachment tokens (e.g. "${ATTACH:id}") with the corresponding attachment
     * content, and returns the full post-replacement message.
     * 
     * @param raw
     *            The raw message string to replace tokens from.
     * @param connectorMessage
     *            The ConnectorMessage associated with this message, used to identify the
     *            channel/message ID.
     * @return The resulting message with all applicable attachment content re-inserted.
     */
    public static String reAttachMessage(String raw, ImmutableConnectorMessage connectorMessage) {
        return getAttachmentHandlerProvider(connectorMessage.getChannelId()).reAttachMessage(raw, connectorMessage, true);
    }

    /**
     * Returns a List of attachment IDs associated with the current channel / message.
     * 
     * @param connectorMessage
     *            The ConnectorMessage associated with this message, used to identify the
     *            channel/message ID.
     * @return A List of attachment IDs associated with the current channel / message.
     * @throws MessageSerializerException
     *             If the attachment IDs could be retrieved.
     */
    public static List<String> getMessageAttachmentIds(ImmutableConnectorMessage connectorMessage) throws MessageSerializerException {
        return getAttachmentHandlerProvider(connectorMessage.getChannelId()).getMessageAttachmentIds(connectorMessage);
    }

    /**
     * Returns a List of attachment IDs associated with the current channel / message.
     * 
     * @param channelId
     *            The ID of the channel the attachments are associated with.
     * @param messageId
     *            The ID of the message the attachments are associated with.
     * @return A List of attachment IDs associated with the current channel / message.
     * @throws MessageSerializerException
     *             If the attachment IDs could be retrieved.
     */
    public static List<String> getMessageAttachmentIds(String channelId, Long messageId) throws MessageSerializerException {
        return getAttachmentHandlerProvider(channelId).getMessageAttachmentIds(channelId, messageId);
    }

    /**
     * Retrieves all attachments associated with a connector message.
     * 
     * @param connectorMessage
     *            The ConnectorMessage associated with this message, used to identify the
     *            channel/message ID.
     * @return A list of attachments associated with the connector message.
     * @throws MessageSerializerException
     *             If the attachments could not be retrieved.
     */
    public static List<Attachment> getMessageAttachments(ImmutableConnectorMessage connectorMessage) throws MessageSerializerException {
        return convertFromDonkeyAttachmentList(getAttachmentHandlerProvider(connectorMessage.getChannelId()).getMessageAttachments(connectorMessage));
    }

    /**
     * Retrieves all attachments associated with a connector message.
     * 
     * @param connectorMessage
     *            The ConnectorMessage associated with this message, used to identify the
     *            channel/message ID.
     * @param base64Decode
     *            If true, the content of each attachment will first be Base64 decoded for
     *            convenient use.
     * @return A list of attachments associated with the connector message.
     * @throws MessageSerializerException
     *             If the attachments could not be retrieved.
     */
    public static List<Attachment> getMessageAttachments(ImmutableConnectorMessage connectorMessage, boolean base64Decode) throws MessageSerializerException {
        return convertFromDonkeyAttachmentList(getAttachmentHandlerProvider(connectorMessage.getChannelId()).getMessageAttachments(connectorMessage, base64Decode));
    }

    /**
     * Retrieves all attachments associated with a specific channel/message ID.
     * 
     * @param channelId
     *            The ID of the channel to retrieve the attachments from.
     * @param messageId
     *            The ID of the message to retrieve the attachments from.
     * 
     * @return A list of attachments associated with the channel/message ID.
     * @throws MessageSerializerException
     *             If the attachments could not be retrieved.
     */
    public static List<Attachment> getMessageAttachments(String channelId, Long messageId) throws MessageSerializerException {
        return convertFromDonkeyAttachmentList(getAttachmentHandlerProvider(channelId).getMessageAttachments(channelId, messageId));
    }

    /**
     * Retrieves all attachments associated with a specific channel/message ID.
     * 
     * @param channelId
     *            The ID of the channel to retrieve the attachments from.
     * @param messageId
     *            The ID of the message to retrieve the attachments from.
     * @param base64Decode
     *            If true, the content of each attachment will first be Base64 decoded for
     *            convenient use.
     * 
     * @return A list of attachments associated with the channel/message ID.
     * @throws MessageSerializerException
     *             If the attachments could not be retrieved.
     */
    public static List<Attachment> getMessageAttachments(String channelId, Long messageId, boolean base64Decode) throws MessageSerializerException {
        return convertFromDonkeyAttachmentList(getAttachmentHandlerProvider(channelId).getMessageAttachments(channelId, messageId, base64Decode));
    }

    /**
     * Retrieves an attachment from the current channel/message ID.
     * 
     * @param connectorMessage
     *            The ConnectorMessage associated with this message, used to identify the
     *            channel/message ID.
     * @param attachmentId
     *            The ID of the attachment to retrieve.
     * 
     * @return The attachment associated with the given IDs, or null if none was found.
     * @throws MessageSerializerException
     *             If the attachment could not be retrieved.
     */
    public static Attachment getMessageAttachment(ImmutableConnectorMessage connectorMessage, String attachmentId) throws MessageSerializerException {
        return getMessageAttachment(connectorMessage, attachmentId, false);
    }

    /**
     * Retrieves an attachment from the current channel/message ID.
     * 
     * @param connectorMessage
     *            The ConnectorMessage associated with this message, used to identify the
     *            channel/message ID.
     * @param attachmentId
     *            The ID of the attachment to retrieve.
     * @param base64Decode
     *            If true, the content of each attachment will first be Base64 decoded for
     *            convenient use.
     * 
     * @return The attachment associated with the given IDs, or null if none was found.
     * @throws MessageSerializerException
     *             If the attachment could not be retrieved.
     */
    public static Attachment getMessageAttachment(ImmutableConnectorMessage connectorMessage, String attachmentId, boolean base64Decode) throws MessageSerializerException {
        return getMessageAttachment(connectorMessage.getChannelId(), connectorMessage.getMessageId(), attachmentId, base64Decode);
    }

    /**
     * Retrieves an attachment from a specific channel/message ID.
     * 
     * @param channelId
     *            The ID of the channel to retrieve the attachment from.
     * @param messageId
     *            The ID of the message to retrieve the attachment from.
     * @param attachmentId
     *            The ID of the attachment to retrieve.
     * 
     * @return The attachment associated with the given IDs, or null if none was found.
     * @throws MessageSerializerException
     *             If the attachment could not be retrieved.
     */
    public static Attachment getMessageAttachment(String channelId, Long messageId, String attachmentId) throws MessageSerializerException {
        Attachment attachment = convertFromDonkeyAttachment(getAttachmentHandlerProvider(channelId).getMessageAttachment(channelId, messageId, attachmentId));
        return StringUtils.equals(attachment.getId(), attachmentId) ? attachment : null;
    }

    /**
     * Retrieves an attachment from a specific channel/message ID.
     * 
     * @param channelId
     *            The ID of the channel to retrieve the attachment from.
     * @param messageId
     *            The ID of the message to retrieve the attachment from.
     * @param attachmentId
     *            The ID of the attachment to retrieve.
     * @param base64Decode
     *            If true, the content of each attachment will first be Base64 decoded for
     *            convenient use.
     * 
     * @return The attachment associated with the given IDs, or null if none was found.
     * @throws MessageSerializerException
     *             If the attachment could not be retrieved.
     */
    public static Attachment getMessageAttachment(String channelId, Long messageId, String attachmentId, boolean base64Decode) throws MessageSerializerException {
        Attachment attachment = convertFromDonkeyAttachment(getAttachmentHandlerProvider(channelId).getMessageAttachment(channelId, messageId, attachmentId, base64Decode));
        return StringUtils.equals(attachment.getId(), attachmentId) ? attachment : null;
    }

    /**
     * Retrieves an attachment from an upstream channel that sent a message to the current channel.
     * 
     * @param connectorMessage
     *            The ConnectorMessage associated with this message. The channel ID and message ID
     *            will be retrieved from the source map.
     * 
     * @return A list of attachments associated with the source channel/message IDs.
     * @throws MessageSerializerException
     *             If the attachments could not be retrieved.
     */
    public static List<Attachment> getMessageAttachmentsFromSourceChannel(ImmutableConnectorMessage connectorMessage) throws MessageSerializerException {
        return getMessageAttachmentsFromSourceChannel(connectorMessage, false);
    }

    /**
     * Retrieves an attachment from an upstream channel that sent a message to the current channel.
     * 
     * @param connectorMessage
     *            The ConnectorMessage associated with this message. The channel ID and message ID
     *            will be retrieved from the source map.
     * @param base64Decode
     *            If true, the content of each attachment will first be Base64 decoded for
     *            convenient use.
     * 
     * @return A list of attachments associated with the source channel/message IDs.
     * @throws MessageSerializerException
     *             If the attachments could not be retrieved.
     */
    @SuppressWarnings("unchecked")
    public static List<Attachment> getMessageAttachmentsFromSourceChannel(ImmutableConnectorMessage connectorMessage, boolean base64Decode) throws MessageSerializerException {
        Map<String, Object> sourceMap = connectorMessage.getSourceMap();

        try {
            String sourceChannelId = (String) sourceMap.get("sourceChannelId");
            Long sourceMessageId = (Long) sourceMap.get("sourceMessageId");

            List<String> sourceChannelIds = (List<String>) sourceMap.get("sourceChannelIds");
            List<Long> sourceMessageIds = (List<Long>) sourceMap.get("sourceMessageIds");
            if (CollectionUtils.isNotEmpty(sourceChannelIds) && CollectionUtils.isNotEmpty(sourceMessageIds)) {
                sourceChannelId = sourceChannelIds.get(0);
                sourceMessageId = sourceMessageIds.get(0);
            }

            if (sourceChannelId != null && sourceMessageId != null) {
                return convertFromDonkeyAttachmentList(getAttachmentHandlerProvider(sourceChannelId).getMessageAttachments(sourceChannelId, sourceMessageId, base64Decode));
            }
        } catch (Exception e) {
        }

        return new ArrayList<Attachment>();
    }

    /**
     * Creates an Attachment and adds it to the provided list.
     * 
     * @param attachments
     *            The list of attachments to add to.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * @return The attachment added to the list.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment addAttachment(List<Attachment> attachments, Object content, String type) throws UnsupportedDataTypeException {
        return addAttachment(attachments, content, type, false);
    }

    /**
     * Creates an Attachment and adds it to the provided list.
     * 
     * @param attachments
     *            The list of attachments to add to.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * @param base64Encode
     *            If true, the content of each attachment will first be Base64 encoded for
     *            convenience.
     * 
     * @return The attachment added to the list.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment addAttachment(List<Attachment> attachments, Object content, String type, boolean base64Encode) throws UnsupportedDataTypeException {
        Attachment userAttachment = convertFromDonkeyAttachment(MessageController.getInstance().createAttachment(content, type, base64Encode));
        attachments.add(userAttachment);
        return userAttachment;
    }

    /**
     * Creates an attachment associated with a given connector message, and inserts it into the
     * database.
     * 
     * @param connectorMessage
     *            The connector message to be associated with the attachment.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * @return The attachment that was created and inserted.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment createAttachment(ImmutableConnectorMessage connectorMessage, Object content, String type) throws UnsupportedDataTypeException {
        return createAttachment(connectorMessage, content, type, false);
    }

    /**
     * Creates an attachment associated with a given connector message, and inserts it into the
     * database.
     * 
     * @param connectorMessage
     *            The connector message to be associated with the attachment.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * @param base64Encode
     *            If true, the content of each attachment will first be Base64 encoded for
     *            convenience.
     * 
     * @return The attachment that was created and inserted.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment createAttachment(ImmutableConnectorMessage connectorMessage, Object content, String type, boolean base64Encode) throws UnsupportedDataTypeException {
        com.mirth.connect.donkey.model.message.attachment.Attachment attachment = MessageController.getInstance().createAttachment(content, type, base64Encode);
        MessageController.getInstance().insertAttachment(attachment, connectorMessage.getChannelId(), connectorMessage.getMessageId());
        return convertFromDonkeyAttachment(attachment);
    }

    /**
     * Updates an attachment associated with a given connector message.
     * 
     * @param connectorMessage
     *            The connector message to be associated with the attachment.
     * @param attachmentId
     *            The unique ID of the attachment to update.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * 
     * @return The attachment that was updated.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment updateAttachment(ImmutableConnectorMessage connectorMessage, String attachmentId, Object content, String type) throws UnsupportedDataTypeException {
        return updateAttachment(connectorMessage, attachmentId, content, type, false);
    }

    /**
     * Updates an attachment associated with a given connector message.
     * 
     * @param connectorMessage
     *            The connector message to be associated with the attachment.
     * @param attachmentId
     *            The unique ID of the attachment to update.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * @param base64Encode
     *            If true, the content of each attachment will first be Base64 encoded for
     *            convenience.
     * 
     * @return The attachment that was updated.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment updateAttachment(ImmutableConnectorMessage connectorMessage, String attachmentId, Object content, String type, boolean base64Encode) throws UnsupportedDataTypeException {
        return updateAttachment(connectorMessage.getChannelId(), connectorMessage.getMessageId(), attachmentId, content, type, base64Encode);
    }

    /**
     * Updates an attachment associated with a given connector message.
     * 
     * @param connectorMessage
     *            The connector message to be associated with the attachment.
     * @param attachment
     *            The Attachment object to update.
     * 
     * @return The attachment that was updated.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment updateAttachment(ImmutableConnectorMessage connectorMessage, Attachment attachment) throws UnsupportedDataTypeException {
        return updateAttachment(connectorMessage, attachment, false);
    }

    /**
     * Updates an attachment associated with a given connector message.
     * 
     * @param connectorMessage
     *            The connector message to be associated with the attachment.
     * @param attachment
     *            The Attachment object to update.
     * @param base64Encode
     *            If true, the content of each attachment will first be Base64 encoded for
     *            convenience.
     * 
     * @return The attachment that was updated.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment updateAttachment(ImmutableConnectorMessage connectorMessage, Attachment attachment, boolean base64Encode) throws UnsupportedDataTypeException {
        return updateAttachment(connectorMessage.getChannelId(), connectorMessage.getMessageId(), attachment.getId(), attachment.getContent(), attachment.getType(), base64Encode);
    }

    /**
     * Updates an attachment associated with a given connector message.
     * 
     * @param channelId
     *            The ID of the channel the attachment is associated with.
     * @param messageId
     *            The ID of the message the attachment is associated with.
     * @param attachment
     *            The Attachment object to update.
     * 
     * @return The attachment that was updated.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment updateAttachment(String channelId, Long messageId, Attachment attachment) throws UnsupportedDataTypeException {
        return updateAttachment(channelId, messageId, attachment, false);
    }

    /**
     * Updates an attachment associated with a given connector message.
     * 
     * @param channelId
     *            The ID of the channel the attachment is associated with.
     * @param messageId
     *            The ID of the message the attachment is associated with.
     * @param attachment
     *            The Attachment object to update.
     * @param base64Encode
     *            If true, the content of each attachment will first be Base64 encoded for
     *            convenience.
     * 
     * @return The attachment that was updated.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment updateAttachment(String channelId, Long messageId, Attachment attachment, boolean base64Encode) throws UnsupportedDataTypeException {
        return updateAttachment(channelId, messageId, attachment.getId(), attachment.getContent(), attachment.getType(), base64Encode);
    }

    /**
     * Updates an attachment associated with a given connector message.
     * 
     * @param channelId
     *            The ID of the channel the attachment is associated with.
     * @param messageId
     *            The ID of the message the attachment is associated with.
     * @param attachmentId
     *            The unique ID of the attachment to update.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * 
     * @return The attachment that was updated.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment updateAttachment(String channelId, Long messageId, String attachmentId, Object content, String type) throws UnsupportedDataTypeException {
        return updateAttachment(channelId, messageId, attachmentId, content, type, false);
    }

    /**
     * Updates an attachment associated with a given connector message.
     * 
     * @param channelId
     *            The ID of the channel the attachment is associated with.
     * @param messageId
     *            The ID of the message the attachment is associated with.
     * @param attachmentId
     *            The unique ID of the attachment to update.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * @param base64Encode
     *            If true, the content of each attachment will first be Base64 encoded for
     *            convenience.
     * 
     * @return The attachment that was updated.
     * @throws UnsupportedDataTypeException
     *             If the attachment content is not a String or byte array.
     */
    public static Attachment updateAttachment(String channelId, Long messageId, String attachmentId, Object content, String type, boolean base64Encode) throws UnsupportedDataTypeException {
        com.mirth.connect.donkey.model.message.attachment.Attachment attachment = MessageController.getInstance().createAttachment(content, type, base64Encode);
        attachment.setId(attachmentId);
        MessageController.getInstance().updateAttachment(attachment, channelId, messageId);
        return convertFromDonkeyAttachment(attachment);
    }

    static List<Attachment> convertFromDonkeyAttachmentList(List<com.mirth.connect.donkey.model.message.attachment.Attachment> attachments) {
        List<Attachment> list = new ArrayList<Attachment>();
        for (com.mirth.connect.donkey.model.message.attachment.Attachment attachment : attachments) {
            list.add(convertFromDonkeyAttachment(attachment));
        }
        return list;
    }

    static List<com.mirth.connect.donkey.model.message.attachment.Attachment> convertToDonkeyAttachmentList(List<Attachment> attachments) {
        List<com.mirth.connect.donkey.model.message.attachment.Attachment> list = new ArrayList<com.mirth.connect.donkey.model.message.attachment.Attachment>();
        for (Attachment attachment : attachments) {
            list.add(convertToDonkeyAttachment(attachment));
        }
        return list;
    }

    static Attachment convertFromDonkeyAttachment(com.mirth.connect.donkey.model.message.attachment.Attachment attachment) {
        return new Attachment(attachment.getId(), attachment.getContent(), attachment.getType());
    }

    static com.mirth.connect.donkey.model.message.attachment.Attachment convertToDonkeyAttachment(Attachment attachment) {
        return new com.mirth.connect.donkey.model.message.attachment.Attachment(attachment.getId(), attachment.getContent(), attachment.getType());
    }

    private static MirthAttachmentHandlerProvider getAttachmentHandlerProvider(String channelId) {
        Channel deployedChannel = engineController.getDeployedChannel(channelId);
        if (deployedChannel != null) {
            MirthAttachmentHandlerProvider provider = (MirthAttachmentHandlerProvider) deployedChannel.getAttachmentHandlerProvider();
            if (provider != null) {
                return provider;
            }
        }
        return new PassthruAttachmentHandlerProvider(com.mirth.connect.server.controllers.MessageController.getInstance());
    }
}
