/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mllp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.CharArrayReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.impl.ResponseOutputStream;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.Disposable;
import org.mule.umo.lifecycle.DisposeException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.mirth.connect.connectors.mllp.protocols.LlpProtocol;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.Protocol;
import com.mirth.connect.model.Response;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.transformers.JavaScriptPostprocessor;
import com.mirth.connect.server.util.BatchMessageProcessor;
import com.mirth.connect.server.util.TemplateValueReplacer;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

public class MllpMessageReceiver extends AbstractMessageReceiver implements Work {
    private Logger logger = Logger.getLogger(this.getClass());
    
    protected ServerSocket serverSocket = null;
    protected Socket clientSocket = null;
    private char END_MESSAGE = 0x1C; // character indicating end of message
    private char START_MESSAGE = 0x0B; // first character of a new message
    private char END_OF_RECORD = 0x0D; // character sent between messages
    private char END_OF_SEGMENT = 0x0D; // character sent between hl7 segments
    private StringBuffer buffer = new StringBuffer();
    // (usually same as end of record)
    private MllpConnector connector;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private TcpWorker work;
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private ConnectorType connectorType = ConnectorType.LISTENER;

    public MllpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
        super(connector, component, endpoint);
        MllpConnector tcpConnector = (MllpConnector) connector;
        this.connector = tcpConnector;

        if (tcpConnector.getCharEncoding().equals("hex")) {
            START_MESSAGE = (char) Integer.decode(tcpConnector.getMessageStart()).intValue();
            END_MESSAGE = (char) Integer.decode(tcpConnector.getMessageEnd()).intValue();
            END_OF_RECORD = (char) Integer.decode(tcpConnector.getRecordSeparator()).intValue();
            END_OF_SEGMENT = (char) Integer.decode(tcpConnector.getSegmentEnd()).intValue();
        } else {
            // TODO: Ensure this is unit-tested
            START_MESSAGE = tcpConnector.getMessageStart().charAt(0);
            END_MESSAGE = tcpConnector.getMessageEnd().charAt(0);
            END_OF_RECORD = tcpConnector.getRecordSeparator().charAt(0);
            END_OF_SEGMENT = tcpConnector.getSegmentEnd().charAt(0);
        }
    }

    public void doConnect() throws ConnectException {
        disposing.set(false);

        if (connector.isServerMode()) {
            URI uri = endpoint.getEndpointURI().getUri();
            try {
                serverSocket = createServerSocket(uri);
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
        } else {
            // If client mode, just start up one thread.
            Thread llpReceiver = new Thread(this);
            llpReceiver.start();
            monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
        }
    }

    public void doDisconnect() throws ConnectException {
        // this will cause the server thread to quit
        disposing.set(true);
        try {
            if (connector.isServerMode()) {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } else {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to close server socket: " + e.getMessage(), e);
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED);
        }
    }

    protected ServerSocket createServerSocket(URI uri) throws IOException {
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

    protected Socket createClientSocket(URI uri) throws IOException {
        String host = uri.getHost();
        InetAddress inetAddress = InetAddress.getByName(host);
        return new Socket(inetAddress, uri.getPort());
    }

    public void run() {
        while (!disposing.get()) {
            if (connector.isStarted() && !disposing.get()) {
                Socket socket = null;

                if (connector.isServerMode()) {
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
                } else {
                    // Create the client socket. If it can't create the
                    // client socket, sleep through the reconnect interval and
                    // try again.
                    URI uri = endpoint.getEndpointURI().getUri();
                    try {
                        clientSocket = createClientSocket(uri);
                        socket = clientSocket;
                        logger.trace("Server socket Accepted on: " + clientSocket.getLocalPort());
                    } catch (Exception e) {
                        try {
                            logger.debug("Socket connection to " + uri.getHost() + ":" + uri.getPort() + " failed, waiting " + connector.getReconnectInterval() + "ms.");
                            Thread.sleep(connector.getReconnectInterval());
                        } catch (Exception ex) {
                        }
                    }
                }

                if (socket != null) {
                    try {
                        monitoringController.updateStatus(connector, connectorType, Event.CONNECTED, socket);
                        work = (TcpWorker) createWork(socket);
                        if (connector.isServerMode()) {
                            try {
                                getWorkManager().scheduleWork(work, WorkManager.IMMEDIATE, null, null);
                            } catch (WorkException e) {
                                logger.error("Tcp Server receiver Work was not processed: " + e.getMessage(), e);
                            }
                        } else {
                            work.run();
                        }
                    } catch (SocketException e) {
                        alertController.sendAlerts(((MllpConnector) connector).getChannelId(), Constants.ERROR_408, null, e);
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
            if (connector.isServerMode()) {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                serverSocket = null;
                if (work != null) {
                    work.dispose();
                }

            } else {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                clientSocket = null;
                if (work != null) {
                    work.dispose();
                }
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

        protected LlpProtocol protocol;
        
        protected Protocol inboundProtocol;

        public TcpWorker(Socket socket) {
            this.socket = socket;

            final MllpConnector tcpConnector = connector;
            this.protocol = tcpConnector.getLlpProtocol();
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
            
            try {
                inboundProtocol = channelController.getDeployedChannelById(connector.getChannelId()).getSourceConnector().getTransformer().getInboundProtocol();
            } catch (Exception e) {
                /*
                 * MIRTH-2095: If the above fails, then for some reason the
                 * channel is not in the deployed channel cache yet. Get it from
                 * the general channel cache.
                 */
                inboundProtocol = channelController.getCachedChannelById(connector.getChannelId()).getSourceConnector().getTransformer().getInboundProtocol();
            }
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
                logger.debug("Socket close failed with: " + e);
            } finally {
                connector.updateReceiveSocketsCount(false);
            }
        }

        /**
         * Accept requests from a given TCP port
         */
        public void run() {
            try {
                dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream(), connector.getBufferSize()));
                dataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                while (!socket.isClosed() && !disposing.get()) {
                    byte[] b;
                    try {
                        b = protocol.read(dataIn);
                        // end of stream
                        if (b == null) {
                            break;
                        } else {
                            monitoringController.updateStatus(connector, connectorType, Event.BUSY, socket);
                            if (connector.isWaitForEndOfMessageCharacter()) {
                                preprocessData(b);
                            } else {
                                processData(b);
                            }
                            dataOut.flush();
                        }

                    } catch (SocketTimeoutException e) {
                        if (!socket.getKeepAlive()) {
                            break;
                        }
                    } catch (ConnectException e) {
                        throw e;
                    } catch (Exception e) {
                        handleException(e);
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

        /*
         * If the option is set to wait for end charactor, the LLP listener will
         * continue accepting data until it finds one.
         */
        protected byte[] preprocessData(byte[] data) throws Exception {
            byte[] processedData = null;

            if (connector.isWaitForEndOfMessageCharacter()) {
                synchronized (buffer) {
                    String charset = connector.getCharsetEncoding();
                    String str_data = new String(data, charset);
                    buffer.append(str_data);

                    int startCharLocation = buffer.toString().indexOf(START_MESSAGE);
                    int endCharLocation = buffer.toString().indexOf(END_MESSAGE + "" + END_OF_RECORD);

                    while (startCharLocation >= 0 && endCharLocation >= 0 && startCharLocation < endCharLocation) {
                        String message = buffer.toString().substring(startCharLocation, endCharLocation);

                        try {
                            processedData = processData(message.getBytes());
                        } catch (Exception e) {
                            throw e;
                        } finally {
                            // clear the buffer up to the next message, if there
                            // is one
                            buffer.delete(startCharLocation, endCharLocation + ((String) (END_MESSAGE + "" + END_OF_RECORD)).length());

                            startCharLocation = buffer.toString().indexOf(START_MESSAGE);
                            endCharLocation = buffer.toString().indexOf(END_MESSAGE + "" + END_OF_RECORD);
                        }
                    }

                    if (buffer.length() > 0 && startCharLocation == -1 || endCharLocation > startCharLocation) {
                        // clear junk data that cannot be processed
                        buffer.delete(0, buffer.length());
                    }
                }
            } else {
                processedData = processData(data);
            }
            return processedData;
        }

        protected byte[] processData(byte[] data) throws Exception {
            String charset = connector.getCharsetEncoding();
            String str_data = new String(data, charset);
            UMOMessage returnMessage = null;
            if (connector.isProcessBatchFiles()) {
                BatchMessageProcessor batchProcessor = new BatchMessageProcessor();
                batchProcessor.setEndOfMessage((byte) END_MESSAGE);
                batchProcessor.setStartOfMessage((byte) START_MESSAGE);
                batchProcessor.setEndOfRecord((byte) END_OF_RECORD);
                Iterator<String> it = batchProcessor.processHL7Messages(str_data).iterator();
                while (it.hasNext()) {
                    returnMessage = processHL7Data(it.next(), returnMessage);
                }
            } else {
                returnMessage = processHL7Data(str_data, returnMessage);
            }
            // The return message is always the last message routed if in a
            // batch
            // TODO: Fix in 1.7!
            if (returnMessage != null) {
                return returnMessage.getPayloadAsBytes();
            } else {
                return null;
            }
        }

        private UMOMessage processHL7Data(String data, UMOMessage returnMessage) throws MessagingException, UnsupportedEncodingException, IOException, Exception {

            // Remove the SOM and EOM characters on all LLP messages.
            int startIndex = 0;
            int endIndex = data.length() - 1;

            if (data.charAt(startIndex) == START_MESSAGE) {
                startIndex++;
            }

            if (data.charAt(endIndex) == END_OF_RECORD) {
                endIndex--;
            }

            if (data.charAt(endIndex) == END_MESSAGE) {
                endIndex--;
            }

            data = data.substring(startIndex, endIndex + 1);

            OutputStream os;
            UMOMessageAdapter adapter = connector.getMessageAdapter(data);
            if (socket.isClosed()) {
                return null;
            }
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

                        if (connector.isResponseFromTransformer() && !connector.getResponseValue().equalsIgnoreCase("None")) {
                            if (connector.isAckOnNewConnection()) {
                                String endpointURI = connector.getAckIP() + ":" + connector.getAckPort();
                                Socket socket = initSocket("mllp://" + endpointURI);
                                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                                protocol.write(bos, ((Response) responseMap.get(connector.getResponseValue())).getMessage().getBytes(connector.getCharsetEncoding()));
                                bos.flush();
                                bos.close();
                            } else {
                                protocol.write(os, ((Response) responseMap.get(connector.getResponseValue())).getMessage().getBytes(connector.getCharsetEncoding()));
                            }
                        } else {
                            // we only want the first line
                            if (messageObjectResponse.getStatus().equals(MessageObject.Status.ERROR) && messageObjectResponse.getErrors() != null) {
                                if (messageObjectResponse.getErrors().indexOf('\n') > -1) {
                                    errorString = messageObjectResponse.getErrors().substring(0, messageObjectResponse.getErrors().indexOf('\n'));
                                } else {
                                    errorString = messageObjectResponse.getErrors();
                                }
                            }
                            generateACK(data, os, messageObjectResponse.getStatus(), errorString, messageObjectResponse);
                        }
                    }
                } else {
                    generateACK(data, os, MessageObject.Status.RECEIVED, new String(), null);
                }
            } catch (Exception e) {
                generateACK(data, os, MessageObject.Status.ERROR, e.getMessage(), null);
                throw e;
            } finally {
                // Let the dispatcher take care of closing the socket +
                // stream
            }
            return returnMessage;
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

        private void generateACK(String message, OutputStream os, MessageObject.Status status, String error, MessageObject messageObject) throws Exception, IOException {
            boolean errorOnly = false;
            boolean always = false;
            boolean successOnly = false;
            if (error == null) {
                error = "";
            }

            // Check if we want to send ACKs at all.
            if (connector.getSendACK()) {
                // Check if we have to look at MSH15
                if (connector.isCheckMSH15()) {
                    // MSH15 Dictionary:
                    // AL: Always
                    // NE: Never
                    // ER: Error / Reject condition
                    // SU: Successful completion only

                    message = message.trim();

                    String msh15 = "";

                    // Check if the message is ER7 or XML
                    if (inboundProtocol.equals(Protocol.XML)) { // XML form
                        XPath xpath = XPathFactory.newInstance().newXPath();
                        XPathExpression msh15Query = xpath.compile("//MSH.15/text()");
                        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = domFactory.newDocumentBuilder();
                        Reader reader = new CharArrayReader(message.toCharArray());
                        Document doc = builder.parse(new InputSource(reader));
                        msh15 = msh15Query.evaluate(doc);
                    } else { // ER7
                        char segmentDelim = '\r';
                        char fieldDelim = message.charAt(3); // Usually |
                        char componentDelim = message.charAt(4); // Usually ^

                        Pattern fieldPattern = Pattern.compile(Pattern.quote(String.valueOf(fieldDelim)));
                        Pattern componentPattern = Pattern.compile(Pattern.quote(String.valueOf(componentDelim)));
                        String mshString = message.substring(0, message.indexOf(String.valueOf(segmentDelim)));
                        String[] mshFields = fieldPattern.split(mshString);

                        if (mshFields.length > 14) {
                            msh15 = componentPattern.split(mshFields[14])[0]; // MSH.15.1
                        }
                    }

                    if (msh15 != null && !msh15.equals("")) {
                        if (msh15.equalsIgnoreCase("AL")) {
                            always = true;
                        } else if (msh15.equalsIgnoreCase("NE")) {
                            logger.debug("MSH15 is NE, Skipping ACK");
                            return;
                        } else if (msh15.equalsIgnoreCase("ER")) {
                            errorOnly = true;

                        } else if (msh15.equalsIgnoreCase("SU")) {
                            successOnly = true;
                        }
                    }
                }
                String ackCode = "AA";
                String textMessage = "";
                if (status.equals(MessageObject.Status.ERROR)) {
                    if (successOnly) {
                        // we only send an ACK on success
                        return;
                    }
                    ackCode = connector.getAckCodeError();
                    String conError = connector.getAckMsgError();
                    textMessage = conError.replaceFirst("\\$\\{ERROR\\}", error);
                } else if (status.equals(MessageObject.Status.FILTERED)) {
                    if (successOnly) {
                        return;
                    }
                    ackCode = connector.getAckCodeRejected();
                    textMessage = connector.getAckMsgRejected();
                } else {
                    if (errorOnly) {
                        return;
                    }
                    ackCode = connector.getAckCodeSuccessful();
                    textMessage = connector.getAckMsgSuccessful();
                }
                // MIRTH-435
                if (textMessage.indexOf('$') > -1) {
                    textMessage = replacer.replaceValues(textMessage, messageObject);
                }

                String ACK = new ACKGenerator().generateAckResponse(message.trim(), inboundProtocol, ackCode, textMessage);

                logger.debug("Sending ACK: " + ACK);
                try {
                    if (connector.isAckOnNewConnection()) {
                        String endpointURI = connector.getAckIP() + ":" + connector.getAckPort();
                        endpointURI = replacer.replaceURLValues(endpointURI, messageObject);
                        Socket socket = initSocket("mllp://" + endpointURI);
                        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                        protocol.write(bos, ACK.getBytes((connector).getCharsetEncoding()));
                        bos.flush();
                    } else {
                        protocol.write(os, ACK.getBytes((connector).getCharsetEncoding()));
                    }
                } catch (Throwable t) {
                    logger.error("Can't write ACK to the sender\n" + ExceptionUtils.getStackTrace(t));
                }
            }
        }
    }
}
