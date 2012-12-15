/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.util.MessageEncryptionUtil;

public class MessageExporter {
    private Logger logger = Logger.getLogger(this.getClass());
    private MessageExportOptions options = new MessageExportOptions();
    private MessageRetriever messageRetriever;
    private Encryptor encryptor;
    private Serializer serializer;
    private String dateFormat = "yyyyMMdd";

    public MessageExportOptions getOptions() {
        return options;
    }

    public void setOptions(MessageExportOptions options) {
        this.options = options;
    }

    public MessageRetriever getMessageRetriever() {
        return messageRetriever;
    }

    public void setMessageRetriever(MessageRetriever messageRetriever) {
        this.messageRetriever = messageRetriever;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public int export() throws MessageExporterException {
        if (options.getChannelId() == null) {
            throw new MessageExporterException("Failed to export messages, a channel ID has not been specified");
        }
        
        if (options.getFolder() == null) {
            throw new MessageExporterException("Failed to export messages, an export folder has not been specified");
        }
        
        Writer writer = null;
        ZipOutputStream zipOutputStream = null;
        String folder = options.getFolder();
        String filePrefix = "msgexport_" + new SimpleDateFormat(dateFormat).format(new Date());
        String fileName = null;
        String fileExtension = (options.getContentType() == null) ? ".xml" : ".txt";
        boolean singleFile = options.isSingleFile();
        boolean compress = options.isCompress();
        int bufferSize = options.getBufferSize();
        int exportCount = 0;
        int offset = 0;
        
        if (!folder.substring(folder.length() - 1).equals("/")) {
            folder = new String(folder + "/");
        }
        
        try {
            File folderFile = new File(folder);
            folderFile.mkdirs();
            
            if (!folderFile.exists() || !folderFile.canWrite()) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new MessageExporterUserError("Cannot write to folder: " + folder);
        }

        try {
            if (compress) {
                fileName = folder + filePrefix + ".zip";
                zipOutputStream = new ZipOutputStream(new FileOutputStream(fileName));
                writer = new OutputStreamWriter(zipOutputStream);

                if (singleFile) {
                    zipOutputStream.putNextEntry(new ZipEntry(filePrefix + fileExtension));
                }
            } else if (singleFile) {
                fileName = folder + filePrefix + fileExtension;
                writer = new OutputStreamWriter(new FileOutputStream(fileName));
            }
        } catch (Exception e) {
            throw new MessageExporterException("Failed to write to file: " + fileName, e);
        }

        List<Message> messages = null;

        try {
            messages = messageRetriever.getMessages(options.getChannelId(), options.getMessageFilter(), true, offset, bufferSize);
        } catch (Exception e) {
            throw new MessageExporterException("Failed to retrieve messages for exporting", e);
        }

        while (!messages.isEmpty()) {
            for (Message message : messages) {
                String filePrefixWithId = filePrefix + "_" + message.getMessageId();

                try {
                    if (!singleFile) {
                        if (compress) {
                            zipOutputStream.putNextEntry(new ZipEntry(filePrefixWithId + fileExtension));
                        } else {
                            fileName = folder + filePrefixWithId + fileExtension;
                            writer = new OutputStreamWriter(new FileOutputStream(fileName));
                        }
                    }
                } catch (Exception e) {
                    throw new MessageExporterException("Failed to write to file: " + fileName, e);
                }

                boolean contentWasWritten = false;

                try {
                    if (options.getContentType() != null) {
                        ContentType contentType = options.getContentType();
                        boolean destinationContent = options.isDestinationContent();
                        
                        for (Entry<Integer, ConnectorMessage> entry : message.getConnectorMessages().entrySet()) {
                            Integer metaDataId = entry.getKey();
                            ConnectorMessage connectorMessage = entry.getValue();

                            if (((destinationContent && metaDataId != 0) || (!destinationContent && metaDataId == 0)) && exportMessageContent(connectorMessage.getContent(contentType), writer)) {
                                writer.append(IOUtils.LINE_SEPARATOR);
                                writer.append(IOUtils.LINE_SEPARATOR);
                                contentWasWritten = true;
                            }
                        }
                    } else {
                        contentWasWritten = exportMessage(message, writer);
                        
                        if (singleFile) {
                            writer.append(IOUtils.LINE_SEPARATOR);
                        }
                    }
    
                    if (contentWasWritten) {
                        writer.flush();
                        exportCount++;
                    }
                } catch (IOException e) {
                    IOUtils.closeQuietly(writer);
                    throw new MessageExporterException("An error occurred while exporting message Id " + message.getMessageId() + ", channel Id " + options.getChannelId(), e);
                }

                try {
                    if (!singleFile) {
                        if (compress) {
                            try {
                                zipOutputStream.closeEntry();
                            } catch (IOException e) {
                                throw new MessageExporterException("Failed in writing to file: " + fileName, e);
                            }
                        } else {
                            writer.close();
                            
                            if (!contentWasWritten) {
                                FileUtils.deleteQuietly(new File(fileName));
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error("Failed to close resources when writing to file: " + fileName, e);
                }
            }

            offset += bufferSize;

            try {
                messages = messageRetriever.getMessages(options.getChannelId(), options.getMessageFilter(), true, offset, bufferSize);
            } catch (Exception e) {
                throw new MessageExporterException("Failed to retrieve messages for exporting", e);
            }
        }
        
        try {
            if (compress) {
                zipOutputStream.close();
                writer.close();
                
                if (exportCount == 0) {
                    FileUtils.deleteQuietly(new File(fileName));
                }
            } else if (singleFile) {
                writer.close();
                
                if (exportCount == 0) {
                    FileUtils.deleteQuietly(new File(fileName));
                }
            }
        } catch (IOException e) {
            logger.error("Failed to close resources after exporting messages", e);
        }

        return exportCount;
    }
    
    private boolean exportMessageContent(MessageContent messageContent, Writer writer) throws IOException {
        if (messageContent == null) {
            return false;
        }
        
        boolean isEncrypt = options.isEncrypt();
        String unencrypted = messageContent.getContent();
        String encrypted = messageContent.getEncryptedContent();
        String content = null;
        
        if (isEncrypt) {
            if (encrypted != null) {
                content = encrypted;
            } else if (unencrypted != null && !StringUtils.isBlank(unencrypted)) {
                content = encryptor.encrypt(unencrypted);
            }
        } else {
            if (unencrypted != null) {
                content = unencrypted;
            } else if (encrypted != null && !StringUtils.isBlank(encrypted)) {
                content = encryptor.decrypt(encrypted);
            }
        }
        
        if (StringUtils.isNotBlank(content)) {
            writer.write(content);
            return true;
        }
        
        return false;
    }
    
    private boolean exportMessage(Message message, Writer writer) throws IOException {
        boolean isEncrypt = options.isEncrypt();
        
        if (isEncrypt) {
            MessageEncryptionUtil.encryptMessage(message, encryptor);
        } else {
            MessageEncryptionUtil.decryptMessage(message, encryptor);
        }
        
        String content = serializer.serialize(message);
        
        if (StringUtils.isNotBlank(content)) {
            writer.write(content);
            return true;
        }
        
        return false;
    }

    public class MessageExporterException extends Exception {
        public MessageExporterException(String message) {
            super(message);
        }

        public MessageExporterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public class MessageExporterUserError extends MessageExporterException {
        public MessageExporterUserError(String message) {
            super(message);
        }

        public MessageExporterUserError(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
