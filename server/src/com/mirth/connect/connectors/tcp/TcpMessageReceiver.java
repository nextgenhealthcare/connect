/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.apache.commons.codec.binary.Base64;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.impl.ResponseOutputStream;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.Disposable;
import org.mule.umo.lifecycle.DisposeException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.Response;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.mule.transformers.JavaScriptPostprocessor;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * <code>TcpMessageReceiver</code> acts like a tcp server to receive socket
 * requests.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 * 
 * @version $Revision: 1.23 $
 */
public class TcpMessageReceiver extends AbstractMessageReceiver implements Work {
    protected ServerSocket serverSocket = null;
    protected TcpConnector connector;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private TcpWorker work;
    private ConnectorType connectorType = ConnectorType.LISTENER;

    public TcpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
        super(connector, component, endpoint);
        TcpConnector tcpConnector = (TcpConnector) connector;
        this.connector = tcpConnector;
    }

    public void doConnect() throws ConnectException {
        disposing.set(false);
        URI uri = endpoint.getEndpointURI().getUri();
        try {
            serverSocket = createSocket(uri);
            monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
        } catch (UnknownHostException e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            throw new org.mule.providers.ConnectException(new Message("tcp", 1, uri), e, this);
        } catch (Exception e) {
            throw new org.mule.providers.ConnectException(new Message("tcp", 1, uri), e, this);
        }

        try {
            getWorkManager().scheduleWork(this, WorkManager.INDEFINITE, null, null);
        } catch (WorkException e) {
            throw new ConnectException(new Message(Messages.FAILED_TO_SCHEDULE_WORK), e, this);
        }
    }

    public void doDisconnect() throws ConnectException {
        // this will cause the server thread to quit
        disposing.set(true);
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.warn("Failed to close server socket: " + e.getMessage(), e);
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED);
        }
    }

    protected ServerSocket createSocket(URI uri) throws IOException {
        String host = uri.getHost();
        int backlog = connector.getBacklog();
        if (host == null || host.length() == 0) {
            host = "localhost";
        }
        InetAddress inetAddress = InetAddress.getByName(host);
        if (inetAddress.equals(InetAddress.getLocalHost()) || inetAddress.isLoopbackAddress() || host.trim().equals("localhost")) {
            return new ServerSocket(uri.getPort(), backlog);
        } else {
            return new ServerSocket(uri.getPort(), backlog, inetAddress);
        }
    }

    /**
     * Obtain the serverSocket
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void run() {
        while (!disposing.get()) {
            if (connector.isStarted() && !disposing.get()) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    logger.trace("Server socket Accepted on: " + serverSocket.getLocalPort());
                } catch (java.io.InterruptedIOException iie) {
                    logger.debug("Interupted IO doing serverSocket.accept: " + iie.getMessage());
                } catch (Exception e) {
                    if (!connector.isDisposed() && !disposing.get()) {
                        logger.warn("Accept failed on socket: " + e, e);
                        handleException(new ConnectException(e, this));
                    }
                }
                if (socket != null) {
                    try {
                        monitoringController.updateStatus(connector, connectorType, Event.CONNECTED, socket);
                        work = (TcpWorker) createWork(socket);
                        try {
                            getWorkManager().scheduleWork(work, WorkManager.IMMEDIATE, null, null);
                        } catch (WorkException e) {
                            logger.error("Tcp Server receiver Work was not processed: " + e.getMessage(), e);
                        }
                    } catch (SocketException e) {
                        alertController.sendAlerts(((TcpConnector) connector).getChannelId(), Constants.ERROR_411, null, e);
                        monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED, socket);
                        handleException(e);
                    }

                }
            }
        }
    }

    public void release() {}

    public void doDispose() {
        try {
            monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED);
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            serverSocket = null;
            if (work != null) {
                work.dispose();
            }
        } catch (Exception e) {
            logger.error(new DisposeException(new Message("tcp", 2), e));
        }
        logger.info("Closed Tcp port");
    }

    protected Work createWork(Socket socket) throws SocketException {
        return new TcpWorker(socket);
    }

    protected class TcpWorker implements Work, Disposable {
        protected Socket socket = null;

        protected DataInputStream dataIn;

        protected DataOutputStream dataOut;

        protected AtomicBoolean closed = new AtomicBoolean(false);

        protected TcpProtocol protocol;

        public TcpWorker(Socket socket) {
            this.socket = socket;

            final TcpConnector tcpConnector = connector;
            this.protocol = tcpConnector.getTcpProtocol();
            tcpConnector.updateReceiveSocketsCount(true);
            try {
                socket.setReceiveBufferSize(tcpConnector.getBufferSize());
                socket.setSendBufferSize(tcpConnector.getBufferSize());
                socket.setSoTimeout(tcpConnector.getReceiveTimeout());
                socket.setTcpNoDelay(true);
                socket.setKeepAlive(tcpConnector.isKeepAlive());
            } catch (SocketException e) {
                logger.error("Failed to set Socket properties: " + e.getMessage(), e);
            }

            logger.info("TCP connection from " + socket.getRemoteSocketAddress().toString() + " on port " + socket.getLocalPort());
        }

        public void release() {
            dispose();
        }

        public void dispose() {
            closed.set(true);
            try {
                if (socket != null && !socket.isClosed()) {
                    logger.debug("Closing listener: " + socket.getLocalSocketAddress().toString());
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.close();
                }
            } catch (IOException e) {
                logger.error("Socket close failed with: " + e);
            } finally {
                connector.updateReceiveSocketsCount(false);
            }
        }

        /**
         * Accept requests from a given TCP port
         */
        public void run() {
            try {
                dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                while (!socket.isClosed() && !disposing.get()) {
                    byte[] b;
                    try {
                        try {
                            b = protocol.read(dataIn);
                        } catch (SocketException e) {
                            /*
                             * If there was a socket exception, break and
                             * dispose the connection whether or not
                             * keepSendSocketOpen is true.
                             */
                            break;
                        } catch (SocketTimeoutException ste) {
                            /*
                             * No message was received because of a timeout. If
                             * keepSendSocketOpen is true then continue waiting,
                             * otherwise break.
                             */
                            if (connector.isKeepSendSocketOpen()) {
                                continue;
                            } else {
                                break;
                            }
                        }

                        if (b == null) {
                            /*
                             * The remote side of the connection was closed.
                             * Break and dispose of the connection whether or not
                             * keepSendSocketOpen is true.
                             */
                            break;
                        } else {
                            monitoringController.updateStatus(connector, connectorType, Event.BUSY, socket);
                            processData(b);
                            dataOut.flush();

                        }

                    } catch (SocketTimeoutException e) {
                        // getKeepAlive always returns true, since it is set
                        // manually.
                        if (!socket.getKeepAlive()) {
                            break;
                        }
                    } finally {
                        monitoringController.updateStatus(connector, connectorType, Event.DONE, socket);
                    }

                }
            } catch (Exception e) {
                handleException(e);
            } finally {
                monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED, socket);
                dispose();
            }
        }

        protected byte[] processData(byte[] data) throws Exception {
            if (data == null) {
                return null;
            }
            String str_data;
            // if we are receiving binary, base64 the bytes
            if (((TcpConnector) connector).isBinary()) {
                str_data = new String(Base64.encodeBase64Chunked(data));
            } else {
                String charset = connector.getCharsetEncoding();
                str_data = new String(data, charset);
            }
            UMOMessage returnMessage = null;
            OutputStream os;

            UMOMessageAdapter adapter = connector.getMessageAdapter(str_data);
            adapter.setProperty("receiverSocket", socket);
            os = new ResponseOutputStream(socket.getOutputStream(), socket);
            try {
                returnMessage = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous(), os);
                // We need to check the message status
                if (returnMessage != null && returnMessage instanceof MuleMessage) {
                    Object payload = returnMessage.getPayload();
                    if (payload instanceof MessageObject) {
                        MessageObject messageObjectResponse = (MessageObject) payload;
                        postProcessor.doPostProcess(messageObjectResponse);
                        Map responseMap = messageObjectResponse.getResponseMap();
                        String errorString = "";

                        if (connector.isResponseFromTransformer()) {
                            if (connector.isAckOnNewConnection()) {
                                String endpointURI = connector.getAckIP() + ":" + connector.getAckPort();
                                endpointURI = replacer.replaceURLValues(endpointURI, messageObjectResponse);
                                Socket socket = initSocket("tcp://" + endpointURI);
                                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                                protocol.write(bos, ((Response) responseMap.get(connector.getResponseValue())).getMessage().getBytes(connector.getCharsetEncoding()));
                                bos.flush();
                                bos.close();
                            } else {
                                protocol.write(os, ((Response) responseMap.get(connector.getResponseValue())).getMessage().getBytes(connector.getCharsetEncoding()));
                                os.flush(); // need to flush the stream to
                                            // actually commit the bytes

                                // Only close the stream (and socket) if
                                // keepSendSocketOpen is false.
                                if (!connector.isKeepSendSocketOpen()) {
                                    os.close();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw e;
            } finally {
                // Let the dispose take care of closing the socket
            }

            // The return message is always the last message routed if in a
            // batch
            // TODO: Check this for 1.2.1
            if (returnMessage != null) {
                return returnMessage.getPayloadAsBytes();
            } else {
                return null;
            }
        }

        protected Socket initSocket(String endpoint) throws IOException, URISyntaxException {
            URI uri = new URI(endpoint);
            int port = uri.getPort();
            InetAddress inetAddress = InetAddress.getByName(uri.getHost());
            Socket socket = createSocket(port, inetAddress);
            socket.setReuseAddress(true);
            socket.setReceiveBufferSize(connector.getBufferSize());
            socket.setSendBufferSize(connector.getBufferSize());
            socket.setSoTimeout(connector.getSendTimeout());
            return socket;
        }

        protected Socket createSocket(int port, InetAddress inetAddress) throws IOException {
            return new Socket(inetAddress, port);
        }
    }
}
