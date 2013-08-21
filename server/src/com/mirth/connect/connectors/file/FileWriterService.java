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
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ConnectionTestResponse;

public class FileWriterService implements ConnectorService {
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public Object invoke(String channelId, String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testWrite")) {
            FileDispatcherProperties connectorProperties = (FileDispatcherProperties) object;

            String host = replacer.replaceValues(connectorProperties.getHost(), channelId);
            String username = replacer.replaceValues(connectorProperties.getUsername(), channelId);
            String password = replacer.replaceValues(connectorProperties.getPassword(), channelId);

            String fileHost = null;
            FileScheme scheme = connectorProperties.getScheme();
            String addressHost = null;
            int port = 0;
            String dir = null;

            boolean secure = false;
            boolean passive = false;
            int timeout = Integer.parseInt(connectorProperties.getTimeout());

            if (scheme.equals(FileScheme.FTP) || scheme.equals(FileScheme.SFTP)) {
                passive = connectorProperties.isPassive();
            }

            if (scheme.equals(FileScheme.FILE)) {
                fileHost = host;
                dir = host;
            } else {
                URI address;
                if (scheme.equals(FileScheme.WEBDAV)) {
                    if (connectorProperties.isSecure()) {
                        secure = true;
                        address = new URI("https://" + host);
                    } else {
                        address = new URI("http://" + host);
                    }
                } else {
                    address = new URI(scheme.getDisplayName(), "//" + host, null);
                }

                fileHost = address.toString();
                addressHost = address.getHost();
                port = address.getPort();
                dir = address.getPath();
            }

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(scheme, username, password, addressHost, port, passive, secure, timeout);

            FileSystemConnection connection = null;

            try {
                connection = (FileSystemConnection) factory.makeObject();

                if (connection.canWrite(dir)) {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to: " + fileHost);
                } else {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + fileHost);
                }
            } catch (Exception e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + fileHost + ", Reason: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.destroy();
                }
            }
        }

        return null;
    }
}
