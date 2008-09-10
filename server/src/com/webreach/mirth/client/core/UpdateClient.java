package com.webreach.mirth.client.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.Preferences;
import com.webreach.mirth.model.ServerInfo;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.UpdateInfo;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.util.PropertyLoader;

public class UpdateClient {
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
    private final static String URL_REGISTRATION = "/RegistrationServlet";
    private final static String URL_UPDATES = "/UpdateServlet";
    private final static String URL_USAGE_STATISTICS = "/UsageDataServlet";
    private final static String USER_PREF_IGNORED_IDS = "ignored.component.ids";
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
            components.put(pmd.getName(), pmd.getPluginVersion());
        }

        for (ConnectorMetaData cmd : connectors.values()) {
            components.put(cmd.getName(), cmd.getPluginVersion());
        }

        components.put("Mirth", version);
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
        Map<String, String> usageData = null;

        try {
            usageData = getUsageData();
        } catch (Exception e) {
            throw new ClientException(e);
        }

        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod(PropertyLoader.getProperty(client.getServerProperties(), "update.url") + URL_USAGE_STATISTICS);
        NameValuePair[] params = { new NameValuePair("serverId", client.getServerId()), new NameValuePair("data", serializer.toXML(usageData)) };
        post.setRequestBody(params);

        try {
            int statusCode = httpClient.executeMethod(post);

            if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new Exception("Failed to connect to update server: " + post.getStatusLine());
            }
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    public void registerUser(User user) throws ClientException {
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod(PropertyLoader.getProperty(client.getServerProperties(), "update.url") + URL_REGISTRATION);
        NameValuePair[] params = { new NameValuePair("serverId", client.getServerId()), new NameValuePair("user", serializer.toXML(requestUser)) };
        post.setRequestBody(params);

        try {
            int statusCode = httpClient.executeMethod(post);

            if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new Exception("Failed to connect to update server: " + post.getStatusLine());
            }
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    private List<UpdateInfo> getUpdatesFromUri(ServerInfo serverInfo) throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod(PropertyLoader.getProperty(client.getServerProperties(), "update.url") + URL_UPDATES);
        NameValuePair[] params = { new NameValuePair("serverInfo", serializer.toXML(serverInfo)) };
        post.setRequestBody(params);

        try {
            int statusCode = httpClient.executeMethod(post);

            if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new Exception("Failed to connect to update server: " + post.getStatusLine());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
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
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    public void setIgnoredComponents(List<IgnoredComponent> ignoredComponents) throws ClientException {
        StringBuilder builder = new StringBuilder();

        for (IgnoredComponent component : ignoredComponents) {
            builder.append(component.toString() + ",");
        }

        client.setUserPreference(requestUser, USER_PREF_IGNORED_IDS, builder.toString());
    }

    public List<IgnoredComponent> getIgnoredComponents() throws ClientException {
        Preferences userPreferences = client.getUserPreferences(requestUser);

        if (userPreferences == null) {
            return new ArrayList<IgnoredComponent>();
        } else {
            String ignoredComponentIds = userPreferences.get(USER_PREF_IGNORED_IDS);

            if (ignoredComponentIds == null) {
                return new ArrayList<IgnoredComponent>();
            } else {
                List<IgnoredComponent> ignoredComponents = new ArrayList<IgnoredComponent>();

                for (String component : Arrays.asList(ignoredComponentIds.split(","))) {
                    if ((component != null) && (component.length() > 0)) {
                        String name = component.split(":")[0];
                        String version = component.split(":")[1];
                        ignoredComponents.add(new IgnoredComponent(name, version));
                    }
                }

                return ignoredComponents;
            }
        }
    }

    private Map<String, String> getUsageData() throws Exception {
        Map<String, String> usageData = new HashMap<String, String>();
        List<Channel> channels = client.getChannel(null);
        usageData.put("channels", String.valueOf(channels.size()));
        int channelIndex = 1;

        for (Channel channel : channels) {
            String channelId = "channel" + channelIndex;

            // number of destinations
            usageData.put(channelId + ".destinations", String.valueOf(channel.getDestinationConnectors().size()));

            // message counts
            ChannelStatistics statistics = client.getStatistics(channel.getId());

            if (statistics != null) {
                usageData.put(channelId + ".received", String.valueOf(statistics.getReceived()));
                usageData.put(channelId + ".filtered", String.valueOf(statistics.getFiltered()));
                usageData.put(channelId + ".sent", String.valueOf(statistics.getSent()));
                usageData.put(channelId + ".errored", String.valueOf(statistics.getError()));
            }

            // connector and protocol counts
            updateCount(usageData, channel.getSourceConnector().getTransportName());
            updateCount(usageData, channel.getSourceConnector().getTransformer().getInboundProtocol().name());
            updateCount(usageData, channel.getSourceConnector().getTransformer().getOutboundProtocol().name());

            for (Connector connector : channel.getDestinationConnectors()) {
                updateCount(usageData, connector.getTransportName());
                updateCount(usageData, connector.getTransformer().getInboundProtocol().name());
                updateCount(usageData, connector.getTransformer().getOutboundProtocol().name());

                for (Step step : connector.getTransformer().getSteps()) {
                    updateCount(usageData, step.getType());
                }
            }

            channelIndex++;
        }

        return usageData;
    }

    private void updateCount(Map<String, String> usageData, String key) {
        String count = usageData.get(key);

        if (count == null) {
            usageData.put(key, "1");
        } else {
            usageData.put(key, String.valueOf(Integer.valueOf(count) + 1));
        }
    }
}
