package com.webreach.mirth.connectors.mllp;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import com.webreach.mirth.connectors.ConnectorService;

public class MllpConnectorService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            Map<String, String> params = (Map<String, String>) object;
            String host = params.get(LLPSenderProperties.LLP_ADDRESS);
            int port = Integer.parseInt(params.get(LLPSenderProperties.LLP_PORT));
            int timeout = Integer.parseInt(params.get(LLPSenderProperties.LLP_SERVER_TIMEOUT));
            
            InetSocketAddress address = null;
            Socket socket = null;
            
            try {
                address = new InetSocketAddress(host, port);
                socket = new Socket();
                socket.connect(address, timeout);
                return "Sucessfully connected to host: " + address.toString();
            } catch (Exception e) {
                if (address != null) {
                    return "Could not connect to host: " + address.toString();
                } else {
                    return "Could not connect to host.";
                }
            } finally {
                if (socket != null) {
                    socket.close();    
                }
            }
        }

        return null;
    }
}
