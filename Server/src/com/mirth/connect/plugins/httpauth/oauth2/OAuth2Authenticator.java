/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.oauth2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.plugins.httpauth.AuthenticationResult;
import com.mirth.connect.plugins.httpauth.Authenticator;
import com.mirth.connect.plugins.httpauth.RequestInfo;
import com.mirth.connect.plugins.httpauth.oauth2.OAuth2HttpAuthProperties.TokenLocation;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.HttpUtil;

public class OAuth2Authenticator extends Authenticator {

    private static final int SOCKET_TIMEOUT = 30000;

    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private OAuth2AuthenticatorProvider provider;

    public OAuth2Authenticator(OAuth2AuthenticatorProvider provider) {
        this.provider = provider;
    }

    @Override
    public AuthenticationResult authenticate(RequestInfo request) throws Exception {
        OAuth2HttpAuthProperties properties = getReplacedProperties(request);

        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try {
            // Create and configure the client and context 
            RegistryBuilder<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.getSocketFactory());
            ConnectorPluginProperties pluginProperties = null;
            if (CollectionUtils.isNotEmpty(properties.getConnectorPluginProperties())) {
                pluginProperties = properties.getConnectorPluginProperties().iterator().next();
            }
            provider.getHttpConfiguration().configureSocketFactoryRegistry(pluginProperties, socketFactoryRegistry);
            BasicHttpClientConnectionManager httpClientConnectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry.build());
            httpClientConnectionManager.setSocketConfig(SocketConfig.custom().setSoTimeout(SOCKET_TIMEOUT).build());
            HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(httpClientConnectionManager);
            HttpUtil.configureClientBuilder(clientBuilder);
            client = clientBuilder.build();

            HttpClientContext context = HttpClientContext.create();
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(SOCKET_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).setStaleConnectionCheckEnabled(true).build();
            context.setRequestConfig(requestConfig);

            URIBuilder uriBuilder = new URIBuilder(properties.getVerificationURL());

            // Add query parameters
            if (properties.getTokenLocation() == TokenLocation.QUERY) {
                List<String> paramList = request.getQueryParameters().get(properties.getLocationKey());
                if (CollectionUtils.isNotEmpty(paramList)) {
                    for (String value : paramList) {
                        uriBuilder.addParameter(properties.getLocationKey(), value);
                    }
                }
            }

            // Build the final URI and create a GET request
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            // Add headers
            if (properties.getTokenLocation() == TokenLocation.HEADER) {
                List<String> headerList = request.getHeaders().get(properties.getLocationKey());
                if (CollectionUtils.isNotEmpty(headerList)) {
                    for (String value : headerList) {
                        httpGet.addHeader(properties.getLocationKey(), value);
                    }
                }
            }

            // Execute the request
            response = client.execute(httpGet, context);

            // Determine authentication from the status code 
            if (response.getStatusLine().getStatusCode() < 400) {
                return AuthenticationResult.Success();
            } else {
                return AuthenticationResult.Failure();
            }
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(client);
        }
    }

    private OAuth2HttpAuthProperties getReplacedProperties(RequestInfo request) {
        OAuth2HttpAuthProperties properties = new OAuth2HttpAuthProperties((OAuth2HttpAuthProperties) provider.getProperties());
        String channelId = provider.getConnector().getChannelId();
        String channelName = provider.getConnector().getChannel().getName();
        Map<String, Object> map = new HashMap<String, Object>();
        request.populateMap(map);

        properties.setLocationKey(replacer.replaceValues(properties.getLocationKey(), channelId, channelName, map));
        properties.setVerificationURL(replacer.replaceValues(properties.getVerificationURL(), channelId, channelName, map));

        return properties;
    }
}