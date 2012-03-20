/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ServerInfo;
import com.mirth.connect.model.UpdateInfo;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.UsageData;
import com.mirth.connect.model.User;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class UpdateClient {
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
    private final static String URL_REGISTRATION = "/RegistrationServlet";
    private final static String URL_UPDATES = "/UpdateServlet";
    private final static String URL_USAGE_STATISTICS = "/UsageStatisticsServlet";
    private final static String USER_PREF_IGNORED_IDS = "ignoredcomponents";
    private final static String COMPONENT_PREFERENCE_SEPARATOR = ",";

    private final static int KEY_DESTINATIONS = 1;
    private final static int KEY_RECEIVED = 2;
    private final static int KEY_FILTERED = 3;
    private final static int KEY_SENT = 4;
    private final static int KEY_ERRORED = 5;
    private final static int KEY_INBOUND_TRANSPORT = 6;
    private final static int KEY_INBOUND_PROTOCOL = 7;
    private final static int KEY_OUTBOUND_TRANSPORTS = 8;
    private final static int KEY_OUTBOUND_PROTOCOLS = 9;

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
        // Only send stats if they haven't been sent in the last 24 hours.
        long now = System.currentTimeMillis();
        Long lastUpdate = client.getUpdateSettings().getLastStatsTime();

        if (lastUpdate != null) {
            long last = lastUpdate;
            // 86400 seconds in a day
            if ((now - last) < (86400 * 1000)) {
                return;
            }
        }

        List<UsageData> usageData = null;

        try {
            usageData = getUsageData();
        } catch (Exception e) {
            throw new ClientException(e);
        }

        HttpClientParams httpClientParams = new HttpClientParams();
        HttpConnectionManager httpConnectionManager = new SimpleHttpConnectionManager();
        httpClientParams.setSoTimeout(10 * 1000);
        httpConnectionManager.getParams().setConnectionTimeout(10 * 1000);
        httpConnectionManager.getParams().setSoTimeout(10 * 1000);
        HttpClient httpClient = new HttpClient(httpClientParams, httpConnectionManager);

        PostMethod post = new PostMethod(client.getUpdateSettings().getUpdateUrl() + URL_USAGE_STATISTICS);
        NameValuePair[] params = { new NameValuePair("serverId", client.getServerId()), new NameValuePair("version", client.getVersion()), new NameValuePair("data", serializer.toXML(usageData)) };
        post.setRequestBody(params);

        try {
            int statusCode = httpClient.executeMethod(post);

            if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new Exception("Failed to connect to update server: " + post.getStatusLine());
            }

            // Save the sent time if sending was successful.
            UpdateSettings settings = new UpdateSettings();
            settings.setLastStatsTime(now);
            client.setUpdateSettings(settings);

        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            post.releaseConnection();
        }
    }

    public void registerUser(User user) throws ClientException {
        HttpClientParams httpClientParams = new HttpClientParams();
        HttpConnectionManager httpConnectionManager = new SimpleHttpConnectionManager();
        httpClientParams.setSoTimeout(10 * 1000);
        httpConnectionManager.getParams().setConnectionTimeout(10 * 1000);
        httpConnectionManager.getParams().setSoTimeout(10 * 1000);
        HttpClient httpClient = new HttpClient(httpClientParams, httpConnectionManager);

        PostMethod post = new PostMethod(client.getUpdateSettings().getUpdateUrl() + URL_REGISTRATION);
        NameValuePair[] params = { new NameValuePair("serverId", client.getServerId()), new NameValuePair("version", client.getVersion()), new NameValuePair("user", serializer.toXML(user)) };
        post.setRequestBody(params);

        try {
            int statusCode = httpClient.executeMethod(post);

            if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new Exception("Failed to connect to update server: " + post.getStatusLine());
            }
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            post.releaseConnection();
        }
    }

    private List<UpdateInfo> getUpdatesFromUri(ServerInfo serverInfo) throws Exception {
        HttpClientParams httpClientParams = new HttpClientParams();
        HttpConnectionManager httpConnectionManager = new SimpleHttpConnectionManager();
        httpClientParams.setSoTimeout(10 * 1000);
        httpConnectionManager.getParams().setConnectionTimeout(10 * 1000);
        httpConnectionManager.getParams().setSoTimeout(10 * 1000);
        HttpClient httpClient = new HttpClient(httpClientParams, httpConnectionManager);

        PostMethod post = new PostMethod(client.getUpdateSettings().getUpdateUrl() + URL_UPDATES);
        NameValuePair[] params = { new NameValuePair("serverInfo", serializer.toXML(serverInfo)) };
        post.setRequestBody(params);

        try {
            int statusCode = httpClient.executeMethod(post);

            if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new Exception("Failed to connect to update server: " + post.getStatusLine());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), post.getResponseCharSet()));
            StringBuilder result = new StringBuilder();
            String input = new String();

            while ((input = reader.readLine()) != null) {
                result.append(input);
                result.append('\n');
            }

            return (List<UpdateInfo>) serializer.fromXML(result.toString());
        } catch (Exception e) {
            throw e;
        } finally {
            post.releaseConnection();
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

    private List<UsageData> getUsageData() throws Exception {
        List<UsageData> usageData = new ArrayList<UsageData>();
        List<Channel> channels = client.getChannel(null);

        for (Channel channel : channels) {
            // number of destinations
            usageData.add(new UsageData(channel.getId(), KEY_DESTINATIONS, String.valueOf(channel.getDestinationConnectors().size())));

            // message counts
            ChannelStatistics statistics = client.getStatistics(channel.getId());

            if (statistics != null) {
                usageData.add(new UsageData(channel.getId(), KEY_RECEIVED, String.valueOf(statistics.getReceived())));
                usageData.add(new UsageData(channel.getId(), KEY_FILTERED, String.valueOf(statistics.getFiltered())));
                usageData.add(new UsageData(channel.getId(), KEY_SENT, String.valueOf(statistics.getSent())));
                usageData.add(new UsageData(channel.getId(), KEY_ERRORED, String.valueOf(statistics.getError())));
            }

            // connector transport and protocol counts
            usageData.add(new UsageData(channel.getId(), KEY_INBOUND_TRANSPORT, channel.getSourceConnector().getTransportName()));
            usageData.add(new UsageData(channel.getId(), KEY_INBOUND_PROTOCOL, channel.getSourceConnector().getTransformer().getInboundProtocol().name()));

            StringBuilder outboundTransports = new StringBuilder();
            StringBuilder outboundProtocols = new StringBuilder();

            for (Iterator<Connector> iterator = channel.getDestinationConnectors().iterator(); iterator.hasNext();) {
                Connector connector = iterator.next();
                outboundTransports.append(connector.getTransportName());
                outboundProtocols.append(connector.getTransformer().getOutboundProtocol().name());

                if (iterator.hasNext()) {
                    outboundTransports.append(",");
                    outboundProtocols.append(",");
                }
            }

            usageData.add(new UsageData(channel.getId(), KEY_OUTBOUND_TRANSPORTS, outboundTransports.toString()));
            usageData.add(new UsageData(channel.getId(), KEY_OUTBOUND_PROTOCOLS, outboundProtocols.toString()));
        }

        return usageData;
    }

}
