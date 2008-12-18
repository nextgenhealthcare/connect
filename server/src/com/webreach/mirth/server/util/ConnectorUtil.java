package com.webreach.mirth.server.util;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ConnectorUtil {
    public static String testConnection(String host, int port, int timeout) throws Exception {
        Socket socket = null;
        InetSocketAddress address = null;

        try {
            address = new InetSocketAddress(host, port);
        } catch (Exception e) {
            return "Invalid host or port.";
        }

        try {
            socket = new Socket();
            socket.connect(address, timeout);
            return "Sucessfully connected to host: " + address.getAddress().getHostAddress() + ":" + address.getPort();
        } catch (SocketTimeoutException ste) {
            return "Timed out connecting to host: " + address.getAddress().getHostAddress() + ":" + address.getPort();
        } catch (Exception e) {
            return "Could not connect to host: " + address.getAddress().getHostAddress() + ":" + address.getPort();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
