package com.webreach.mirth.connectors.soap.axis.transport.http;

import org.mule.providers.http.HttpConnector;

public class SoapConnector extends HttpConnector {
	/**
     * @see org.mule.umo.provider.UMOConnector#getProtocol()
     */
    public String getProtocol()
    {
        return "soap";
    }
}
