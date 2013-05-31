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
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.event.ConnectorCountEvent;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.util.ThreadUtils;
import com.mirth.connect.model.transmission.StreamHandler;
import com.mirth.connect.model.transmission.StreamHandlerException;
import com.mirth.connect.model.transmission.batch.BatchStreamReader;
import com.mirth.connect.model.transmission.batch.DefaultBatchStreamReader;
import com.mirth.connect.model.transmission.batch.ER7BatchStreamReader;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.plugins.BasicModeProvider;
import com.mirth.connect.plugins.TransmissionModeProvider;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.ErrorConstants;
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
    private Thread thread;
    private ExecutorService executor;
    private Set<Future<Throwable>> results = new HashSet<Future<Throwable>>();
    private Set<TcpReader> clientReaders = new HashSet<TcpReader>();
    private AtomicBoolean disposing;

    private int maxConnections;
    TransmissionModeProvider transmissionModeProvider;

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (TcpReceiverProperties) getConnectorProperties();
        maxConnections = parseInt(connectorProperties.getMaxConnections());

        String pluginPointName = (String) connectorProperties.getTransmissionModeProperties().getPluginPointName();
        if (pluginPointName.equals("Basic")) {
            transmissionModeProvider = new BasicModeProvider();
        } else {
            transmissionModeProvider = (TransmissionModeProvider) ControllerFactory.getFactory().createExtensionController().getServicePlugins().get(pluginPointName);
        }

        if (transmissionModeProvider == null) {
            throw new DeployException("Unable to find transmission mode plugin: " + pluginPointName);
        }

        disposing = new AtomicBoolean(false);

        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.IDLE));
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
                while (getCurrentState() == ChannelState.STARTED) {
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
                            socket = SocketUtil.createSocket(getHost(), getPort());
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
                            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.INFO, info));

                            // Use the reconnect interval to determine how long to wait until creating another socket
                            sleep(parseInt(connectorProperties.getReconnectInterval()));
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
            // Prevent any new client threads from being submitted to the executor
            executor.shutdown();
        }

        if (serverSocket != null) {
            // Close the server socket
            try {
                logger.debug("Closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                serverSocket.close();
            } catch (IOException e) {
                firstCause = new StopException("Error closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
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
            }
            clientReaders.clear();
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

        if (serverSocket != null) {
            // Close the server socket
            try {
                logger.debug("Closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                serverSocket.close();
            } catch (IOException e) {
                firstCause = new HaltException("Error closing server socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
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
        boolean attemptedResponse = false;
        String errorMessage = null;
        try {
            if (dispatchResult.getSelectedResponse() != null) {
                // Only if we're responding on a new connection can we handle recovered responses
                if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION || connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION_ON_RECOVERY) {
                    BatchStreamReader batchStreamReader = new DefaultBatchStreamReader(null);
                    StreamHandler streamHandler = transmissionModeProvider.getStreamHandler(null, null, batchStreamReader, connectorProperties.getTransmissionModeProperties());
                    StateAwareSocket responseSocket = null;

                    try {
                        attemptedResponse = true;
                        responseSocket = createResponseSocket(streamHandler);
                        sendResponse(dispatchResult.getSelectedResponse().getMessage(), responseSocket, streamHandler, true);
                    } catch (IOException e) {
                        errorMessage = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_411, "Error sending response.", e);
                    } finally {
                        closeSocketQuietly(responseSocket);
                    }
                } else {
                    errorMessage = "Cannot respond on original connection during message recovery. In order to send a response, enable \"Respond on New Connection\" in Tcp Listener settings.";
                }
            }
        } finally {
            finishDispatch(dispatchResult, attemptedResponse, errorMessage);
        }
    }

    protected class TcpReader implements Callable<Throwable> {
        private StateAwareSocket socket = null;
        private StateAwareSocket responseSocket = null;
        private AtomicBoolean reading = null;
        private AtomicBoolean canRead = null;

        public TcpReader(StateAwareSocket socket) throws SocketException {
            this.socket = socket;
            initSocket(socket);
            reading = new AtomicBoolean(false);
            canRead = new AtomicBoolean(true);
        }

        public StateAwareSocket getSocket() {
            return socket;
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

            eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.CONNECTED, null, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), true));

            try {
                while (!done && getCurrentState() == ChannelState.STARTED) {
                    ThreadUtils.checkInterruptedStatus();
                    StreamHandler streamHandler = null;

                    try {
                        boolean streamDone = false;
                        // TODO: Put this on the DataType object; let it decide based on the properties which stream handler to use
                        BatchStreamReader batchStreamReader = null;
                        if (connectorProperties.isProcessBatch() && getInboundDataType().getType().equals("HL7V2")) {
                            if (connectorProperties.getTransmissionModeProperties() instanceof FrameModeProperties) {
                                batchStreamReader = new ER7BatchStreamReader(socket.getInputStream(), TcpUtil.stringToByteArray(((FrameModeProperties) connectorProperties.getTransmissionModeProperties()).getEndOfMessageBytes()));
                            } else {
                                batchStreamReader = new ER7BatchStreamReader(socket.getInputStream());
                            }
                        } else {
                            batchStreamReader = new DefaultBatchStreamReader(socket.getInputStream());
                        }
                        streamHandler = transmissionModeProvider.getStreamHandler(socket.getInputStream(), socket.getOutputStream(), batchStreamReader, connectorProperties.getTransmissionModeProperties());

                        if (connectorProperties.getRespondOnNewConnection() != TcpReceiverProperties.NEW_CONNECTION) {
                            // If we're not responding on a new connection, then write to the output stream of the same socket
                            responseSocket = socket;
                            BufferedOutputStream bos = new BufferedOutputStream(responseSocket.getOutputStream(), parseInt(connectorProperties.getBufferSize()));
                            streamHandler.setOutputStream(bos);
                        }

                        while (!streamDone && !done) {
                            ThreadUtils.checkInterruptedStatus();

                            /*
                             * We need to keep track of whether the worker thread is currently
                             * trying to read from the input stream because the read() method is not
                             * interruptable. To do this we store two booleans, canRead and reading.
                             * The canRead boolean is checked internally here and set externally
                             * (e.g. by the onStop() or onHalt() methods). The reading boolean is
                             * set in here when the thread is about to attempt to read from the
                             * stream. After the read() method returns (or throws an exception),
                             * reading is set to false.
                             */
                            synchronized (this) {
                                if (canRead.get()) {
                                    reading.set(true);
                                }
                            }

                            byte[] bytes = null;

                            if (reading.get()) {
                                logger.debug("Reading from socket input stream (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ")...");
                                try {
                                    /*
                                     * Read from the socket's input stream. If we're keeping the
                                     * connection open, then bytes will be read until the socket
                                     * timeout is reached, or until an EOF marker or the ending
                                     * bytes are encountered. If we're not keeping the connection
                                     * open, then a socket timeout will not be silently caught, and
                                     * instead will be thrown from here and cause the worker thread
                                     * to abort.
                                     */
                                    bytes = streamHandler.read();
                                } finally {
                                    reading.set(false);
                                }
                            }

                            if (bytes != null) {
                                logger.debug("Bytes returned from socket, length: " + bytes.length + " (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ")");
                                eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.RECEIVING, "Message received from " + SocketUtil.getLocalAddress(socket) + ", processing... "));

                                RawMessage rawMessage = null;

                                if (connectorProperties.isDataTypeBinary()) {
                                    // Store the raw bytes in the RawMessage object
                                    rawMessage = new RawMessage(bytes);
                                } else {
                                    // Encode the bytes using the charset encoding property and store the string in the RawMessage object
                                    rawMessage = new RawMessage(new String(bytes, CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding())));
                                }

                                // Add the socket information to the channelMap
                                Map<String, Object> channelMap = new HashMap<String, Object>();
                                channelMap.put("clientAddress", socket.getLocalAddress().getHostAddress());
                                channelMap.put("clientPort", socket.getLocalPort());
                                if (socket.getRemoteSocketAddress() instanceof InetSocketAddress) {
                                    channelMap.put("localAddress", ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress());
                                    channelMap.put("localPort", ((InetSocketAddress) socket.getRemoteSocketAddress()).getPort());
                                }
                                rawMessage.setChannelMap(channelMap);

                                DispatchResult dispatchResult = null;

                                // Keep attempting while the channel is still started
                                while (dispatchResult == null && getCurrentState() == ChannelState.STARTED) {
                                    ThreadUtils.checkInterruptedStatus();

                                    boolean attemptedResponse = false;
                                    String errorMessage = null;

                                    // Send the message to the source connector
                                    try {
                                        dispatchResult = dispatchRawMessage(rawMessage);
                                        streamHandler.commit(true);

                                        // Check to see if we have a response to send
                                        if (dispatchResult.getSelectedResponse() != null) {
                                            // Send the response
                                            attemptedResponse = true;

                                            try {
                                                // If the response socket hasn't been initialized, do that now
                                                if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION) {
                                                    responseSocket = createResponseSocket(streamHandler);
                                                }

                                                sendResponse(dispatchResult.getSelectedResponse().getMessage(), responseSocket, streamHandler, connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION);
                                            } catch (IOException e) {
                                                errorMessage = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_411, "Error sending response.", e);
                                            } finally {
                                                if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION || !connectorProperties.isKeepConnectionOpen()) {
                                                    closeSocketQuietly(responseSocket);
                                                }
                                            }
                                        }
                                    } catch (ChannelException e) {
                                        streamHandler.commit(false);
                                    } finally {
                                        finishDispatch(dispatchResult, attemptedResponse, errorMessage);
                                    }
                                }

                                eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.IDLE, ConnectorEventType.CONNECTED, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), null));
                            } else {
                                // If no bytes were returned, then assume we have finished processing all possible messages from the input stream.
                                logger.debug("Stream reader returned null (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                                streamDone = true;
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
                                eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.FAILURE, "Timeout waiting for message from " + SocketUtil.getLocalAddress(socket) + ". "));
                            } else {
                                // Set the return value and send an alert
                                t = new Exception("Error receiving message (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                                logger.error("Error receiving message (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), "Error receiving message", e));
                                eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.FAILURE, "Error receiving message from " + SocketUtil.getLocalAddress(socket) + ": " + e.getMessage()));
                            }
                        } else {
                            logger.debug("Timeout reading from socket input stream (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
                            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.INFO, "Timeout waiting for message from " + SocketUtil.getLocalAddress(socket) + ". "));
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Error receiving message (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), "Error receiving message", e));
                eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.FAILURE, "Error receiving message from " + SocketUtil.getLocalAddress(socket) + ": " + e.getMessage()));
            } finally {
                logger.debug("Done with socket, closing (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ")...");

                // We're done reading, so close everything up
                closeSocketQuietly(socket);
                if (connectorProperties.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION) {
                    closeSocketQuietly(responseSocket);
                }

                eventController.dispatchEvent(new ConnectorCountEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.DISCONNECTED, ConnectorEventType.CONNECTED, SocketUtil.getLocalAddress(socket) + " -> " + SocketUtil.getInetAddress(socket), false));

                synchronized (clientReaders) {
                    clientReaders.remove(this);
                }
            }

            return t;
        }
    }

    private void createServerSocket() throws IOException {
        // Create the server socket
        int backlog = DEFAULT_BACKLOG;
        String host = getHost();
        int port = getPort();

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

    private StateAwareSocket createResponseSocket(StreamHandler streamHandler) throws IOException {
        logger.debug("Creating response socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
        StateAwareSocket responseSocket = SocketUtil.createSocket(replacer.replaceValues(connectorProperties.getResponseAddress(), getChannelId()), replacer.replaceValues(connectorProperties.getResponsePort(), getChannelId()), getHost());
        initSocket(responseSocket);
        BufferedOutputStream bos = new BufferedOutputStream(responseSocket.getOutputStream(), parseInt(connectorProperties.getBufferSize()));
        streamHandler.setOutputStream(bos);
        return responseSocket;
    }

    private void sendResponse(String response, StateAwareSocket responseSocket, StreamHandler streamHandler, boolean newConnection) throws IOException {
        try {
            if (responseSocket != null && streamHandler != null) {
                // Send the response
                eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.INFO, "Sending response to " + (newConnection ? SocketUtil.getInetAddress(responseSocket) : SocketUtil.getLocalAddress(responseSocket)) + "... "));
                streamHandler.write(getBytes(response));
            } else {
                throw new IOException((responseSocket == null ? "Response socket" : "Stream handler") + " is null.");
            }
        } catch (IOException e) {
            if (responseSocket != null && responseSocket.remoteSideHasClosed()) {
                e = new IOException("Remote socket has closed.");
            }

            logger.error("Error sending response (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", e);
            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectorEventType.FAILURE, "Error sending response to " + (newConnection ? SocketUtil.getInetAddress(responseSocket) : SocketUtil.getLocalAddress(responseSocket)) + ": " + e.getMessage() + " "));
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

    private String getHost() {
        return TcpUtil.getFixedHost(replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getHost(), getChannelId()));
    }

    private int getPort() {
        return parseInt(replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getPort(), getChannelId()));
    }

    /*
     * Sets the socket settings using the connector properties.
     */
    private void initSocket(Socket socket) throws SocketException {
        logger.debug("Initializing socket (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").");
        socket.setReceiveBufferSize(parseInt(connectorProperties.getBufferSize()));
        socket.setSendBufferSize(parseInt(connectorProperties.getBufferSize()));
        socket.setSoTimeout(parseInt(connectorProperties.getReceiveTimeout()));
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
