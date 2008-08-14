package com.webreach.mirth.client.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.ServerInfo;
import com.webreach.mirth.model.UpdateInfo;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.util.PropertyLoader;

public class UpdateClient {
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
    private final static String MIRTH_GUID = "23f1841f-b172-445f-8f45-45d2204d3908";
    private final static String USER_PREF_IGNORED_IDS = "ignored.component.ids";
    private Client client;
    private User requestUser;

    public UpdateClient(Client client, User requestUser) {
        this.client = client;
        this.requestUser = requestUser;
    }

    public List<UpdateInfo> getUpdates() throws ClientException {
        Map<String, PluginMetaData> plugins = client.getPluginMetaData();
        String serverId = client.getServerId();
        String version = client.getVersion();

        ServerInfo serverInfo = new ServerInfo();
        Map<String, String> components = new HashMap<String, String>();

        for (PluginMetaData pmd : plugins.values()) {
            components.put(pmd.getId(), pmd.getPluginVersion());
        }

        components.put(MIRTH_GUID, version);
        serverInfo.setComponents(components);
        serverInfo.setServerId(serverId);

        Map<String, String> contacts = new HashMap<String, String>();


        if (PropertyLoader.getProperty(client.getServerProperties(), "update.ident").equals("1")) {
            List<User> users = client.getUser(null);

            for (User user : users) {
                contacts.put(user.getEmail(), user.getOrganization());
            }

            serverInfo.setContacts(contacts);    
        }
        
        List<UpdateInfo> updates = null;

        try {
            List<String> ignore = getIgnoredComponentIds();
            updates = getUpdatesFromUri(serverInfo);

            for (UpdateInfo updateInfo : updates) {
                if (ignore.contains(updateInfo.getId())) {
                    updateInfo.setIgnored(true);
                }
            }
        } catch (Exception e) {
            throw new ClientException("Could not retrieve update list from server.", e);
        }

        return updates;
    }

    public void setIgnoredComponentIds(List<String> ignoredComponentIds) throws ClientException {
        StringBuilder builder = new StringBuilder();

        for (ListIterator iterator = ignoredComponentIds.listIterator(); iterator.hasNext();) {
            String componentId = (String) iterator.next();

            if (iterator.nextIndex() != ignoredComponentIds.size()) {
                builder.append(componentId);
            } else {
                builder.append(componentId + ",");
            }
        }

        client.setUserPreference(requestUser, USER_PREF_IGNORED_IDS, builder.toString());
    }

    private List<UpdateInfo> getUpdatesFromUri(ServerInfo serverInfo) throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod(PropertyLoader.getProperty(client.getServerProperties(), "update.url"));
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
            throw new Exception("Failed to connect to update server.");
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    private List<String> getIgnoredComponentIds() throws Exception {
        Map<String, String> userPreferences = client.getUserPreferences(requestUser);

        if (userPreferences == null) {
            return new ArrayList<String>();
        } else {
            String ignoredComponentIds = userPreferences.get(USER_PREF_IGNORED_IDS);

            if (ignoredComponentIds == null) {
                throw new Exception("Could not find user preference: " + USER_PREF_IGNORED_IDS);
            } else {
                return Arrays.asList(ignoredComponentIds.split(","));
            }
        }
    }
}
