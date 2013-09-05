/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ErrorContent;
import com.mirth.connect.donkey.model.message.MapContent;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MessageEncryptionUtil {

    public static void decryptMessage(Message message, Encryptor encryptor) {
        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            decryptConnectorMessage(connectorMessage, encryptor);
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
            decryptMapContent(connectorMessage.getConnectorMapContent(), encryptor);
            decryptMapContent(connectorMessage.getChannelMapContent(), encryptor);
            decryptMapContent(connectorMessage.getResponseMapContent(), encryptor);
            decryptErrorContent(connectorMessage.getProcessingErrorContent(), encryptor);
            decryptErrorContent(connectorMessage.getPostProcessorErrorContent(), encryptor);
            decryptErrorContent(connectorMessage.getResponseErrorContent(), encryptor);
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

    public static void encryptMessage(Message message, Encryptor encryptor) {
        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            encryptConnectorMessage(connectorMessage, encryptor);
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
            encryptMapContent(connectorMessage.getConnectorMapContent(), encryptor);
            encryptMapContent(connectorMessage.getChannelMapContent(), encryptor);
            encryptMapContent(connectorMessage.getResponseMapContent(), encryptor);
            encryptErrorContent(connectorMessage.getProcessingErrorContent(), encryptor);
            encryptErrorContent(connectorMessage.getPostProcessorErrorContent(), encryptor);
            encryptErrorContent(connectorMessage.getResponseErrorContent(), encryptor);
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
}