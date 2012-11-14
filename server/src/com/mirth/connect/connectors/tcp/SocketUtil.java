package com.mirth.connect.connectors.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.mirth.connect.util.TcpUtil;

public class SocketUtil {

    public static StateAwareSocket createSocket(String host, String port) throws UnknownHostException, IOException {
        return createSocket(host, TcpUtil.parseInt(port));
    }

    public static StateAwareSocket createSocket(String host, int port) throws UnknownHostException, IOException {
        StateAwareSocket socket = new StateAwareSocket();
        socket.connect(new InetSocketAddress(InetAddress.getByName(TcpUtil.getFixedHost(host)), port));
        return socket;
    }

    public static void closeSocket(StateAwareSocket socket) throws IOException {
        if (socket != null) {
            try {
                socket.shutdownInput();
            } catch (IOException e) {
            }
            try {
                socket.shutdownOutput();
            } catch (IOException e) {
            }
            socket.close();
        }
    }
}
