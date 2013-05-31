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
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectorEventType;
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
import com.mirth.connect.donkey.server.event.ConnectorCountEvent;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.model.transmission.StreamHandler;
import com.mirth.connect.model.transmission.batch.BatchStreamReader;
import com.mirth.connect.model.transmission.batch.DefaultBatchStreamReader;
import com.mirth.connect.plugins.BasicModeProvider;
import com.mirth.connect.plugins.TransmissionModeProvider;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class TcpDispatcher extends DestinationConnector {

    private Logger logger = Logger.getLogger(this.getClass());
    protected TcpDispatcherProperties connectorProperties;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private StateAwareSocket socket;
    private Thread thread;
    private AtomicBoolean sending;

    TransmissionModeProvider transmissionModeProvider;

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        TcpDispatcherProperties tcpSenderProperties = (TcpDispatcherProperties) connectorProperties;

        tcpSenderProperties.setRemoteAddress(replacer.replaceValues(tcpSenderProperties.getRemoteAddress(), connectorMessage));
        tcpSenderProperties.setRemotePort(replacer.replaceValues(tcpSenderProperties.getRemotePort(), connectorMessage));
        tcpSenderProperties.setLocalAddress(replacer.replaceValues(tcpSenderProperties.getLocalAddress(), connectorMessage));
        tcpSenderProperties.setLocalPort(replacer.replaceValues(tcpSenderProperties.getLocalPort(), connectorMessage));
        tcpSenderProperties.setTemplate(replacer.replaceValues(tcpSenderProperties.getTemplate(), connectorMessage));
    }

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (TcpDispatcherProperties) getConnectorProperties();

        String pluginPointName = (String) connectorProperties.getTransmissionModeProperties().getPluginPointName();
        if (pluginPointName.equals("Basic")) {
            transmissionModeProvider = new BasicModeProvider();
        } else {
            transmissionModeProvider = (TransmissionModeProvider) ControllerFactory.getFactory().createExtensionController().getServicePlugins().get(pluginPointName);
        }

        if (transmissionModeProvider == null) {
            throw new DeployException("Unable to find transmission mode plugin: " + pluginPointName);
        }

        sending = new AtomicBoolean(false);

        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {
        sending.set(false);
    }

    @Override
    public void onStop() throws StopException {
        // Interrupt and join the connector thread
        try {
            disposeThread();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StopException("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }

        try {
            while (sending.get()) {
                Thread.sleep(100);
            }

            // Close the connector client socket
            try {
                closeSocket();
            } catch (IOException e) {
                throw new StopException("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StopException("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }
    }

    @Override
    public void onHalt() throws HaltException {
        HaltException firstCause = null;

        // Interrupt and join the connector thread
        try {
            disposeThread();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            firstCause = new HaltException("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }

        // Close the connector client socket
        try {
            closeSocket();
        } catch (IOException e) {
            if (firstCause == null) {
                firstCause = new HaltException("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }
        }

        if (firstCause != null) {
            throw firstCause;
        }
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) {
        TcpDispatcherProperties tcpDispatcherProperties = (TcpDispatcherProperties) connectorProperties;
        Status responseStatus = Status.QUEUED;
        String responseData = null;
        String responseStatusMessage = null;
        String responseError = null;
        boolean validateResponse = false;

        // If keep connection open is true, then interrupt the thread so it won't close the socket
        if (tcpDispatcherProperties.isKeepConnectionOpen() && thread != null) {
            thread.interrupt();
        }

        try {
            sending.set(true);

            // Initialize a new socket if our current one is invalid, the remote side has closed, or keep connection open is false
            if (socket == null || socket.isClosed() || socket.remoteSideHasClosed() || !tcpDispatcherProperties.isKeepConnectionOpen()) {
                closeSocketQuietly();

                logger.debug("Creating new socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                String info = "Trying to connect on " + tcpDispatcherProperties.getRemoteAddress() + ":" + tcpDispatcherProperties.getRemotePort() + "...";
                eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.CONNECTING, info));

                if (tcpDispatcherProperties.isOverrideLocalBinding()) {
                    socket = SocketUtil.createSocket(tcpDispatcherProperties.getRemoteAddress(), tcpDispatcherProperties.getRemotePort(), tcpDispatcherProperties.getLocalAddress(), tcpDispatcherProperties.getLocalPort());
                } else {
                    socket = SocketUtil.createSocket(tcpDispatcherProperties.getRemoteAddress(), tcpDispatcherProperties.getRemotePort());
                }

                socket.setReuseAddress(true);
                socket.setReceiveBufferSize(parseInt(tcpDispatcherProperties.getBufferSize()));
                socket.setSendBufferSize(parseInt(tcpDispatcherProperties.getBufferSize()));
                socket.setSoTimeout(parseInt(tcpDispatcherProperties.getResponseTimeout()));
                socket.setKeepAlive(tcpDispatcherProperties.isKeepConnectionOpen());
                eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.CONNECTED, null, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), true));
            }

            ThreadUtils.checkInterruptedStatus();

            // Send the message
            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.SENDING, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket)));
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream(), parseInt(tcpDispatcherProperties.getBufferSize()));
            BatchStreamReader batchStreamReader = new DefaultBatchStreamReader(socket.getInputStream());
            StreamHandler streamHandler = transmissionModeProvider.getStreamHandler(socket.getInputStream(), bos, batchStreamReader, tcpDispatcherProperties.getTransmissionModeProperties());
            streamHandler.write(getTemplateBytes(tcpDispatcherProperties, message));
            bos.flush();

            if (!tcpDispatcherProperties.isIgnoreResponse()) {
                ThreadUtils.checkInterruptedStatus();

                // Attempt to get the response from the remote endpoint
                try {
                    String info = "Waiting for response from " + SocketUtil.getInetAddress(socket) + " (Timeout: " + tcpDispatcherProperties.getResponseTimeout() + " ms)... ";
                    eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.WAITING_FOR_RESPONSE, info));
                    byte[] responseBytes = streamHandler.read();
                    if (responseBytes != null) {
                        streamHandler.commit(true);
                        responseData = new String(responseBytes, CharsetUtils.getEncoding(tcpDispatcherProperties.getCharsetEncoding()));
                        responseStatus = Status.SENT;
                        responseStatusMessage = "Message successfully sent.";
                    } else {
                        responseStatusMessage = "Response was not received.";
                        responseError = "Response was not received.";
                        logger.debug("Response was not received (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                    }

                    // We only want to validate the response if we were able to retrieve it successfully
                    validateResponse = tcpDispatcherProperties.isProcessHL7ACK();
                } catch (IOException e) {
                    // An exception occurred while retrieving the response
                    if (e instanceof SocketTimeoutException || e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
                        responseStatusMessage = "Timeout waiting for response";

                        if (!tcpDispatcherProperties.isQueueOnResponseTimeout()) {
                            responseStatus = Status.ERROR;
                        }
                    } else {
                        responseStatusMessage = "Error receiving response";
                    }

                    responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_411, responseStatusMessage + ": " + e.getMessage(), e);
                    logger.warn(responseStatusMessage + " (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), responseStatusMessage + ".", e));
                    eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.FAILURE, responseStatusMessage + " from " + SocketUtil.getInetAddress(socket)));

                    closeSocketQuietly();
                }
            } else {
                // We're ignoring the response, so always return a successful response
                responseStatus = Status.SENT;
            }

            if (tcpDispatcherProperties.isKeepConnectionOpen()) {
                // Close the connection after the send timeout has been reached
                startThread();
            } else {
                // If keep connection open is false, then close the socket right now
                closeSocketQuietly();
            }
        } catch (Exception e) {
            String monitorMessage = "Error sending message (" + SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket) + "): " + e.getMessage() + (e.getMessage().endsWith(".") ? "" : ". ");
            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.FAILURE, monitorMessage));

            // If an exception occurred then close the socket, even if keep connection open is true
            closeSocketQuietly();
            responseStatusMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_411, e.getMessage(), e);

            if (e instanceof ConnectException || e.getCause() != null && e.getCause() instanceof ConnectException) {
                logger.error("Error sending message via TCP (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            } else {
                logger.debug("Error sending message via TCP (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }

            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), "Error sending message via TCP.", e));
        } finally {
            sending.set(false);
            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.IDLE, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket)));
        }

        if (responseStatus == Status.SENT) {
            responseStatusMessage = "Message successfully sent.";
        }

        Response response = new Response(responseStatus, responseData, responseStatusMessage, responseError);
        if (validateResponse) {
            return getResponseTransformerExecutor().getInbound().getResponseValidator().validate(response, message);
        }
        return response;
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
                eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.DISCONNECTED, ConnectorEventType.CONNECTED, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), false));
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
            disposeThread();
        } catch (InterruptedException e) {
            logger.warn("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }
    }

    private void disposeThread() throws InterruptedException {
        if (thread != null && thread.isAlive()) {
            logger.trace("Interrupting thread (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
            thread.interrupt();

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
