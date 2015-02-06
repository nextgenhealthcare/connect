/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.mirth.connect.connectors.file.FileScheme;

/**
 * A factory to create instances of FileSystemConnection based on the endpoint and connector
 * properties, and to adapt between them and the connection pool.
 */
public class FileSystemConnectionFactory implements PooledObjectFactory<FileSystemConnection> {
    private static transient Log logger = LogFactory.getLog(FileSystemConnectionFactory.class);
    protected FileScheme scheme;
    protected String username;
    protected String password;
    protected String host;
    protected int port;
    protected boolean passive;
    protected boolean secure;
    protected int timeout;

    /**
     * Construct a FileSystemConnectionFactory from the endpoint URI and connector properties
     */
    public FileSystemConnectionFactory(FileScheme scheme, String username, String password, String host, int port, boolean passive, boolean secure, int timeout) {
        this.scheme = scheme;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.passive = passive;
        this.secure = secure;
        this.timeout = timeout;
    }

    /**
     * Gets a pool key for connections on this endpoint
     */
    public String getPoolKey() {
        if (scheme.equals(FileScheme.FILE)) {
            return "file://";
        } else if (scheme.equals(FileScheme.FTP)) {
            return "ftp://" + username + ":" + password + "@" + host + ":" + port;
        } else if (scheme.equals(FileScheme.SFTP)) {
            return "sftp://" + username + ":" + password + "@" + host + ":" + port;
        } else if (scheme.equals(FileScheme.SMB)) {
            return "smb://" + username + ":" + password + "@" + host + ":" + port;
        } else if (scheme.equals(FileScheme.WEBDAV)) {
            String webdavScheme = "";

            if (secure) {
                webdavScheme = "https://";

                if (port < 0) {
                    port = 443;
                }
            } else {
                webdavScheme = "http://";

                if (port < 0) {
                    port = 80;
                }
            }

            if (username.equals("null")) {
                return webdavScheme + host + ":" + port;
            } else {
                return webdavScheme + username + ":" + password + "@" + host + ":" + port;
            }
        } else {
            logger.error("getPoolKey doesn't handle scheme " + scheme);
            return "default";
        }
    }

    @Override
    public PooledObject<FileSystemConnection> makeObject() throws Exception {
        if (scheme.equals(FileScheme.FILE)) {
            return new DefaultPooledObject<FileSystemConnection>(new FileConnection());
        } else if (scheme.equals(FileScheme.FTP)) {
            return new DefaultPooledObject<FileSystemConnection>(new FtpConnection(host, port, username, password, passive, timeout));
        } else if (scheme.equals(FileScheme.SFTP)) {
            return new DefaultPooledObject<FileSystemConnection>(new SftpConnection(host, port, username, password, timeout));
        } else if (scheme.equals(FileScheme.SMB)) {
            return new DefaultPooledObject<FileSystemConnection>(new SmbFileConnection(host, username, password, timeout));
        } else if (scheme.equals(FileScheme.WEBDAV)) {
            return new DefaultPooledObject<FileSystemConnection>(new WebDavConnection(host, secure, username, password));
        } else {
            logger.error("makeObject doesn't handle scheme " + scheme);
            throw new IOException("Unimplemented or unrecognized scheme");
        }
    }

    @Override
    public void destroyObject(PooledObject<FileSystemConnection> pooledConnection) throws Exception {
        pooledConnection.getObject().destroy();
    }

    @Override
    public void activateObject(PooledObject<FileSystemConnection> pooledConnection) throws Exception {
        pooledConnection.getObject().activate();
    }

    @Override
    public void passivateObject(PooledObject<FileSystemConnection> pooledConnection) throws Exception {
        pooledConnection.getObject().passivate();
    }

    @Override
    public boolean validateObject(PooledObject<FileSystemConnection> pooledConnection) {
        return pooledConnection.getObject().isValid();
    }
}