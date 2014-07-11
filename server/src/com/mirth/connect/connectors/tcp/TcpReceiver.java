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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ConnectorCountEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.message.StreamHandler;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReceiver;
import com.mirth.connect.donkey.server.message.batch.BatchStreamReader;
import com.mirth.connect.donkey.server.message.batch.ResponseHandler;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.model.transmission.StreamHandlerException;
import com.mirth.connect.model.transmission.batch.DefaultBatchStreamReader;
import com.mirth.connect.plugins.BasicModeProvider;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.plugins.TransmissionModeProvider;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.ErrorMessageBuilder;
import com.mirth.connect.util.TcpUtil;

public class TcpReceiver extends SourceConnector {
    // This determines how many client requests can queue up while waiting for the server socket to accept
    private static final int DEFAULT_BACKLOG = 256;

    private Logger logger = Logger.getLogger(this.getClass());
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    protected TcpReceiverProperties connectorProperties;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private StateAwareServerSocket serverSocket;
    private StateAwareSocket clientSocket;
    private StateAwareSocket recoveryResponseSocket;
    private Thread thread;
    private ExecutorService executor;
    private Set<Future<Throwable>> results = new HashSet<Future<Throwable>>();
    private Set<TcpReader> clientReaders = new HashSet<TcpReader>();
    private AtomicBoolean disposing;

    private int maxConnections;
    private int timeout;
    private int bufferSize;
    private int reconnectInterval;
    private TransmissionModeProvider transmissionModeProvider;
    private DataTypeServerPlugin dataTypeServerPlugin;

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (TcpReceiverProperties) getConnectorProperties();
        maxConnections = NumberUtils.toInt(connectorProperties.getMaxConnections());
        timeout = NumberUtils.toInt(connectorProperties.getReceiveTimeout());
        bufferSize = NumberUtils.toInt(connectorProperties.getBufferSize());
        reconnectInterval = NumberUtils.toInt(connectorProperties.getReconnectInterval());

        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

        String pluginPointName = (String) connectorProperties.getTransmissionModeProperties().getPluginPointName();
        if (pluginPointName.equals("Basic")) {
            transmissionModeProvider = new BasicModeProvider();
        } else {
            transmissionModeProvider = (TransmissionModeProvider) extensionController.getServicePlugins().get(pluginPointName);
        }

        if (transmissionModeProvider == null) {
            throw new DeployException("Unable to find transmission mode plugin: " + pluginPointName);
        }

        dataTypeServerPlugin = extensionController.getDataTypePlugins().get(getInboundDataType().getType());

        if (dataTypeServerPlugin == null) {
            throw new DeployException("Unable to find data type plugin: " + getInboundDataType().getType());
        }

        disposing = new AtomicBoolean(false);

        eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE, null, maxConnections));
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {
        disposing.set(false);
        results.clear();
        clientReaders.clear();

        if (connectorProperties.isServerMode()) {
            // If we're in server mode, use the max connections property to initialize the thread pool
            executor = new ThreadPoolExecutor(0, maxConnections, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        } else {
            // If we're in client mode, only a single thread is needed
            executor = Executors.newSingleThreadExecutor();
        }

        if (connectorProperties.isServerMode()) {
            try {
                createServerSocket();
            } catch (IOException e) {
                throw new StartException("Failed to create server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            }
        }

        // Create the acceptor thread
        thread = new Thread() {
            @Override
            public void run() {
                while (getCurrentState() == DeployedState.STARTED) {
                    StateAwareSocket socket = null;

                    if (connectorProperties.isServerMode()) {
                        // Server mode; wait to accept a client socket on the ServerSocket
                        try {
                            logger.debug("Waiting for new client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                            socket = serverSocket.accept();
                            logger.trace("Accepted new socket: " + socket.getRemoteSocketAddress().toString() + " -> " + socket.getLocalSocketAddress());
                        } catch (java.io.InterruptedIOException e) {
                            logger.debug("Interruption during server socket accept operation (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                        } catch (Exception e) {
                            logger.debug("Error accepting new socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                        }
                    } else {
                        // Client mode, manually initiate a client socket
                        try {
                            logger.debug("Initiating for new client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                            if (connectorProperties.isOverrideLocalBinding()) {
                                socket = SocketUtil.createSocket(getLocalAddress(), getLocalPort());
                            } else {
                                socket = SocketUtil.createSocket();
                            }
                            clientSocket = socket;
                            SocketUtil.connectSocket(socket, getRemoteAddress(), getRemotePort(), timeout);
                        } catch (Exception e) {
                            logger.error("Error initiating new socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                        }
                    }

                    try {
                        ThreadUtils.checkInterruptedStatus();

                        if (socket != null) {
                            try {
                                synchronized (clientReaders) {
                                    // Only allow worker threads to be submitted if we're not currently trying to stop the connector
                                    if (disposing.get()) {
                                        return;
                                    }
                                    TcpReader reader = new TcpReader(socket);
                                    clientReaders.add(reader);
                                    results.add(executor.submit(reader));
                                }
                            } catch (RejectedExecutionException e) {
                                logger.debug("Executor rejected new task (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                                closeSocketQuietly(socket);
                            } catch (SocketException e) {
                                logger.debug("Error initializing socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                                closeSocketQuietly(socket);
                            }
                        }

                        if (connectorProperties.isServerMode()) {
                            // Remove any completed tasks from the list, but don't try to retrieve currently running tasks
                            cleanup(false, false, true);
                        } else {
                            // Wait until the TcpReader is done
                            cleanup(true, false, true);

                            String info = "Client socket finished, waiting " + connectorProperties.getReconnectInterval() + " ms...";
                            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.INFO, info));

                            // Use the reconnect interval to determine how long to wait until creating another socket
                            sleep(reconnectInterval);
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        thread.start();
    }

    @Override
    public void onStop() throws StopException {
        StopException firstCause = null;

        synchronized (clientReaders) {
            disposing.set(true);

            if (executor != null) {
                // Prevent any new client threads from being submitted to the executor
                executor.shutdown();
            }
        }

        if (connectorProperties.isServerMode()) {
            if (serverSocket != null) {
                // Close the server socket
                try {
                    logger.debug("Closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                    serverSocket.close();
                } catch (IOException e) {
                    firstCause = new StopException("Error closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                }
            }
        } else {
            // Close the client socket
            try {
                SocketUtil.closeSocket(clientSocket);
            } catch (IOException e) {
                firstCause = new StopException("Error closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            } finally {
                clientSocket = null;
            }
        }

        // Join the connector thread
        try {
            disposeThread(false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StopException("Thread join operation interrupted (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
        }

        synchronized (clientReaders) {
            for (TcpReader reader : clientReaders) {
                try {
                    synchronized (reader) {
                        reader.setCanRead(false);

                        /*
                         * We only want to close the worker's socket if it's currently in the read()
                         * method. If keep connection open is true and the receive timeout is zero,
                         * that read() would have blocked forever, so we need to close the socket
                         * here so it will throw an exception. However even if the worker was in the
                         * middle of reading bytes from the input stream, we still want to close the
                         * socket. That message would never have been dispatched to the channel
                         * anyway because the connectors current state would not be equal to
                         * STARTED.
                         */
                        if (reader.isReading()) {
                            reader.getSocket().close();
                        }
                    }
                } catch (IOException e) {
                    if (firstCause == null) {
                        firstCause = new StopException("Error closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                    }
                }
            }
            clientReaders.clear();
        }

        // Wait for any remaining tasks to complete
        try {
            cleanup(true, false, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StopException("Client thread disposal interrupted (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
        }

        // Close all client sockets after canceling tasks in case a task failed to complete
        synchronized (clientReaders) {
            for (TcpReader reader : clientReaders) {
                try {
                    reader.getSocket().close();
                } catch (IOException e) {
                    if (firstCause == null) {
                        firstCause = new StopException("Error closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                    }
                }

                try {
                    SocketUtil.closeSocket(reader.getResponseSocket());
                } catch (IOException e) {
                    if (firstCause == null) {
                        firstCause = new StopException("Error closing response socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                    }
                }
            }
            clientReaders.clear();
        }

        // Close the recovery response socket, if applicable
        try {
            SocketUtil.closeSocket(recoveryResponseSocket);
        } catch (IOException e) {
            if (firstCause == null) {
                firstCause = new StopException("Error closing response socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            }
        }

        if (firstCause != null) {
            throw firstCause;
        }
    }

    @Override
    public void onHalt() throws HaltException {
        HaltException firstCause = null;

        synchronized (clientReaders) {
            disposing.set(true);
            // Prevent any new client threads from being submitted to the executor
            executor.shutdownNow();
        }

        if (connectorProperties.isServerMode()) {
            if (serverSocket != null) {
                // Close the server socket
                try {
                    logger.debug("Closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                    serverSocket.close();
                } catch (IOException e) {
                    firstCause = new HaltException("Error closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                }
            }
        } else {
            // Close the client socket
            try {
                SocketUtil.closeSocket(clientSocket);
            } catch (IOException e) {
                firstCause = new HaltException("Error closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            } finally {
                clientSocket = null;
            }
        }

        // Join the connector thread
        try {
            disposeThread(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (firstCause == null) {
                firstCause = new HaltException("Thread join operation interrupted (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            }
        }

        // Close all client sockets before interrupting tasks
        synchronized (clientReaders) {
            for (TcpReader reader : clientReaders) {
                try {
                    reader.getSocket().close();
                } catch (IOException e) {
                    if (firstCause == null) {
                        logger.debug("Error closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                        firstCause = new HaltException("Error closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                    }
                }

                try {
                    SocketUtil.closeSocket(reader.getResponseSocket());
                } catch (IOException e) {
                    if (firstCause == null) {
                        logger.debug("Error closing response socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                        firstCause = new HaltException("Error closing response socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                    }
                }
            }
        }

        // Close the recovery response socket, if applicable
        try {
            SocketUtil.closeSocket(recoveryResponseSocket);
        } catch (IOException e) {
            if (firstCause == null) {
                firstCause = new HaltException("Error closing response socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            }
        }

        // Attempt to cancel any remaining tasks
        try {
            cleanup(false, true, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (firstCause == null) {
                firstCause = new HaltException("Client thread disposal interrupted (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            }
        }

        synchronized (clientReaders) {
            clientReaders.clear();
        }

        if (firstCause != null) {
            throw firstCause;
        }
    }

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        try {
            if (dispatchResult.getSelectedResponse() != null) {
                // Only if we're responding on a new connection can we handle recovered responses
                if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION || connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION_ON_RECOVERY) {
                    BatchStreamReader batchStreamReader = new DefaultBatchStreamReader(null);
                    StreamHandler streamHandler = transmissionModeProvider.getStreamHandler(null, null, batchStreamReader, connectorProperties.getTransmissionModeProperties());

                    try {
                        dispatchResult.setAttemptedResponse(true);
                        recoveryResponseSocket = createResponseSocket();
                        connectResponseSocket(recoveryResponseSocket, streamHandler);
                        sendResponse(dispatchResult.getSelectedResponse().getMessage(), recoveryResponseSocket, streamHandler, true);
                    } catch (IOException e) {
                        dispatchResult.setResponseError(ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error sending response.", e));
                    } finally {
                        closeSocketQuietly(recoveryResponseSocket);
                        recoveryResponseSocket = null;
                    }
                } else {
                    dispatchResult.setResponseError("Cannot respond on original connection during message recovery. In order to send a response, enable \"Respond on New Connection\" in Tcp Listener settings.");
                }
            }
        } finally {
            finishDispatch(dispatchResult);
        }
    }

    protected class TcpReader extends ResponseHandler implements Callable<Throwable>, BatchMessageReceiver {
        private StateAwareSocket socket = null;
        private StateAwareSocket responseSocket = null;
        private AtomicBoolean reading = null;
        private AtomicBoolean canRead = null;
        private StreamHandler streamHandler = null;
        private boolean firstMessage;

        public TcpReader(StateAwareSocket socket) throws SocketException {
            this.socket = socket;
            initSocket(socket);
            reading = new AtomicBoolean(false);
            canRead = new AtomicBoolean(true);
        }

        public StateAwareSocket getSocket() {
            return socket;
        }

        public StateAwareSocket getResponseSocket() {
            return responseSocket;
        }

        public boolean isReading() {
            return reading.get();
        }

        public void setCanRead(boolean canRead) {
            this.canRead.set(canRead);
        }

        @Override
        public Throwable call() {
            Throwable t = null;
            boolean done = false;

            eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.CONNECTED, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), true));

            try {
                while (!done && getCurrentState() == DeployedState.STARTED) {
                    ThreadUtils.checkInterruptedStatus();
                    streamHandler = null;

                    try {
                        // Add the socket information to the channelMap
                        Map<String, Object> sourceMap = new HashMap<String, Object>();
                        sourceMap.put("localAddress", socket.getLocalAddress().getHostAddress());
                        sourceMap.put("localPort", socket.getLocalPort());
                        if (socket.getRemoteSocketAddress() instanceof InetSocketAddress) {
                            sourceMap.put("remoteAddress", ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress());
                            sourceMap.put("remotePort", ((InetSocketAddress) socket.getRemoteSocketAddress()).getPort());
                        }

                        firstMessage = true;
                        OutputStream outputStream = null;

                        if (connectorProperties.getRespondOnNewConnection() != TcpReceiverProperties.NEW_CONNECTION) {
                            // If we're not responding on a new connection, then write to the output stream of the same socket
                            responseSocket = socket;
                            outputStream = new BufferedOutputStream(responseSocket.getOutputStream(), bufferSize);
                        } else {
                            outputStream = socket.getOutputStream();
                        }

                        boolean canStreamBatch = true;
                        BatchStreamReader batchStreamReader = null;
                        // If batch is enabled, attempt to get the batch stream reader from the data type
                        if (isProcessBatch()) {
                            batchStreamReader = dataTypeServerPlugin.getBatchStreamReader(socket.getInputStream(), connectorProperties.getTransmissionModeProperties());
                        }

                        // If the data type does not support batch streaming then use the default reader
                        if (batchStreamReader == null) {
                            canStreamBatch = false;
                            batchStreamReader = new DefaultBatchStreamReader(socket.getInputStream());
                        }

                        streamHandler = transmissionModeProvider.getStreamHandler(socket.getInputStream(), outputStream, batchStreamReader, connectorProperties.getTransmissionModeProperties());

                        if (canStreamBatch) {
                            BatchRawMessage rawMessage = new BatchRawMessage(this);

                            rawMessage.setSourceMap(sourceMap);

                            // Send the message to the source connector
                            try {
                                dispatchBatchMessage(rawMessage, this);
                            } catch (BatchMessageException e) {
                                Throwable cause = e.getCause();
                                if (cause instanceof IOException) {
                                    throw (IOException) cause;
                                }

                                if (cause instanceof InterruptedException) {
                                    throw (InterruptedException) cause;
                                }

                                done = true;
                                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error processing batch message", e));

                                logger.error("Error processing batch message", e);
                            }
                        } else if (!done) {
                            ThreadUtils.checkInterruptedStatus();

                            byte[] bytes = null;

                            if (canRead()) {
                                logger.debug("Reading from socket input stream (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ")...");
                                try {
                                    bytes = readBytes();
                                } finally {
                                    readCompleted();
                                }
                            }

                            if (bytes != null) {
                                logger.debug("Bytes returned from socket, length: " + bytes.length + " (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ")");
                                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.RECEIVING, "Message received from " + SocketUtil.getLocalAddress(socket) + ", processing... "));

                                RawMessage rawMessage = null;

                                if (connectorProperties.isDataTypeBinary()) {
                                    // Store the raw bytes in the RawMessage object
                                    rawMessage = new RawMessage(bytes);
                                } else {
                                    // Encode the bytes using the charset encoding property and store the string in the RawMessage object
                                    rawMessage = new RawMessage(getStringFromBytes(bytes));
                                }

                                rawMessage.setSourceMap(sourceMap);

                                DispatchResult dispatchResult = null;

                                ThreadUtils.checkInterruptedStatus();

                                // Send the message to the source connector
                                try {
                                    dispatchResult = dispatchRawMessage(rawMessage);

                                    streamHandler.commit(true);

                                    // Check to see if we have a response to send
                                    if (dispatchResult.getSelectedResponse() != null) {
                                        // Send the response
                                        dispatchResult.setAttemptedResponse(true);

                                        try {
                                            // If the response socket hasn't been initialized, do that now
                                            if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION) {
                                                responseSocket = createResponseSocket();
                                                connectResponseSocket(responseSocket, streamHandler);
                                            }

                                            sendResponse(dispatchResult.getSelectedResponse().getMessage(), responseSocket, streamHandler, connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION);
                                        } catch (IOException e) {
                                            dispatchResult.setResponseError(ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error sending response.", e));
                                        } finally {
                                            if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION || !connectorProperties.isKeepConnectionOpen()) {
                                                closeSocketQuietly(responseSocket);
                                            }
                                        }
                                    }
                                } catch (ChannelException e) {
                                    streamHandler.commit(false);
                                } finally {
                                    finishDispatch(dispatchResult);
                                }

                                eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), (Boolean) null));
                            }
                        }

                        logger.debug("Done with socket input stream (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");

                        // If we're not keeping the connection open or if the remote side has already closed the connection, then we're done with the socket
                        if (checkSocket(socket)) {
                            done = true;
                        }
                    } catch (IOException e) {
                        boolean timeout = e instanceof SocketTimeoutException || !(e instanceof StreamHandlerException) && e.getCause() != null && e.getCause() instanceof SocketTimeoutException;

                        // If we're keeping the connection open and a timeout occurred, then continue processing. Otherwise, abort.
                        if (!connectorProperties.isKeepConnectionOpen() || !timeout) {
                            // If an exception occurred then abort, even if keep connection open is true
                            done = true;

                            if (timeout) {
                                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.FAILURE, "Timeout waiting for message from " + SocketUtil.getLocalAddress(socket) + ". "));
                            } else {
                                // Set the return value and send an alert
                                String errorMessage = "Error receiving message (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").";

                                SocketException cause = null;
                                if (e instanceof SocketException) {
                                    cause = (SocketException) e;
                                } else if (e.getCause() != null && e.getCause() instanceof SocketException) {
                                    cause = (SocketException) e.getCause();
                                }

                                if (cause != null && cause.getMessage() != null && cause.getMessage().contains("Connection reset")) {
                                    logger.warn(errorMessage, e);
                                } else {
                                    logger.error(errorMessage, e);
                                }

                                t = new Exception(errorMessage, e);
                                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error receiving message", e));
                                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.FAILURE, "Error receiving message from " + SocketUtil.getLocalAddress(socket) + ": " + e.getMessage()));
                            }
                        } else {
                            logger.debug("Timeout reading from socket input stream (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.INFO, "Timeout waiting for message from " + SocketUtil.getLocalAddress(socket) + ". "));
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Error receiving message (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error receiving message", e));
                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.FAILURE, "Error receiving message from " + SocketUtil.getLocalAddress(socket) + ": " + e.getMessage()));
            } finally {
                logger.debug("Done with socket, closing (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ")...");

                // We're done reading, so close everything up
                closeSocketQuietly(socket);
                if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION) {
                    closeSocketQuietly(responseSocket);
                }

                eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.DISCONNECTED, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), false));

                synchronized (clientReaders) {
                    clientReaders.remove(this);
                }
            }

            return t;
        }

        @Override
        public boolean canRead() {
            /*
             * We need to keep track of whether the worker thread is currently trying to read from
             * the input stream because the read() method is not interruptible. To do this we store
             * two booleans, canRead and reading. The canRead boolean is checked internally here and
             * set externally (e.g. by the onStop() or onHalt() methods). The reading boolean is set
             * in here when the thread is about to attempt to read from the stream. After the read()
             * method returns (or throws an exception), reading is set to false.
             */
            synchronized (this) {
                if (canRead.get()) {
                    reading.set(true);
                }
            }

            return reading.get();
        }

        @Override
        public byte[] readBytes() throws IOException {
            /*
             * Read from the socket's input stream. If we're keeping the connection open, then bytes
             * will be read until the socket timeout is reached, or until an EOF marker or the
             * ending bytes are encountered. If we're not keeping the connection open, then a socket
             * timeout will not be silently caught, and instead will be thrown from here and cause
             * the worker thread to abort.
             */
            return streamHandler.read();
        }

        @Override
        public void readCompleted() {
            reading.set(false);
        }

        @Override
        public String getStringFromBytes(byte[] bytes) throws IOException {
            return new String(bytes, CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding()));
        }

        @Override
        public void responseProcess() throws IOException {
            // Only send a response for the first message
            if (firstMessage) {
                firstMessage = false;
                streamHandler.commit(true);

                // Check to see if we have a response to send
                if (dispatchResult.getSelectedResponse() != null) {
                    // Send the response
                    dispatchResult.setAttemptedResponse(true);

                    try {
                        // If the response socket hasn't been initialized, do that now
                        if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION) {
                            responseSocket = createResponseSocket();
                            connectResponseSocket(responseSocket, streamHandler);
                        }

                        sendResponse(dispatchResult.getSelectedResponse().getMessage(), responseSocket, streamHandler, connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION);
                    } catch (IOException e) {
                        dispatchResult.setResponseError(ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error sending response.", e));
                    } finally {
                        if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION || !connectorProperties.isKeepConnectionOpen()) {
                            closeSocketQuietly(responseSocket);
                        }
                    }
                }
            }
        }

        @Override
        public void responseError(ChannelException e) {
            if (firstMessage) {
                firstMessage = false;
                try {
                    streamHandler.commit(false);
                } catch (Throwable t) {
                    logger.warn("Error commiting ACK or NACK bytes", t);
                }
            }
        }
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
                if (hostAddress.equals(InetAddress.getLocalHost()) || hostAddress.isLoopbackAddress() || host.trim().equals("localhost")) {
                    serverSocket = new StateAwareServerSocket(port, backlog);
                } else {
                    serverSocket = new StateAwareServerSocket(port, backlog, hostAddress);
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

    private StateAwareSocket createResponseSocket() throws IOException {
        logger.debug("Creating response socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
        return SocketUtil.createSocket();
    }

    private void connectResponseSocket(StateAwareSocket responseSocket, StreamHandler streamHandler) throws IOException {
        int responsePort = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getResponsePort(), getChannelId()));
        SocketUtil.connectSocket(responseSocket, replacer.replaceValues(connectorProperties.getResponseAddress(), getChannelId()), responsePort, timeout);
        initSocket(responseSocket);
        BufferedOutputStream bos = new BufferedOutputStream(responseSocket.getOutputStream(), bufferSize);
        streamHandler.setOutputStream(bos);
    }

    private void sendResponse(String response, StateAwareSocket responseSocket, StreamHandler streamHandler, boolean newConnection) throws IOException {
        try {
            if (responseSocket != null && streamHandler != null) {
                // Send the response
                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.INFO, "Sending response to " + (newConnection ? SocketUtil.getInetAddress(responseSocket) : SocketUtil.getLocalAddress(responseSocket)) + "... "));
                streamHandler.write(getBytes(response));
            } else {
                throw new IOException((responseSocket == null ? "Response socket" : "Stream handler") + " is null.");
            }
        } catch (IOException e) {
            if (responseSocket != null && responseSocket.remoteSideHasClosed()) {
                e = new IOException("Remote socket has closed.");
            }

            logger.error("Error sending response (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.FAILURE, "Error sending response to " + (newConnection ? SocketUtil.getInetAddress(responseSocket) : SocketUtil.getLocalAddress(responseSocket)) + ": " + e.getMessage() + " "));
            throw e;
        }
    }

    private boolean checkSocket(StateAwareSocket socket) throws IOException {
        return !connectorProperties.isKeepConnectionOpen() || socket.isClosed() || socket.remoteSideHasClosed();
    }

    private void closeSocketQuietly(StateAwareSocket socket) {
        try {
            if (socket != null) {
                logger.trace("Closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                SocketUtil.closeSocket(socket);
            }
        } catch (IOException e) {
            logger.debug("Error closing client socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
        }
    }

    private void disposeThread(boolean interrupt) throws InterruptedException {
        if (thread != null && thread.isAlive()) {
            if (interrupt) {
                logger.trace("Interrupting thread (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                thread.interrupt();
            }

            logger.trace("Joining thread (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }
    }

    /**
     * Attempts to get the result of any Future tasks which may still be running. Any completed
     * tasks are removed from the Future list.
     * 
     * This can ensure that all client socket threads are disposed, so that a remote client wouldn't
     * be able to still send a message after a channel has been stopped or undeployed (even though
     * it wouldn't be processed through the channel anyway).
     * 
     * @param block
     *            - If true, then each Future task will be joined to this one, blocking until the
     *            task thread dies.
     * @param interrupt
     *            - If true, each currently running task thread will be interrupted in an attempt to
     *            stop the task. Any interrupted exceptions will be caught and not thrown, in a best
     *            effort to ensure that all results are taken care of.
     * @param remove
     *            - If true, each completed result will be removed from the Future set during
     *            iteration.
     */
    private void cleanup(boolean block, boolean interrupt, boolean remove) throws InterruptedException {
        for (Iterator<Future<Throwable>> it = results.iterator(); it.hasNext();) {
            Future<Throwable> result = it.next();

            if (interrupt) {
                // Cancel the task, with the option of whether or not to forcefully interrupt it
                result.cancel(true);
            }

            if (block) {
                // Attempt to get the result (which blocks until it returns)
                Throwable t = null;
                try {
                    // If the return value is not null, then an exception was raised somewhere in the client socket thread
                    if ((t = result.get()) != null) {
                        logger.debug("Client socket thread returned unsuccessfully (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", t);
                    }
                } catch (Exception e) {
                    logger.debug("Error retrieving client socket thread result for " + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ".", e);

                    Throwable cause;
                    if (t instanceof ExecutionException) {
                        cause = t.getCause();
                    } else {
                        cause = t;
                    }

                    if (cause instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        if (!interrupt) {
                            throw (InterruptedException) cause;
                        }
                    }
                }
            }

            if (remove) {
                // Remove the task from the list if it's done, or if it's been cancelled
                if (result.isDone()) {
                    it.remove();
                }
            }
        }
    }

    private String getLocalAddress() {
        return TcpUtil.getFixedHost(replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getHost(), getChannelId()));
    }

    private int getLocalPort() {
        return NumberUtils.toInt(replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getPort(), getChannelId()));
    }

    private String getRemoteAddress() {
        return TcpUtil.getFixedHost(replacer.replaceValues(connectorProperties.getRemoteAddress(), getChannelId()));
    }

    private int getRemotePort() {
        return NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRemotePort(), getChannelId()));
    }

    /*
     * Sets the socket settings using the connector properties.
     */
    private void initSocket(Socket socket) throws SocketException {
        logger.debug("Initializing socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
        socket.setReceiveBufferSize(bufferSize);
        socket.setSendBufferSize(bufferSize);
        socket.setSoTimeout(timeout);
        socket.setKeepAlive(connectorProperties.isKeepConnectionOpen());
        socket.setReuseAddress(true);
        socket.setTcpNoDelay(true);
    }

    /*
     * Converts a string to a byte array using the connector properties to determine whether or not
     * to encode in Base64, and what charset to use.
     */
    private byte[] getBytes(String str) throws UnsupportedEncodingException {
        byte[] bytes = new byte[0];

        if (str != null) {
            if (connectorProperties.isDataTypeBinary()) {
                bytes = Base64.decodeBase64(str);
            } else {
                bytes = str.getBytes(CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding()));
            }
        }
        return bytes;
    }
}
