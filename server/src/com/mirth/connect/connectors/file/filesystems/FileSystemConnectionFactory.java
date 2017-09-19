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
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.mirth.connect.connectors.file.FileScheme;
import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.S3SchemeProperties;
import com.mirth.connect.connectors.file.SftpSchemeProperties;

/**
 * A factory to create instances of FileSystemConnection based on the endpoint and connector
 * properties, and to adapt between them and the connection pool.
 */
public class FileSystemConnectionFactory implements PooledObjectFactory<FileSystemConnection> {
    private static transient Log logger = LogFactory.getLog(FileSystemConnectionFactory.class);
    protected FileScheme scheme;
    protected FileSystemConnectionOptions fileSystemOptions;
    protected String host;
    protected int port;
    protected boolean passive;
    protected boolean secure;
    protected int timeout;

    /**
     * Construct a FileSystemConnectionFactory from the endpoint URI and connector properties
     */
    public FileSystemConnectionFactory(FileScheme scheme, FileSystemConnectionOptions userCredentials, String host, int port, boolean passive, boolean secure, int timeout) {
        this.scheme = scheme;
        this.fileSystemOptions = userCredentials;
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
        String username = fileSystemOptions.getUsername();
        String password = fileSystemOptions.getPassword();

        if (scheme.equals(FileScheme.FILE)) {
            return "file://";
        } else if (scheme.equals(FileScheme.FTP)) {
            return "ftp://" + username + ":" + password + "@" + host + ":" + port;
        } else if (scheme.equals(FileScheme.SFTP)) {
            StringBuilder poolKey = new StringBuilder();
            SftpSchemeProperties sftpSchemeProperties = (SftpSchemeProperties) fileSystemOptions.getSchemeProperties();

            poolKey.append("sftp://");
            poolKey.append(username);

            if (sftpSchemeProperties.isPasswordAuth()) {
                poolKey.append(":");
                poolKey.append(password);
            }

            if (sftpSchemeProperties.isKeyAuth()) {
                poolKey.append(":");
                poolKey.append(sftpSchemeProperties.getKeyFile());
                poolKey.append(":");
                poolKey.append(sftpSchemeProperties.getPassPhrase());
            }

            String knownHostsFile = sftpSchemeProperties.getKnownHostsFile();
            if (StringUtils.isNotEmpty(knownHostsFile)) {
                poolKey.append(":");
                poolKey.append(knownHostsFile);
            }

            Map<String, String> configSettings = sftpSchemeProperties.getConfigurationSettings();
            if (MapUtils.isNotEmpty(configSettings)) {
                for (Map.Entry<String, String> setting : configSettings.entrySet()) {
                    poolKey.append(":" + setting.getValue());
                }
            }

            poolKey.append("@");
            poolKey.append(host);
            poolKey.append(":");
            poolKey.append(port);

            return poolKey.toString();
        } else if (scheme.equals(FileScheme.S3)) {
            StringBuilder poolKey = new StringBuilder();
            S3SchemeProperties s3SchemeProperties = (S3SchemeProperties) fileSystemOptions.getSchemeProperties();

            poolKey.append("s3://");

            if (!s3SchemeProperties.isUseDefaultCredentialProviderChain()) {
                poolKey.append(username);
                poolKey.append(':');
                poolKey.append(password);
            }

            if (s3SchemeProperties.isUseTemporaryCredentials()) {
                poolKey.append(":STS");
            }

            poolKey.append('@');
            poolKey.append(s3SchemeProperties.getRegion());

            return poolKey.toString();
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
            return new DefaultPooledObject<FileSystemConnection>(new FtpConnection(host, port, fileSystemOptions, passive, timeout));
        } else if (scheme.equals(FileScheme.SFTP)) {
            return new DefaultPooledObject<FileSystemConnection>(new SftpConnection(host, port, fileSystemOptions, timeout));
        } else if (scheme.equals(FileScheme.S3)) {
            return new DefaultPooledObject<FileSystemConnection>(new S3Connection(fileSystemOptions, timeout));
        } else if (scheme.equals(FileScheme.SMB)) {
            return new DefaultPooledObject<FileSystemConnection>(new SmbFileConnection(host, fileSystemOptions, timeout));
        } else if (scheme.equals(FileScheme.WEBDAV)) {
            return new DefaultPooledObject<FileSystemConnection>(new WebDavConnection(host, secure, fileSystemOptions));
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