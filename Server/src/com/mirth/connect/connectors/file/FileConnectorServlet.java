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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.pool2.PooledObject;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnectionFactory;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ConnectionTestResponse;

public class FileConnectorServlet extends MirthServlet implements FileConnectorServletInterface {

    protected static final TemplateValueReplacer replacer = new TemplateValueReplacer();

    public FileConnectorServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public ConnectionTestResponse testRead(String channelId, String channelName, FileReceiverProperties properties) {
        return testReadOrWrite(channelId, channelName, properties, properties.getHost(), properties.isAnonymous(), properties.getUsername(), properties.getPassword(), properties.getSchemeProperties(), properties.getScheme(), properties.getTimeout(), properties.isSecure(), properties.isPassive(), true);
    }

    @Override
    public ConnectionTestResponse testWrite(String channelId, String channelName, FileDispatcherProperties properties) {
        return testReadOrWrite(channelId, channelName, properties, properties.getHost(), properties.isAnonymous(), properties.getUsername(), properties.getPassword(), properties.getSchemeProperties(), properties.getScheme(), properties.getTimeout(), properties.isSecure(), properties.isPassive(), false);
    }

    protected ConnectionTestResponse testReadOrWrite(String channelId, String channelName, ConnectorProperties connectorProperties, String host, boolean anonymous, String username, String password, SchemeProperties schemeProperties, FileScheme scheme, String timeoutString, boolean secure, boolean passive, boolean read) {
        try {
            host = replacer.replaceValues(host, channelId, channelName);
            username = replacer.replaceValues(username, channelId, channelName);
            password = replacer.replaceValues(password, channelId, channelName);

            if (schemeProperties instanceof SftpSchemeProperties) {
                SftpSchemeProperties sftpProperties = (SftpSchemeProperties) schemeProperties;

                sftpProperties.setKeyFile(replacer.replaceValues(sftpProperties.getKeyFile(), channelId, channelName));
                sftpProperties.setPassPhrase(replacer.replaceValues(sftpProperties.getPassPhrase(), channelId, channelName));
                sftpProperties.setKnownHostsFile(replacer.replaceValues(sftpProperties.getKnownHostsFile(), channelId, channelName));
                sftpProperties.setConfigurationSettings(replacer.replaceValues(sftpProperties.getConfigurationSettings(), channelId, channelName));
            } else if (schemeProperties instanceof S3SchemeProperties) {
                S3SchemeProperties s3Properties = (S3SchemeProperties) schemeProperties;

                s3Properties.setCustomHeaders(replacer.replaceKeysAndValuesInMap(s3Properties.getCustomHeaders(), channelId, channelName));
            }

            int timeout = Integer.parseInt(timeoutString);

            FileConnector fileConnector = new FileConnector(channelId, connectorProperties, null);
            URI address = fileConnector.getEndpointURI(host, scheme, schemeProperties, secure);
            String addressHost = address.getHost();
            int port = address.getPort();
            String dir = address.getPath();

            String hostDisplayName = "";
            if (!scheme.equals(FileScheme.FILE)) {
                hostDisplayName = scheme.getDisplayName() + "://" + address.getHost();
            }
            hostDisplayName += dir;

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(scheme, new FileSystemConnectionOptions(anonymous, username, password, schemeProperties), addressHost, port, passive, secure, timeout);

            FileSystemConnection connection = null;

            try {
                connection = ((PooledObject<FileSystemConnection>) factory.makeObject()).getObject();

                if (read && connection.canRead(dir) || !read && connection.canWrite(dir)) {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to: " + hostDisplayName);
                } else {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + hostDisplayName);
                }
            } catch (Exception e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + hostDisplayName + ", Reason: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.destroy();
                }
            }
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }
}
