package com.mirth.connect.connectors.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketImpl;

/**
 * An extension of ServerSocket that returns a StateAwareSocket from accept().
 */
public class StateAwareServerSocket extends ServerSocket {

    public StateAwareServerSocket() throws IOException {
        super();
    }

    public StateAwareServerSocket(int port) throws IOException {
        super(port);
    }

    public StateAwareServerSocket(int port, int backlog) throws IOException {
        super(port, backlog);
    }

    public StateAwareServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        super(port, backlog, bindAddr);
    }

    @Override
    public StateAwareSocket accept() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        if (!isBound()) {
            throw new SocketException("Socket is not bound yet");
        }
        StateAwareSocket s = new StateAwareSocket((SocketImpl) null);
        implAccept(s);
        return s;
    }
}
