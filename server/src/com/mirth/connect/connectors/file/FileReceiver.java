/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mirth.connect.connectors.file.filesystems.FileInfo;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReader;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;

public class FileReceiver extends PollConnector {
    protected transient Log logger = LogFactory.getLog(getClass());

    private String moveToDirectory = null;
    private String moveToFileName = null;
    private String errorMoveToDirectory = null;
    private String errorMoveToFileName = null;
    private String filenamePattern = null;
    private FileSystemConnectionOptions fileSystemOptions = null;

    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private FileConfiguration configuration = null;
    private FileConnector fileConnector = null;

    private String originalFilename = null;

    private FileReceiverProperties connectorProperties;
    private String charsetEncoding;

    private long fileSizeMinimum;
    private long fileSizeMaximum;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (FileReceiverProperties) SerializationUtils.clone(getConnectorProperties());

        if (connectorProperties.isBinary() && isProcessBatch()) {
            throw new ConnectorTaskException("Batch processing is not supported for binary data.");
        }

        this.charsetEncoding = CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding(), System.getProperty("ca.uhn.hl7v2.llp.charset"));

        // Load the default configuration
        String configurationClass = configurationController.getProperty(connectorProperties.getProtocol(), "fileConfigurationClass");

        try {
            configuration = (FileConfiguration) Class.forName(configurationClass).newInstance();
        } catch (Exception e) {
            logger.trace("could not find custom configuration class, using default");
            configuration = new DefaultFileConfiguration();
        }

        try {
            configuration.configureConnectorDeploy(this, connectorProperties);
        } catch (Exception e) {
            throw new ConnectorTaskException(e);
        }

        this.moveToDirectory = connectorProperties.getMoveToDirectory();
        this.moveToFileName = connectorProperties.getMoveToFileName();
        this.errorMoveToDirectory = connectorProperties.getErrorMoveToDirectory();
        this.errorMoveToFileName = connectorProperties.getErrorMoveToFileName();

        fileSizeMinimum = NumberUtils.toLong(connectorProperties.getFileSizeMinimum(), 0);
        fileSizeMaximum = NumberUtils.toLong(connectorProperties.getFileSizeMaximum(), 0);

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {}

    @Override
    public void onStart() throws ConnectorTaskException {
        try {
            String channelId = getChannelId();
            String channelName = getChannel().getName();
            String username = replacer.replaceValues(connectorProperties.getUsername(), channelId, channelName);
            String password = replacer.replaceValues(connectorProperties.getPassword(), channelId, channelName);
            String host = replacer.replaceValues(connectorProperties.getHost(), channelId, channelName);

            SchemeProperties schemeProperties = null;
            if (connectorProperties.getSchemeProperties() != null) {
                schemeProperties = connectorProperties.getSchemeProperties().clone();
            }

            if (schemeProperties instanceof SftpSchemeProperties) {
                SftpSchemeProperties sftpProperties = (SftpSchemeProperties) schemeProperties;

                sftpProperties.setKeyFile(replacer.replaceValues(sftpProperties.getKeyFile(), channelId, channelName));
                sftpProperties.setPassPhrase(replacer.replaceValues(sftpProperties.getPassPhrase(), channelId, channelName));
                sftpProperties.setKnownHostsFile(replacer.replaceValues(sftpProperties.getKnownHostsFile(), channelId, channelName));
                sftpProperties.setConfigurationSettings(replacer.replaceValues(sftpProperties.getConfigurationSettings(), channelId, channelName));
            } else if (schemeProperties instanceof S3SchemeProperties) {
                S3SchemeProperties s3Properties = (S3SchemeProperties) schemeProperties;

                s3Properties.setRegion(replacer.replaceValues(s3Properties.getRegion(), channelId, channelName));
                s3Properties.setCustomHeaders(replacer.replaceKeysAndValuesInMap(s3Properties.getCustomHeaders(), channelId, channelName));
            }

            URI uri = fileConnector.getEndpointURI(host, connectorProperties.getScheme(), schemeProperties, connectorProperties.isSecure());

            fileSystemOptions = new FileSystemConnectionOptions(uri, connectorProperties.isAnonymous(), username, password, schemeProperties);
            FileSystemConnection con = fileConnector.getConnection(fileSystemOptions);
            fileConnector.releaseConnection(con, fileSystemOptions);
        } catch (URISyntaxException e1) {
            throw new ConnectorTaskException("Error creating URI.", e1);
        } catch (Exception e) {
            throw new ConnectorTaskException(e.getMessage(), e);
        }
    }

    @Override
    public void onStop() throws ConnectorTaskException {
        try {
            fileConnector.doStop();
        } catch (FileConnectorException e) {
            throw new ConnectorTaskException("Failed to stop File Connector", e);
        }

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onHalt() throws ConnectorTaskException {
        fileConnector.disconnect();
        onStop();
    }

    @Override
    protected void poll() {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.POLLING));
        try {
            String channelId = getChannelId();
            String channelName = getChannel().getName();
            String host = replacer.replaceValues(connectorProperties.getHost(), channelId, channelName);

            String username = replacer.replaceValues(connectorProperties.getUsername(), channelId, channelName);
            String password = replacer.replaceValues(connectorProperties.getPassword(), channelId, channelName);
            filenamePattern = replacer.replaceValues(connectorProperties.getFileFilter(), channelId, channelName);

            SchemeProperties schemeProperties = null;
            if (connectorProperties.getSchemeProperties() != null) {
                schemeProperties = connectorProperties.getSchemeProperties().clone();
            }

            if (schemeProperties instanceof SftpSchemeProperties) {
                SftpSchemeProperties sftpProperties = (SftpSchemeProperties) schemeProperties;

                sftpProperties.setKeyFile(replacer.replaceValues(sftpProperties.getKeyFile(), channelId, channelName));
                sftpProperties.setPassPhrase(replacer.replaceValues(sftpProperties.getPassPhrase(), channelId, channelName));
                sftpProperties.setKnownHostsFile(replacer.replaceValues(sftpProperties.getKnownHostsFile(), channelId, channelName));
                sftpProperties.setConfigurationSettings(replacer.replaceValues(sftpProperties.getConfigurationSettings(), channelId, channelName));
            } else if (schemeProperties instanceof S3SchemeProperties) {
                S3SchemeProperties s3Properties = (S3SchemeProperties) schemeProperties;

                s3Properties.setRegion(replacer.replaceValues(s3Properties.getRegion(), channelId, channelName));
                s3Properties.setCustomHeaders(replacer.replaceKeysAndValuesInMap(s3Properties.getCustomHeaders(), channelId, channelName));
            }

            URI uri = fileConnector.getEndpointURI(host, connectorProperties.getScheme(), schemeProperties, connectorProperties.isSecure());
            String readDir = fileConnector.getPathPart(uri);

            fileSystemOptions = new FileSystemConnectionOptions(uri, connectorProperties.isAnonymous(), username, password, schemeProperties);

            String pollId = "" + System.nanoTime();
            AtomicInteger pollSequenceId = new AtomicInteger(1);

            if (connectorProperties.isDirectoryRecursion()) {
                Set<String> visitedDirectories = new HashSet<String>();
                Stack<String> directoryStack = new Stack<String>();
                directoryStack.push(readDir);

                List<FileInfo> previousFiles = null;
                List<FileInfo> files;

                while ((files = listFilesRecursively(visitedDirectories, directoryStack)) != null) {
                    if (!files.isEmpty()) {
                        if (previousFiles != null) {
                            processFiles(previousFiles, pollId, pollSequenceId, false);
                        }
                        previousFiles = files;
                    }
                }

                if (previousFiles != null) {
                    processFiles(previousFiles, pollId, pollSequenceId, true);
                }
            } else {
                processFiles(listFiles(readDir), pollId, pollSequenceId, true);
            }
        } catch (Throwable t) {
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), null, ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), null, t));
            logger.error("Error polling in channel: " + getChannelId(), t);
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
        }
    }

    private List<FileInfo> listFilesRecursively(Set<String> visitedDirectories, Stack<String> directoryStack) throws Exception {
        while (!directoryStack.isEmpty()) {
            // Get the current directory
            String fromDir = directoryStack.pop();

            if (!visitedDirectories.contains(fromDir)) {
                visitedDirectories.add(fromDir);

                // Add any subdirectories to the stack
                List<String> directories = listDirectories(fromDir);

                for (int i = directories.size() - 1; i >= 0; i--) {
                    directoryStack.push(directories.get(i));
                }

                // Return the files from the current directory
                return listFiles(fromDir);
            }
        }

        return null;
    }

    private void processFiles(List<FileInfo> files, String pollId, AtomicInteger pollSequenceId, boolean recursionComplete) {
        // sort files by specified attribute before processing
        sortFiles(files);

        for (int i = 0, size = files.size(); i < size; i++) {
            if (isTerminated()) {
                return;
            }
            FileInfo file = files.get(i);

            // We know that if recursion is not complete, there are more directories to go, so the poll is not complete.
            boolean pollComplete = recursionComplete && i == size - 1;

            boolean fileValid = isFileValid(file);

            if (fileValid) {
                /*
                 * If recursion is incomplete, pollComplete will be false and we don't need to
                 * update it.
                 * 
                 * If recursion is complete and the poll is complete, nothing more needs to be
                 * checked.
                 * 
                 * If recursion is complete and the poll is not yet complete, we need to check to
                 * see if there's another valid file in the list. The rest of the files may be
                 * invalid, in which case pollComplete needs to be updated to true.
                 */
                if (recursionComplete && !pollComplete) {
                    boolean nextValidFileFound = false;

                    for (int j = i + 1; j < size; j++) {
                        FileInfo nextFile = files.get(j);

                        if (isFileValid(nextFile)) {
                            nextValidFileFound = true;
                            break;
                        }
                    }

                    if (!nextValidFileFound) {
                        // No other valid files left, so the poll is complete
                        pollComplete = true;
                    }
                }
            }

            /*
             * If the file is valid, process it. If the file is invalid, but the poll is complete,
             * still process it, and log a warning.
             */
            if (fileValid || pollComplete) {
                if (!fileValid) {
                    logger.warn("The file " + file.getName() + " may have been modified since being listed by the File Reader. This message will still be processed by the channel, but the file age/size may not be correct with respect to the current File Reader settings.");
                }

                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.READING));
                processFile(file, pollId, pollSequenceId, pollComplete);
                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));

                if (pollComplete) {
                    break;
                }
            }
        }
    }

    public void sortFiles(List<FileInfo> files) {
        String sortAttribute = connectorProperties.getSortBy();

        if (sortAttribute.equals(FileReceiverProperties.SORT_BY_DATE)) {
            files.sort(Comparator.comparingLong(FileInfo::getLastModified));
        } else if (sortAttribute.equals(FileReceiverProperties.SORT_BY_SIZE)) {
            files.sort(Comparator.comparingLong(FileInfo::getSize));
        } else {
            files.sort((file1, file2) -> file1.getName().compareToIgnoreCase(file2.getName()));
        }
    }

    public synchronized void processFile(FileInfo file, String pollId, AtomicInteger pollSequenceId, boolean pollComplete) {
        try {
            // Add the original filename to the channel map
            originalFilename = file.getName();
            Map<String, Object> sourceMap = new HashMap<String, Object>();
            sourceMap.put("originalFilename", originalFilename);
            sourceMap.put("fileDirectory", file.getParent());
            sourceMap.put("fileSize", file.getSize());
            sourceMap.put("fileLastModified", file.getLastModified());
            sourceMap.put("pollId", pollId);
            sourceMap.put("pollSequenceId", pollSequenceId.get());
            if (pollComplete) {
                sourceMap.put("pollComplete", true);
            }

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
                boolean error = false;

                try {
                    Response response = null;

                    // Allow implementation to inject custom source map variables
                    file.populateSourceMap(sourceMap);

                    // ast: use the user-selected encoding
                    if (isProcessBatch()) {
                        FileSystemConnection con = fileConnector.getConnection(fileSystemOptions);
                        Reader in = null;
                        try {
                            in = new InputStreamReader(con.readFile(file.getName(), file.getParent(), sourceMap), charsetEncoding);
                            BatchRawMessage batchRawMessage = new BatchRawMessage(new BatchMessageReader(in), sourceMap);

                            Boolean messagesExist = dispatchBatchMessage(batchRawMessage, null);
                            if (messagesExist != null && !messagesExist) {
                                logger.warn("File " + originalFilename + " was successfully processed, but no messages were dispatched to the channel.");
                            }
                        } finally {
                            pollSequenceId.incrementAndGet();
                            if (in != null) {
                                in.close();
                            }
                            con.closeReadFile();
                            fileConnector.releaseConnection(con, fileSystemOptions);
                        }
                    } else {
                        RawMessage rawMessage;
                        if (connectorProperties.isBinary()) {
                            rawMessage = new RawMessage(getBytesFromFile(file, sourceMap));
                        } else {
                            rawMessage = new RawMessage(new String(getBytesFromFile(file, sourceMap), charsetEncoding));
                        }

                        rawMessage.setSourceMap(sourceMap);

                        DispatchResult dispatchResult = null;
                        try {
                            dispatchResult = dispatchRawMessage(rawMessage);
                        } finally {
                            pollSequenceId.incrementAndGet();
                            finishDispatch(dispatchResult);
                        }

                        response = dispatchResult.getSelectedResponse();
                    }

                    // True if the response status is ERROR and we're not processing a batch
                    errorResponse = response != null && response.getStatus() == Status.ERROR;
                } catch (Exception e) {
                    error = true;
                    logger.error("Unable to dispatch message to channel " + getChannelId() + ". File: " + file.getAbsolutePath(), e);
                } catch (Throwable t) {
                    error = true;
                    String errorMessage = "Error reading file " + file.getAbsolutePath() + "\n" + t.getMessage();
                    logger.error(errorMessage);
                    fileProcessedException = new FileConnectorException(errorMessage, t);
                }

                boolean shouldUseErrorFields = false;

                // If the message wasn't successfully processed through the channel, set the error file action
                if (error) {
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
                        destinationDir = replacer.replaceValues(destinationDir, getChannelId(), getChannel().getName(), sourceMap);
                    } else {
                        destinationDir = file.getParent();
                    }

                    // If the user-specified filename is blank, use the default (original filename)
                    if (StringUtils.isNotBlank(destinationName)) {
                        destinationName = replacer.replaceValues(destinationName, getChannelId(), getChannel().getName(), sourceMap);
                    } else {
                        destinationName = originalFilename;
                    }

                    if (!filesEqual(file.getParent(), originalFilename, destinationDir, destinationName)) {
                        if (shouldUseErrorFields) {
                            logger.error("Moving file to error directory: " + destinationDir);
                        }

                        // Delete the destination file if it exists, and then rename the original file
                        deleteFile(destinationName, destinationDir, true);
                        boolean resultOfFileMoveOperation = renameFile(file.getName(), file.getParent(), destinationName, destinationDir);

                        if (!resultOfFileMoveOperation) {
                            throw new FileConnectorException("Error moving file from [" + pathname(file.getName(), file.getParent()) + "] to [" + pathname(destinationName, destinationDir) + "]");
                        }
                    }
                } else if (action == FileAction.DELETE) {
                    // Delete the original file
                    boolean resultOfFileMoveOperation = deleteFile(file.getName(), file.getParent(), false);

                    if (!resultOfFileMoveOperation) {
                        throw new FileConnectorException("Error deleting file from [" + pathname(file.getName(), file.getParent()) + "]");
                    }
                }

                if (fileProcessedException != null) {
                    throw fileProcessedException;
                }
            }
        } catch (Exception e) {
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), 12345L, ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "", e));
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

    /** Delete a file */
    private boolean deleteFile(String name, String dir, boolean mayNotExist) throws Exception {
        FileSystemConnection con = fileConnector.getConnection(fileSystemOptions);
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
            fileConnector.releaseConnection(con, fileSystemOptions);
        }
    }

    private boolean renameFile(String fromName, String fromDir, String toName, String toDir) throws Exception {
        FileSystemConnection con = fileConnector.getConnection(fileSystemOptions);
        try {

            con.move(fromName, fromDir, toName, toDir);
            return true;
        } catch (Exception e) {

            return false;
        } finally {
            fileConnector.releaseConnection(con, fileSystemOptions);
        }
    }

    // Returns the contents of the file in a byte array.
    private byte[] getBytesFromFile(FileInfo file, Map<String, Object> sourceMap) throws Exception {
        FileSystemConnection con = fileConnector.getConnection(fileSystemOptions);

        try {
            InputStream is = null;
            try {
                is = con.readFile(file.getName(), file.getParent(), sourceMap);

                // Get the size of the file
                long length = file.getSize();

                // You cannot create an array using a long type.
                // It needs to be an int type.
                // Before converting to an int type, check
                // to ensure that file is not larger than Integer.MAX_VALUE.
                if (length > Integer.MAX_VALUE) {
                    // File is too large
                    throw new IOException("File " + file.getName() + " is too large. Unable to read files greater than " + Integer.MAX_VALUE + " bytes.");
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

                // Verify the download was complete
                con.closeReadFile();
                return bytes;
            } finally {
                // Close the input stream
                if (is != null) {
                    is.close();
                }
            }
        } finally {
            fileConnector.releaseConnection(con, fileSystemOptions);
        }
    }

    boolean isFileValid(FileInfo file) {
        if (file.isDirectory() || !file.isReadable() || !file.isFile()) {
            return false;
        }

        boolean checkFileAge = connectorProperties.isCheckFileAge();
        if (checkFileAge) {
            long fileAge = Long.valueOf(connectorProperties.getFileAge());
            long lastMod = file.getLastModified();
            long now = System.currentTimeMillis();
            if ((now - lastMod) < fileAge) {
                return false;
            }
        }

        long fileSize = file.getSize();

        if (fileSize < fileSizeMinimum) {
            return false;
        }
        if (!connectorProperties.isIgnoreFileSizeMaximum() && fileSize > fileSizeMaximum) {
            return false;
        }

        return true;
    }

    /**
     * Get a list of files to be processed.
     * 
     * @return a list of files to be processed.
     * @throws Exception
     */
    List<FileInfo> listFiles(String fromDir) throws Exception {
        FileSystemConnection con = fileConnector.getConnection(fileSystemOptions);

        try {
            List<FileInfo> files = con.listFiles(fromDir, filenamePattern, connectorProperties.isRegex(), connectorProperties.isIgnoreDot());

            if (files != null) {
                for (Iterator<FileInfo> it = files.iterator(); it.hasNext();) {
                    if (!isFileValid(it.next())) {
                        it.remove();
                    }
                }
            }

            return CollectionUtils.isNotEmpty(files) ? files : new ArrayList<FileInfo>();
        } finally {
            fileConnector.releaseConnection(con, fileSystemOptions);
        }
    }

    /**
     * Get a list of subdirectories within a directory.
     * 
     * @return a list of subdirectories.
     * @throws Exception
     */
    List<String> listDirectories(String fromDir) throws Exception {
        FileSystemConnection con = fileConnector.getConnection(fileSystemOptions);

        try {
            return con.listDirectories(fromDir);
        } finally {
            fileConnector.releaseConnection(con, fileSystemOptions);
        }
    }

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        //TODO add cleanup code
        finishDispatch(dispatchResult);
    }

    public void setFileConnector(FileConnector fileConnector) {
        this.fileConnector = fileConnector;
    }
}
