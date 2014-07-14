/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ServerInfo;
import com.mirth.connect.model.UpdateInfo;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.User;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.UsageUtil;

public class UpdateClient {
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private final static String URL_REGISTRATION = "/RegistrationServlet";
    private final static String URL_UPDATES = "/UpdateServlet";
    private final static String USER_PREF_IGNORED_IDS = "ignoredcomponents";
    private final static String COMPONENT_PREFERENCE_SEPARATOR = ",";
    
    private final static int TIMEOUT = 10000;

    private Client client;
    private User requestUser;

    public UpdateClient(Client client, User requestUser) {
        this.client = client;
        this.requestUser = requestUser;
    }

    public List<UpdateInfo> getUpdates() throws ClientException {
        Map<String, PluginMetaData> plugins = client.getPluginMetaData();
        Map<String, ConnectorMetaData> connectors = client.getConnectorMetaData();
        String serverId = client.getServerId();
        String version = client.getVersion();

        ServerInfo serverInfo = new ServerInfo();
        Map<String, String> components = new HashMap<String, String>();

        for (PluginMetaData pmd : plugins.values()) {
            components.put(pmd.getPath(), pmd.getPluginVersion());
        }

        for (ConnectorMetaData cmd : connectors.values()) {
            components.put(cmd.getPath(), cmd.getPluginVersion());
        }

        components.put("mirth", version);
        serverInfo.setComponents(components);
        serverInfo.setServerId(serverId);

        List<UpdateInfo> updates = null;

        try {
            List<IgnoredComponent> ignore = getIgnoredComponents();
            updates = getUpdatesFromUri(serverInfo);

            for (UpdateInfo updateInfo : updates) {
                if (ignore.contains(new IgnoredComponent(updateInfo.getName(), updateInfo.getVersion()))) {
                    updateInfo.setIgnored(true);
                }
            }
        } catch (Exception e) {
            throw new ClientException(e);
        }

        return updates;
    }

    public void sendUsageStatistics() throws ClientException {
        String usageData = client.getUsageData();
        if (usageData == null) {
            return;
        }
        boolean isSent = UsageUtil.sendStatistics(client.getVersion(), usageData);
        if (isSent) {
            Long now = System.currentTimeMillis();
            UpdateSettings settings = new UpdateSettings();
            settings.setLastStatsTime(now);
            client.setUpdateSettings(settings);
        }
    }

    public void registerUser(User user) throws ClientException {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        NameValuePair[] params = { new BasicNameValuePair("serverId", client.getServerId()),
                new BasicNameValuePair("version", client.getVersion()),
                new BasicNameValuePair("user", serializer.serialize(user)) };
        
        HttpPost post = new HttpPost();
        post.setURI(URI.create(client.getUpdateSettings().getUpdateUrl() + URL_REGISTRATION));
        post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), Charset.forName("UTF-8")));
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();
        
        try {
            HttpClientContext postContext = HttpClientContext.create();
            postContext.setRequestConfig(requestConfig);
            httpClient = HttpClients.createDefault();
            httpResponse = httpClient.execute(post, postContext);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new Exception("Failed to connect to update server: " + statusLine);
            }
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
            HttpClientUtils.closeQuietly(httpClient);
        }
    }

    private List<UpdateInfo> getUpdatesFromUri(ServerInfo serverInfo) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        NameValuePair[] params = { new BasicNameValuePair("serverInfo", serializer.serialize(serverInfo)) };
        
        HttpPost post = new HttpPost();
        post.setURI(URI.create(client.getUpdateSettings().getUpdateUrl() + URL_UPDATES));
        post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), Charset.forName("UTF-8")));
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();
        
        try {
            HttpClientContext postContext = HttpClientContext.create();
            postContext.setRequestConfig(requestConfig);
            httpClient = HttpClients.createDefault();
            httpResponse = httpClient.execute(post, postContext);
            
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == HttpStatus.SC_FORBIDDEN) {
                throw new InvalidLoginException(statusLine.toString());
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new UnauthorizedException(statusLine.toString());
            } else if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new ClientException("method failed: " + statusLine);
            }
            
            HttpEntity responseEntity = httpResponse.getEntity();
            Charset responseCharset = null;
            try {
                responseCharset = ContentType.getOrDefault(responseEntity).getCharset();
            } catch (Exception e) {
                responseCharset = ContentType.TEXT_PLAIN.getCharset();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent(), responseCharset));
            StringBuilder result = new StringBuilder();
            String input = new String();

            while ((input = reader.readLine()) != null) {
                result.append(input);
                result.append('\n');
            }
            return serializer.deserializeList(result.toString(), UpdateInfo.class);
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
            HttpClientUtils.closeQuietly(httpClient);
        }
    }

    public void setIgnoredComponents(List<IgnoredComponent> ignoredComponents) throws ClientException {
        StringBuilder builder = new StringBuilder();

        for (IgnoredComponent component : ignoredComponents) {
            builder.append(component.toString() + COMPONENT_PREFERENCE_SEPARATOR);
        }

        client.setUserPreference(requestUser, USER_PREF_IGNORED_IDS, builder.toString());
    }

    public List<IgnoredComponent> getIgnoredComponents() throws ClientException {
        Properties userPreferences = client.getUserPreferences(requestUser);

        if (userPreferences == null) {
            return new ArrayList<IgnoredComponent>();
        } else {
            String ignoredComponentsPreference = (String) userPreferences.get(USER_PREF_IGNORED_IDS);

            if (ignoredComponentsPreference == null) {
                return new ArrayList<IgnoredComponent>();
            } else {
                List<IgnoredComponent> ignoredComponents = new ArrayList<IgnoredComponent>();

                for (String component : Arrays.asList(ignoredComponentsPreference.split(COMPONENT_PREFERENCE_SEPARATOR))) {
                    if ((component != null) && (component.length() > 0)) {
                        String name = component.split(IgnoredComponent.COMPONENT_NAME_VERSION_SEPARATOR)[0];
                        String version = component.split(IgnoredComponent.COMPONENT_NAME_VERSION_SEPARATOR)[1];
                        ignoredComponents.add(new IgnoredComponent(name, version));
                    }
                }

                return ignoredComponents;
            }
        }
    }
}
