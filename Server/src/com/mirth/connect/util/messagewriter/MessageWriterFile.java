/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Content;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MapContent;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.MessageEncryptionUtil;
import com.mirth.connect.util.ValueReplacer;

public class MessageWriterFile implements MessageWriter {
    private String path;
    private String filePattern;
    private ContentType contentType;
    private boolean destinationContent;
    private boolean encrypted;
    private Encryptor encryptor;
    private String currentFile;
    private Writer writer;
    private ValueReplacer replacer = new ValueReplacer();
    private Serializer serializer = ObjectXMLSerializer.getInstance();

    public MessageWriterFile(String path, String exportFilePattern, ContentType contentType, boolean destinationContent, boolean encrypted, Encryptor encryptor) {
        this.path = path;
        this.filePattern = exportFilePattern;
        this.contentType = contentType;
        this.destinationContent = destinationContent;
        this.encrypted = encrypted;
        this.encryptor = encryptor;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean write(Message message) throws MessageWriterException {
        try {
            String file = path + IOUtils.DIR_SEPARATOR;
            String content = null;
            boolean replaced = false;

            if (contentType == null) {
                // If we're serializing and encrypting the message, we have to do replacement first
                if (encrypted) {
                    file += replacer.replaceValues(filePattern, message);
                    replaced = true;
                }

                content = toXml(message);
            } else {
                content = extractContent(message);
            }

            if (StringUtils.isNotBlank(content)) {
                // Do the replacement here if we haven't already
                if (!replaced) {
                    file += replacer.replaceValues(filePattern, message);
                }

                if (!file.equals(currentFile)) {
                    if (writer != null) {
                        writer.close();
                    }

                    currentFile = file;
                    File fileObject = new File(file);

                    if (fileObject.isDirectory()) {
                        throw new MessageWriterException("Cannot save message to file \"" + file + "\", it is a directory");
                    }

                    writer = new OutputStreamWriter(FileUtils.openOutputStream(fileObject, true));
                }

                writer.write(content);
                writer.append(IOUtils.LINE_SEPARATOR_WINDOWS); // windows newlines were required previously when commons-vfs was used
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
                Content content = null;

                if (contentType == ContentType.SOURCE_MAP) {
                    content = message.getMergedConnectorMessage().getSourceMapContent();
                } else if (contentType == ContentType.CHANNEL_MAP) {
                    content = message.getMergedConnectorMessage().getChannelMapContent();
                } else if (contentType == ContentType.RESPONSE_MAP) {
                    content = message.getMergedConnectorMessage().getResponseMapContent();
                } else {
                    content = connectorMessage.getMessageContent(contentType);
                }

                if (content != null) {
                    String stringContent = null;
                    boolean contentEncrypted = content.isEncrypted();

                    if (contentType == ContentType.SENT) {
                        String tempContent = (String) content.getContent();

                        if (contentEncrypted) {
                            tempContent = encryptor.decrypt(tempContent);
                            contentEncrypted = false;
                        }

                        if (StringUtils.isNotEmpty(tempContent)) {
                            ConnectorProperties sentContent = serializer.deserialize(tempContent, ConnectorProperties.class);
                            stringContent = sentContent.toFormattedString();
                        }
                    } else if (content instanceof MapContent) {
                        /*
                         * We don't need to check if the content is encrypted because map content is
                         * always decrypted when it is retrieved by JdbcDao
                         */
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tempContent = (Map<String, Object>) content.getContent();
                        if (MapUtils.isNotEmpty(tempContent)) {
                            stringContent = MapUtil.serializeMap(serializer, tempContent);
                        }
                    } else {
                        stringContent = (String) content.getContent();
                    }

                    if (StringUtils.isNotEmpty(stringContent)) {
                        if (encrypted) {
                            if (!contentEncrypted) {
                                stringContent = encryptor.encrypt(stringContent);
                            }
                        } else {
                            if (contentEncrypted) {
                                stringContent = encryptor.decrypt(stringContent);
                            }
                        }

                        if (StringUtils.isNotBlank(stringContent)) {
                            stringBuilder.append(stringContent);
                            stringBuilder.append(IOUtils.LINE_SEPARATOR_WINDOWS); // the VFS output stream requires windows newlines
                            stringBuilder.append(IOUtils.LINE_SEPARATOR_WINDOWS);
                        }
                    }
                }
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public void finishWrite() throws MessageWriterException {}

    @Override
    public void close() throws MessageWriterException {
        currentFile = null;

        if (writer != null) {
            try {
                writer.close();
                writer = null;
            } catch (Exception e) {
                throw new MessageWriterException(e);
            }
        }
    }
}
