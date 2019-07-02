/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.IgnoreSpecProvider;
import org.apache.http.impl.cookie.NetscapeDraftSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.util.MessageMaps;

public class HttpUtil {

    private static Logger logger = Logger.getLogger(HttpUtil.class);

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
        Map<String, List<String>> map = new HashMap<String, List<String>>();

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
}