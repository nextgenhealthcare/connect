package com.mirth.connect.util;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;

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
            decryptMessageContent(connectorMessage.getProcessedResponse(), encryptor);
        }
    }

    public static void decryptMessageContent(MessageContent content, Encryptor encryptor) {
        if (content != null) {
            if (content.getContent() == null) {
                String encryptedContent = content.getEncryptedContent();
                
                if (encryptedContent != null) {
                    content.setContent(encryptor.decrypt(encryptedContent));
                }
            }
            
            content.setEncryptedContent(null);
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
            encryptMessageContent(connectorMessage.getProcessedResponse(), encryptor);
        }
    }

    public static void encryptMessageContent(MessageContent content, Encryptor encryptor) {
        if (content != null) {
            if (content.getEncryptedContent() == null) {
                String unencryptedContent = content.getContent();
                
                if (unencryptedContent != null) {
                    content.setEncryptedContent(encryptor.encrypt(unencryptedContent));
                }
            }
            
            content.setContent(null);
        }
    }
}
