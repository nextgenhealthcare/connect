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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.mule.config.MuleProperties;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.transformers.simple.ByteArrayToSerializable;
import org.mule.transformers.simple.SerializableToByteArray;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.ConnectorException;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.util.Utility;

import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnectionFactory;
import com.mirth.connect.model.MessageObject;


public class FileConnector extends AbstractServiceEnabledConnector {
    private Logger logger = Logger.getLogger(this.getClass());

    // These are properties that can be overridden on the Receiver by the
    // endpoint
    // declarations
    public static final String PROPERTY_POLLING_TYPE = "pollingType";
    public static final String PROPERTY_POLLING_TIME = "pollingTime";
    public static final String PROPERTY_ORIGINAL_FILENAME = "originalFilename";
    public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";
    public static final String PROPERTY_TEMPLATE = "template";
    public static final String PROPERTY_BINARY = "binary";
    public static final String PROPERTY_FILE_AGE = "fileAge";
    public static final String PROPERTY_FILE_FILTER = "fileFilter";
    public static final String PROPERTY_MOVE_TO_PATTERN = "moveToPattern";
    public static final String PROPERTY_MOVE_TO_DIRECTORY = "moveToDirectory";
    public static final String PROPERTY_MOVE_TO_ERROR_DIRECTORY = "moveToErrorDirectory";
    public static final String PROPERTY_DELETE_ON_READ = "autoDelete";
    public static final String PROPERTY_DIRECTORY = "directory";
    public static final String PROPERTY_SORT_ATTRIBUTE = "sortAttribute";
    public static final String PROPERTY_BATCH_PROCESS = "processBatchFiles";
    public static final String PROPERTY_CHANNEL_ID = "channelId";
    public static final String PROPERTY_SCHEME = "scheme";
    public static final String PROPERTY_PASSIVE_MODE = "passive";
    public static final String PROPERTY_SECURE_MODE = "secure";
    public static final String PROPERTY_REGEX = "regex";
    public static final String PROPERTY_TIMEOUT = "timeout";

    public static final String SORT_NAME = "name";
    public static final String SORT_DATE = "date";
    public static final String SORT_SIZE = "size";
    public static final long DEFAULT_POLLING_FREQUENCY = 1000;

    public static final String POLLING_TYPE_INTERVAL = "interval";
    public static final String POLLING_TYPE_TIME = "time";

    // ast: encoding Charset
    public static final String PROPERTY_CHARSET_ENCODING = "charsetEncoding";
    public static final String CHARSET_KEY = "ca.uhn.hl7v2.llp.charset";
    public static final String DEFAULT_CHARSET_ENCODING = System.getProperty(CHARSET_KEY, java.nio.charset.Charset.defaultCharset().name());

    public static final String SCHEME_FILE = "file";
    public static final String SCHEME_FTP = "ftp";
    public static final String SCHEME_SFTP = "sftp";
    public static final String SCHEME_SMB = "smb";
    public static final String SCHEME_WEBDAV = "webdav";

    /**
     * Time in milliseconds to poll. On each poll the poll() method is called
     */
    private String pollingType = POLLING_TYPE_INTERVAL;
    private String pollingTime = "12:00 AM";
    private long pollingFrequency = 0;
    private String outputPattern = null;
    private String template = null;
    public FilenameParser filenameParser = new VariableFilenameParser();
    private Map<String, ObjectPool> pools = new HashMap<String, ObjectPool>();
    private String username;
    private String password;
    private boolean binary = false;
    private String moveToPattern = null;
    private String writeToDirectoryName = null;
    private String moveToDirectory = null;
    private String moveToErrorDirectory = null;
    private String sortAttribute = SORT_NAME;
    private boolean outputAppend = false;
    private boolean autoDelete = true;
    private boolean checkFileAge = false;
    private String fileFilter = "*";
    private long fileAge = 0;
    private FileOutputStream outputStream = null;
    private boolean serialiseObjects = false;
    private UMOMessageReceiver receiver = null;
    private boolean processBatchFiles = true;
    private boolean validateConnections = true;
    // ast: encoding charset
    private String charsetEncoding = DEFAULT_CHARSET_ENCODING;
    private String channelId;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private Map protocolProperties;
    private String inboundProtocol;
    private String scheme = SCHEME_FILE;
    private boolean passive = false;
    private boolean secure = false;
    private boolean regex = false;
    private int timeout;

    public FileConnector() {
        filenameParser = new VariableFilenameParser();
        setCharsetEncoding(DEFAULT_CHARSET_ENCODING);
    }

    protected Object getReceiverKey(UMOComponent component, UMOEndpoint endpoint) {
        return endpoint.getEndpointURI().getAddress();
    }

    /**
     * Extract the path part of a URI as needed by the rest of this code.
     * UMOEndpointURI.getPath fails for some URI's when the scheme is file.
     * 
     * @param uri
     *            The URI from which the path part is to be taken.
     * @return The path (directory, folder) part of the URI.
     */
    protected String getPathPart(UMOEndpointURI uri) {
        if (scheme.equals("file")) {
            // In //xyz, return xyz.
            return uri.getUri().getSchemeSpecificPart().substring(2);
        } else {
            // For the remaining cases, getPath seems to do the right thing.
            return uri.getPath();
        }
    }

    /**
     * Registers a listener for a particular directory The following properties
     * can be overriden in the endpoint declaration
     * <ul>
     * <li>moveToDirectory</li>
     * <li>filterPatterns</li>
     * <li>filterClass</li>
     * <li>pollingFrequency</li>
     * </ul>
     */
    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
        String readDir = getPathPart(endpoint.getEndpointURI());
        long polling = this.pollingFrequency;

        String moveTo = moveToDirectory;
        Map props = endpoint.getProperties();
        if (props != null) {
            // Override properties on the endpoint for the specific endpoint
            String move = (String) props.get(PROPERTY_MOVE_TO_DIRECTORY);
            if (move != null) {
                moveTo = move;
            }
            String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);
            if (tempPolling != null) {
                polling = Long.parseLong(tempPolling);
            }
            Long tempFileAge = (Long) props.get(PROPERTY_FILE_AGE);
            if (tempFileAge != null) {
                setFileAge(tempFileAge.longValue());
            }
            String pollingType = (String) props.get(PROPERTY_POLLING_TYPE);
            if (pollingType != null) {
                setPollingType(pollingType);
            }
            String pollingTime = (String) props.get(PROPERTY_POLLING_TIME);
            if (pollingTime != null) {
                setPollingTime(pollingTime);
            }
        }
        if (polling <= 0) {
            polling = DEFAULT_POLLING_FREQUENCY;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("set polling frequency to: " + polling);
        }
        try {
            // TODO: file has more parameters than FTP, must apparently update
            // FTP.
            receiver = serviceDescriptor.createMessageReceiver(this, component, endpoint, new Object[] { readDir, moveTo, moveToPattern, moveToErrorDirectory, new Long(polling) });
            return receiver;
        } catch (Exception e) {
            throw new InitialisationException(new Message(Messages.FAILED_TO_CREATE_X_WITH_X, "Message Receiver", serviceDescriptor.getMessageReceiver()), e, this);
        }
    }

    protected synchronized void doStop() throws UMOException {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.warn("Failed to close file output stream on stop: " + e);
            }
        }
        try {
            for (Iterator<ObjectPool> it = pools.values().iterator(); it.hasNext();) {
                ObjectPool pool = it.next();
                pool.close();
            }
            pools.clear();
        } catch (Exception e) {
            throw new ConnectorException(new Message(Messages.FAILED_TO_STOP_X, "File Connector"), this, e);
        }
    }

    protected synchronized void doStart() throws UMOException {
        if (receiver != null) {
            ((FileMessageReceiver) receiver).setRoutingError(false);
        }
    }

    protected void doDispose() {
        try {
            doStop();
        } catch (UMOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // ********************************************
    // connection pool management

    /**
     * Allocate a connection from the pool
     * 
     * @param uri
     *            The URI of the endpoint for which the connection is being
     *            created.
     * @param messageObject
     *            ??
     * @return The allocated connection.
     */
    protected FileSystemConnection getConnection(UMOEndpointURI uri, MessageObject messageObject) throws Exception {
        ObjectPool pool = getConnectionPool(uri, messageObject);
        FileSystemConnection con = (FileSystemConnection) pool.borrowObject();
        if (!con.isConnected() || !con.isValid()) {
            destroyConnection(uri, con, messageObject);
            con = (FileSystemConnection) pool.borrowObject();
        }
        return con;
    }

    /**
     * Return a connection to the pool
     * 
     * @param uri
     *            The URI of the endpoint from which the connection is being
     *            released.
     * @param client
     *            The connection that is being released.
     * @param messageObject
     *            ??
     * @throws Exception
     */
    protected void releaseConnection(UMOEndpointURI uri, FileSystemConnection connection, MessageObject messageObject) throws Exception {
        if (isCreateDispatcherPerRequest()) {
            destroyConnection(uri, connection, messageObject);
        } else {
            if (connection != null && connection.isConnected()) {
                ObjectPool pool = getConnectionPool(uri, messageObject);
                pool.returnObject(connection);
            }
        }
    }

    /**
     * Permanently destroy a connection.
     * 
     * @param uri
     *            The URI of the endpoint from which the connection is being
     *            released.
     * @param connection
     *            The connection that is to be destroyed.
     * @param messageObject
     *            ??
     * @throws Exception
     */
    protected void destroyConnection(UMOEndpointURI uri, FileSystemConnection connection, MessageObject messageObject) throws Exception {
        if (connection != null) {
            ObjectPool pool = getConnectionPool(uri, messageObject);
            pool.invalidateObject(connection);
        }
    }

    private String replace(String src, MessageObject messageObject) {
        if (messageObject == null) {
            return replacer.replaceValues(src, channelId);
        } else if (src.indexOf('$') > -1) {
            return replacer.replaceValues(src, messageObject);
        } else {
            return src;
        }
    }

    /**
     * Gets the pool of connections to the "server" for the specified endpoint,
     * creating the pool if necessary.
     * 
     * @param uri
     *            The URI of the endpoint the created pool should be associated
     *            with.
     * @param messageObject
     *            ???
     * @return The pool of connections for this endpoint.
     */
    private synchronized ObjectPool getConnectionPool(UMOEndpointURI endpointUri, MessageObject messageObject) {

        // Resolve all the connection parameters to final substituted values,
        // since we're about to actually use them.
        String username = replace(getUsername(), messageObject);
        String password = replace(getPassword(), messageObject);
        URI uri;

        try {
            uri = new URI(replace(endpointUri.toString(), messageObject));
        } catch (URISyntaxException e) {
            logger.error("Could not create URI from endpoint: " + endpointUri.toString());
            uri = endpointUri.getUri();
        }

        String key = FileSystemConnectionFactory.getPoolKey(getScheme(), username, password, uri.getHost(), uri.getPort(), isSecure());
        ObjectPool pool = pools.get(key);
        if (pool == null) {
            GenericObjectPool.Config config = new GenericObjectPool.Config();
            if (isValidateConnections()) {
                config.testOnBorrow = true;
                config.testOnReturn = true;
            }
            pool = new GenericObjectPool(new FileSystemConnectionFactory(getScheme(), username, password, uri.getHost(), uri.getPort(), isPassive(), isSecure(), getTimeout()), config);

            pools.put(key, pool);
        }
        return pool;
    }

    // ********************************************
    // getters & setters

    public String getProtocol() {
        return "FILE";
    }

    public FilenameParser getFilenameParser() {
        return filenameParser;
    }

    public void setFilenameParser(FilenameParser filenameParser) {
        this.filenameParser = filenameParser;
    }

    /**
     * @return Returns the moveToDirectoryName.
     */
    public String getMoveToDirectory() {
        return moveToDirectory;
    }

    /**
     * @param dir
     *            The moveToDirectoryName to set.
     */
    public void setMoveToDirectory(String dir) {
        this.moveToDirectory = dir;
    }

    public String getMoveToErrorDirectory() {
        return moveToErrorDirectory;
    }

    public void setMoveToErrorDirectory(String dir) {
        this.moveToErrorDirectory = dir;
    }

    /**
     * @return Returns the outputAppend.
     */
    public boolean isOutputAppend() {
        return outputAppend;
    }

    /**
     * @param outputAppend
     *            The outputAppend to set.
     */
    public void setOutputAppend(boolean outputAppend) {
        this.outputAppend = outputAppend;
    }

    /**
     * @return Returns the outputPattern.
     */
    public String getOutputPattern() {
        return outputPattern;
    }

    /**
     * @param outputPattern
     *            The outputPattern to set.
     */
    public void setOutputPattern(String outputPattern) {
        this.outputPattern = outputPattern;
    }

    /**
     * @return Returns the outputStream.
     */
    public FileOutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * @param outputStream
     *            The outputStream to set.
     */
    public void setOutputStream(FileOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * @return Returns the pollingFrequency.
     */
    public long getPollingFrequency() {
        return pollingFrequency;
    }

    /**
     * @param pollingFrequency
     *            The pollingFrequency to set.
     */
    public void setPollingFrequency(long pollingFrequency) {
        this.pollingFrequency = pollingFrequency;
    }

    public boolean isCheckFileAge() {
        return checkFileAge;
    }

    public void setCheckFileAge(boolean checkFileAge) {
        this.checkFileAge = checkFileAge;
    }

    /**
     * @return Returns the fileAge.
     */
    public long getFileAge() {
        return fileAge;
    }

    /**
     * @param fileAge
     *            The fileAge in seconds to set.
     */
    public void setFileAge(long fileAge) {
        this.fileAge = fileAge;
        this.checkFileAge = true;
    }

    /**
     * @return Returns the writeToDirectory.
     */
    public String getWriteToDirectory() {
        return writeToDirectoryName;
    }

    /**
     * @return Contents
     */
    public String getTemplate() {
        return template;
    }

    /**
     * 
     * @param val
     *            = template to set
     */
    public void setTemplate(String val) {
        template = val;
    }

    /**
     * @param dir
     *            The writeToDirectory to set.
     */
    public void setWriteToDirectory(String dir) throws IOException {
        this.writeToDirectoryName = dir;
        if (writeToDirectoryName != null) {
            File writeToDirectory = Utility.openDirectory((writeToDirectoryName));
            if (!(writeToDirectory.canRead()) || !writeToDirectory.canWrite()) {
                throw new IOException("Error on initialization, Write To directory does not exist or is not read/write");
            }
        }
    }

    public boolean isSerialiseObjects() {
        return serialiseObjects;
    }

    public void setSerialiseObjects(boolean serialiseObjects) {
        // set serialisable transformers on the connector if this is set
        if (serialiseObjects) {
            if (serviceOverrides == null)
                serviceOverrides = new Properties();
            serviceOverrides.setProperty(MuleProperties.CONNECTOR_INBOUND_TRANSFORMER, ByteArrayToSerializable.class.getName());
            serviceOverrides.setProperty(MuleProperties.CONNECTOR_OUTBOUND_TRANSFORMER, SerializableToByteArray.class.getName());
        }

        this.serialiseObjects = serialiseObjects;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
        if (!autoDelete) {
            if (serviceOverrides == null)
                serviceOverrides = new Properties();
            if (serviceOverrides.getProperty(MuleProperties.CONNECTOR_MESSAGE_ADAPTER) == null)
                serviceOverrides.setProperty(MuleProperties.CONNECTOR_MESSAGE_ADAPTER, TextLineMessageAdapter.class.getName());
        }
    }

    public String getMoveToPattern() {
        return moveToPattern;
    }

    public void setMoveToPattern(String moveToPattern) {
        this.moveToPattern = moveToPattern;
    }

    public String getSortAttribute() {
        return this.sortAttribute;
    }

    public void setSortAttribute(String sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    // ast: set the charset Encoding
    public void setCharsetEncoding(String charsetEncoding) {
        if ((charsetEncoding == null) || (charsetEncoding.equals("")) || (charsetEncoding.equalsIgnoreCase("DEFAULT_ENCODING"))) {
            charsetEncoding = DEFAULT_CHARSET_ENCODING;
        }
        
        logger.debug("FileConnector: trying to set the encoding to " + charsetEncoding);
        
        try {
            this.charsetEncoding = charsetEncoding;
        } catch (Exception e) {
            // set the encoding to the default one: this charset can't launch an
            // exception
            this.charsetEncoding = java.nio.charset.Charset.defaultCharset().name();
            logger.error("Impossible to use [" + charsetEncoding + "] as the Charset Encoding: changing to the platform default [" + this.charsetEncoding + "]");
        }
    }

    // ast: get the charset encoding
    public String getCharsetEncoding() {
        if ((this.charsetEncoding == null) || (this.charsetEncoding.equals("")) || (this.charsetEncoding.equalsIgnoreCase("DEFAULT_ENCODING"))) {
            // Default Charset
            return DEFAULT_CHARSET_ENCODING;
        }
        return (this.charsetEncoding);
    }

    public String getFileFilter() {
        return fileFilter;
    }

    public void setFileFilter(String fileFilter) {
        this.fileFilter = fileFilter;
    }

    public boolean isProcessBatchFiles() {
        return processBatchFiles;
    }

    public void setProcessBatchFiles(boolean processBatchFiles) {
        this.processBatchFiles = processBatchFiles;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getPollingTime() {
        return pollingTime;
    }

    public void setPollingTime(String pollingTime) {
        this.pollingTime = pollingTime;
    }

    public String getPollingType() {
        return pollingType;
    }

    public void setPollingType(String pollingType) {
        this.pollingType = pollingType;
    }

    public Map getProtocolProperties() {
        return protocolProperties;
    }

    public void setProtocolProperties(Map protocolProperties) {
        this.protocolProperties = protocolProperties;
    }

    public String getInboundProtocol() {
        return inboundProtocol;
    }

    public void setInboundProtocol(String inboundProtocol) {
        this.inboundProtocol = inboundProtocol;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isValidateConnections() {
        return validateConnections;
    }

    public void setValidateConnections(boolean validateConnections) {
        this.validateConnections = validateConnections;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(boolean passive) {
        this.passive = passive;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
