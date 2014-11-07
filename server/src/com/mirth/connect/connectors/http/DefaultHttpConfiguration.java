/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.bio.SocketConnector;

import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.MirthSSLUtil;

public class DefaultHttpConfiguration implements HttpConfiguration {

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    @Override
    public void configureConnectorDeploy(Connector connector) throws Exception {
        if (connector instanceof HttpDispatcher) {
            String[] enabledProtocols = MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsProtocols());
            String[] enabledCipherSuites = MirthSSLUtil.getEnabledHttpsCipherSuites(configurationController.getHttpsCipherSuites());
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(SSLContexts.createDefault(), enabledProtocols, enabledCipherSuites, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ((HttpDispatcher) connector).getSocketFactoryRegistry().register("https", sslConnectionSocketFactory);
        }
    }

    @Override
    public void configureConnectorUndeploy(Connector connector) {}

    @Override
    public void configureReceiver(HttpReceiver connector) throws Exception {
        SocketConnector listener = new SocketConnector();
        listener.setHost(connector.getHost());
        listener.setPort(connector.getPort());
        listener.setMaxIdleTime(connector.getTimeout());
        connector.getServer().addConnector(listener);
    }

    @Override
    public void configureDispatcher(HttpDispatcher connector, HttpDispatcherProperties connectorProperties) throws Exception {}

    @Override
    public Map<String, Object> getRequestInformation(Request request) {
        return new HashMap<String, Object>();
    }
}