/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import static com.mirth.connect.util.TcpUtil.parseInt;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

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
import com.mirth.connect.donkey.server.event.ConnectorCountEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.model.transmission.StreamHandler;
import com.mirth.connect.model.transmission.batch.BatchStreamReader;
import com.mirth.connect.model.transmission.batch.DefaultBatchStreamReader;
import com.mirth.connect.plugins.BasicModeProvider;
import com.mirth.connect.plugins.TransmissionModeProvider;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.MessageAttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.ErrorMessageBuilder;

public class TcpDispatcher extends DestinationConnector {

    private Logger logger = Logger.getLogger(this.getClass());
    protected TcpDispatcherProperties connectorProperties;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private Map<String, StateAwareSocket> connectedSockets;
    private Map<String, Thread> timeoutThreads;
    private int sendTimeout;
    private int responseTimeout;
    private int bufferSize;

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

        connectedSockets = new ConcurrentHashMap<String, StateAwareSocket>();
        timeoutThreads = new ConcurrentHashMap<String, Thread>();
        sendTimeout = parseInt(connectorProperties.getSendTimeout());
        responseTimeout = parseInt(connectorProperties.getResponseTimeout());
        bufferSize = parseInt(connectorProperties.getBufferSize());

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {
        StopException firstCause = null;

        try {
            // Interrupt and join the connector timeout threads
            for (String socketKey : timeoutThreads.keySet().toArray(new String[timeoutThreads.size()])) {
                disposeThread(socketKey);
            }

            // Close the connector client sockets
            for (String socketKey : connectedSockets.keySet().toArray(new String[connectedSockets.size()])) {
                try {
                    closeSocket(socketKey);
                } catch (IOException e) {
                    if (firstCause == null) {
                        firstCause = new StopException("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", firstCause);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StopException(e);
        }

        if (firstCause != null) {
            throw new StopException("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", firstCause);
        }
    }

    @Override
    public void onHalt() throws HaltException {
        HaltException firstCause = null;

        // Interrupt and join the connector timeout threads
        for (String socketKey : timeoutThreads.keySet().toArray(new String[timeoutThreads.size()])) {
            try {
                disposeThread(socketKey);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                firstCause = new HaltException("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }
        }

        // Close the connector client sockets
        for (String socketKey : connectedSockets.keySet().toArray(new String[connectedSockets.size()])) {
            try {
                closeSocket(socketKey);
            } catch (IOException e) {
                if (firstCause == null) {
                    firstCause = new HaltException("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                }
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

        long dispatcherId = getDispatcherId();

        String socketKey = dispatcherId + tcpDispatcherProperties.getRemoteAddress() + tcpDispatcherProperties.getRemotePort();
        if (tcpDispatcherProperties.isOverrideLocalBinding()) {
            socketKey += tcpDispatcherProperties.getLocalAddress() + tcpDispatcherProperties.getLocalPort();
        }

        StateAwareSocket socket = null;
        Thread timeoutThread = null;

        try {
            socket = connectedSockets.get(socketKey);
            timeoutThread = timeoutThreads.get(socketKey);

            // If keep connection open is true, then interrupt the thread so it won't close the socket
            if (tcpDispatcherProperties.isKeepConnectionOpen() && timeoutThread != null) {
                disposeThreadQuietly(socketKey);
            }

            // Initialize a new socket if our current one is invalid, the remote side has closed, or keep connection open is false
            if (socket == null || socket.isClosed() || socket.remoteSideHasClosed() || !tcpDispatcherProperties.isKeepConnectionOpen()) {
                closeSocketQuietly(socketKey);

                logger.debug("Creating new socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                String info = "Trying to connect on " + tcpDispatcherProperties.getRemoteAddress() + ":" + tcpDispatcherProperties.getRemotePort() + "...";
                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.CONNECTING, info));

                if (tcpDispatcherProperties.isOverrideLocalBinding()) {
                    socket = SocketUtil.createSocket(tcpDispatcherProperties.getRemoteAddress(), tcpDispatcherProperties.getRemotePort(), tcpDispatcherProperties.getLocalAddress(), tcpDispatcherProperties.getLocalPort(), responseTimeout);
                } else {
                    socket = SocketUtil.createSocket(tcpDispatcherProperties.getRemoteAddress(), tcpDispatcherProperties.getRemotePort(), responseTimeout);
                }

                socket.setReuseAddress(true);
                socket.setReceiveBufferSize(bufferSize);
                socket.setSendBufferSize(bufferSize);
                socket.setSoTimeout(responseTimeout);
                socket.setKeepAlive(tcpDispatcherProperties.isKeepConnectionOpen());

                connectedSockets.put(socketKey, socket);
                eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.CONNECTED, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), true));
            }

            ThreadUtils.checkInterruptedStatus();

            // Send the message
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.SENDING, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket)));
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream(), bufferSize);
            BatchStreamReader batchStreamReader = new DefaultBatchStreamReader(socket.getInputStream());
            StreamHandler streamHandler = transmissionModeProvider.getStreamHandler(socket.getInputStream(), bos, batchStreamReader, tcpDispatcherProperties.getTransmissionModeProperties());
            streamHandler.write(getTemplateBytes(tcpDispatcherProperties, message));
            bos.flush();

            if (!tcpDispatcherProperties.isIgnoreResponse()) {
                ThreadUtils.checkInterruptedStatus();

                // Attempt to get the response from the remote endpoint
                try {
                    String info = "Waiting for response from " + SocketUtil.getInetAddress(socket) + " (Timeout: " + tcpDispatcherProperties.getResponseTimeout() + " ms)... ";
                    eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.WAITING_FOR_RESPONSE, info));
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

                    responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), responseStatusMessage + ": " + e.getMessage(), e);
                    logger.warn(responseStatusMessage + " (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), responseStatusMessage + ".", e));
                    eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.FAILURE, responseStatusMessage + " from " + SocketUtil.getInetAddress(socket)));

                    closeSocketQuietly(socketKey);
                }
            } else {
                // We're ignoring the response, so always return a successful response
                responseStatus = Status.SENT;
            }

            if (tcpDispatcherProperties.isKeepConnectionOpen()) {
                if (sendTimeout > 0) {
                    // Close the connection after the send timeout has been reached
                    startThread(socketKey);
                }
            } else {
                // If keep connection open is false, then close the socket right now
                closeSocketQuietly(socketKey);
            }
        } catch (Exception e) {
            String monitorMessage = "Error sending message (" + SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket) + "): " + e.getMessage() + (e.getMessage().endsWith(".") ? "" : ". ");
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.FAILURE, monitorMessage));

            // If an exception occurred then close the socket, even if keep connection open is true
            disposeThreadQuietly(socketKey);
            closeSocketQuietly(socketKey);
            responseStatusMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), e.getMessage(), e);

            if (e instanceof ConnectException || e.getCause() != null && e.getCause() instanceof ConnectException) {
                logger.error("Error sending message via TCP (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            } else {
                logger.debug("Error sending message via TCP (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }

            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error sending message via TCP.", e));
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket)));
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

    private void closeSocketQuietly(String socketKey) {
        try {
            closeSocket(socketKey);
        } catch (IOException e) {
            logger.debug("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }
    }

    private void closeSocket(String socketKey) throws IOException {
        StateAwareSocket socket = connectedSockets.get(socketKey);
        if (socket != null) {
            boolean wasOpen = !socket.isClosed();
            try {
                logger.trace("Closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                SocketUtil.closeSocket(socket);
            } finally {
                connectedSockets.remove(socketKey);
                if (wasOpen) {
                    eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.DISCONNECTED, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), false));
                }
            }
        }
    }

    /*
     * Starts up the connector thread which closes the connection after the send timeout has been
     * reached.
     */
    private void startThread(final String socketKey) {
        disposeThreadQuietly(socketKey);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(sendTimeout);
                    closeSocketQuietly(socketKey);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    timeoutThreads.remove(socketKey);
                }
            }
        };

        timeoutThreads.put(socketKey, thread);
        thread.start();
    }

    private void disposeThreadQuietly(String socketKey) {
        try {
            disposeThread(socketKey);
        } catch (InterruptedException e) {
            logger.warn("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }
    }

    private void disposeThread(String socketKey) throws InterruptedException {
        Thread thread = timeoutThreads.get(socketKey);

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
     * Returns the byte array representation of the connector properties template, using the
     * properties to determine whether or not to encode in Base64, and what charset to use.
     */
    private byte[] getTemplateBytes(TcpDispatcherProperties tcpSenderProperties, ConnectorMessage connectorMessage) throws UnsupportedEncodingException {
        byte[] bytes = new byte[0];

        if (tcpSenderProperties.getTemplate() != null) {
            bytes = MessageAttachmentUtil.reAttachMessage(tcpSenderProperties.getTemplate(), connectorMessage, CharsetUtils.getEncoding(tcpSenderProperties.getCharsetEncoding()), tcpSenderProperties.isDataTypeBinary());
        }

        return bytes;
    }
}
