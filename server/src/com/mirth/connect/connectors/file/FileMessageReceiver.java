/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.PollingMessageReceiver;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.routing.RoutingException;

import com.mirth.connect.connectors.file.filesystems.FileInfo;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.model.MessageObject.Protocol;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.mule.adaptors.Adaptor;
import com.mirth.connect.server.mule.adaptors.AdaptorFactory;
import com.mirth.connect.server.mule.adaptors.BatchAdaptor;
import com.mirth.connect.server.mule.adaptors.BatchMessageProcessor;
import com.mirth.connect.server.mule.transformers.JavaScriptPostprocessor;

public class FileMessageReceiver extends PollingMessageReceiver implements BatchMessageProcessor {
    private String readDir = null;
    private String moveDir = null;
    private String errorDir = null;
    private String moveToPattern = null;
    private String filenamePattern = null;
    private boolean routingError = false;

    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private ConnectorType connectorType = ConnectorType.READER;
    private FileConnector fileConnector = null;

    private String originalFilename = null;

    public FileMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, String readDir, String moveDir, String moveToPattern, String errorDir, Long frequency) throws InitialisationException {
        super(connector, component, endpoint, frequency);
        fileConnector = (FileConnector) connector;
        
        // Replace variables in the readDir now, all others will be done every message
        this.readDir = replacer.replaceValues(readDir, fileConnector.getChannelId());
        this.moveDir = moveDir;
        this.moveToPattern = moveToPattern;
        this.errorDir = errorDir;

        if (fileConnector.getPollingType().equals(FileConnector.POLLING_TYPE_TIME)) {
            setTime(fileConnector.getPollingTime());
        } else {
            setFrequency(fileConnector.getPollingFrequency());
        }

        filenamePattern = replacer.replaceValues(fileConnector.getFileFilter(), fileConnector.getChannelId());
        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    public void doConnect() throws Exception {
        FileSystemConnection con = fileConnector.getConnection(getEndpointURI(), null);
        fileConnector.releaseConnection(getEndpointURI(), con, null);
    }

    public void doDisconnect() throws Exception {}

    public void poll() {

        monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);
        try {

            FileInfo[] files = listFiles();

            if (files == null) {
                return;
            }

            // sort files by specified attribute before processing
            sortFiles(files);
            routingError = false;

            for (int i = 0; i < files.length; i++) {
                //
                if (!routingError && !files[i].isDirectory()) {
                    monitoringController.updateStatus(connector, connectorType, Event.BUSY);
                    processFile(files[i]);
                    monitoringController.updateStatus(connector, connectorType, Event.DONE);
                }
            }
        } catch (Throwable t) {
            alertController.sendAlerts(((FileConnector) connector).getChannelId(), Constants.ERROR_403, null, t);
            handleException(new Exception(t));
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    public void sortFiles(FileInfo[] files) {
        String sortAttribute = ((FileConnector) connector).getSortAttribute();

        if (sortAttribute.equals(FileConnector.SORT_DATE)) {
            Arrays.sort(files, new Comparator<FileInfo>() {
                public int compare(FileInfo file1, FileInfo file2) {
                    return Float.compare(file1.getLastModified(), file2.getLastModified());
                }
            });
        } else if (sortAttribute.equals(FileConnector.SORT_SIZE)) {
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

    /**
     * Converts the supplied message into a MuleMessage and routes it.
     * 
     * @param message
     *            The message to be converted and routed.
     * @throws MessagingException
     *             , UMOException
     */
    public void processBatchMessage(String message) throws MessagingException, UMOException {
        UMOMessageAdapter messageAdapter = connector.getMessageAdapter(message);
        messageAdapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
        UMOMessage umoMessage = routeMessage(new MuleMessage(messageAdapter), endpoint.isSynchronous());
        if (umoMessage != null) {
            postProcessor.doPostProcess(umoMessage.getPayload());
        }
    }

    public synchronized void processFile(FileInfo file) throws UMOException {

        boolean checkFileAge = fileConnector.isCheckFileAge();
        if (checkFileAge) {
            long fileAge = fileConnector.getFileAge();
            long lastMod = file.getLastModified();
            long now = (new java.util.Date()).getTime();
            if ((now - lastMod) < fileAge)
                return;
        }

        String destinationDir = null;
        String destinationName = null;
        originalFilename = file.getName();
        UMOMessageAdapter adapter = connector.getMessageAdapter(file);
        adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);

        if (moveDir != null) {
            destinationName = file.getName();

            VariableFilenameParser filenameParser = (VariableFilenameParser) fileConnector.getFilenameParser();
            filenameParser.setChannelId(fileConnector.getChannelId());
            
            if (moveToPattern != null) {
                destinationName = filenameParser.getFilename(adapter, moveToPattern);
            }

            destinationDir = filenameParser.getFilename(adapter, moveDir);
        }

        boolean resultOfFileMoveOperation = false;

        try {
            // Perform some quick checks to make sure file can be processed
            if (file.isDirectory()) {
                // ignore directories
            } else if (!(file.isReadable() && file.isFile())) {
                // it's either not readable, or something odd like a link */
                throw new MuleException(new Message(Messages.FILE_X_DOES_NOT_EXIST, file.getName()));
            } else {

                Exception fileProcessedException = null;

                try {

                    // ast: use the user-selected encoding
                    if (fileConnector.isProcessBatchFiles()) {
                        processBatch(file);
                    } else {
                        String message = "";
                        if (fileConnector.isBinary()) {
                            message = new String(new Base64().encode(getBytesFromFile(file)));
                        } else {
                            message = new String(getBytesFromFile(file), fileConnector.getCharsetEncoding());
                        }
                        adapter = connector.getMessageAdapter(message);
                        adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
                        UMOMessage umoMessage = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());
                        if (umoMessage != null) {
                            postProcessor.doPostProcess(umoMessage.getPayload());
                        }
                    }
                } catch (RoutingException e) {
                    logger.error("Unable to route." + ExceptionUtils.getStackTrace(e));

                    // routingError is reset to false at the beginning of the
                    // poll method
                    routingError = true;

                    if (errorDir != null) {
                        logger.error("Moving file to error directory: " + errorDir);
                        
                        VariableFilenameParser filenameParser = (VariableFilenameParser) fileConnector.getFilenameParser();
                        filenameParser.setChannelId(fileConnector.getChannelId());
                        
                        destinationDir = filenameParser.getFilename(adapter, errorDir);
                        destinationName = file.getName();
                    }
                } catch (Throwable t) {
                    logger.error("Error reading file " + file.getAbsolutePath() + "\n" + t.getMessage());
                    fileProcessedException = new MuleException(new Message(Messages.FAILED_TO_READ_PAYLOAD, file.getName()));
                }

                // move the file if needed
                if (destinationDir != null) {
                    deleteFile(destinationName, destinationDir, true);

                    resultOfFileMoveOperation = renameFile(file.getName(), readDir, destinationName, destinationDir);

                    if (!resultOfFileMoveOperation) {
                        throw new MuleException(new Message("file", 4, pathname(file.getName(), readDir), pathname(destinationName, destinationDir)));
                    }
                } else if (fileConnector.isAutoDelete()) {
                    // adapter.getPayloadAsBytes();

                    resultOfFileMoveOperation = deleteFile(file.getName(), readDir, false);

                    if (!resultOfFileMoveOperation) {
                        throw new MuleException(new Message("file", 3, pathname(file.getName(), readDir)));
                    }
                }

                if (fileProcessedException != null) {
                    throw fileProcessedException;
                }
            }
        } catch (Exception e) {
            alertController.sendAlerts(((FileConnector) connector).getChannelId(), Constants.ERROR_403, "", e);
            handleException(e);
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
        UMOEndpointURI uri = endpoint.getEndpointURI();
        Protocol protocol = Protocol.valueOf(fileConnector.getInboundProtocol());
        Adaptor adaptor = AdaptorFactory.getAdaptor(protocol);

        if (adaptor instanceof BatchAdaptor) {
            BatchAdaptor batchAdaptor = (BatchAdaptor) adaptor;
            FileSystemConnection con = fileConnector.getConnection(uri, null);
            Reader in = null;
            try {
                in = new InputStreamReader(con.readFile(file.getName(), readDir), fileConnector.getCharsetEncoding());
                Map<String, String> protocolProperties = fileConnector.getProtocolProperties();
                protocolProperties.put("batchScriptId", fileConnector.getChannelId());
                batchAdaptor.processBatch(in, fileConnector.getProtocolProperties(), this, endpoint);
            } finally {
                if (in != null) {
                    in.close();
                }
                con.closeReadFile();
                fileConnector.releaseConnection(uri, con, null);
            }
        } else {
            throw new Exception("Data type " + protocol + " does not support batch processing.");
        }
    }

    /** Delete a file */
    private boolean deleteFile(String name, String dir, boolean mayNotExist) throws Exception {
        UMOEndpointURI uri = endpoint.getEndpointURI();
        FileSystemConnection con = fileConnector.getConnection(uri, null);
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
            fileConnector.releaseConnection(uri, con, null);
        }
    }

    private boolean renameFile(String fromName, String fromDir, String toName, String toDir) throws Exception {

        UMOEndpointURI uri = endpoint.getEndpointURI();
        FileSystemConnection con = fileConnector.getConnection(uri, null);
        try {

            con.move(fromName, fromDir, toName, toDir);
            return true;
        } catch (Exception e) {

            return false;
        } finally {
            fileConnector.releaseConnection(uri, con, null);
        }
    }

    // Returns the contents of the file in a byte array.
    private byte[] getBytesFromFile(FileInfo file) throws Exception {

        UMOEndpointURI uri = endpoint.getEndpointURI();
        FileSystemConnection con = fileConnector.getConnection(uri, null);

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
            fileConnector.releaseConnection(uri, con, null);
        }
    }

    /**
     * Get a list of files to be processed.
     * 
     * @return a list of files to be processed.
     * @throws org.mule.MuleException
     *             which will wrap any other exceptions or errors.
     */
    FileInfo[] listFiles() throws Exception {

        UMOEndpointURI uri = endpoint.getEndpointURI();
        FileSystemConnection con = fileConnector.getConnection(uri, null);

        try {
            return con.listFiles(readDir, filenamePattern, fileConnector.isRegex()).toArray(new FileInfo[0]);
        } finally {
            fileConnector.releaseConnection(uri, con, null);
        }
    }

    public boolean isRoutingError() {
        return routingError;
    }

    public void setRoutingError(boolean routingError) {
        this.routingError = routingError;
    }

}
