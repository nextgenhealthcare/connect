/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.net.URI;

import org.apache.commons.pool2.PooledObject;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnectionFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ConnectionTestResponse;

public class FileReaderService implements ConnectorService {
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public Object invoke(String channelId, String channelName, String method, Object object, String sessionsId) throws Exception {
        if (method.equals(FileServiceMethods.METHOD_TEST_READ)) {
            FileReceiverProperties connectorProperties = (FileReceiverProperties) object;

            String host = replacer.replaceValues(connectorProperties.getHost(), channelId, channelName);
            String username = replacer.replaceValues(connectorProperties.getUsername(), channelId, channelName);
            String password = replacer.replaceValues(connectorProperties.getPassword(), channelId, channelName);

            SftpSchemeProperties sftpProperties = null;
            SchemeProperties schemeProperties = connectorProperties.getSchemeProperties();
            if (schemeProperties instanceof SftpSchemeProperties) {
                sftpProperties = (SftpSchemeProperties) schemeProperties;

                sftpProperties.setKeyFile(replacer.replaceValues(sftpProperties.getKeyFile(), channelId, channelName));
                sftpProperties.setPassPhrase(replacer.replaceValues(sftpProperties.getPassPhrase(), channelId, channelName));
                sftpProperties.setKnownHostsFile(replacer.replaceValues(sftpProperties.getKnownHostsFile(), channelId, channelName));
                sftpProperties.setConfigurationSettings(replacer.replaceValues(sftpProperties.getConfigurationSettings(), channelId, channelName));
            }

            FileScheme scheme = connectorProperties.getScheme();
            int timeout = Integer.parseInt(connectorProperties.getTimeout());

            URI address = FileConnector.getEndpointURI(host, scheme, connectorProperties.isSecure());
            String addressHost = address.getHost();
            int port = address.getPort();
            String dir = address.getPath();

            String hostDisplayName = "";
            if (!scheme.equals(FileScheme.FILE)) {
                hostDisplayName = scheme.getDisplayName() + "://" + address.getHost();
            }
            hostDisplayName += dir;

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(scheme, new FileSystemConnectionOptions(username, password, sftpProperties), addressHost, port, connectorProperties.isPassive(), connectorProperties.isSecure(), timeout);

            FileSystemConnection connection = null;

            try {
                connection = ((PooledObject<FileSystemConnection>) factory.makeObject()).getObject();

                if (connection.canRead(dir)) {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to: " + hostDisplayName);
                } else {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + hostDisplayName);
                }
            } catch (Exception e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + hostDisplayName);
            } finally {
                if (connection != null) {
                    connection.destroy();
                }
            }
        }

        return null;
    }
}
