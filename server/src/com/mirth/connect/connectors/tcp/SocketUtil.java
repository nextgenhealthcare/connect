package com.mirth.connect.connectors.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.util.TcpUtil;

public class SocketUtil {

    public static StateAwareSocket createSocket(String host, String port) throws UnknownHostException, IOException {
        return createSocket(host, TcpUtil.parseInt(port));
    }

    public static StateAwareSocket createSocket(String host, int port) throws UnknownHostException, IOException {
        return createSocket(host, port, "127.0.0.1");
    }

    public static StateAwareSocket createSocket(String host, String port, String localAddr) throws UnknownHostException, IOException {
        return createSocket(host, TcpUtil.parseInt(port), localAddr);
    }

    public static StateAwareSocket createSocket(String host, int port, String localAddr) throws UnknownHostException, IOException {
        return createSocket(host, port, localAddr, 0);
    }

    public static StateAwareSocket createSocket(String host, int port, String localAddr, String localPort) throws UnknownHostException, IOException {
        return createSocket(host, port, localAddr, TcpUtil.parseInt(localPort));
    }

    public static StateAwareSocket createSocket(String host, String port, String localAddr, int localPort) throws UnknownHostException, IOException {
        return createSocket(host, TcpUtil.parseInt(port), localAddr, localPort);
    }

    public static StateAwareSocket createSocket(String host, String port, String localAddr, String localPort) throws UnknownHostException, IOException {
        return createSocket(host, TcpUtil.parseInt(port), localAddr, TcpUtil.parseInt(localPort));
    }

    /**
     * Creates a socket and connects it to the specified remote host on the
     * specified remote port.
     * 
     * @param host
     *            - The remote host to connect on.
     * @param port
     *            - The remote port to connect on.
     * @param localAddr
     *            - The local address to bind the socket to.
     * @param localPort
     *            - The local port to bind the socket to.
     * @return The bound and connected StateAwareSocket.
     * @throws UnknownHostException
     *             if the IP address of the host could not be determined
     * @throws IOException
     *             if an I/O error occurs when creating the socket
     */
    public static StateAwareSocket createSocket(String host, int port, String localAddr, int localPort) throws UnknownHostException, IOException {
        StateAwareSocket socket = new StateAwareSocket();
        InetAddress localAddress = null;

        if (StringUtils.isNotEmpty(localAddr)) {
            localAddress = InetAddress.getByName(TcpUtil.getFixedHost(localAddr));
        }

        socket.bind(new InetSocketAddress(localAddress, localPort));
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
