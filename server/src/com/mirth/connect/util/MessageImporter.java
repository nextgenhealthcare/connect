/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterException;

public class MessageImporter {
    private final static String OPEN_ELEMENT = "<message>";
    private final static String CLOSE_ELEMENT = "</message>";
    private final static String CHARSET = "UTF-8";
    private final static int XML_SCAN_BUFFER_SIZE = 20;

    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private Logger logger = Logger.getLogger(getClass());

    public MessageImportResult importMessages(String path, Boolean recursive, MessageWriter messageWriter, String baseDir) throws InterruptedException, MessageImportException, MessageImportInvalidPathException {
        int[] result = new int[] { 0, 0 };

        if (baseDir == null) {
            baseDir = System.getProperty("user.dir");
        }

        try {
            path = FilenameUtils.getAbsolutePath(new File(baseDir), path);

            if (!new File(path).canRead()) {
                throw new MessageImportInvalidPathException("The file/folder was not found or is not readable: " + path);
            }

            FileObject file = VfsUtils.getManager().resolveFile(VfsUtils.pathToUri(path));

            switch (file.getType()) {
                case FOLDER:
                    for (FileObject child : file.getChildren()) {
                        if (recursive) {
                            importVfsFileRecursive(child, messageWriter, result);
                        } else if (child.getType() == FileType.FILE) {
                            importVfsFile(child, messageWriter, result);
                        }
                    }

                    break;

                case FILE:
                    importVfsFile(file, messageWriter, result);
                    break;
            }
        } catch (Exception e) {
            throw new MessageImportException(e);
        } finally {
            try {
                messageWriter.close();
            } catch (Exception e) {
                logger.error("Failed to close message writer", e);
            }
        }

        return new MessageImportResult(result[0], result[1]);
    }

    private void importVfsFileRecursive(FileObject file, MessageWriter messageWriter, int[] result) throws InterruptedException, MessageImportException {
        try {
            switch (file.getType()) {
                case FOLDER:
                    logger.debug("Reading folder: " + file.getName().getURI());

                    for (FileObject child : file.getChildren()) {
                        ThreadUtils.checkInterruptedStatus();
                        importVfsFileRecursive(child, messageWriter, result);
                    }

                    break;

                case FILE:
                    importVfsFile(file, messageWriter, result);
                    break;
            }
        } catch (FileSystemException e) {
            logger.error("An error occurred when accessing: " + file.getName().getURI(), e);
        }
    }

    private void importVfsFile(FileObject file, MessageWriter messageWriter, int[] result) throws InterruptedException, MessageImportException {
        InputStream inputStream = null;

        try {
            inputStream = file.getContent().getInputStream();

            // scan the first XML_SCAN_BUFFER_SIZE bytes in the file to see if it contains message xml
            char[] cbuf = new char[XML_SCAN_BUFFER_SIZE];
            new InputStreamReader(inputStream, CHARSET).read(cbuf);

            if (StringUtils.contains(new String(cbuf), OPEN_ELEMENT)) {
                logger.debug("Importing file: " + file.getName().getURI());

                // re-open the input stream to reposition it at the beginning of the stream
                inputStream.close();
                inputStream = file.getContent().getInputStream();
                importMessagesFromInputStream(inputStream, messageWriter, result);
            }
        } catch (IOException e) {
            throw new MessageImportException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void importMessagesFromInputStream(InputStream inputStream, MessageWriter messageWriter, int[] result) throws IOException, InterruptedException {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder serializedMessage = new StringBuilder();
            boolean enteredMessage = true;

            while ((line = reader.readLine()) != null) {
                ThreadUtils.checkInterruptedStatus();

                if (line.equals(OPEN_ELEMENT)) {
                    enteredMessage = true;
                }

                if (enteredMessage) {
                    serializedMessage.append(line);

                    if (line.equals(CLOSE_ELEMENT)) {
                        Message message = serializer.deserialize(serializedMessage.toString(), Message.class);
                        serializedMessage.delete(0, serializedMessage.length());
                        enteredMessage = false;
                        result[0]++;

                        try {
                            if (messageWriter.write(message)) {
                                result[1]++;
                            }
                        } catch (MessageWriterException e) {
                            logger.error("Failed to write message", e);
                        }
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
    
    public static class MessageImportException extends Exception {
        public MessageImportException(String message) {
            super(message);
        }
        
        public MessageImportException(Throwable cause) {
            super(cause);
        }
        
        public MessageImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class MessageImportInvalidPathException extends Exception {
        public MessageImportInvalidPathException(String message) {
            super(message);
        }
    }
}
