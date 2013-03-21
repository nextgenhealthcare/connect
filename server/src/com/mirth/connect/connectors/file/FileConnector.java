/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnectionFactory;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;

public class FileConnector {
    private Logger logger = Logger.getLogger(this.getClass());

    private Map<String, ObjectPool> pools = new HashMap<String, ObjectPool>();
    private FileOutputStream outputStream = null;

    private String channelId;
    private FileScheme scheme;
    private String username;
    private String password;
    private String timeout;
    private boolean passive;
    private boolean secure;
    private boolean validateConnection;

    public FileConnector(String channelId, ConnectorProperties connectorProperties) {
        this.channelId = channelId;

        if (connectorProperties instanceof FileReceiverProperties) {
            FileReceiverProperties fileReceiverProperties = (FileReceiverProperties) connectorProperties;
            this.scheme = fileReceiverProperties.getScheme();
            this.username = fileReceiverProperties.getUsername();
            this.password = fileReceiverProperties.getPassword();
            this.timeout = fileReceiverProperties.getTimeout();
            this.passive = fileReceiverProperties.isPassive();
            this.secure = fileReceiverProperties.isSecure();
            this.validateConnection = fileReceiverProperties.isValidateConnection();
        } else if (connectorProperties instanceof FileDispatcherProperties) {
            FileDispatcherProperties fileDispatcherProperties = (FileDispatcherProperties) connectorProperties;
            this.scheme = fileDispatcherProperties.getScheme();
            this.username = fileDispatcherProperties.getUsername();
            this.password = fileDispatcherProperties.getPassword();
            this.timeout = fileDispatcherProperties.getTimeout();
            this.passive = fileDispatcherProperties.isPassive();
            this.secure = fileDispatcherProperties.isSecure();
            this.validateConnection = fileDispatcherProperties.isValidateConnection();
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
     * Registers a listener for a particular directory The following properties
     * can be overriden in the endpoint declaration
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
            for (Iterator<ObjectPool> it = pools.values().iterator(); it.hasNext();) {
                ObjectPool pool = it.next();
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
     *            The URI of the endpoint for which the connection is being
     *            created.
     * @param message
     *            ??
     * @return The allocated connection.
     */
    protected FileSystemConnection getConnection(URI uri, ConnectorMessage message, ConnectorProperties connectorProperties) throws Exception {
        ObjectPool pool = getConnectionPool(uri, message, connectorProperties);
        FileSystemConnection con = (FileSystemConnection) pool.borrowObject();
        if (!con.isConnected() || !con.isValid()) {
            destroyConnection(uri, con, message, connectorProperties);
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
     * @param message
     *            ??
     * @throws Exception
     */
    protected void releaseConnection(URI uri, FileSystemConnection connection, ConnectorMessage message, ConnectorProperties connectorProperties) throws Exception {
//        if (isCreateDispatcherPerRequest()) {
//            destroyConnection(uri, connection, message);
//        } else {
        if (connection != null && connection.isConnected()) {
            ObjectPool pool = getConnectionPool(uri, message, connectorProperties);
            pool.returnObject(connection);
        }
//        }
    }

    /**
     * Permanently destroy a connection.
     * 
     * @param uri
     *            The URI of the endpoint from which the connection is being
     *            released.
     * @param connection
     *            The connection that is to be destroyed.
     * @param message
     *            ??
     * @throws Exception
     */
    protected void destroyConnection(URI uri, FileSystemConnection connection, ConnectorMessage message, ConnectorProperties connectorProperties) throws Exception {
        if (connection != null) {
            ObjectPool pool = getConnectionPool(uri, message, connectorProperties);
            pool.invalidateObject(connection);
        }
    }

    /**
     * Gets the pool of connections to the "server" for the specified endpoint,
     * creating the pool if necessary.
     * 
     * @param uri
     *            The URI of the endpoint the created pool should be associated
     *            with.
     * @param message
     *            ???
     * @return The pool of connections for this endpoint.
     */
    private synchronized ObjectPool getConnectionPool(URI uri, ConnectorMessage message, ConnectorProperties connectorProperties) throws URISyntaxException {
        String username;
        String password;

        if (connectorProperties instanceof FileReceiverProperties) {
            FileReceiverProperties fileReceiverProperties = (FileReceiverProperties) connectorProperties;
            username = fileReceiverProperties.getUsername();
            password = fileReceiverProperties.getPassword();
        } else {
            FileDispatcherProperties fileDispatcherProperties = (FileDispatcherProperties) connectorProperties;
            username = fileDispatcherProperties.getUsername();
            password = fileDispatcherProperties.getPassword();
        }

        String key = FileSystemConnectionFactory.getPoolKey(getScheme(), username, password, uri.getHost(), uri.getPort(), isSecure());
        ObjectPool pool = pools.get(key);
        if (pool == null) {
            GenericObjectPool.Config config = new GenericObjectPool.Config();
            if (isValidateConnection()) {
                config.testOnBorrow = true;
                config.testOnReturn = true;
            }
            pool = new GenericObjectPool(new FileSystemConnectionFactory(getScheme(), username, password, uri.getHost(), uri.getPort(), isPassive(), isSecure(), NumberUtils.toInt(getTimeout())), config);

            pools.put(key, pool);
        }
        return pool;
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

    URI getEndpointURI(String host) throws URISyntaxException {
        StringBuilder sspBuilder = new StringBuilder();

        sspBuilder.append("//");
        if (scheme == FileScheme.FILE && StringUtils.isNotBlank(host) && host.length() >= 3 && host.substring(1, 3).equals(":/")) {
            sspBuilder.append("/");
        }

        sspBuilder.append(host);

        String schemeName;
        if (scheme == FileScheme.WEBDAV) {
            if (secure) {
                schemeName = "https";
            } else {
                schemeName = "http";
            }
        } else {
            schemeName = scheme.getDisplayName();
        }

        return new URI(schemeName, sspBuilder.toString(), null);
    }
}
