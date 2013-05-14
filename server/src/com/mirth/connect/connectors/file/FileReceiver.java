/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mirth.connect.connectors.file.filesystems.FileInfo;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.message.BatchAdaptor;
import com.mirth.connect.donkey.server.message.BatchMessageProcessor;
import com.mirth.connect.donkey.server.message.BatchMessageProcessorException;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;

public class FileReceiver extends PollConnector implements BatchMessageProcessor {
    protected transient Log logger = LogFactory.getLog(getClass());

    private String readDir = null;
    private String moveToDirectory = null;
    private String moveToFileName = null;
    private String errorMoveToDirectory = null;
    private String errorMoveToFileName = null;
    private String filenamePattern = null;
    private boolean routingError = false;

    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private FileConnector fileConnector = null;

    private String originalFilename = null;

    private FileReceiverProperties connectorProperties;
    private String charsetEncoding;
    private String batchScriptId;
    private URI uri;

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (FileReceiverProperties) getConnectorProperties();

        this.charsetEncoding = CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding(), System.getProperty("ca.uhn.hl7v2.llp.charset"));

        // Replace variables in the readDir, username, and password now, all others will be done every message
        connectorProperties.setHost(replacer.replaceValues(connectorProperties.getHost(), getChannelId()));
        connectorProperties.setUsername(replacer.replaceValues(connectorProperties.getUsername(), getChannelId()));
        connectorProperties.setPassword(replacer.replaceValues(connectorProperties.getPassword(), getChannelId()));

        this.fileConnector = new FileConnector(getChannelId(), connectorProperties);

        try {
            uri = fileConnector.getEndpointURI(connectorProperties.getHost());
        } catch (URISyntaxException e1) {
            throw new DeployException("Error creating URI.", e1);
        }

        this.readDir = uri.getPath();
        this.moveToDirectory = connectorProperties.getMoveToDirectory();
        this.moveToFileName = connectorProperties.getMoveToFileName();
        this.errorMoveToDirectory = connectorProperties.getErrorMoveToDirectory();
        this.errorMoveToFileName = connectorProperties.getErrorMoveToFileName();

        this.filenamePattern = replacer.replaceValues(connectorProperties.getFileFilter(), getChannelId());

        DataType dataType = getInboundDataType();
        String batchScript = ExtensionController.getInstance().getDataTypePlugins().get(dataType.getType()).getBatchScript(dataType.getBatchAdaptor());

        if (StringUtils.isNotEmpty(batchScript)) {

            try {
                String batchScriptId = UUID.randomUUID().toString();

                JavaScriptUtil.compileAndAddScript(batchScriptId, batchScript.toString());

                this.batchScriptId = batchScriptId;
            } catch (Exception e) {
                throw new DeployException("Error compiling " + connectorProperties.getName() + " script " + batchScriptId + ".", e);
            }
        }

        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws UndeployException {
        if (batchScriptId != null) {
            JavaScriptUtil.removeScriptFromCache(batchScriptId);
        }
    }

    @Override
    public void onStart() throws StartException {
        setRoutingError(false);

        try {
            FileSystemConnection con = fileConnector.getConnection(uri, null, connectorProperties);
            fileConnector.releaseConnection(uri, con, null, connectorProperties);
        } catch (Exception e) {
            throw new StartException(e.getMessage(), e);
        }
    }

    @Override
    public void onStop() throws StopException {
        try {
            fileConnector.doStop();
        } catch (FileConnectorException e) {
            throw new StopException("Failed to stop File Connector", e);
        }

        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
    }

    @Override
    protected void poll() {
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.POLLING));
        try {

            FileInfo[] files = listFiles();

            if (files == null) {
                return;
            }

            // sort files by specified attribute before processing
            sortFiles(files);
            routingError = false;

            for (int i = 0; i < files.length; i++) {
                if (isTerminated()) {
                    return;
                }

                if (!routingError && !files[i].isDirectory()) {
                    eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.READING));
                    processFile(files[i]);
                    eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
                }
            }
        } catch (Throwable t) {
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, connectorProperties.getName(), null, t));
            logger.error("Error polling in channel: " + getChannelId(), t);
        } finally {
            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
        }
    }

    public void sortFiles(FileInfo[] files) {
        String sortAttribute = connectorProperties.getSortBy();

        if (sortAttribute.equals(FileReceiverProperties.SORT_BY_DATE)) {
            Arrays.sort(files, new Comparator<FileInfo>() {
                public int compare(FileInfo file1, FileInfo file2) {
                    return Float.compare(file1.getLastModified(), file2.getLastModified());
                }
            });
        } else if (sortAttribute.equals(FileReceiverProperties.SORT_BY_SIZE)) {
            Arrays.sort(files, new Comparator<FileInfo>() {
                public int compare(FileInfo file1, FileInfo file2) {
                    return Float.compare(file1.getSize(), file2.getSize());
                }
            });
        } else {
            Arrays.sort(files, new Comparator<FileInfo>() {
                public int compare(FileInfo file1, FileInfo file2) {
                    return file1.getName().compareTo(file2.getName());
                }
            });
        }
    }

    public synchronized void processFile(FileInfo file) {
        try {
            boolean checkFileAge = connectorProperties.isCheckFileAge();
            if (checkFileAge) {
                long fileAge = Long.valueOf(connectorProperties.getFileAge());
                long lastMod = file.getLastModified();
                long now = System.currentTimeMillis();
                if ((now - lastMod) < fileAge)
                    return;
            }

            // Add the original filename to the channel map
            originalFilename = file.getName();
            Map<String, Object> channelMap = new HashMap<String, Object>();
            channelMap.put("originalFilename", originalFilename);

            // Set the default file action
            FileAction action = FileAction.NONE;

            boolean errorResponse = false;

            // Perform some quick checks to make sure file can be processed
            if (file.isDirectory()) {
                // ignore directories
            } else if (!(file.isReadable() && file.isFile())) {
                // it's either not readable, or something odd like a link */
                throw new FileConnectorException("File is not readable.");
            } else {
                Exception fileProcessedException = null;

                try {
                    Response response = null;

                    // ast: use the user-selected encoding
                    if (connectorProperties.isProcessBatch()) {
                        processBatch(file);
                    } else {
                        RawMessage rawMessage;
                        if (connectorProperties.isBinary()) {
                            rawMessage = new RawMessage(getBytesFromFile(file));
                        } else {
                            rawMessage = new RawMessage(new String(getBytesFromFile(file), charsetEncoding));
                        }

                        rawMessage.setChannelMap(channelMap);

                        DispatchResult dispatchResult = null;
                        try {
                            dispatchResult = dispatchRawMessage(rawMessage);
                        } finally {
                            finishDispatch(dispatchResult);
                        }

                        response = dispatchResult.getSelectedResponse();
                    }

                    // True if the response status is ERROR and we're not processing a batch
                    errorResponse = response != null && response.getStatus() == Status.ERROR;
                } catch (Exception e) {
                    logger.error("Unable to route: " + ExceptionUtils.getStackTrace(e));

                    // routingError is reset to false at the beginning of the poll method
                    routingError = true;
                } catch (Throwable t) {
                    routingError = true;
                    String errorMessage = "Error reading file " + file.getAbsolutePath() + "\n" + t.getMessage();
                    logger.error(errorMessage);
                    fileProcessedException = new FileConnectorException(errorMessage);
                }

                boolean shouldUseErrorFields = false;

                // If the message wasn't successfully processed through the channel, set the error file action
                if (routingError) {
                    action = connectorProperties.getErrorReadingAction();
                    shouldUseErrorFields = true;
                } else if (errorResponse && connectorProperties.getErrorResponseAction() != FileAction.AFTER_PROCESSING) {
                    action = connectorProperties.getErrorResponseAction();
                    shouldUseErrorFields = true;
                } else {
                    action = connectorProperties.getAfterProcessingAction();
                }

                // Move or delete the file based on the selected file action
                if (action == FileAction.MOVE) {
                    // Replace and set the directory/filename
                    String destinationDir = shouldUseErrorFields ? errorMoveToDirectory : moveToDirectory;
                    String destinationName = shouldUseErrorFields ? errorMoveToFileName : moveToFileName;

                    // If the user-specified directory is blank, use the default (file's current directory)
                    if (StringUtils.isNotBlank(destinationDir)) {
                        destinationDir = replacer.replaceValues(destinationDir, getChannelId(), channelMap);
                    } else {
                        destinationDir = file.getParent();
                    }

                    // If the user-specified filename is blank, use the default (original filename)
                    if (StringUtils.isNotBlank(destinationName)) {
                        destinationName = replacer.replaceValues(destinationName, getChannelId(), channelMap);
                    } else {
                        destinationName = originalFilename;
                    }
                    
                    if (!filesEqual(file.getParent(), originalFilename, destinationDir, destinationName)) {
                        if (shouldUseErrorFields) {
                            logger.error("Moving file to error directory: " + destinationDir);
                        }

                        // Delete the destination file if it exists, and then rename the original file
                        deleteFile(destinationName, destinationDir, true);
                        boolean resultOfFileMoveOperation = renameFile(file.getName(), readDir, destinationName, destinationDir);

                        if (!resultOfFileMoveOperation) {
                            throw new FileConnectorException("Error moving file from [" + pathname(file.getName(), readDir) + "] to [" + pathname(destinationName, destinationDir) + "]");
                        }
                    }
                } else if (action == FileAction.DELETE) {
                    // Delete the original file
                    boolean resultOfFileMoveOperation = deleteFile(file.getName(), readDir, false);

                    if (!resultOfFileMoveOperation) {
                        throw new FileConnectorException("Error deleting file from [" + pathname(file.getName(), readDir) + "]");
                    }
                }

                if (fileProcessedException != null) {
                    throw fileProcessedException;
                }
            }
        } catch (Exception e) {
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, connectorProperties.getName(), "", e));
            logger.error("Error processing file in channel: " + getChannelId(), e);
        }
    }

    private boolean filesEqual(String dir1, String name1, String dir2, String name2) {
        String separator = System.getProperty("file.separator");
        String escapedSeparator = StringEscapeUtils.escapeJava(separator);
        String file1 = dir1 + (dir1.endsWith(separator) ? "" : separator) + name1.replaceAll("^" + escapedSeparator, "");
        String file2 = dir2 + (dir2.endsWith(separator) ? "" : separator) + name2.replaceAll("^" + escapedSeparator, "");
        try {
            return new File(file1).getCanonicalPath().equals(new File(file2).getCanonicalPath());
        } catch (IOException e) {
            return file1.equals(file2);
        }
    }

    /** Convert a directory path and a filename into a pathname */
    private String pathname(String name, String dir) {

        if (dir != null && dir.length() > 0) {

            return dir + "/" + name;
        } else {

            return name;
        }
    }

    /** Process a single file as a batched message source */
    private void processBatch(FileInfo file) throws Exception {
        DataType dataType = getInboundDataType();

        if (dataType.getBatchAdaptor() != null) {
            BatchAdaptor batchAdaptor = dataType.getBatchAdaptor();
            FileSystemConnection con = fileConnector.getConnection(uri, null, connectorProperties);
            Reader in = null;
            try {
                in = new InputStreamReader(con.readFile(file.getName(), readDir), charsetEncoding);
                batchAdaptor.processBatch(in, this);
            } finally {
                if (in != null) {
                    in.close();
                }
                con.closeReadFile();
                fileConnector.releaseConnection(uri, con, null, connectorProperties);
            }
        } else {
            throw new Exception("Data type " + dataType.getType() + " does not support batch processing.");
        }
    }

    /** Delete a file */
    private boolean deleteFile(String name, String dir, boolean mayNotExist) throws Exception {
        FileSystemConnection con = fileConnector.getConnection(uri, null, connectorProperties);
        try {
            con.delete(name, dir, mayNotExist);
            return true;
        } catch (Exception e) {
            if (mayNotExist) {
                return true;
            } else {
                logger.info("Unable to delete destination file");
                return false;
            }
        } finally {
            fileConnector.releaseConnection(uri, con, null, connectorProperties);
        }
    }

    private boolean renameFile(String fromName, String fromDir, String toName, String toDir) throws Exception {
        FileSystemConnection con = fileConnector.getConnection(uri, null, connectorProperties);
        try {

            con.move(fromName, fromDir, toName, toDir);
            return true;
        } catch (Exception e) {

            return false;
        } finally {
            fileConnector.releaseConnection(uri, con, null, connectorProperties);
        }
    }

    // Returns the contents of the file in a byte array.
    private byte[] getBytesFromFile(FileInfo file) throws Exception {
        FileSystemConnection con = fileConnector.getConnection(uri, null, connectorProperties);

        try {
            InputStream is = con.readFile(file.getName(), readDir);

            // Get the size of the file
            long length = file.getSize();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
                // TODO: throw new
                // ??Exception("Implementation restriction: file too large.");
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }

            // Close the input stream and return bytes
            is.close();
            con.closeReadFile();
            return bytes;
        } finally {
            fileConnector.releaseConnection(uri, con, null, connectorProperties);
        }
    }

    /**
     * Get a list of files to be processed.
     * 
     * @return a list of files to be processed.
     * @throws Exception
     */
    FileInfo[] listFiles() throws Exception {
        FileSystemConnection con = fileConnector.getConnection(uri, null, connectorProperties);

        try {
            return con.listFiles(readDir, filenamePattern, connectorProperties.isRegex(), connectorProperties.isIgnoreDot()).toArray(new FileInfo[0]);
        } finally {
            fileConnector.releaseConnection(uri, con, null, connectorProperties);
        }
    }

    public boolean isRoutingError() {
        return routingError;
    }

    public void setRoutingError(boolean routingError) {
        this.routingError = routingError;
    }

    @Override
    public boolean processBatchMessage(String message) throws BatchMessageProcessorException {
        if (isTerminated()) {
            return false;
        }

        Map<String, Object> channelMap = new HashMap<String, Object>();
        channelMap.put("originalFilename", originalFilename);

        RawMessage rawMessage = new RawMessage(message);
        rawMessage.setChannelMap(channelMap);
        DispatchResult dispatchResult = null;

        try {
            dispatchResult = dispatchRawMessage(rawMessage);
        } catch (ChannelException e) {
            throw new BatchMessageProcessorException(e);
        } finally {
            finishDispatch(dispatchResult);
        }

        return true;
    }

    @Override
    public String getBatchScriptId() {
        return batchScriptId;
    }

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        //TODO add cleanup code
        finishDispatch(dispatchResult);
    }
}
