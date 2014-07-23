/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.ErrorMessageBuilder;

public class FileDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private FileDispatcherProperties connectorProperties;
    private FileConnector fileConnector;
    private String charsetEncoding;

    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private FileConfiguration configuration = null;

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (FileDispatcherProperties) getConnectorProperties();

        // Load the default configuration
        String configurationClass = configurationController.getProperty(connectorProperties.getProtocol(), "fileConfigurationClass");

        try {
            configuration = (FileConfiguration) Class.forName(configurationClass).newInstance();
        } catch (Exception e) {
            logger.trace("could not find custom configuration class, using default");
            configuration = new DefaultFileConfiguration();
        }

        try {
            configuration.configureConnectorDeploy(this, connectorProperties);
        } catch (Exception e) {
            throw new DeployException(e);
        }

        this.charsetEncoding = CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding(), System.getProperty("ca.uhn.hl7v2.llp.charset"));

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {
        try {
            fileConnector.doStop();
        } catch (FileConnectorException e) {
            throw new StopException("Failed to stop File Connector", e);
        }
    }

    @Override
    public void onHalt() throws HaltException {
        fileConnector.disconnect();
        try {
            onStop();
        } catch (StopException e) {
            throw new HaltException(e);
        }
    }

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        FileDispatcherProperties fileDispatcherProperties = (FileDispatcherProperties) connectorProperties;

        fileDispatcherProperties.setHost(replacer.replaceValues(fileDispatcherProperties.getHost(), connectorMessage));
        fileDispatcherProperties.setOutputPattern(replacer.replaceValues(fileDispatcherProperties.getOutputPattern(), connectorMessage));
        fileDispatcherProperties.setUsername(replacer.replaceValues(fileDispatcherProperties.getUsername(), connectorMessage));
        fileDispatcherProperties.setPassword(replacer.replaceValues(fileDispatcherProperties.getPassword(), connectorMessage));
        fileDispatcherProperties.setTemplate(replacer.replaceValues(fileDispatcherProperties.getTemplate(), connectorMessage));
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

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.WRITING, "Writing file to: " + info));

        String responseData = null;
        String responseError = null;
        String responseStatusMessage = null;
        Status responseStatus = Status.QUEUED;

        FileSystemConnection fileSystemConnection = null;
        URI uri = null;

        InputStream is = null;

        try {
            uri = fileConnector.getEndpointURI(fileDispatcherProperties.getHost());
            String filename = fileDispatcherProperties.getOutputPattern();

            if (filename == null) {
                throw new IOException("Filename is null");
            }

            String path = fileConnector.getPathPart(uri);
            String template = fileDispatcherProperties.getTemplate();

            byte[] bytes = getAttachmentHandler().reAttachMessage(template, connectorMessage, charsetEncoding, fileDispatcherProperties.isBinary());

            is = new ByteArrayInputStream(bytes);

            ThreadUtils.checkInterruptedStatus();
            fileSystemConnection = fileConnector.getConnection(uri, connectorMessage, fileDispatcherProperties);
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
            responseStatusMessage = "File successfully written: " + fileDispatcherProperties.toURIString();
            responseStatus = Status.SENT;
        } catch (Exception e) {
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error writing file", e));
            responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error writing file", e);
            responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error writing file", e);
            // TODO: handleException
//            fileConnector.handleException(e);
        } finally {
            IOUtils.closeQuietly(is);

            if (fileSystemConnection != null) {
                try {
                    fileConnector.releaseConnection(uri, fileSystemConnection, connectorMessage, fileDispatcherProperties);
                } catch (Exception e) {
                    // TODO: Ignore?
                }
            }

            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError);
    }

    public void setFileConnector(FileConnector fileConnector) {
        this.fileConnector = fileConnector;
    }
}
