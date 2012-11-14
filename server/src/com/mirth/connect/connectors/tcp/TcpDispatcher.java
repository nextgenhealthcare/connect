/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import static com.mirth.connect.util.TcpUtil.parseInt;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.tcp.stream.StreamHandler;
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
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.TcpUtil;

public class TcpDispatcher extends DestinationConnector {

    private Logger logger = Logger.getLogger(this.getClass());
    protected TcpDispatcherProperties connectorProperties;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.SENDER;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private StateAwareSocket socket;
    private Thread thread;

    private byte[] startOfMessageBytes;
    private byte[] endOfMessageBytes;
    private boolean returnDataOnException;

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage connectorMessage) {
        TcpDispatcherProperties tcpSenderProperties = (TcpDispatcherProperties) SerializationUtils.clone(connectorProperties);

        tcpSenderProperties.setHost(replacer.replaceValues(tcpSenderProperties.getHost(), connectorMessage));
        tcpSenderProperties.setPort(replacer.replaceValues(tcpSenderProperties.getPort(), connectorMessage));
        tcpSenderProperties.setTemplate(replacer.replaceValues(tcpSenderProperties.getTemplate(), connectorMessage));

        return tcpSenderProperties;
    }

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (TcpDispatcherProperties) getConnectorProperties();
        startOfMessageBytes = TcpUtil.stringToByteArray(connectorProperties.getStartOfMessageBytes());
        endOfMessageBytes = TcpUtil.stringToByteArray(connectorProperties.getEndOfMessageBytes());
        // Only return data on exceptions if there are no end bytes defined
        returnDataOnException = endOfMessageBytes.length == 0;
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {
        StopException firstCause = null;

        // Interrupt and join the connector thread
        try {
            disposeThread(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            firstCause = new StopException("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }

        // Close the connector client socket
        try {
            closeSocket();
        } catch (IOException e) {
            if (firstCause == null) {
                firstCause = new StopException("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }
        }

        if (firstCause != null) {
            throw firstCause;
        }
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) {
        TcpDispatcherProperties tcpSenderProperties = (TcpDispatcherProperties) connectorProperties;
        Status responseStatus = Status.QUEUED;
        String responseData = null;
        String responseError = null;

        // If keep connection open is true, then interrupt the thread so it won't close the socket
        if (tcpSenderProperties.isKeepConnectionOpen() && thread != null) {
            thread.interrupt();
        }

        try {
            // Initialize a new socket if our current one is invalid, the remote side has closed, or keep connection open is false
            if (socket == null || socket.isClosed() || socket.remoteSideHasClosed() || !tcpSenderProperties.isKeepConnectionOpen()) {
                closeSocketQuietly();
                logger.debug("Creating new socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                socket = SocketUtil.createSocket(tcpSenderProperties.getHost(), tcpSenderProperties.getPort());
                socket.setReuseAddress(true);
                socket.setReceiveBufferSize(parseInt(tcpSenderProperties.getBufferSize()));
                socket.setSendBufferSize(parseInt(tcpSenderProperties.getBufferSize()));
                socket.setSoTimeout(parseInt(tcpSenderProperties.getResponseTimeout()));
                socket.setKeepAlive(tcpSenderProperties.isKeepConnectionOpen());
                monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.CONNECTED, socket);
            }

            // Send the message
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY, socket);
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream(), parseInt(tcpSenderProperties.getBufferSize()));
            StreamHandler streamHandler = new StreamHandler(socket.getInputStream(), bos, startOfMessageBytes, endOfMessageBytes, returnDataOnException);
            streamHandler.writeFrame(getTemplateBytes(tcpSenderProperties));
            bos.flush();

            if (!tcpSenderProperties.isIgnoreResponse()) {
                // Attempt to get the response from the remote endpoint
                try {
                    byte[] responseBytes = streamHandler.getNextMessage();
                    if (responseBytes != null) {
                        responseData = new String(responseBytes, CharsetUtils.getEncoding(tcpSenderProperties.getCharsetEncoding()));

                        // TODO: Handle this differently; maybe add a default validator to the data type itself
                        if (tcpSenderProperties.isProcessHL7ACK()) {
                            if (responseData.matches("[\\s\\S]*MSA.[AC][RE][\\s\\S]*")) {
                                responseStatus = Status.ERROR;
                                responseError = "NACK sent from receiver.";
                            } else if (responseData.matches("[\\s\\S]*MSA.[AC]A[\\s\\S]*")) {
                                responseStatus = Status.SENT;
                            }
                        }
                    } else {
                        responseError = "Response was not received.";
                        logger.debug("Response was not received (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                    }
                } catch (IOException e) {
                    // An exception occurred while retrieving the response
                    responseData = ErrorMessageBuilder.buildErrorResponse(e.getMessage(), e);
                    responseError = ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_411, e.getMessage(), null);
                    closeSocketQuietly();
                }
            }

            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE, socket);

            if (tcpSenderProperties.isKeepConnectionOpen()) {
                // Close the connection after the send timeout has been reached
                startThread();
            } else {
                // If keep connection open is false, then close the socket right now
                closeSocketQuietly();
            }
        } catch (Exception e) {
            // If an exception occurred then close the socket, even if keep connection open is true
            closeSocketQuietly();
            responseData = ErrorMessageBuilder.buildErrorResponse(e.getMessage(), e);
            responseError = ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_411, e.getMessage(), null);
            logger.debug("Error sending message via TCP (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            alertController.sendAlerts(getChannelId(), Constants.ERROR_411, "Error sending message via TCP.", e);
        }

        return new Response(responseStatus, responseData, responseError);
    }

    private void closeSocketQuietly() {
        try {
            closeSocket();
        } catch (IOException e) {
            logger.debug("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }
    }

    private void closeSocket() throws IOException {
        boolean wasOpen = socket != null && !socket.isClosed();
        try {
            logger.trace("Closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
            SocketUtil.closeSocket(socket);
        } finally {
            if (wasOpen) {
                monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DISCONNECTED, socket);
            }
        }
    }

    /*
     * Starts up the connector thread which closes the connection after the send
     * timeout has been reached.
     */
    private void startThread() {
        disposeThreadQuietly();

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(parseInt(connectorProperties.getSendTimeout()));
                    closeSocketQuietly();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        thread.start();
    }

    private void disposeThreadQuietly() {
        try {
            disposeThread(true);
        } catch (InterruptedException e) {
            logger.warn("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }
    }

    private void disposeThread(boolean interrupt) throws InterruptedException {
        if (thread != null && thread.isAlive()) {
            if (interrupt) {
                logger.trace("Interrupting thread (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                thread.interrupt();
            }

            logger.trace("Joining thread (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }
    }

    /*
     * Returns the byte array representation of the connector properties
     * template, using the properties to determine whether or not to encode in
     * Base64, and what charset to use.
     */
    private byte[] getTemplateBytes(TcpDispatcherProperties tcpSenderProperties) throws UnsupportedEncodingException {
        byte[] bytes = new byte[0];

        if (tcpSenderProperties.getTemplate() != null) {
            if (tcpSenderProperties.isDataTypeBinary()) {
                bytes = Base64.decodeBase64(tcpSenderProperties.getTemplate());
            } else {
                bytes = tcpSenderProperties.getTemplate().getBytes(CharsetUtils.getEncoding(tcpSenderProperties.getCharsetEncoding()));
            }
        }

        return bytes;
    }
}
