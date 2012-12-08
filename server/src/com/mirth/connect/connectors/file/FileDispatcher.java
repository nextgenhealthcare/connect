/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.builders.ErrorMessageBuilder;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;

public class FileDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private FileDispatcherProperties connectorProperties;
    private FileConnector fileConnector;
    private String charsetEncoding;

    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.WRITER;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (FileDispatcherProperties) getConnectorProperties();
        this.fileConnector = new FileConnector(getChannelId(), connectorProperties);

        this.charsetEncoding = CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding(), System.getProperty("ca.uhn.hl7v2.llp.charset"));

        // TODO: Is this needed?
//        String writeToDirectoryName = connectorProperties.getDirectory();
//
//        if (writeToDirectoryName != null) {
//            // Mule's Utility.openDirectory:
//            File writeToDirectory = new File(writeToDirectoryName);
//            if (!writeToDirectory.exists()) {
//                writeToDirectory.mkdirs();
//            }
//            if (!writeToDirectory.isDirectory() || !writeToDirectory.canRead()) {
//                throw new DeployException("Directory: " + writeToDirectoryName + " exists but isn't a directory");
//            }
//            // End Utility.openDirectory
//
//            if (!writeToDirectory.canRead() || !writeToDirectory.canWrite()) {
//                throw new DeployException("Error on initialization, Write To directory does not exist or is not read/write");
//            }
//        }

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);
    }

    @Override
    public void onUndeploy() throws UndeployException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart() throws StartException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStop() throws StopException {
        // TODO Auto-generated method stub
    }

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage connectorMessage) {
        FileDispatcherProperties fileDispatcherProperties = (FileDispatcherProperties) SerializationUtils.clone(connectorProperties);

        fileDispatcherProperties.setHost(replacer.replaceValues(fileDispatcherProperties.getHost(), connectorMessage));
        fileDispatcherProperties.setOutputPattern(replacer.replaceValues(fileDispatcherProperties.getOutputPattern(), connectorMessage));
        fileDispatcherProperties.setUsername(replacer.replaceValues(fileDispatcherProperties.getUsername(), connectorMessage));
        fileDispatcherProperties.setPassword(replacer.replaceValues(fileDispatcherProperties.getPassword(), connectorMessage));
        fileDispatcherProperties.setTemplate(replacer.replaceValues(fileDispatcherProperties.getTemplate(), connectorMessage));

        return fileDispatcherProperties;
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        FileDispatcherProperties fileDispatcherProperties = (FileDispatcherProperties) connectorProperties;
        
        String info = fileDispatcherProperties.getHost() + "/" + fileDispatcherProperties.getOutputPattern();
        if (fileDispatcherProperties.getScheme().equals(FileScheme.FTP) || fileDispatcherProperties.getScheme().equals(FileScheme.SFTP)) {
            if (fileDispatcherProperties.isBinary()) {
                info += "   File Type: Binary";
            } else {
                info += "   File Type: ASCII";
            }
        }
        
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY, "Writing file to: " + info);

        String responseData = null;
        String responseError = null;
        Status responseStatus = Status.QUEUED;

        FileSystemConnection fileSystemConnection = null;
        URI uri = null;

        InputStream is = null;

        try {
            uri = getEndpointURI();
            String filename = fileDispatcherProperties.getOutputPattern();

            if (filename == null) {
                throw new IOException("Filename is null");
            }

            String path = replacer.replaceValues(fileConnector.getPathPart(uri), connectorMessage);
            String template = fileDispatcherProperties.getTemplate();

            byte[] bytes = AttachmentUtil.reAttachMessage(template, connectorMessage, charsetEncoding, fileDispatcherProperties.isBinary());

            is = new ByteArrayInputStream(bytes);

            fileSystemConnection = fileConnector.getConnection(uri, connectorMessage);
            if (fileDispatcherProperties.isErrorOnExists() && fileSystemConnection.exists(filename, path)) {
                throw new IOException("Destination file already exists, will not overwrite.");
            } else if (fileDispatcherProperties.isTemporary()) {
                String tempFilename = filename + ".tmp";
                logger.debug("writing temp file: " + tempFilename);
                fileSystemConnection.writeFile(tempFilename, path, false, is);
                logger.debug("renaming temp file: " + filename);
                fileSystemConnection.move(tempFilename, path, filename, path);
            } else {
                fileSystemConnection.writeFile(filename, path, fileDispatcherProperties.isOutputAppend(), is);
            }

            // update the message status to sent
            responseData = "File successfully written: " + filename;
            responseStatus = Status.SENT;
        } catch (Exception e) {
            alertController.sendAlerts(fileConnector.getChannelId(), Constants.ERROR_403, "Error writing file", e);
            responseData = ErrorMessageBuilder.buildErrorResponse("Error writing file", e);
            responseError = ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_403, "Error writing file", e);
            // TODO: handleException
//            fileConnector.handleException(e);
        } finally {
            IOUtils.closeQuietly(is);

            if (fileSystemConnection != null) {
                try {
                    fileConnector.releaseConnection(uri, fileSystemConnection, connectorMessage);
                } catch (Exception e) {
                    // TODO: Ignore?
                }
            }

            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
        }

        return new Response(responseStatus, responseData, responseError);
    }

    private URI getEndpointURI() throws URISyntaxException {
        return new URI(connectorProperties.getScheme().getDisplayName(), connectorProperties.getHost(), null);
    }
}
