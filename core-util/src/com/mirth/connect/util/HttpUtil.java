/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.IgnoreSpecProvider;
import org.apache.http.impl.cookie.NetscapeDraftSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.util.MessageMaps;

public class HttpUtil {

    private static Logger logger = LogManager.getLogger(HttpUtil.class);

    /**
     * Applies global settings to any Apache HttpComponents HttpClientBuilder.<br/>
     * <br/>
     * As of version 4.5, the default cookie specifications used by Apache HttpClient are more
     * strict in order to abide by RFC 6265. As part of this change, domain parameters in cookies
     * are checked against a public ICANN suffix matcher before they are allowed to be added to
     * outgoing requests. However this may cause clients to fail to connect if they are using custom
     * hostnames like "mycustomhost". To prevent that, we're building our own CookieSpecProvider
     * registry, and setting that on the client. Look at CookieSpecRegistries to see how the default
     * registry is built. The only difference now is that DefaultCookieSpecProvider is being
     * constructed without a PublicSuffixMatcher.
     */
    public static void configureClientBuilder(HttpClientBuilder clientBuilder) {
        PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
        clientBuilder.setPublicSuffixMatcher(publicSuffixMatcher);
        RegistryBuilder<CookieSpecProvider> cookieSpecBuilder = RegistryBuilder.<CookieSpecProvider> create();
        CookieSpecProvider defaultProvider = new DefaultCookieSpecProvider();
        CookieSpecProvider laxStandardProvider = new RFC6265CookieSpecProvider(RFC6265CookieSpecProvider.CompatibilityLevel.RELAXED, publicSuffixMatcher);
        CookieSpecProvider strictStandardProvider = new RFC6265CookieSpecProvider(RFC6265CookieSpecProvider.CompatibilityLevel.STRICT, publicSuffixMatcher);
        cookieSpecBuilder.register(CookieSpecs.DEFAULT, defaultProvider);
        cookieSpecBuilder.register("best-match", defaultProvider);
        cookieSpecBuilder.register("compatibility", defaultProvider);
        cookieSpecBuilder.register(CookieSpecs.STANDARD, laxStandardProvider);
        cookieSpecBuilder.register(CookieSpecs.STANDARD_STRICT, strictStandardProvider);
        cookieSpecBuilder.register(CookieSpecs.NETSCAPE, new NetscapeDraftSpecProvider());
        cookieSpecBuilder.register(CookieSpecs.IGNORE_COOKIES, new IgnoreSpecProvider());
        clientBuilder.setDefaultCookieSpecRegistry(cookieSpecBuilder.build());
    }

    public static void closeVeryQuietly(CloseableHttpResponse response) {
        try {
            HttpClientUtils.closeQuietly(response);
        } catch (Throwable ignore) {
        }
    }

    public static Map<String, List<String>> getTableMap(boolean useVariable, String mapVariable, Map<String, List<String>> tableMap, MessageMaps messageMaps, ConnectorMessage connectorMessage) {
        if (useVariable) {
            return getTableMap(mapVariable, messageMaps, connectorMessage);
        } else {
            return tableMap;
        }
    }

    public static Map<String, List<String>> getTableMap(String mapVariable, MessageMaps messageMaps, ConnectorMessage connectorMessage) {
        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

        try {
            Map<?, ?> source = (Map<?, ?>) messageMaps.get(mapVariable, connectorMessage);

            if (source != null) {
                for (Entry<?, ?> entry : source.entrySet()) {
                    try {
                        String key = String.valueOf(entry.getKey());
                        Object value = entry.getValue();

                        if (value instanceof List) {
                            List<String> validListEntries = new ArrayList<String>();
                            for (Object listEntry : (List<?>) value) {
                                validListEntries.add(String.valueOf(listEntry));
                            }

                            if (validListEntries.size() > 0) {
                                map.put(key, validListEntries);
                            } else {
                                logger.trace("No values found for '" + key + "' from map '" + mapVariable + "'. Skipping.");
                            }
                        } else {
                            List<String> list = new ArrayList<String>();
                            list.add(String.valueOf(value));
                            map.put(key, list);
                        }
                    } catch (Exception e) {
                        logger.trace("Error getting map entry '" + entry.getKey().toString() + "' from map '" + mapVariable + "'. Skipping entry.", e);
                    }
                }
            } else {
                logger.warn("Map variable '" + mapVariable + "' not found.");
            }
        } catch (Exception e) {
            logger.warn("Error getting values from map '" + mapVariable + "'.", e);
        }

        return map;
    }

    public static String executeGetRequest(String url, int timeout, boolean hostnameVerification, String[] protocols, String[] cipherSuites) {
        return executeGetRequest(url, timeout, hostnameVerification, protocols, cipherSuites, SSLContexts.createSystemDefault());
    }

    public static String executeGetRequest(String url, int timeout, boolean hostnameVerification, String[] protocols, String[] cipherSuites, SSLContext sslContext) {
        try {
            return doExecuteGetRequest(url, timeout, hostnameVerification, protocols, cipherSuites, sslContext);
        } catch (Throwable t) {
            logger.error("Error executing GET request at URL " + url, t);
            return "";
        }
    }

    static String doExecuteGetRequest(String url, int timeout, boolean hostnameVerification, String[] protocols, String[] cipherSuites, SSLContext sslContext) throws Exception {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try {
            client = getHttpClient(timeout, hostnameVerification, protocols, cipherSuites, sslContext);

            HttpGet get = new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
            HttpClientContext getContext = HttpClientContext.create();
            getContext.setRequestConfig(requestConfig);

            response = client.execute(get, getContext);

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception(statusLine.toString());
            }

            HttpEntity responseEntity = response.getEntity();

            Charset responseCharset = null;
            try {
                responseCharset = ContentType.getOrDefault(responseEntity).getCharset();
            } catch (Exception e) {
                responseCharset = ContentType.TEXT_PLAIN.getCharset();
            }

            return IOUtils.toString(responseEntity.getContent(), responseCharset).trim();
        } finally {
            if (client != null) {
                HttpClientUtils.closeQuietly(response);
                HttpClientUtils.closeQuietly(client);
            }
        }
    }

    public static CloseableHttpClient getHttpClient(int timeout, boolean hostnameVerification, String[] protocols, String[] cipherSuites, SSLContext sslContext) {
        HostnameVerifier hostnameVerifier = hostnameVerification ? new DefaultHostnameVerifier() : NoopHostnameVerifier.INSTANCE;

        RegistryBuilder<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.getSocketFactory());
        String[] enabledProtocols = MirthSSLUtil.getEnabledHttpsProtocols(protocols);
        String[] enabledCipherSuites = MirthSSLUtil.getEnabledHttpsCipherSuites(cipherSuites);
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, enabledProtocols, enabledCipherSuites, hostnameVerifier);
        socketFactoryRegistry.register("https", sslConnectionSocketFactory);

        BasicHttpClientConnectionManager httpClientConnectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry.build());
        httpClientConnectionManager.setSocketConfig(SocketConfig.custom().setSoTimeout(timeout).build());
        HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(httpClientConnectionManager);
        configureClientBuilder(clientBuilder);

        return clientBuilder.build();
    }
}