/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;

public class FileMessageDispatcher extends AbstractMessageDispatcher {
    private FileConnector connector;

    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.WRITER;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public FileMessageDispatcher(FileConnector connector) {
        super(connector);
        this.connector = connector;
        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    public void doDispatch(UMOEvent event) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);

        if (messageObject == null) {
            return;
        }

        FileSystemConnection fileSystemConnection = null;
        UMOEndpointURI uri = event.getEndpoint().getEndpointURI();

        try {
            String filename = (String) event.getProperty(FileConnector.PROPERTY_FILENAME);

            if (filename == null) {
                String pattern = (String) event.getProperty(FileConnector.PROPERTY_OUTPUT_PATTERN);

                if (pattern == null) {
                    pattern = connector.getOutputPattern();
                }

                filename = generateFilename(event, pattern, messageObject);
            }

            if (filename == null) {
                messageObjectController.setError(messageObject, Constants.ERROR_403, "Filename is null", null, null);
                throw new IOException("Filename is null");
            }

            String path = generateFilename(event, connector.getPathPart(uri), messageObject);
            String template = replacer.replaceValues(connector.getTemplate(), messageObject);

            byte[] bytes = null;

            if (connector.isBinary()) {
                bytes = Base64.decodeBase64(template);
            } else {
                bytes = template.getBytes(connector.getCharsetEncoding());
            }

            fileSystemConnection = connector.getConnection(uri, messageObject);

            if (connector.isErrorOnExists() && fileSystemConnection.exists(filename, path)) {
                throw new IOException("Destination file already exists, will not overwrite.");
            } else if (connector.isTemporary()) {
                String tempFilename = filename + ".tmp";
                logger.debug("writing temp file: " + tempFilename);
                fileSystemConnection.writeFile(tempFilename, path, false, bytes);
                logger.debug("renaming temp file: " + filename);
                fileSystemConnection.move(tempFilename, path, filename, path);
            } else {
                fileSystemConnection.writeFile(filename, path, connector.isOutputAppend(), bytes);
            }

            // update the message status to sent
            messageObjectController.setSuccess(messageObject, "File successfully written: " + filename, null);
        } catch (Exception e) {
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_403, "Error writing file", e);
            messageObjectController.setError(messageObject, Constants.ERROR_403, "Error writing file", e, null);
            connector.handleException(e);
        } finally {
            if (fileSystemConnection != null) {
                connector.releaseConnection(uri, fileSystemConnection, messageObject);
            }

            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    /**
     * Will attempt to do a receive from a directory, if the endpointUri
     * resolves to a file name the file will be returned, otherwise the first
     * file in the directory according to the filename filter configured on the
     * connector.
     * 
     * TODO: This method is not implemented by the FTP or SFTP message
     * dispatchers. Is it actually used?
     * 
     * @param endpointUri
     *            a path to a file or directory
     * @param timeout
     *            this is ignored when doing a receive on this dispatcher
     * @return a message containing file contents or null if there was nothing
     *         to receive
     * @throws Exception
     */
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        return null;
    }

    public UMOMessage doSend(UMOEvent event) throws Exception {
        doDispatch(event);
        return event.getMessage();
    }

    public Object getDelegateSession() throws UMOException {
        return null;
    }

    public void doDispose() {

    }

    private String generateFilename(UMOEvent event, String pattern, MessageObject messageObject) {
        if (connector.getFilenameParser() instanceof VariableFilenameParser) {
            VariableFilenameParser filenameParser = (VariableFilenameParser) connector.getFilenameParser();
            filenameParser.setMessageObject(messageObject);
            return filenameParser.getFilename(event.getMessage(), pattern);
        } else {
            return connector.getFilenameParser().getFilename(event.getMessage(), pattern);
        }
    }
}
