/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import org.eclipse.jetty.server.Server;
import org.mule.umo.endpoint.UMOEndpoint;

public interface HttpConfiguration {

    /**
     * Configures the settings for the specified HTTP connector.
     * 
     * @param connector the HTTP connector to configure
     */
    public void configureConnector(HttpConnector connector);
    
    public void configureReceiver(Server server, UMOEndpoint endpoint, int timeout) throws Exception;
    
    public void configureDispatcher();
}
