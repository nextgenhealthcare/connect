/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.commons.lang.StringUtils;

import com.mirth.connect.util.ConnectionTestResponse;

public class ConnectorUtil {
    public static ConnectionTestResponse testConnection(String host, int port, int timeout) throws Exception {
        Socket socket = null;
        InetSocketAddress address = null;

        try {
            address = new InetSocketAddress(host, port);
            
            if (StringUtils.isBlank(address.getAddress().getHostAddress()) || (address.getPort() < 0) || (address.getPort() > 65534)) {
                throw new Exception();
            }
        } catch (Exception e) {
            return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Invalid host or port.");
        }

        try {
            socket = new Socket();
            socket.connect(address, timeout);
            return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to host: " + address.getAddress().getHostAddress() + ":" + address.getPort());
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
