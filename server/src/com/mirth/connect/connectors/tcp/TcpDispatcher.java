/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ConnectorCountEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.message.StreamHandler;
import com.mirth.connect.donkey.server.message.batch.BatchStreamReader;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.model.transmission.batch.DefaultBatchStreamReader;
import com.mirth.connect.plugins.BasicModeProvider;
import com.mirth.connect.plugins.TransmissionModeProvider;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
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
    public void onDeploy() throws ConnectorTaskException {
        connectorProperties = (TcpDispatcherProperties) getConnectorProperties();

        String pluginPointName = (String) connectorProperties.getTransmissionModeProperties().getPluginPointName();
        if (pluginPointName.equals("Basic")) {
            transmissionModeProvider = new BasicModeProvider();
        } else {
            transmissionModeProvider = (TransmissionModeProvider) ControllerFactory.getFactory().createExtensionController().getServicePlugins().get(pluginPointName);
        }

        if (transmissionModeProvider == null) {
            throw new ConnectorTaskException("Unable to find transmission mode plugin: " + pluginPointName);
        }

        connectedSockets = new ConcurrentHashMap<String, StateAwareSocket>();
        timeoutThreads = new ConcurrentHashMap<String, Thread>();
        sendTimeout = NumberUtils.toInt(connectorProperties.getSendTimeout());
        responseTimeout = NumberUtils.toInt(connectorProperties.getResponseTimeout());
        bufferSize = NumberUtils.toInt(connectorProperties.getBufferSize());

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {}

    @Override
    public void onStart() throws ConnectorTaskException {}

    @Override
    public void onStop() throws ConnectorTaskException {
        try {
            // Interrupt and join the connector timeout threads
            for (String socketKey : timeoutThreads.keySet().toArray(new String[timeoutThreads.size()])) {
                disposeThread(socketKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectorTaskException(e);
        }

        ConnectorTaskException firstCause = null;

        // Close the connector client sockets
        for (String socketKey : connectedSockets.keySet().toArray(new String[connectedSockets.size()])) {
            try {
                closeSocket(socketKey);
            } catch (IOException e) {
                if (firstCause == null) {
                    firstCause = new ConnectorTaskException("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                }
            }
        }

        if (firstCause != null) {
            throw firstCause;
        }
    }

    @Override
    public void onHalt() throws ConnectorTaskException {
        ConnectorTaskException firstCause = null;

        // Interrupt and join the connector timeout threads
        for (String socketKey : timeoutThreads.keySet().toArray(new String[timeoutThreads.size()])) {
            try {
                disposeThread(socketKey);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                firstCause = new ConnectorTaskException("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }
        }

        // Close the connector client sockets
        for (String socketKey : connectedSockets.keySet().toArray(new String[connectedSockets.size()])) {
            try {
                closeSocket(socketKey);
            } catch (IOException e) {
                if (firstCause == null) {
                    firstCause = new ConnectorTaskException("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
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
            if (!tcpDispatcherProperties.isKeepConnectionOpen() || socket == null || socket.isClosed() || (tcpDispatcherProperties.isCheckRemoteHost() && socket.remoteSideHasClosed())) {
                closeSocketQuietly(socketKey);

                logger.debug("Creating new socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                String info = "Trying to connect on " + tcpDispatcherProperties.getRemoteAddress() + ":" + tcpDispatcherProperties.getRemotePort() + "...";
                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.CONNECTING, info));

                if (tcpDispatcherProperties.isOverrideLocalBinding()) {
                    socket = SocketUtil.createSocket(tcpDispatcherProperties.getLocalAddress(), NumberUtils.toInt(tcpDispatcherProperties.getLocalPort()));
                } else {
                    socket = SocketUtil.createSocket();
                }

                ThreadUtils.checkInterruptedStatus();
                connectedSockets.put(socketKey, socket);

                SocketUtil.connectSocket(socket, tcpDispatcherProperties.getRemoteAddress(), NumberUtils.toInt(tcpDispatcherProperties.getRemotePort()), responseTimeout);

                socket.setReuseAddress(true);
                socket.setReceiveBufferSize(bufferSize);
                socket.setSendBufferSize(bufferSize);
                socket.setSoTimeout(responseTimeout);
                socket.setKeepAlive(tcpDispatcherProperties.isKeepConnectionOpen());

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
                        responseData = new String(responseBytes, CharsetUtils.getEncoding(tcpDispatcherProperties.getCharsetEncoding()));
                        responseStatusMessage = "Message successfully sent.";
                    } else {
                        responseStatusMessage = "Message successfully sent, but no response received.";
                    }

                    streamHandler.commit(true);
                    responseStatus = Status.SENT;

                    // We only want to validate the response if we were able to retrieve it successfully
                    validateResponse = tcpDispatcherProperties.getDestinationConnectorProperties().isValidateResponse();
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
                try {
                    // MIRTH-2980: Since we're ignoring responses, flush out the socket's input stream so it doesn't continually grow
                    socket.getInputStream().skip(socket.getInputStream().available());
                } catch (IOException e) {
                    logger.warn("Error flushing socket input stream.", e);
                }

                // We're ignoring the response, so always return a successful response
                responseStatus = Status.SENT;
                responseStatusMessage = "Message successfully sent.";
            }

            if (tcpDispatcherProperties.isKeepConnectionOpen() && (getCurrentState() == DeployedState.STARTED || getCurrentState() == DeployedState.STARTING)) {
                if (sendTimeout > 0) {
                    // Close the connection after the send timeout has been reached
                    startThread(socketKey);
                }
            } else {
                // If keep connection open is false, then close the socket right now
                closeSocketQuietly(socketKey);
            }
        } catch (Throwable t) {
            disposeThreadQuietly(socketKey);
            closeSocketQuietly(socketKey);

            String monitorMessage = "Error sending message (" + SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket) + "): " + t.getMessage();
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.FAILURE, monitorMessage));

            // If an exception occurred then close the socket, even if keep connection open is true
            responseStatusMessage = t.getClass().getSimpleName() + ": " + t.getMessage();
            responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), t.getMessage(), t);

            String logMessage = "Error sending message via TCP (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").";

            if (t instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            } else if (t instanceof ConnectException || t.getCause() != null && t.getCause() instanceof ConnectException) {
                if (isQueueEnabled()) {
                    logger.warn(logMessage, t);
                } else {
                    logger.error(logMessage, t);
                }
            } else {
                logger.debug(logMessage, t);
            }

            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error sending message via TCP.", t));
        } finally {
            eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), (Boolean) null));
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError, validateResponse);
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
                if (wasOpen) {
                    logger.trace("Closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                    SocketUtil.closeSocket(socket);
                }
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
                    if (getCurrentState() == DeployedState.STOPPING || getCurrentState() == DeployedState.STOPPED) {
                        closeSocketQuietly(socketKey);
                    }

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
            bytes = getAttachmentHandler().reAttachMessage(tcpSenderProperties.getTemplate(), connectorMessage, CharsetUtils.getEncoding(tcpSenderProperties.getCharsetEncoding()), tcpSenderProperties.isDataTypeBinary());
        }

        return bytes;
    }
}
