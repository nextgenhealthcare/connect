/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.File;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.util.messagewriter.AttachmentSource;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class MessageExporter {
    private int numExported;
    private int numProcessed;

    public int getNumExported() {
        return numExported;
    }

    public int getNumProcessed() {
        return numProcessed;
    }

    /**
     * Executes the message exporter.
     * 
     * @param messageList
     *            The paginated list to read messages from
     * @param messageWriter
     *            The message writer to write messages to
     * @return A list of the message ids that were exported.
     */
    public synchronized int exportMessages(PaginatedList<Message> messageList, MessageWriter messageWriter, AttachmentSource attachmentSource, MessageWriterOptions options) throws InterruptedException, MessageExportException {
        int pageNumber = 0;
        numExported = 0;

        do {
            ThreadUtils.checkInterruptedStatus();

            try {
                messageList.loadPageNumber(++pageNumber);
            } catch (Exception e) {
                throw new MessageExportException(e);
            }

            for (Message message : messageList) {
                ThreadUtils.checkInterruptedStatus();

                try {
                    if (attachmentSource != null) {
                        List<Attachment> attachments = attachmentSource.getMessageAttachments(message);

                        if (CollectionUtils.isNotEmpty(attachments)) {
                            message.setAttachments(attachments);
                        }
                    }

                    if (messageWriter.write(message)) {
                        numExported++;
                    }

                    numProcessed++;
                } catch (Exception e) {
                    Throwable cause = ExceptionUtils.getRootCause(e);
                    throw new MessageExportException("Failed to export message: " + cause.getMessage(), cause);
                }
            }
        } while (messageList.hasNextPage());
        
        if (numExported > 0) {
            writeExportReadMe(options);
        }
        
        return numExported;
    }
    
    /**
     * Exports the EXPORTREADME.txt
     * 
     * @param options
     *            The message writer options that contains the options from the MessageExportDialog screen
     * @return void
     */
    public synchronized void writeExportReadMe(MessageWriterOptions options) throws InterruptedException, MessageExportException {
        String docsDirectory = new File(this.getClass().getClassLoader().getResource("mirth.properties").getFile()).getParentFile().getParent() + File.separator + "docs";
        
        File sourceFile = new File(docsDirectory + File.separator + "EXPORTREADME.txt");
        File destinationDirectory = new File(options.getRootFolder());
        try {
            FileUtils.copyFileToDirectory(sourceFile, destinationDirectory);
        } catch(Exception e) {
            Throwable cause = ExceptionUtils.getRootCause(e);
            throw new MessageExportException("Failed to export the EXPORTREADME.txt file: " + cause.getMessage(), cause);
        }
    }

    public static class MessageExportException extends Exception {
        public MessageExportException(String message) {
            super(message);
        }

        public MessageExportException(Throwable cause) {
            super(cause);
        }

        public MessageExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
