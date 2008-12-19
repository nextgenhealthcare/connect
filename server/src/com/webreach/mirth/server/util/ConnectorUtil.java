package com.webreach.mirth.server.util;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.webreach.mirth.util.ConnectionTestResponse;

public class ConnectorUtil {
    public static ConnectionTestResponse testConnection(String host, int port, int timeout) throws Exception {
        Socket socket = null;
        InetSocketAddress address = null;

        try {
            address = new InetSocketAddress(host, port);
        } catch (Exception e) {
            return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Invalid host or port.");
        }

        try {
            socket = new Socket();
            socket.connect(address, timeout);
            return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully connected to host: " + address.getAddress().getHostAddress() + ":" + address.getPort());
        } catch (SocketTimeoutException ste) {
            return new ConnectionTestResponse(ConnectionTestResponse.Type.TIME_OUT, "Timed out connecting to host: " + address.getAddress().getHostAddress() + ":" + address.getPort());
        } catch (Exception e) {
            return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Could not connect to host: " + address.getAddress().getHostAddress() + ":" + address.getPort());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
