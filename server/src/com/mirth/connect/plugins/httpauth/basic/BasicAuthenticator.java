/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.basic;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpHeader;

import com.mirth.connect.donkey.util.MessageMaps;
import com.mirth.connect.plugins.httpauth.AuthenticationResult;
import com.mirth.connect.plugins.httpauth.Authenticator;
import com.mirth.connect.plugins.httpauth.RequestInfo;
import com.mirth.connect.server.channel.MirthMessageMaps;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class BasicAuthenticator extends Authenticator {

    private BasicAuthenticatorProvider provider;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    protected Logger logger = Logger.getLogger(this.getClass());
    private MessageMaps messageMaps;

    public BasicAuthenticator(BasicAuthenticatorProvider provider) {
        this.provider = provider;
        this.messageMaps = new MirthMessageMaps(provider.getConnector().getChannelId());
    }

    BasicAuthenticator(BasicAuthenticatorProvider provider, MessageMaps messageMaps) {
        this.provider = provider;
        this.messageMaps = messageMaps;
    }

    @Override
    public AuthenticationResult authenticate(RequestInfo request) {
        BasicHttpAuthProperties properties = getReplacedProperties(request);
        List<String> authHeaderList = request.getHeaders().get(HttpHeader.AUTHORIZATION.asString());

        if (CollectionUtils.isNotEmpty(authHeaderList)) {
            String authHeader = StringUtils.trimToEmpty(authHeaderList.iterator().next());

            int index = authHeader.indexOf(' ');
            if (index > 0) {
                String method = authHeader.substring(0, index);
                if (StringUtils.equalsIgnoreCase(method, "Basic")) {
                    // Get Base64-encoded credentials
                    String credentials = StringUtils.trim(authHeader.substring(index));
                    // Get the raw, colon-separated credentials
                    credentials = new String(Base64.decodeBase64(credentials), StandardCharsets.ISO_8859_1);

                    // Split on ':' to get the username and password
                    index = credentials.indexOf(':');
                    if (index > 0) {
                        String username = credentials.substring(0, index);
                        String password = credentials.substring(index + 1);

                        Map<String, String> credentialsSource = getCredentials(properties);
                        // Return successful result if the passwords match
                        if (StringUtils.equals(password, credentialsSource.get(username))) {
                            return AuthenticationResult.Success(username, properties.getRealm());
                        }
                    }
                }
            }
        }

        // Return authentication challenge
        return AuthenticationResult.Challenged("Basic realm=\"" + properties.getRealm() + "\"");
    }

    private BasicHttpAuthProperties getReplacedProperties(RequestInfo request) {
        BasicHttpAuthProperties properties = new BasicHttpAuthProperties((BasicHttpAuthProperties) provider.getProperties());
        String channelId = provider.getConnector().getChannelId();
        String channelName = provider.getConnector().getChannel().getName();
        Map<String, Object> map = new HashMap<String, Object>();
        request.populateMap(map);

        properties.setRealm(replacer.replaceValues(properties.getRealm(), channelId, channelName, map));

        Map<String, String> credentials = new LinkedHashMap<String, String>();
        for (Entry<String, String> entry : properties.getCredentialsMap().entrySet()) {
            String username = replacer.replaceValues(entry.getKey(), channelId, channelName, map);
            if (StringUtils.isNotBlank(username)) {
                credentials.put(username, replacer.replaceValues(entry.getValue(), channelId, channelName, map));
            }
        }
        properties.setCredentialsMap(credentials);

        properties.setCredentialsVariable(replacer.replaceValues(properties.getCredentialsVariable()));

        return properties;
    }

    protected Map<String, String> getCredentials(BasicHttpAuthProperties properties) {
        Map<String, String> credentialsSource;

        if (properties.isUseCredentialsVariable()) {
            credentialsSource = new HashMap<>();

            try {
                Map<?, ?> source = (Map<?, ?>) messageMaps.get(properties.getCredentialsVariable(), null);

                if (source != null) {
                    for (Entry<?, ?> entry : source.entrySet()) {
                        credentialsSource.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                    }
                } else {
                    logger.warn("Credentials map variable '" + properties.getCredentialsVariable() + "' not found.");
                }
            } catch (Exception e) {
                logger.warn("Error getting credentials from map '" + properties.getCredentialsVariable() + "'.", e);
            }
        } else {
            credentialsSource = properties.getCredentialsMap();
        }

        return credentialsSource;
    }
}