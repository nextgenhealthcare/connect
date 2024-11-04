package com.mirth.connect.connectors.core.ws;

import com.mirth.connect.donkey.server.channel.ISourceConnector;
import com.sun.net.httpserver.HttpServer;

public interface IWebServiceReceiver extends ISourceConnector {

	public void setServer(HttpServer server);
	
	public Map<String, List<String>> getHttpHeaders();
}
