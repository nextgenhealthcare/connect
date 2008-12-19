package com.webreach.mirth.server.util;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.webreach.mirth.util.ConnectionTestResponse;

public class ConnectorUtil {
    public static ConnectionTestResponse testConnection(String host, int port, int timeout) throws Exception {
        Socket socket = null;
        InetSocketAddress address = null;
        ConnectionTestResponse response = null;

        try {
            address = new InetSocketAddress(host, port);
        } catch (Exception e) {
            response = ConnectionTestResponse.FAILURE;
            response.setMessage("Invalid host or port.");
            return response;
        }

        try {
            socket = new Socket();
            socket.connect(address, timeout);
            response = ConnectionTestResponse.SUCCESS;
            response.setMessage(("Sucessfully connected to host: " + address.getAddress().getHostAddress() + ":" + address.getPort()));
            return response;
        } catch (SocketTimeoutException ste) {
            response = ConnectionTestResponse.TIME_OUT;
            response.setMessage("Timed out connecting to host: " + address.getAddress().getHostAddress() + ":" + address.getPort());
            return response;
        } catch (Exception e) {
            response = ConnectionTestResponse.FAILURE;
            response.setMessage("Could not connect to host: " + address.getAddress().getHostAddress() + ":" + address.getPort());
            return response;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
