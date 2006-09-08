package com.webreach.mirth.client.core;

public class ServerConnectionFactory {
	public static ServerConnection createServerConnection(String address) {
		return new ServerConnection(address);
	}
}
