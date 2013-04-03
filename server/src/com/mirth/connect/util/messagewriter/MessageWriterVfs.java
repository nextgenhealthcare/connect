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
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.MessageEncryptionUtil;

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
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

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

            String content = (contentType == null) ? toXml(message) : extractContent(message);

            if (StringUtils.isNotBlank(content)) {
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

        return serializer.toXML(message);
    }

    private String extractContent(Message message) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Entry<Integer, ConnectorMessage> entry : message.getConnectorMessages().entrySet()) {
            Integer metaDataId = entry.getKey();
            ConnectorMessage connectorMessage = entry.getValue();

            if (((destinationContent && metaDataId != 0) || (!destinationContent && metaDataId == 0))) {
                MessageContent messageContent = connectorMessage.getContent(contentType);

                if (messageContent != null) {
                    String unencryptedContent = messageContent.getContent();
                    String encryptedContent = messageContent.getEncryptedContent();
                    String content = null;

                    if (encrypted) {
                        if (encryptedContent != null) {
                            content = encryptedContent;
                        } else if (unencryptedContent != null && StringUtils.isNotBlank(unencryptedContent)) {
                            content = encryptor.encrypt(unencryptedContent);
                        }
                    } else {
                        if (unencryptedContent != null) {
                            content = unencryptedContent;
                        } else if (encryptedContent != null && StringUtils.isNotBlank(encryptedContent)) {
                            content = encryptor.decrypt(encryptedContent);
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
