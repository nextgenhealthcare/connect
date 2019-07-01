/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLSocketFactoryWrapper extends SSLSocketFactory {

    private SSLSocketFactory delegate;
    private String[] enabledProtocols;
    private String[] enabledCipherSuites;

    public SSLSocketFactoryWrapper(SSLSocketFactory delegate, String[] enabledProtocols, String[] enabledCipherSuites) {
        this.delegate = delegate;
        this.enabledProtocols = enabledProtocols;
        this.enabledCipherSuites = enabledCipherSuites;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return initSocket(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return initSocket(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return initSocket(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return initSocket(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return initSocket(delegate.createSocket(address, port, localAddress, localPort));
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return enabledCipherSuites;
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return enabledCipherSuites;
    }

    private Socket initSocket(Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(enabledProtocols);
            ((SSLSocket) socket).setEnabledCipherSuites(enabledCipherSuites);
        }
        return socket;
    }
}