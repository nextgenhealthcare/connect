/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;

import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.server.channel.Connector;

public interface HttpConfiguration {

    public void configureConnectorDeploy(Connector connector) throws Exception;

    public void configureConnectorUndeploy(Connector connector);

    public void configureReceiver(HttpReceiver connector) throws Exception;

    public void configureDispatcher(HttpDispatcher connector, HttpDispatcherProperties connectorProperties) throws Exception;

    public void configureSocketFactoryRegistry(ConnectorPluginProperties properties, RegistryBuilder<ConnectionSocketFactory> registry) throws Exception;

    public Map<String, Object> getRequestInformation(ServletRequest request);
}