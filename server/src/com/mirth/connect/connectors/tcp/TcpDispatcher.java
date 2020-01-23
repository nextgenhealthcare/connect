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
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
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
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.ErrorMessageBuilder;
import com.mirth.connect.util.TcpUtil;

public class TcpDispatcher extends DestinationConnector {
    // This determines how many client requests can queue up while waiting for the server socket to accept
    private static final int DEFAULT_BACKLOG = 256;

    private Logger logger = Logger.getLogger(this.getClass());
    protected TcpDispatcherProperties connectorProperties;
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private TcpConfiguration configuration = null;
    private Map<String, Socket> connectedSockets;
    private Map<String, Thread> timeoutThreads;

    private int sendTimeout;
    private int responseTimeout;
    private int bufferSize;

    TransmissionModeProvider transmissionModeProvider;

    // Server mode variables
    private ServerSocket serverSocket;
    private Set<Socket> serverModeSockets = new HashSet<>();
    private Thread thread;
    private int maxConnections;

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
            transmissionModeProvider = (TransmissionModeProvider) ControllerFactory.getFactory().createExtensionController().getTransmissionModeProviders().get(pluginPointName);
        }

        if (transmissionModeProvider == null) {
            throw new ConnectorTaskException("Unable to find transmission mode plugin: " + pluginPointName);
        }

        // load the default configuration
        String configurationClass = getConfigurationClass();

        try {
            configuration = (TcpConfiguration) Class.forName(configurationClass).newInstance();
        } catch (Throwable t) {
            logger.trace("could not find custom configuration class, using default");
            configuration = new DefaultTcpConfiguration();
        }

        try {
            configuration.configureConnectorDeploy(this);
        } catch (Exception e) {
            throw new ConnectorTaskException(e);
        }

        connectedSockets = new ConcurrentHashMap<String, Socket>();
        timeoutThreads = new ConcurrentHashMap<String, Thread>();
        sendTimeout = NumberUtils.toInt(connectorProperties.getSendTimeout());
        responseTimeout = NumberUtils.toInt(connectorProperties.getResponseTimeout());
        bufferSize = NumberUtils.toInt(connectorProperties.getBufferSize());
        maxConnections = NumberUtils.toInt(connectorProperties.getMaxConnections());

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {}

    @Override
    public void onStart() throws ConnectorTaskException {
        if (connectorProperties.isServerMode()) {
            try {
                createServerSocket();
            } catch (IOException e) {
                throw new ConnectorTaskException("Failed to create server socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }

            // Create the acceptor thread
            thread = new Thread("TCP Sender Server Acceptor Thread on " + getChannel().getName() + " (" + getChannelId() + ")") {
                @Override
                public void run() {
                    do {
                        // Server mode; wait to accept a client socket on the ServerSocket
                        try {
                            logger.debug("Waiting for new client socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                            Socket serverModeSocket = serverSocket.accept();
                            initSocket(serverModeSocket);
                            logger.trace("Accepted new socket: " + serverModeSocket.getRemoteSocketAddress().toString() + " -> " + serverModeSocket.getLocalSocketAddress());

                            if (serverModeSocket instanceof StateAwareSocketInterface && ((StateAwareSocketInterface) serverModeSocket).remoteSideHasClosed()) {
                                logger.debug("Remote side closed connection (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                                serverModeSocket.close();
                            } else {
                                synchronized (serverModeSockets) {
                                    if (serverModeSockets.size() < maxConnections) {
                                        serverModeSockets.add(serverModeSocket);
                                    } else {
                                        serverModeSocket.close();
                                        logger.debug("Reached maximum connnections (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                                    }
                                }
                            }
                        } catch (java.io.InterruptedIOException e) {
                            logger.debug("Interruption during server socket accept operation (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                        } catch (Exception e) {
                            logger.debug("Error accepting new socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                        }

                        try {
                            ThreadUtils.checkInterruptedStatus();
                        } catch (InterruptedException e) {
                            return;
                        }
                    } while (getCurrentState() == DeployedState.STARTED);
                }
            };

            thread.start();
        }
    }

    @Override
    public void onStop() throws ConnectorTaskException {
        ConnectorTaskException firstCause = null;

        if (connectorProperties.isServerMode()) {
            if (serverSocket != null) {
                // Close the server socket
                try {
                    logger.debug("Closing server socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                    serverSocket.close();
                } catch (IOException e) {
                    firstCause = new ConnectorTaskException("Error closing server socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                }
            }

            try {
                disposeThread(thread, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ConnectorTaskException("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }

            synchronized (serverModeSockets) {
                for (Socket socket : serverModeSockets) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        if (firstCause == null) {
                            firstCause = new ConnectorTaskException("Error closing client socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                        }
                    }
                }
                serverModeSockets.clear();
            }
        } else {
            try {
                // Interrupt and join the connector timeout threads
                for (String socketKey : timeoutThreads.keySet().toArray(new String[timeoutThreads.size()])) {
                    disposeThread(socketKey);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ConnectorTaskException(e);
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
        }

        if (firstCause != null) {
            throw firstCause;
        }
    }

    @Override
    public void onHalt() throws ConnectorTaskException {
        ConnectorTaskException firstCause = null;

        if (connectorProperties.isServerMode()) {
            if (serverSocket != null) {
                // Close the server socket
                try {
                    logger.debug("Closing server socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                    serverSocket.close();
                } catch (IOException e) {
                    firstCause = new ConnectorTaskException("Error closing server socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                }
            }

            try {
                disposeThread(thread, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                firstCause = new ConnectorTaskException("Thread join operation interrupted (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            }

            synchronized (serverModeSockets) {
                for (Socket socket : serverModeSockets) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        if (firstCause == null) {
                            firstCause = new ConnectorTaskException("Error closing client socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
                        }
                    }
                }
                serverModeSockets.clear();
            }
        } else {
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

        long dispatcherId = message.getDispatcherId();

        String socketKey = dispatcherId + tcpDispatcherProperties.getRemoteAddress() + tcpDispatcherProperties.getRemotePort();
        if (tcpDispatcherProperties.isOverrideLocalBinding()) {
            socketKey += tcpDispatcherProperties.getLocalAddress() + tcpDispatcherProperties.getLocalPort();
        }

        Socket socket = null;
        Thread timeoutThread = null;
        Response response = null;

        try {
            if (!tcpDispatcherProperties.isServerMode()) {
                // Do some validation first to avoid unnecessarily creating sockets
                if (StringUtils.isBlank(tcpDispatcherProperties.getRemoteAddress())) {
                    throw new Exception("Remote address is blank.");
                } else if (NumberUtils.toInt(tcpDispatcherProperties.getRemotePort()) <= 0) {
                    throw new Exception("Remote port is invalid.");
                }

                socket = connectedSockets.get(socketKey);
                timeoutThread = timeoutThreads.get(socketKey);

                // If keep connection open is true, then interrupt the thread so it won't close the socket
                if (tcpDispatcherProperties.isKeepConnectionOpen() && timeoutThread != null) {
                    disposeThreadQuietly(socketKey);
                }

                // Initialize a new socket if our current one is invalid, the remote side has closed, or keep connection open is false
                if (!tcpDispatcherProperties.isKeepConnectionOpen() || socket == null || socket.isClosed() || (tcpDispatcherProperties.isCheckRemoteHost() && socket instanceof StateAwareSocketInterface && ((StateAwareSocketInterface) socket).remoteSideHasClosed())) {
                    closeSocketQuietly(socketKey);

                    logger.debug("Creating new socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                    String info = "Trying to connect on " + tcpDispatcherProperties.getRemoteAddress() + ":" + tcpDispatcherProperties.getRemotePort() + "...";
                    eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.CONNECTING, info));

                    if (tcpDispatcherProperties.isOverrideLocalBinding()) {
                        socket = SocketUtil.createSocket(configuration, tcpDispatcherProperties.getLocalAddress(), NumberUtils.toInt(tcpDispatcherProperties.getLocalPort()));
                    } else {
                        socket = SocketUtil.createSocket(configuration);
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
            }

            ThreadUtils.checkInterruptedStatus();

            if (tcpDispatcherProperties.isServerMode()) {
                synchronized (serverModeSockets) {
                    List<Response> responseList = new ArrayList<>();
                    int successes = 0;

                    for (Socket serverModeSocket : serverModeSockets) {
                        try {
                            // Initialize a new socket if our current one is invalid, the remote side has closed
                            if (serverModeSocket == null || serverModeSocket.isClosed() || (serverModeSocket instanceof StateAwareSocketInterface && ((StateAwareSocketInterface) serverModeSocket).remoteSideHasClosed())) {
                                closeServerModeSocketQuietly(serverModeSocket);
                            }
                        } catch (IOException e) {
                            closeServerModeSocketQuietly(serverModeSocket);
                        }

                        if (!serverModeSocket.isClosed()) {
                            Response currentResponse = send(tcpDispatcherProperties, message, serverModeSocket, socketKey);

                            responseList.add(currentResponse);

                            if (response == null) {
                                response = currentResponse;
                                if (response.getStatus() == Status.SENT) {
                                    successes++;
                                }
                            } else {
                                switch (currentResponse.getStatus()) {
                                    case SENT:
                                        if (response.getStatus() != Status.SENT) {
                                            response = currentResponse;
                                        }
                                        successes++;
                                        break;
                                    case QUEUED:
                                        if (response.getStatus() != Status.SENT) {
                                            response = currentResponse;
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }

                    // set response status message
                    if (response != null) {
                        response = new Response(response);
                    } else {
                        response = new Response(responseStatus, null, responseStatusMessage, responseError, validateResponse);
                    }
                    response.setStatusMessage(getStatusMessage(responseList, response, successes));

                    // compile all responses and add to connector map
                    Map<String, Object> connectorMap = message.getConnectorMap();
                    connectorMap.put("allResponses", responseList);

                    connectorMap.put("localAddress", getLocalAddress());
                    connectorMap.put("localPort", getLocalPort());
                    connectorMap.put("numberOfClients", responseList.size());
                    connectorMap.put("successfulSends", successes);

                    for (Iterator<Socket> iter = serverModeSockets.iterator(); iter.hasNext();) {
                        if (iter.next().isClosed()) {
                            iter.remove();
                        }
                    }
                }
            } else {
                response = send(tcpDispatcherProperties, message, socket, socketKey);
            }

            return response;
        } catch (Throwable t) {
            String monitorMessage = "Error sending message: " + t.getMessage();

            if (!tcpDispatcherProperties.isServerMode()) {
                disposeThreadQuietly(socketKey);
                closeSocketQuietly(socketKey);
                monitorMessage = "Error sending message (" + SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket) + "): " + t.getMessage();
            }

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

            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), message.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error sending message via TCP.", t));

            return new Response(responseStatus, responseData, responseStatusMessage, responseError, validateResponse);
        } finally {
            eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), (Boolean) null));
        }
    }

    private String getStatusMessage(List<Response> responseList, Response response, int successes) {
        String responseString = "";

        if (response.getStatus() == Status.SENT) {
            responseString = "Message successfully sent to " + String.valueOf(successes) + " of " + responseList.size();
        } else if (response.getStatus() == Status.ERROR) {
            responseString = response.getStatusMessage();
        }

        return responseString;
    }

    private Response send(TcpDispatcherProperties tcpDispatcherProperties, ConnectorMessage message, Socket socket, String socketKey) {
        Status responseStatus = Status.QUEUED;
        String responseData = null;
        String responseStatusMessage = null;
        String responseError = null;
        boolean validateResponse = false;

        try {
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
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), message.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), responseStatusMessage + ".", e));
                    eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.FAILURE, responseStatusMessage + " from " + SocketUtil.getInetAddress(socket)));

                    if (tcpDispatcherProperties.isServerMode()) {
                        closeServerModeSocketQuietly(socket);
                    } else {
                        closeSocketQuietly(socketKey);
                    }
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

            if (!tcpDispatcherProperties.isServerMode()) {
                if (tcpDispatcherProperties.isKeepConnectionOpen() && (getCurrentState() == DeployedState.STARTED || getCurrentState() == DeployedState.STARTING)) {
                    if (sendTimeout > 0) {
                        // Close the connection after the send timeout has been reached
                        startThread(socketKey);
                    }
                } else {
                    // If keep connection open is false, then close the socket right now
                    closeSocketQuietly(socketKey);
                }
            }
        } catch (Throwable t) {
            if (tcpDispatcherProperties.isServerMode()) {
                closeServerModeSocketQuietly(socket);
            } else {
                disposeThreadQuietly(socketKey);
                closeSocketQuietly(socketKey);
            }

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

            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), message.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error sending message via TCP.", t));
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError, validateResponse);
    }

    @Override
    protected String getConfigurationClass() {
        return configurationController.getProperty(connectorProperties.getProtocol(), "tcpConfigurationClass");
    }

    private void closeSocketQuietly(String socketKey) {
        try {
            closeSocket(socketKey);
        } catch (IOException e) {
            logger.debug("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }
    }

    private void closeSocket(String socketKey) throws IOException {
        Socket socket = connectedSockets.get(socketKey);
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

    private void closeServerModeSocketQuietly(Socket socket) {
        try {
            closeServerModeSocket(socket);
        } catch (IOException e) {
            logger.debug("Error closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
        }
    }

    private void closeServerModeSocket(Socket socket) throws IOException {
        if (socket != null) {
            boolean wasOpen = !socket.isClosed();
            try {
                if (wasOpen) {
                    logger.trace("Closing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                    SocketUtil.closeSocket(socket);
                }
            } finally {
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

        Thread thread = new Thread("TCP Dispatcher Send Timeout Thread for key " + socketKey) {
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

        disposeThread(thread, true);
    }

    private void disposeThread(Thread thread, boolean interrupt) throws InterruptedException {
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
     * Returns the byte array representation of the connector properties template, using the
     * properties to determine whether or not to encode in Base64, and what charset to use.
     */
    private byte[] getTemplateBytes(TcpDispatcherProperties tcpSenderProperties, ConnectorMessage connectorMessage) throws UnsupportedEncodingException {
        byte[] bytes = new byte[0];

        if (tcpSenderProperties.getTemplate() != null) {
            bytes = getAttachmentHandlerProvider().reAttachMessage(tcpSenderProperties.getTemplate(), connectorMessage, CharsetUtils.getEncoding(tcpSenderProperties.getCharsetEncoding()), tcpSenderProperties.isDataTypeBinary(), tcpSenderProperties.getDestinationConnectorProperties().isReattachAttachments());
        }

        return bytes;
    }

    private void createServerSocket() throws IOException {
        // Create the server socket
        int backlog = DEFAULT_BACKLOG;
        String host = getLocalAddress();
        int port = getLocalPort();

        InetAddress hostAddress = InetAddress.getByName(host);
        int bindAttempts = 0;
        boolean success = false;

        // If an error occurred during binding, try again. If the JVM fails to bind ten times, throw the exception.
        while (!success) {
            try {
                bindAttempts++;
                boolean isLoopback = false;

                try {
                    isLoopback = (hostAddress.isLoopbackAddress() || host.trim().equals("localhost") || hostAddress.equals(InetAddress.getLocalHost()));
                } catch (UnknownHostException e) {
                    logger.warn("Failed to determine if '" + hostAddress.getHostAddress() + "' is a loopback address. Could not resolve the system's host name to an address.", e);
                }

                if (isLoopback) {
                    serverSocket = configuration.createServerSocket(port, backlog);
                } else {
                    serverSocket = configuration.createServerSocket(port, backlog, hostAddress);
                }
                success = true;
            } catch (BindException e) {
                if (bindAttempts >= 10) {
                    throw e;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private String getLocalAddress() {
        return TcpUtil.getFixedHost(replacer.replaceValues(connectorProperties.getLocalAddress(), getChannelId(), getChannel().getName()));
    }

    private int getLocalPort() {
        return NumberUtils.toInt(replacer.replaceValues(connectorProperties.getLocalPort(), getChannelId(), getChannel().getName()));
    }

    /*
     * Sets the socket settings using the connector properties.
     */
    private void initSocket(Socket socket) throws SocketException {
        logger.debug("Initializing socket (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
        socket.setReceiveBufferSize(bufferSize);
        socket.setSendBufferSize(bufferSize);
        socket.setSoTimeout(responseTimeout);
        socket.setKeepAlive(true);
        socket.setReuseAddress(true);
        socket.setTcpNoDelay(true);
    }
}
