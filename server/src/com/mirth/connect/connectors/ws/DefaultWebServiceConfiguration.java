/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;

import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.MirthSSLUtil;
import com.sun.net.httpserver.HttpServer;

public class DefaultWebServiceConfiguration implements WebServiceConfiguration {

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private SSLContext sslContext;
    private String[] enabledProtocols;
    private String[] enabledCipherSuites;

    @Override
    public void configureConnectorDeploy(Connector connector) throws Exception {
        if (connector instanceof WebServiceDispatcher) {
            sslContext = SSLContexts.createDefault();
            enabledProtocols = MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsProtocols());
            enabledCipherSuites = MirthSSLUtil.getEnabledHttpsCipherSuites(configurationController.getHttpsCipherSuites());
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, enabledProtocols, enabledCipherSuites, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ((WebServiceDispatcher) connector).getSocketFactoryRegistry().register("https", sslConnectionSocketFactory);
        }
    }

    @Override
    public void configureConnectorUndeploy(Connector connector) {}

    @Override
    public void configureReceiver(WebServiceReceiver connector) throws Exception {
        connector.setServer(HttpServer.create());
    }

    @Override
    public void configureDispatcher(WebServiceDispatcher connector, WebServiceDispatcherProperties connectorProperties, Map<String, Object> requestContext) throws Exception {
        requestContext.put("com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory", new SSLSocketFactoryWrapper(sslContext.getSocketFactory(), enabledProtocols, enabledCipherSuites));
    }
}