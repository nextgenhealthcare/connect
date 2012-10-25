/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.net.URI;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnectionFactory;
import com.mirth.connect.util.ConnectionTestResponse;

public class FileWriterService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testWrite")) {
            FileDispatcherProperties connectorProperties = (FileDispatcherProperties) object;
            String fileHost = null;
            FileScheme scheme = connectorProperties.getScheme();
            String host = null;
            int port = 0;
            String dir = null;

            String username = connectorProperties.getUsername();
            String password = connectorProperties.getPassword();
            boolean secure = false;
            boolean passive = false;
            int timeout = Integer.parseInt(connectorProperties.getTimeout());

            if (scheme.equals(FileScheme.FTP) || scheme.equals(FileScheme.SFTP)) {
                passive = connectorProperties.isPassive();
            }

            if (scheme.equals(FileScheme.FILE)) {
                fileHost = connectorProperties.getHost();
                dir = connectorProperties.getHost();
            } else {
                URI address;
                if (scheme.equals(FileScheme.WEBDAV)) {
                    if (connectorProperties.isSecure()) {
                        secure = true;
                        address = new URI("https://" + connectorProperties.getHost());
                    } else {
                        address = new URI("http://" + connectorProperties.getHost());
                    }
                } else {
                    address = new URI(scheme.getDisplayName(), "//" + connectorProperties.getHost(), null);
                }

                fileHost = address.toString();
                host = address.getHost();
                port = address.getPort();
                dir = address.getPath();
            }

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(scheme, username, password, host, port, passive, secure, timeout);

            try {
                FileSystemConnection connection = (FileSystemConnection) factory.makeObject();

                if (connection.canWrite(dir)) {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to: " + fileHost);
                } else {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + fileHost);
                }
            } catch (Exception e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + fileHost + ", Reason: " + e.getMessage());
            }
        }

        return null;
    }
}
