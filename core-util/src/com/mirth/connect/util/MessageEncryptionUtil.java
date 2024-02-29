/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.commons.encryption.Encryptor.EncryptedData;
import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ErrorContent;
import com.mirth.connect.donkey.model.message.MapContent;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MessageEncryptionUtil {

    public static void decryptMessage(Message message, Encryptor encryptor) {
        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            decryptConnectorMessage(connectorMessage, encryptor);
        }

        if (message.getAttachments() != null) {
            for (Attachment attachment : message.getAttachments()) { //test archiving
                decryptAttachment(attachment, encryptor);
            }
        }
    }

    public static void decryptConnectorMessage(ConnectorMessage connectorMessage, Encryptor encryptor) {
        if (connectorMessage != null) {
            decryptMessageContent(connectorMessage.getRaw(), encryptor);
            decryptMessageContent(connectorMessage.getProcessedRaw(), encryptor);
            decryptMessageContent(connectorMessage.getTransformed(), encryptor);
            decryptMessageContent(connectorMessage.getEncoded(), encryptor);
            decryptMessageContent(connectorMessage.getSent(), encryptor);
            decryptMessageContent(connectorMessage.getResponse(), encryptor);
            decryptMessageContent(connectorMessage.getResponseTransformed(), encryptor);
            decryptMessageContent(connectorMessage.getProcessedResponse(), encryptor);
            decryptMapContent(connectorMessage.getSourceMapContent(), encryptor);
            decryptMapContent(connectorMessage.getConnectorMapContent(), encryptor);
            decryptMapContent(connectorMessage.getChannelMapContent(), encryptor);
            decryptMapContent(connectorMessage.getResponseMapContent(), encryptor);
            decryptErrorContent(connectorMessage.getProcessingErrorContent(), encryptor);
            decryptErrorContent(connectorMessage.getPostProcessorErrorContent(), encryptor);
            decryptErrorContent(connectorMessage.getResponseErrorContent(), encryptor);
            decryptMetaDataMap(connectorMessage.getMetaDataMap(), encryptor);
        }
    }

    public static void decryptMessageContent(MessageContent content, Encryptor encryptor) {
        if (content != null) {
            if (content.getContent() != null && content.isEncrypted()) {
                content.setContent(encryptor.decrypt(content.getContent()));
                content.setEncrypted(false);
            }
        }
    }

    public static void decryptMapContent(MapContent content, Encryptor encryptor) {
        if (content != null) {
            if (content.getContent() != null && content.isEncrypted()) {
                content.setMap(MapUtil.deserializeMap(ObjectXMLSerializer.getInstance(), encryptor.decrypt((String) content.getContent())));
                content.setEncrypted(false);
            }
        }
    }

    public static void decryptErrorContent(ErrorContent content, Encryptor encryptor) {
        if (content != null) {
            if (content.getContent() != null && content.isEncrypted()) {
                content.setContent(encryptor.decrypt(content.getContent()));
                content.setEncrypted(false);
            }
        }
    }

    public static void decryptAttachment(Attachment attachment, Encryptor encryptor) {
        if (attachment != null && attachment.getContent() != null && attachment.isEncrypted()) {
            attachment.setContent(encryptor.decrypt(attachment.getEncryptionHeader(), attachment.getContent()));
            attachment.setEncryptionHeader(null);
            attachment.setEncrypted(false);
        }
    }

    public static void decryptMetaDataMap(Map<String, Object> metaDataMap, Encryptor encryptor) {
        if (metaDataMap != null) {
            for (String key : metaDataMap.keySet().toArray(new String[metaDataMap.size()])) {
                Object value = metaDataMap.get(key);
                if (value instanceof String && StringUtils.startsWith((String) value, KeyEncryptor.HEADER_INDICATOR)) {
                    metaDataMap.put(key, encryptor.decrypt((String) value));
                }
            }
        }
    }

    public static void encryptMessage(Message message, Encryptor encryptor) {
        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            encryptConnectorMessage(connectorMessage, encryptor);
        }

        if (message.getAttachments() != null) {
            for (Attachment attachment : message.getAttachments()) {
                encryptAttachment(attachment, encryptor);
            }
        }
    }

    public static void encryptConnectorMessage(ConnectorMessage connectorMessage, Encryptor encryptor) {
        if (connectorMessage != null) {
            encryptMessageContent(connectorMessage.getRaw(), encryptor);
            encryptMessageContent(connectorMessage.getProcessedRaw(), encryptor);
            encryptMessageContent(connectorMessage.getTransformed(), encryptor);
            encryptMessageContent(connectorMessage.getEncoded(), encryptor);
            encryptMessageContent(connectorMessage.getSent(), encryptor);
            encryptMessageContent(connectorMessage.getResponse(), encryptor);
            encryptMessageContent(connectorMessage.getResponseTransformed(), encryptor);
            encryptMessageContent(connectorMessage.getProcessedResponse(), encryptor);
            encryptMapContent(connectorMessage.getSourceMapContent(), encryptor);
            encryptMapContent(connectorMessage.getConnectorMapContent(), encryptor);
            encryptMapContent(connectorMessage.getChannelMapContent(), encryptor);
            encryptMapContent(connectorMessage.getResponseMapContent(), encryptor);
            encryptErrorContent(connectorMessage.getProcessingErrorContent(), encryptor);
            encryptErrorContent(connectorMessage.getPostProcessorErrorContent(), encryptor);
            encryptErrorContent(connectorMessage.getResponseErrorContent(), encryptor);
            encryptMetaDataMap(connectorMessage.getMetaDataMap(), encryptor);
        }
    }

    public static void encryptMessageContent(MessageContent content, Encryptor encryptor) {
        if (content != null) {
            if (content.getContent() != null && !content.isEncrypted()) {
                content.setContent(encryptor.encrypt(content.getContent()));
                content.setEncrypted(true);
            }
        }
    }

    public static void encryptMapContent(MapContent content, Encryptor encryptor) {
        if (content != null) {
            if (content.getContent() != null && !content.isEncrypted()) {
                content.setContent(encryptor.encrypt(MapUtil.serializeMap(ObjectXMLSerializer.getInstance(), content.getMap())));
                content.setEncrypted(true);
            }
        }
    }

    public static void encryptErrorContent(ErrorContent content, Encryptor encryptor) {
        if (content != null) {
            if (content.getContent() != null && !content.isEncrypted()) {
                content.setContent(encryptor.encrypt(content.getContent()));
                content.setEncrypted(true);
            }
        }
    }

    public static void encryptAttachment(Attachment attachment, Encryptor encryptor) {
        if (attachment != null && attachment.getContent() != null && !attachment.isEncrypted()) {
            EncryptedData result = encryptor.encrypt(attachment.getContent());
            attachment.setEncryptionHeader(result.getHeader());
            attachment.setContent(result.getEncryptedData());
            attachment.setEncrypted(true);
        }
    }

    public static void encryptMetaDataMap(Map<String, Object> metaDataMap, Encryptor encryptor) {
        if (metaDataMap != null) {
            for (String key : metaDataMap.keySet().toArray(new String[metaDataMap.size()])) {
                Object value = metaDataMap.get(key);
                if (value instanceof String && !StringUtils.startsWith((String) value, KeyEncryptor.HEADER_INDICATOR)) {
                    metaDataMap.put(key, encryptor.encrypt((String) value));
                }
            }
        }
    }
}