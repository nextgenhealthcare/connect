package com.webreach.mirth.connectors;


public interface ConnectorService {
	public Object invoke(String method, Object object, String sessionsId) throws Exception;
}
