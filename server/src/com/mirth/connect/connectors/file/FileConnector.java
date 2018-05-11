/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnectionFactory;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.DestinationConnector;

public class FileConnector {
    private Logger logger = Logger.getLogger(this.getClass());

    private Map<String, ObjectPool<FileSystemConnection>> pools = new HashMap<String, ObjectPool<FileSystemConnection>>();
    private FileOutputStream outputStream = null;
    private Set<FileSystemConnection> connections = new HashSet<FileSystemConnection>();

    private String channelId;
    private FileScheme scheme;
    private String timeout;
    private boolean passive;
    private boolean secure;
    private boolean validateConnection;
    private int maxTotalConnections = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;

    public FileConnector(String channelId, ConnectorProperties connectorProperties, Connector connector) {
        this.channelId = channelId;

        if (connectorProperties instanceof FileReceiverProperties) {
            FileReceiverProperties fileReceiverProperties = (FileReceiverProperties) connectorProperties;
            this.scheme = fileReceiverProperties.getScheme();
            this.timeout = fileReceiverProperties.getTimeout();
            this.passive = fileReceiverProperties.isPassive();
            this.secure = fileReceiverProperties.isSecure();
            this.validateConnection = fileReceiverProperties.isValidateConnection();
        } else if (connectorProperties instanceof FileDispatcherProperties) {
            FileDispatcherProperties fileDispatcherProperties = (FileDispatcherProperties) connectorProperties;
            this.scheme = fileDispatcherProperties.getScheme();
            this.timeout = fileDispatcherProperties.getTimeout();
            this.passive = fileDispatcherProperties.isPassive();
            this.secure = fileDispatcherProperties.isSecure();
            this.validateConnection = fileDispatcherProperties.isValidateConnection();
        }

        if (connector instanceof DestinationConnector) {
            // Set the max total to at least the default value
            maxTotalConnections = Math.max(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL, ((DestinationConnector) connector).getPotentialThreadCount());
        }
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public FileScheme getScheme() {
        return scheme;
    }

    public void setScheme(FileScheme scheme) {
        this.scheme = scheme;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
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

    public boolean isValidateConnection() {
        return validateConnection;
    }

    public void setValidateConnection(boolean validateConnection) {
        this.validateConnection = validateConnection;
    }

    /**
     * URI.getPath() does not retrieve the desired result for relative paths. The first directory
     * would be omitted and the second directory would be used with the system's root as the base.
     * Thus for connectors using the FILE scheme, we retrieve the path using an alternate method.
     */
    protected String getPathPart(URI uri) {
        if (scheme == FileScheme.FILE) {
            // In //xyz, return xyz.
            return uri.getSchemeSpecificPart().substring(2);
        } else {
            // For the remaining cases, getPath seems to do the right thing.
            return uri.getPath();
        }
    }

    /**
     * Registers a listener for a particular directory The following properties can be overriden in
     * the endpoint declaration
     * <ul>
     * <li>moveToDirectory</li>
     * <li>filterPatterns</li>
     * <li>filterClass</li>
     * <li>pollingFrequency</li>
     * </ul>
     */

    protected synchronized void doStop() throws FileConnectorException {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.warn("Failed to close file output stream on stop: " + e);
            }
        }
        try {
            for (Iterator<ObjectPool<FileSystemConnection>> it = pools.values().iterator(); it.hasNext();) {
                ObjectPool<FileSystemConnection> pool = it.next();
                pool.close();
            }
            pools.clear();
        } catch (Exception e) {
            throw new FileConnectorException(e);
        }
    }

    // ********************************************
    // connection pool management

    /**
     * Allocate a connection from the pool
     * 
     * @param uri
     *            The URI of the endpoint for which the connection is being created.
     * @param message
     *            ??
     * @return The allocated connection.
     */
    protected FileSystemConnection getConnection(FileSystemConnectionOptions fileSystemOptions) throws Exception {
        ObjectPool<FileSystemConnection> pool = getConnectionPool(fileSystemOptions);
        FileSystemConnection con = pool.borrowObject();
        if (!con.isConnected() || !con.isValid()) {
            destroyConnection(con, fileSystemOptions);
            con = pool.borrowObject();
        }
        synchronized (connections) {
            connections.add(con);
        }
        return con;
    }

    /**
     * Return a connection to the pool
     * 
     * @param uri
     *            The URI of the endpoint from which the connection is being released.
     * @param client
     *            The connection that is being released.
     * @param message
     *            ??
     * @throws Exception
     */
    protected void releaseConnection(FileSystemConnection connection, FileSystemConnectionOptions fileSystemOptions) throws Exception {
        synchronized (connections) {
            connections.remove(connection);
        }

        // MIRTH-4266: Return the connection to the pool even if it's not connected.
        if (connection != null) {
            ObjectPool<FileSystemConnection> pool = getConnectionPool(fileSystemOptions);
            pool.returnObject(connection);
        }
    }

    /**
     * Forcibly disconnect all current connections
     */
    protected void disconnect() {
        synchronized (connections) {
            for (FileSystemConnection connection : connections) {
                connection.disconnect();
            }
        }
    }

    /**
     * Permanently destroy a connection.
     * 
     * @param uri
     *            The URI of the endpoint from which the connection is being released.
     * @param connection
     *            The connection that is to be destroyed.
     * @param message
     *            ??
     * @throws Exception
     */
    protected void destroyConnection(FileSystemConnection connection, FileSystemConnectionOptions fileSystemOptions) throws Exception {
        if (connection != null) {
            ObjectPool<FileSystemConnection> pool = getConnectionPool(fileSystemOptions);
            pool.invalidateObject(connection);
        }
    }

    /**
     * Gets the pool of connections to the "server" for the specified endpoint, creating the pool if
     * necessary.
     * 
     * @param uri
     *            The URI of the endpoint the created pool should be associated with.
     * @param message
     *            ???
     * @return The pool of connections for this endpoint.
     */
    private synchronized ObjectPool<FileSystemConnection> getConnectionPool(FileSystemConnectionOptions fileSystemOptions) throws Exception {
        FileSystemConnectionFactory fileSystemConnectionFactory = getFileSystemConnectionFactory(fileSystemOptions);
        String key = fileSystemConnectionFactory.getPoolKey();
        ObjectPool<FileSystemConnection> pool = pools.get(key);

        if (pool == null) {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(maxTotalConnections);
            if (isValidateConnection()) {
                config.setTestOnBorrow(true);
                config.setTestOnReturn(true);
            }
            pool = new GenericObjectPool<FileSystemConnection>(fileSystemConnectionFactory, config);

            pools.put(key, pool);
        }
        return pool;
    }

    protected FileSystemConnectionFactory getFileSystemConnectionFactory(FileSystemConnectionOptions fileSystemOptions) throws Exception {
        return new FileSystemConnectionFactory(getScheme(), fileSystemOptions, fileSystemOptions.getUri().getHost(), fileSystemOptions.getUri().getPort(), isPassive(), isSecure(), NumberUtils.toInt(getTimeout()));
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

    public URI getEndpointURI(String host, FileScheme scheme, SchemeProperties schemeProperties, boolean isSecure) throws URISyntaxException {
        StringBuilder sspBuilder = new StringBuilder();

        sspBuilder.append("//");
        if (scheme == FileScheme.FILE && StringUtils.isNotBlank(host) && host.length() >= 3 && host.substring(1, 3).equals(":/")) {
            sspBuilder.append("/");
        }

        if (scheme == FileScheme.S3) {
            sspBuilder.append(((S3SchemeProperties) schemeProperties).getRegion()).append('/');
        }

        sspBuilder.append(host);

        String schemeName;
        if (scheme == FileScheme.WEBDAV) {
            if (isSecure) {
                schemeName = "https";
            } else {
                schemeName = "http";
            }
        } else if (scheme == FileScheme.S3) {
            schemeName = "s3";
        } else {
            schemeName = scheme.getDisplayName();
        }

        return new URI(schemeName, sspBuilder.toString(), null);
    }
}
