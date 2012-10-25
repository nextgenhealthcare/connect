/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.MessageResponse;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.CharsetUtils;

public class TcpReceiver extends SourceConnector {
    // TODO: Add MonitoringController stuff
    public static final int DEFAULT_BACKLOG = 256;

    private Logger logger = Logger.getLogger(this.getClass());
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    protected TcpReceiverProperties connectorProperties;

    private ServerSocket serverSocket;
    private Thread thread;
    private StreamHandler streamHandler = new StreamHandler();
    private ExecutorService executor;
    private Set<Future<Throwable>> results = new HashSet<Future<Throwable>>();
    private final Object lock = new Object();

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (TcpReceiverProperties) getConnectorProperties();

        executor = new ThreadPoolExecutor(0, parseInt(connectorProperties.getMaxConnections()), 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }

    @Override
    public void onUndeploy() throws UndeployException {
        UndeployException firstCause = null;

        // Interrupt and join the connector thread
        try {
            disposeThread(true);
        } catch (InterruptedException e) {
            firstCause = new UndeployException("Interruption while disposing client socket threads for " + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ".", e);
        }

        // Forcefully cancel any remaining tasks
        cleanup(true, true);

        executor.shutdownNow();

        if (firstCause != null) {
            throw firstCause;
        }
    }

    @Override
    public void onStart() throws StartException {
        // Create the server socket
        try {
            String host = connectorProperties.getListenerConnectorProperties().getHost();
            if (host == null || host.length() == 0) {
                host = "localhost";
            }

            int backlog = DEFAULT_BACKLOG;
            int port = parseInt(connectorProperties.getListenerConnectorProperties().getPort());

            InetAddress hostAddress = InetAddress.getByName(connectorProperties.getListenerConnectorProperties().getHost());
            if (hostAddress.equals(InetAddress.getLocalHost()) || hostAddress.isLoopbackAddress() || host.trim().equals("localhost")) {
                serverSocket = new ServerSocket(port, backlog);
            } else {
                serverSocket = new ServerSocket(port, backlog, hostAddress);
            }

            streamHandler.setBeginBytes(stringToByteArray(connectorProperties.getBeginBytes(), connectorProperties.isFrameEncodingHex()));
            streamHandler.setEndBytes(stringToByteArray(connectorProperties.getEndBytes(), connectorProperties.isFrameEncodingHex()));
        } catch (IOException e) {
            throw new StartException("Failed to open server socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
        }

        // Create the acceptor thread
        thread = new Thread() {
            @Override
            public void run() {
                while (isRunning()) {
                    Socket socket = null;

                    try {
                        logger.debug("Waiting for new client socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ".");

                        socket = serverSocket.accept();

                        logger.trace("Accepted new socket: " + socket.getRemoteSocketAddress().toString() + " -> " + socket.getLocalSocketAddress());
                    } catch (java.io.InterruptedIOException e) {
                        logger.debug("Interruption during server socket accept operation for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
                    } catch (Exception e) {
                        logger.debug("Error accepting new socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
                    }

                    if (socket != null) {
                        try {
                            results.add(executor.submit(new TcpReader(socket)));
                        } catch (Exception e) {
                            logger.debug("Error initializing socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
                        }
                    }

                    // Remove any completed tasks from the list
                    cleanup(false, false);
                }
            }
        };
        thread.start();
    }

    @Override
    public void onStop() throws StopException {
        StopException firstCause = null;

        // Close the server socket
        try {
            logger.debug("Closing server socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".");
            serverSocket.close();
        } catch (IOException e) {
            firstCause = new StopException("Error closing server socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
        }

        // Join the connector thread
        try {
            disposeThread(false);
        } catch (InterruptedException e) {
            if (firstCause == null) {
                firstCause = new StopException("Thread join operation interrupted for " + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ".", e);
            }
        }

        // Attempt to cancel any remaining tasks
        cleanup(true, false);

        if (firstCause != null) {
            throw firstCause;
        }
    }

    @Override
    public void handleRecoveredResponse(MessageResponse messageResponse) {
        // TODO: Maybe if we're ACKing on a new connection, then we can handle recovered responses
    }

    protected class TcpReader implements Callable<Throwable> {
        private Socket socket = null;
        private Socket responseSocket = null;

        public TcpReader(Socket socket) throws Exception {
            this.socket = socket;

            logger.debug("Initializing socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ".");
            initSocket(socket);

            if (connectorProperties.isAckOnNewConnection()) {
                logger.debug("Creating response socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ".");
                responseSocket = new StateAwareSocket(connectorProperties.getAckIP(), parseInt(connectorProperties.getAckPort()));
                initSocket(responseSocket);
            } else {
                // If we're not responding on a new connection, then write to the output stream of the same socket
                responseSocket = socket;
            }
        }

        @Override
        public Throwable call() {
            Throwable t = null;
            boolean done = false;

            while (!done) {
                try {
                    RawMessage rawMessage = null;

                    /*
                     * Read from the socket's input stream. If we're keeping the
                     * connection open, then bytes will be read until the socket
                     * timeout is reached, or until an EOF marker or the ending
                     * bytes are encountered. If we're not keeping the
                     * connection open, then a socket timeout will not be
                     * silently caught, and instead will be thrown from here and
                     * cause the worker thread to abort.
                     */
                    if (connectorProperties.isDataTypeBase64()) {
                        byte[] bytes = streamHandler.read(socket.getInputStream(), connectorProperties.isKeepConnectionOpen());
                        if (bytes != null && bytes.length > 0) {
                            rawMessage = new RawMessage(bytes);
                        }
                    } else {
                        String raw = new String(streamHandler.read(socket.getInputStream(), connectorProperties.isKeepConnectionOpen()), CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding()));
                        if (StringUtils.isNotEmpty(raw)) {
                            rawMessage = new RawMessage(raw);
                        }
                    }

                    if (rawMessage != null) {
                        // Send the message to the source connector
                        synchronized (lock) {
                            MessageResponse messageResponse = null;
                            
                            try {
                                messageResponse = handleRawMessage(rawMessage);
                                
                                if (messageResponse != null && messageResponse.getResponse() != null) {
                                    try {
                                        // Send the response
                                        streamHandler.write(responseSocket.getOutputStream(), getBytes(messageResponse.getResponse().getMessage()));
                                    } catch (IOException e) {
                                        // If an error occurred while sending the response then still allow the worker to continue processing messages
                                        // TODO: Should we abort subsequent processing here instead?
                                        logger.error("Error sending response for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
                                    }
                                }
                            } catch (ChannelException e) {
                            } finally {
                                try {
                                    storeMessageResponse(messageResponse);
                                } catch (ChannelException e) {}
                            }
                        }
                    }

                    // If we're not keeping the connection open or if the remote side has already closed the connection, then stop reading
                    if (!connectorProperties.isKeepConnectionOpen() || SocketUtil.remoteSideHasClosed(socket)) {
                        done = true;
                    }
                } catch (IOException e) {
                    // If an exception occurred then abort, even if keep connection open is true
                    done = true;
                    // Set the return value and send an alert
                    t = new Exception("Error receiving message for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
                    logger.error("Error receiving message for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
                    alertController.sendAlerts(getChannelId(), Constants.ERROR_411, "Error receiving message for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
                }
            }

            // We're done reading, so close everything up
            closeSocketQuietly(socket);

            if (connectorProperties.isAckOnNewConnection()) {
                closeSocketQuietly(responseSocket);
            }

            return t;
        }
    }

    private void closeSocketQuietly(Socket socket) {
        try {
            if (socket != null) {
                logger.trace("Closing client socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".");
                socket.close();
            }
        } catch (IOException e) {
            logger.debug("Error closing client socket for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
        }
    }

    private void disposeThread(boolean interrupt) throws InterruptedException {
        if (thread != null && thread.isAlive()) {
            if (interrupt) {
                logger.trace("Interrupting thread for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".");
                thread.interrupt();
            }

            logger.trace("Joining thread for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".");
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }
    }

    /*
     * Attempts to get the result of any future tasks which may still be
     * running. The function takes two arguments: cancel and interrupt.
     * 
     * If we're canceling but not interrupting, then each future task will block
     * the thread until it returns.
     * 
     * If we're canceling and interrupting, then the task is forcefully canceled
     * and removed from the list.
     * 
     * If we're neither canceling nor interrupting, then this method simply
     * removes any future results which have completed.
     * 
     * This ensures that all client socket threads are disposed, so that a
     * remote client wouldn't be able to still send a message after a channel
     * has been stopped or undeployed (even though it wouldn't be processed
     * through the channel anyway).
     */
    private void cleanup(boolean cancel, boolean interrupt) {
        for (Iterator<Future<Throwable>> it = results.iterator(); it.hasNext();) {
            Future<Throwable> result = it.next();

            if (cancel) {
                // Cancel the task, with the option of whether or not to forcefully interrupt it
                result.cancel(interrupt);

                // If we're not interrupting the task, then attempt to get the result (which blocks until it returns)
                if (!interrupt) {
                    Throwable t = null;
                    try {
                        // If the return value is not null, then an exception was raised somewhere in the client socket thread
                        if ((t = result.get()) != null) {
                            logger.debug("Client socket thread returned unsuccessfully for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", t);
                        }
                    } catch (Exception e) {
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                        logger.debug("Error retrieving client socket thread result for " + connectorProperties.getName() + " \"Source\" on channel " + getChannel().getChannelId() + ".", e);
                    }
                }
            }

            // Remove the task from the list if it's done, or if it's been cancelled
            if (result.isDone()) {
                it.remove();
            }
        }
    }

    /*
     * Sets the socket settings using the connector properties.
     */
    private void initSocket(Socket socket) throws SocketException {
        socket.setReceiveBufferSize(parseInt(connectorProperties.getBufferSize()));
        socket.setSendBufferSize(parseInt(connectorProperties.getBufferSize()));
        socket.setSoTimeout(parseInt(connectorProperties.getTimeout()));
        socket.setKeepAlive(connectorProperties.isKeepConnectionOpen());
        socket.setReuseAddress(true);
        socket.setTcpNoDelay(true);
    }

    /*
     * Converts a string to a byte array using the connector properties to
     * determine whether or not to encode in Base64, and what charset to use.
     */
    private byte[] getBytes(String str) throws UnsupportedEncodingException {
        byte[] bytes = new byte[0];

        if (str != null) {
            if (connectorProperties.isDataTypeBase64()) {
                bytes = Base64.decodeBase64(str);
            } else {
                bytes = str.getBytes(CharsetUtils.getEncoding(connectorProperties.getCharsetEncoding()));
            }
        }

        return bytes;
    }

    /*
     * Converts a string to an integer. If the string is null or contains no
     * digits, then zero is returned.
     */
    private int parseInt(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        } else {
            String replacedStr = str.replaceAll("[^0-9]", "");
            if (StringUtils.isBlank(replacedStr)) {
                return 0;
            } else {
                return Integer.parseInt(replacedStr, 10);
            }
        }
    }

    private byte[] stringToByteArray(String str, boolean hex) {
        byte[] bytes = new byte[0];

        if (StringUtils.isNotBlank(str)) {
            if (hex) {
                String hexString = str.toUpperCase().replaceAll("(^0X)|[^0-9A-F]", "");
                if (StringUtils.isNotBlank(hexString)) {
                    bytes = (new BigInteger(hexString, 16)).toByteArray();
                }
            } else {
                bytes = str.getBytes(Charset.forName("US-ASCII"));
            }
        }

        return bytes;
    }
}
