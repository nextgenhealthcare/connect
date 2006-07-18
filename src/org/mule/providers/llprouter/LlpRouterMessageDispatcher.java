/*
 * $Header: /home/projects/mule/scm/mule/providers/tcp/src/java/org/mule/providers/tcp/TcpMessageDispatcher.java,v 1.12 2005/11/05 12:23:27 aperepel Exp $
 * $Revision: 1.12 $
 * $Date: 2005/11/05 12:23:27 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.llprouter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.Utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * <code>TcpMessageDispatcher</code> will send transformed mule events over
 * tcp.
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 *
 * @version $Revision: 1.12 $
 */

public class LlpRouterMessageDispatcher extends AbstractMessageDispatcher
{
    /////////////////////////////////////////////////////////////////
    // keepSocketOpen option variables
    /////////////////////////////////////////////////////////////////

    protected Socket connectedSocket = null;

    /////////////////////////////////////////////////////////////////
    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(LlpRouterMessageDispatcher.class);

    private LlpRouterConnector connector;

    public LlpRouterMessageDispatcher(LlpRouterConnector connector)
    {
        super(connector);
        this.connector = connector;
    }

    protected Socket initSocket(String endpoint) throws IOException, URISyntaxException
    {
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

    public void doDispatch(UMOEvent event) throws Exception
    {
        Socket socket = null;
        try {
            Object payload = event.getTransformedMessage();
            socket = initSocket(event.getEndpoint().getEndpointURI().getAddress());
            write(socket, payload);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    protected Socket createSocket(int port, InetAddress inetAddress) throws IOException
    {
        return new Socket(inetAddress, port);
    }

    protected void write(Socket socket, Object data) throws IOException
    {
        TcpProtocol protocol = connector.getTcpProtocol();
        byte[] binaryData;
        if (data instanceof String) {
            binaryData = data.toString().getBytes();
        } else if (data instanceof byte[]) {
            binaryData = (byte[]) data;
        } else {
            binaryData = Utility.objectToByteArray(data);
        }
        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
        protocol.write(bos, binaryData);
        bos.flush();
    }

    public UMOMessage doSend(UMOEvent event) throws Exception
    {
        try {
            Object payload = event.getTransformedMessage();

            if (!connector.isKeepSendSocketOpen()) {
                connectedSocket = initSocket(event.getEndpoint().getEndpointURI().getAddress());
            } else {
                reconnect(event, connector.getMaxRetryCount());
            }

            try {
                write(connectedSocket, payload);
                // If we're doing sync receive try and read return info from socket
            }
            catch (IOException e) {
                if (connector.isKeepSendSocketOpen()) {
                    logger.warn("Write raised exception: '" + e.getMessage() + "' attempting to reconnect.");

                    doDispose();

                    if (reconnect(event, connector.getMaxRetryCount()))
                        write(connectedSocket, payload);
                } else {
                    throw e;
                }
            }

            if (useRemoteSync(event)) {
                try {
                    byte[] result = receive(connectedSocket, event.getEndpoint().getRemoteSyncTimeout());
                    if (result == null) {
                        return null;
                    }
                    return new MuleMessage(connector.getMessageAdapter(result));
                }
                catch (SocketTimeoutException e) {
                    // we don't necessarily expect to receive a response here
                    logger.info("Socket timed out normally while doing a synchronous receive on endpointUri: "
                            + event.getEndpoint().getEndpointURI());
                    return null;
                }
            } else {
                return event.getMessage();
            }
        }
        finally {
            if (!connector.isKeepSendSocketOpen()) {
                doDispose();
            }
        }
    }

    protected byte[] receive(Socket socket, int timeout) throws IOException
    {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        if (timeout >= 0) {
            socket.setSoTimeout(timeout);
        }
        return connector.getTcpProtocol().read(dis);
    }

    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception
    {
        Socket socket = null;
        try {
            socket = initSocket(endpointUri.getAddress());
            try {
                byte[] result = receive(socket, (int) timeout);
                if (result == null) {
                    return null;
                }
                UMOMessage message = new MuleMessage(connector.getMessageAdapter(result));
                return message;
            } catch (SocketTimeoutException e) {
                // we dont necesarily expect to receive a resonse here
                logger.info("Socket timed out normally while doing a synchronous receive on endpointUri: "
                        + endpointUri);
                return null;
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public Object getDelegateSession() throws UMOException
    {
        return null;
    }

    public UMOConnector getConnector()
    {
        return connector;
    }

    public void doDispose()
    {
        if (null != connectedSocket && !connectedSocket.isClosed()) {
            try {
                connectedSocket.close();

                connectedSocket = null;
            }
            catch (IOException e) {
                logger.warn("ConnectedSocked close raised exception. Reason: " + e.getMessage());
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    // New keepSocketOpen option methods by P.Oikari
    /////////////////////////////////////////////////////////////////
    public boolean reconnect(UMOEvent event, int maxRetries) throws Exception
    {
        if (null != connectedSocket) {
            // We already have a connected socket
            return true;
        }

        boolean success = false;

        int retryCount = -1;

        while (!success && !disposed && (retryCount < maxRetries)) {
            try {
                connectedSocket = initSocket(event.getEndpoint().getEndpointURI().getAddress());

                success = true;

                connector.setSendSocketValid(true);
            }
            catch (Exception e) {
                success = false;

                connector.setSendSocketValid(false);

                if (maxRetries != LlpRouterConnector.KEEP_RETRYING_INDEFINETLY) {
                    retryCount++;
                }

                logger.warn("run() warning at host: '" + event.getEndpoint().getEndpointURI().getAddress() +
                            "'. Reason: " + e.getMessage());

                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(connector.getReconnectMillisecs());
                    }
                    catch (Exception ex) {
                        logger.warn("SocketConnector threadsleep interrupted. Reason: " + ex.getMessage());
                    }
                } else {
                    throw e;
                }
            }
        }

        return (success);
    }
}
