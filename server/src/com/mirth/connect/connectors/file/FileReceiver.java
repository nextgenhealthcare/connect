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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mirth.connect.connectors.file.filesystems.FileInfo;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;
import com.mirth.connect.model.converters.BatchAdaptor;
import com.mirth.connect.model.converters.BatchMessageProcessor;
import com.mirth.connect.model.converters.BatchMessageProcessorException;
import com.mirth.connect.model.converters.DataTypeFactory;
import com.mirth.connect.model.converters.delimited.DelimitedSerializer;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.ErrorConstants;

public class FileReceiver extends PollConnector implements BatchMessageProcessor {
    protected transient Log logger = LogFactory.getLog(getClass());

    private String readDir = null;
    private String moveDir = null;
    private String errorDir = null;
    private String moveToPattern = null;
    private String filenamePattern = null;
    private boolean routingError = false;

    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private ConnectorType connectorType = ConnectorType.READER;
    private FileConnector fileConnector = null;

    private String originalFilename = null;

    private FileReceiverProperties connectorProperties;
    private String charsetEncoding;
    private String batchScriptId;

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (FileReceiverProperties) getConnectorProperties();

        this.charsetEncoding = CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding(), System.getProperty("ca.uhn.hl7v2.llp.charset"));

        // Replace variables in the readDir, username, and password now, all others will be done every message
        connectorProperties.setHost(replacer.replaceValues(connectorProperties.getHost(), getChannelId()));
        connectorProperties.setUsername(replacer.replaceValues(connectorProperties.getUsername(), getChannelId()));
        connectorProperties.setPassword(replacer.replaceValues(connectorProperties.getPassword(), getChannelId()));
        
        this.fileConnector = new FileConnector(getChannelId(), connectorProperties);

        URI uri;
        try {
            uri = getEndpointURI();
        } catch (URISyntaxException e1) {
            throw new DeployException("Error creating URI.", e1);
        }
        
        this.readDir = uri.getPath();
        this.moveDir = connectorProperties.getMoveToDirectory();
        this.moveToPattern = connectorProperties.getMoveToPattern();
        this.errorDir = connectorProperties.getMoveToErrorDirectory();

        this.filenamePattern = replacer.replaceValues(connectorProperties.getFileFilter(), getChannelId());

        String batchScript = null;

        if (getInboundDataType().getType().equals(DataTypeFactory.DELIMITED)) {
            DelimitedSerializer serializer = (DelimitedSerializer) getInboundDataType().getSerializer();
            batchScript = serializer.getDelimitedProperties().getBatchScript();
        }

        if (StringUtils.isNotEmpty(batchScript)) {

            try {
                String batchScriptId = UUID.randomUUID().toString();

                JavaScriptUtil.compileAndAddScript(batchScriptId, batchScript.toString());

                this.batchScriptId = batchScriptId;
            } catch (Exception e) {
                throw new DeployException("Error compiling " + connectorProperties.getName() + " script " + batchScriptId + ".", e);
            }
        }

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);

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
            FileSystemConnection con = fileConnector.getConnection(getEndpointURI(), null);
            fileConnector.releaseConnection(getEndpointURI(), con, null);
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
        
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DISCONNECTED);
    }

    @Override
    protected void poll() {
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.CONNECTED);
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
                
                //
                if (!routingError && !files[i].isDirectory()) {
                    monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY);
                    processFile(files[i]);
                    monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
                }
            }
        } catch (Throwable t) {
            alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_403, null, t);
            // TODO: handleException
//            handleException(new Exception(t));
        } finally {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
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
        boolean checkFileAge = connectorProperties.isCheckFileAge();
        if (checkFileAge) {
            long fileAge = Long.valueOf(connectorProperties.getFileAge());
            long lastMod = file.getLastModified();
            long now = System.currentTimeMillis();
            if ((now - lastMod) < fileAge)
                return;
        }

        String destinationDir = null;
        String destinationName = null;
        originalFilename = file.getName();

        Map<String, Object> channelMap = new HashMap<String, Object>();
        channelMap.put("originalFilename", originalFilename);

        if (StringUtils.isNotBlank(moveDir)) {
            destinationName = file.getName();

            if (StringUtils.isNotBlank(moveToPattern)) {
                destinationName = replacer.replaceValues(moveToPattern, getChannelId(), channelMap);
            }

            destinationDir = replacer.replaceValues(moveDir, getChannelId(), channelMap);
        }

        boolean resultOfFileMoveOperation = false;

        try {
            // Perform some quick checks to make sure file can be processed
            if (file.isDirectory()) {
                // ignore directories
            } else if (!(file.isReadable() && file.isFile())) {
                // it's either not readable, or something odd like a link */
                throw new FileConnectorException("File is not readable.");
            } else {
                Exception fileProcessedException = null;

                try {

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
                    }
                } catch (Exception e) {
                    logger.error("Unable to route." + ExceptionUtils.getStackTrace(e));

                    // routingError is reset to false at the beginning of the
                    // poll method
                    routingError = true;

                    if (StringUtils.isNotBlank(errorDir)) {
                        logger.error("Moving file to error directory: " + errorDir);

                        destinationDir = replacer.replaceValues(errorDir, getChannelId(), channelMap);
                        destinationName = file.getName();
                    }
                } catch (Throwable t) {
                    String errorMessage = "Error reading file " + file.getAbsolutePath() + "\n" + t.getMessage();
                    logger.error(errorMessage);
                    fileProcessedException = new FileConnectorException(errorMessage);
                }

                // move the file if needed
                if (StringUtils.isNotBlank(destinationDir)) {
                    deleteFile(destinationName, destinationDir, true);

                    resultOfFileMoveOperation = renameFile(file.getName(), readDir, destinationName, destinationDir);

                    if (!resultOfFileMoveOperation) {
                        throw new FileConnectorException("Error moving file from [" + pathname(file.getName(), readDir) + "] to [" + pathname(destinationName, destinationDir) + "]");
                    }
                } else if (connectorProperties.isAutoDelete()) {
                    // adapter.getPayloadAsBytes();

                    resultOfFileMoveOperation = deleteFile(file.getName(), readDir, false);

                    if (!resultOfFileMoveOperation) {
                        throw new FileConnectorException("Error deleting file from [" + pathname(file.getName(), readDir) + "]");
                    }
                }

                if (fileProcessedException != null) {
                    throw fileProcessedException;
                }
            }
        } catch (Exception e) {
            alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_403, "", e);
            // TODO: handleException
//            handleException(e);
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
        URI uri = getEndpointURI();
        DataType dataType = getInboundDataType();

        if (dataType.getSerializer() instanceof BatchAdaptor) {
            BatchAdaptor batchAdaptor = (BatchAdaptor) dataType.getSerializer();
            FileSystemConnection con = fileConnector.getConnection(uri, null);
            Reader in = null;
            try {
                in = new InputStreamReader(con.readFile(file.getName(), readDir), charsetEncoding);
                batchAdaptor.processBatch(in, this);
            } finally {
                if (in != null) {
                    in.close();
                }
                con.closeReadFile();
                fileConnector.releaseConnection(uri, con, null);
            }
        } else {
            throw new Exception("Data type " + dataType.getType() + " does not support batch processing.");
        }
    }

    /** Delete a file */
    private boolean deleteFile(String name, String dir, boolean mayNotExist) throws Exception {
        URI uri = getEndpointURI();
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
        URI uri = getEndpointURI();
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
        URI uri = getEndpointURI();
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
     * @throws Exception
     */
    FileInfo[] listFiles() throws Exception {
        URI uri = getEndpointURI();
        FileSystemConnection con = fileConnector.getConnection(uri, null);

        try {
            return con.listFiles(readDir, filenamePattern, connectorProperties.isRegex(), connectorProperties.isIgnoreDot()).toArray(new FileInfo[0]);
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

    private URI getEndpointURI() throws URISyntaxException {
        StringBuilder sspBuilder = new StringBuilder();
        FileScheme scheme = connectorProperties.getScheme();
        String host = connectorProperties.getHost();
        
        sspBuilder.append("//");
        if (scheme == FileScheme.FILE && StringUtils.isNotBlank(host) && host.length() >= 3 && host.substring(1, 3).equals(":/")) {
            sspBuilder.append("/");
        }
        
        sspBuilder.append(host);
        
        String schemeName;
        if (scheme == FileScheme.WEBDAV) {
            if (connectorProperties.isSecure()) {
                schemeName = "https";
            } else {
                schemeName = "http";
            }
        } else {
            schemeName = scheme.getDisplayName();
        }
        
        return new URI(schemeName, sspBuilder.toString(), null);
    }

    @Override
    public void processBatchMessage(String message) throws BatchMessageProcessorException {
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
