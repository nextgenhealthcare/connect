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
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.CharsetUtils;

public class TcpDispatcher extends DestinationConnector {
    public static final int KEEP_RETRYING_INDEFINITELY = 100;

    private Logger logger = Logger.getLogger(this.getClass());
    protected TcpDispatcherProperties connectorProperties;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private StateAwareSocket socket;
    private Thread thread;

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage connectorMessage) {
        TcpDispatcherProperties tcpSenderProperties = (TcpDispatcherProperties) SerializationUtils.clone(connectorProperties);

        tcpSenderProperties.setHost(replacer.replaceValues(tcpSenderProperties.getHost(), connectorMessage));
        tcpSenderProperties.setPort(replacer.replaceValues(tcpSenderProperties.getPort(), connectorMessage));
        tcpSenderProperties.setTemplate(replacer.replaceValues(tcpSenderProperties.getTemplate(), connectorMessage));

        return tcpSenderProperties;
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) {
        TcpDispatcherProperties tcpSenderProperties = (TcpDispatcherProperties) connectorProperties;
        String responseData = null;
        Status responseStatus = Status.QUEUED;

        // If keep connection open is true, then interrupt the thread so it won't close the socket
        if (tcpSenderProperties.isKeepConnectionOpen() && thread != null) {
            thread.interrupt();
        }

        StreamHandler streamHandler = new StreamHandler();
        streamHandler.setBeginBytes(stringToByteArray(tcpSenderProperties.getBeginBytes(), tcpSenderProperties.isFrameEncodingHex()));
        streamHandler.setEndBytes(stringToByteArray(tcpSenderProperties.getEndBytes(), tcpSenderProperties.isFrameEncodingHex()));

        int retryCount = -1;
        int maxRetryCount = parseInt(tcpSenderProperties.getMaxRetryCount());

        // Keep retrying if the response status isn't SENT and the retry count hasn't hit the maximum
        while (responseStatus != Status.SENT && (retryCount < maxRetryCount)) {
            if (maxRetryCount != KEEP_RETRYING_INDEFINITELY) {
                retryCount++;
            }

            try {
                // Initialize a new socket if our current one is invalid, the remote side has closed, or keep connection open is false
                if (socket == null || socket.isClosed() || socket.remoteSideHasClosed() || !tcpSenderProperties.isKeepConnectionOpen()) {
                    closeSocketQuietly();
                    logger.debug("Creating new socket for " + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ".");
                    socket = new StateAwareSocket(tcpSenderProperties.getHost(), parseInt(tcpSenderProperties.getPort()));
                    socket.setReuseAddress(true);
                    socket.setReceiveBufferSize(parseInt(tcpSenderProperties.getBufferSize()));
                    socket.setSendBufferSize(parseInt(tcpSenderProperties.getBufferSize()));
                    socket.setSoTimeout(parseInt(tcpSenderProperties.getResponseTimeout()));
                    socket.setKeepAlive(tcpSenderProperties.isKeepConnectionOpen());
                }

                // Send the message
                streamHandler.write(socket.getOutputStream(), getTemplateBytes(tcpSenderProperties));
                responseStatus = Status.SENT;

                try {
                    if (!tcpSenderProperties.isIgnoreResponse()) {
                        // Attempt to get the response from the remote endpoint
                        try {
                            responseData = new String(streamHandler.read(socket.getInputStream()));
                        } catch (SocketTimeoutException e) {
                            responseData = "Timeout waiting for response.";
                        }
                    }

                    if (tcpSenderProperties.isKeepConnectionOpen()) {
                        // Close the connection after the send timeout has been reached
                        startThread();
                    } else {
                        // If keep connection open is false, then close the socket right now
                        closeSocketQuietly();
                    }
                } catch (IOException e) {
                    // Return an ERROR response if an exception occurred while retrieving the response
                    responseData = e.getMessage();
                    responseStatus = Status.ERROR;
                    closeSocketQuietly();
                }
            } catch (Exception e) {
                // If an exception occurred then close the socket, even if keep connection open is true
                closeSocketQuietly();

                // Only handle the exception if we won't be retrying again
                if (retryCount >= maxRetryCount || e instanceof UnsupportedEncodingException) {
                    responseData = e.getMessage();

                    // Leave the response status as QUEUED for a ConnectException, otherwise ERROR
                    if ((e.getClass() == ConnectException.class) || ((e.getCause() != null) && (e.getCause().getClass() == ConnectException.class))) {
                        alertController.sendAlerts(getChannelId(), Constants.ERROR_411, "Connection refused.", e);
                    } else {
                        responseStatus = Status.ERROR;
                        alertController.sendAlerts(getChannelId(), Constants.ERROR_411, "Error sending message via TCP.", e);
                    }

                    retryCount = maxRetryCount;
                } else {
                    // Since we're retrying, wait the specified amount of time
                    try {
                        Thread.sleep(parseInt(tcpSenderProperties.getReconnectInterval()));
                    } catch (InterruptedException e2) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return new Response(responseStatus, responseData);
    }

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (TcpDispatcherProperties) getConnectorProperties();
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {
        // Interrupt and join the connector thread
        try {
            disposeThread(true);
        } catch (InterruptedException e) {
            throw new StopException("Thread join operation interrupted for " + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ".", e);
        }

        // Close the connector client socket
        try {
            closeSocket();
        } catch (IOException e) {
            throw new StopException("Error closing socket for " + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ".", e);
        }
    }

    private void closeSocketQuietly() {
        try {
            closeSocket();
        } catch (IOException e) {
            logger.debug("Error closing socket for " + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ".", e);
        }
    }

    private void closeSocket() throws IOException {
        if (socket != null) {
            logger.trace("Closing socket for " + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ".");
            socket.close();
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
            logger.warn("Thread join operation interrupted for " + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ".", e);
        }
    }

    private void disposeThread(boolean interrupt) throws InterruptedException {
        if (thread != null && thread.isAlive()) {
            if (interrupt) {
                logger.trace("Interrupting thread for " + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ".");
                thread.interrupt();
            }

            logger.trace("Joining thread for " + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ".");
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
            if (tcpSenderProperties.isDataTypeBase64()) {
                bytes = Base64.decodeBase64(tcpSenderProperties.getTemplate());
            } else {
                bytes = tcpSenderProperties.getTemplate().getBytes(CharsetUtils.getEncoding(tcpSenderProperties.getCharsetEncoding()));
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
