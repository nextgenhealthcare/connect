/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.MessageEncryptionUtil;
import com.mirth.connect.util.ValueReplacer;

public class MessageWriterVfs implements MessageWriter {
    private String uri;
    private String filePattern;
    private ContentType contentType;
    private boolean destinationContent;
    private boolean encrypted;
    private Encryptor encryptor;
    private String currentFile;
    private FileObject currentFileObject;
    private Writer writer;
    private ValueReplacer replacer = new ValueReplacer();
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

    public MessageWriterVfs(String uri, String exportFilePattern, ContentType contentType, boolean destinationContent, boolean encrypted, Encryptor encryptor) {
        this.uri = uri;
        this.filePattern = exportFilePattern;
        this.contentType = contentType;
        this.destinationContent = destinationContent;
        this.encrypted = encrypted;
        this.encryptor = encryptor;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public boolean isDestinationContent() {
        return destinationContent;
    }

    public void setDestinationContent(boolean destinationContent) {
        this.destinationContent = destinationContent;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public boolean write(Message message) throws MessageWriterException {
        try {
            String content = (contentType == null) ? toXml(message) : extractContent(message);

            if (StringUtils.isNotBlank(content)) {
                String file = uri + IOUtils.DIR_SEPARATOR + replacer.replaceValues(filePattern, message);

                if (!file.equals(currentFile)) {
                    if (writer != null) {
                        writer.close();
                    }

                    if (currentFileObject != null) {
                        currentFileObject.close();
                    }

                    currentFile = file;
                    currentFileObject = VFS.getManager().resolveFile(file);

                    if (currentFileObject.getType() == FileType.FOLDER) {
                        throw new MessageWriterException("Cannot save message to file \"" + file + "\", it is a directory");
                    }

                    writer = new OutputStreamWriter(currentFileObject.getContent().getOutputStream(true));
                }

                writer.write(content);
                writer.append(IOUtils.LINE_SEPARATOR_WINDOWS); // the VFS output stream requires windows newlines
                writer.flush();
                return true;
            }

            return false;
        } catch (Exception e) {
            throw new MessageWriterException(e);
        }
    }

    private String toXml(Message message) {
        if (encrypted) {
            MessageEncryptionUtil.encryptMessage(message, encryptor);
        } else {
            MessageEncryptionUtil.decryptMessage(message, encryptor);
        }

        return serializer.serialize(message);
    }

    private String extractContent(Message message) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Entry<Integer, ConnectorMessage> entry : message.getConnectorMessages().entrySet()) {
            Integer metaDataId = entry.getKey();
            ConnectorMessage connectorMessage = entry.getValue();

            if (((destinationContent && metaDataId != 0) || (!destinationContent && metaDataId == 0))) {
                MessageContent messageContent = connectorMessage.getMessageContent(contentType);

                if (messageContent != null) {
                    String content = messageContent.getContent();

                    if (encrypted) {
                        if (!messageContent.isEncrypted()) {
                            content = encryptor.encrypt(content);
                        }
                    } else {
                        if (messageContent.isEncrypted()) {
                            content = encryptor.decrypt(content);
                        }
                    }

                    if (StringUtils.isNotBlank(content)) {
                        stringBuilder.append(content);
                        stringBuilder.append(IOUtils.LINE_SEPARATOR_WINDOWS); // the VFS output stream requires windows newlines
                        stringBuilder.append(IOUtils.LINE_SEPARATOR_WINDOWS);
                    }
                }
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public void close() throws MessageWriterException {
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
                throw new MessageWriterException(e);
            }
        }

        if (currentFileObject != null) {
            try {
                currentFileObject.close();
            } catch (Exception e) {
                throw new MessageWriterException(e);
            }
        }
    }
}
