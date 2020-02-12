/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.cloudsolutions.ssl.SSLUtils;
import com.mirth.connect.donkey.server.channel.Connector;

public class DefaultTcpConfigurationSecure implements TcpConfiguration {

	SSLServerSocketFactory ssf; // Server Factory
	SSLSocketFactory scf; // Client Factory

	public DefaultTcpConfigurationSecure() {
		try {
			SSLContext ctx = SSLUtils.getATNAContext();
			ssf = (SSLServerSocketFactory) ctx.getServerSocketFactory();
			scf = (SSLSocketFactory) ctx.getSocketFactory();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void configureConnectorDeploy(Connector connector) throws Exception {
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		return ssf.createServerSocket(port, backlog);
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
		return ssf.createServerSocket(port, backlog, bindAddr);
	}

	@Override
	public Socket createSocket() throws IOException {
		return scf.createSocket();
	}

	@Override
	public Socket createResponseSocket() throws IOException {
		return scf.createSocket();
	}

	@Override
	public Map<String, Object> getSocketInformation(Socket socket) {
		return new HashMap<String, Object>();
	}
}