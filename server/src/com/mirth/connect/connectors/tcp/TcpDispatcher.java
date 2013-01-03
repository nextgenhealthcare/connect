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
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.model.transmission.StreamHandler;
import com.mirth.connect.model.transmission.batch.BatchStreamReader;
import com.mirth.connect.model.transmission.batch.DefaultBatchStreamReader;
import com.mirth.connect.plugins.BasicModeProvider;
import com.mirth.connect.plugins.TransmissionModeProvider;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class TcpDispatcher extends DestinationConnector {

    private Logger logger = Logger.getLogger(this.getClass());
    protected TcpDispatcherProperties connectorProperties;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.SENDER;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private StateAwareSocket socket;
    private Thread thread;

    TransmissionModeProvider transmissionModeProvider;

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage connectorMessage) {
        TcpDispatcherProperties tcpSenderProperties = (TcpDispatcherProperties) SerializationUtils.clone(connectorProperties);

        tcpSenderProperties.setRemoteAddress(replacer.replaceValues(tcpSenderProperties.getRemoteAddress(), connectorMessage));
        tcpSenderProperties.setRemotePort(replacer.replaceValues(tcpSenderProperties.getRemotePort(), connectorMessage));
        tcpSenderProperties.setLocalAddress(replacer.replaceValues(tcpSenderProperties.getLocalAddress(), connectorMessage));
        tcpSenderProperties.setLocalPort(replacer.replaceValues(tcpSenderProperties.getLocalPort(), connectorMessage));
        tcpSenderProperties.setTemplate(replacer.replaceValues(tcpSenderProperties.getTemplate(), connectorMessage));

        return tcpSenderProperties;
    }

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (TcpDispatcherProperties) getConnectorProperties();

        String pluginPointName = (String) connectorProperties.getTransmissionModeProperties().getPluginPointName();
        transmissionModeProvider = (TransmissionModeProvider) ControllerFactory.getFactory().createExtensionController().getServicePlugins().get(pluginPointName);
        if (transmissionModeProvider == null) {
            transmissionModeProvider = new BasicModeProvider();
        }

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
                monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.ATTEMPTING, "Trying to connect on " + tcpSenderProperties.getRemoteAddress() + ":" + tcpSenderProperties.getRemotePort() + "...");

                if (tcpSenderProperties.isOverrideLocalBinding()) {
                    socket = SocketUtil.createSocket(tcpSenderProperties.getRemoteAddress(), tcpSenderProperties.getRemotePort(), tcpSenderProperties.getLocalAddress(), tcpSenderProperties.getLocalPort());
                } else {
                    socket = SocketUtil.createSocket(tcpSenderProperties.getRemoteAddress(), tcpSenderProperties.getRemotePort());
                }

                socket.setReuseAddress(true);
                socket.setReceiveBufferSize(parseInt(tcpSenderProperties.getBufferSize()));
                socket.setSendBufferSize(parseInt(tcpSenderProperties.getBufferSize()));
                socket.setSoTimeout(parseInt(tcpSenderProperties.getResponseTimeout()));
                socket.setKeepAlive(tcpSenderProperties.isKeepConnectionOpen());
                monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.CONNECTED, socket);
            }

            // Send the message
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY, socket, "Sending data... ");
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream(), parseInt(tcpSenderProperties.getBufferSize()));
            BatchStreamReader batchStreamReader = new DefaultBatchStreamReader(socket.getInputStream());
            StreamHandler streamHandler = transmissionModeProvider.getStreamHandler(socket.getInputStream(), bos, batchStreamReader, tcpSenderProperties.getTransmissionModeProperties());
            streamHandler.write(getTemplateBytes(tcpSenderProperties, message));
            bos.flush();

            if (!tcpSenderProperties.isIgnoreResponse()) {
                // Attempt to get the response from the remote endpoint
                try {
                    monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY, socket, "Waiting for response (Timeout: " + tcpSenderProperties.getResponseTimeout() + " ms)... ");
                    byte[] responseBytes = streamHandler.read();
                    if (responseBytes != null) {
                        streamHandler.commit(true);
                        responseData = new String(responseBytes, CharsetUtils.getEncoding(tcpSenderProperties.getCharsetEncoding()));

                        // TODO: Handle this differently; maybe add a default validator to the data type itself
                        if (tcpSenderProperties.isProcessHL7ACK()) {
                            if (responseData.trim().startsWith("<")) {
                                // XML response received
                                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new CharArrayReader(responseData.toCharArray())));
                                String ackCode = XPathFactory.newInstance().newXPath().compile("//MSA.1/text()").evaluate(doc).trim();
                                if (ackCode.matches("[AC][RE]")) {
                                    responseStatus = Status.ERROR;
                                } else if (ackCode.matches("[AC]A")) {
                                    responseStatus = Status.SENT;
                                }
                            } else {
                                // ER7 response received
                                if (responseData.matches("[\\s\\S]*MSA.[AC][RE][\\s\\S]*")) {
                                    responseStatus = Status.ERROR;
                                } else if (responseData.matches("[\\s\\S]*MSA.[AC]A[\\s\\S]*")) {
                                    responseStatus = Status.SENT;
                                }
                            }

                            if (responseStatus == Status.ERROR) {
                                responseError = "NACK sent from receiver: " + responseData;
                            }
                        }
                    } else {
                        responseData = "Response was not received.";
                        responseError = "Response was not received.";
                        logger.debug("Response was not received (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                    }
                } catch (IOException e) {
                    // An exception occurred while retrieving the response
                    responseData = e.getClass().getSimpleName() + ": " + e.getMessage();
                    String errorMessage = (e instanceof SocketTimeoutException || e.getCause() != null && e.getCause() instanceof SocketTimeoutException) ? "Timeout waiting for response" : "Error receiving response";

                    responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_411, errorMessage + ": " + e.getMessage(), e);
                    logger.warn(errorMessage + " (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                    alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_411, errorMessage + ".", e);
                    monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.FAILURE, socket, errorMessage + ". ");

                    closeSocketQuietly();
                }
            } else {
                // We're ignoring the response, so always return a successful response
                responseStatus = Status.SENT;
            }

            if (tcpSenderProperties.isKeepConnectionOpen()) {
                // Close the connection after the send timeout has been reached
                startThread();
            } else {
                // If keep connection open is false, then close the socket right now
                closeSocketQuietly();
            }
        } catch (Exception e) {
            String monitorMessage = "Error sending message: " + e.getMessage() + (e.getMessage().endsWith(".") ? "" : ". ");
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.FAILURE, socket, monitorMessage);

            // If an exception occurred then close the socket, even if keep connection open is true
            closeSocketQuietly();
            responseData = e.getClass().getSimpleName() + ": " + e.getMessage();
            responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_411, e.getMessage(), e);

            if (e instanceof ConnectException || e.getCause() != null && e.getCause() instanceof ConnectException) {
                logger.error("Error sending message via TCP (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            } else {
                logger.debug("Error sending message via TCP (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }

            alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_411, "Error sending message via TCP.", e);
        }

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE, socket);

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
    private byte[] getTemplateBytes(TcpDispatcherProperties tcpSenderProperties, ConnectorMessage connectorMessage) throws UnsupportedEncodingException {
        byte[] bytes = new byte[0];

        if (tcpSenderProperties.getTemplate() != null) {
            bytes = AttachmentUtil.reAttachMessage(tcpSenderProperties.getTemplate(), connectorMessage, CharsetUtils.getEncoding(tcpSenderProperties.getCharsetEncoding()), tcpSenderProperties.isDataTypeBinary());
        }

        return bytes;
    }
}
