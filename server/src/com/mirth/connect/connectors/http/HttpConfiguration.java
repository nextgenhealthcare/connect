package com.mirth.connect.connectors.http;

import org.mortbay.http.HttpServer;
import org.mule.umo.endpoint.UMOEndpoint;

public interface HttpConfiguration {

    /**
     * Configures the settings for the specified HTTP connector.
     * 
     * @param connector the HTTP connector to configure
     */
    public void configureConnector(HttpConnector connector);
    
    public void configureReceiver(HttpServer server, UMOEndpoint endpoint) throws Exception;
    
    public void configureDispatcher();
}
