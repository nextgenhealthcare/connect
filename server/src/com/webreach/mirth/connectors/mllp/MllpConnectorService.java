package com.webreach.mirth.connectors.mllp;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;

import com.webreach.mirth.connectors.ConnectorService;

public class MllpConnectorService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            Map<String, String> params = (Map<String, String>) object;
            String host = params.get(LLPSenderProperties.LLP_ADDRESS);
            int port = Integer.parseInt(params.get(LLPSenderProperties.LLP_PORT));
            int timeout = Integer.parseInt(params.get(LLPSenderProperties.LLP_SERVER_TIMEOUT));
            Socket socket = null;

            try {
                InetSocketAddress address = new InetSocketAddress(host, port);
                socket = new Socket();
                socket.connect(address, timeout);
                return "Sucessfully connected to host.";
            } catch (SocketTimeoutException ste) {
                return "Timed out connecting to host.";
            } catch (Exception e) {
                return "Could not connect to host.";
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }

        return null;
    }
}
