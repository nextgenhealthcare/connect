package com.webreach.mirth.connectors;

public interface ConnectorService {
	public abstract Object invoke(String method, Object object, String sessionsId);
}
