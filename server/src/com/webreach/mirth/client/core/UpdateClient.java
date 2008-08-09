package com.webreach.mirth.client.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.UpdateInfo;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.UpdateInfo.Type;
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

    public List<UpdateInfo> getUpdates() throws Exception {
        Map<String, PluginMetaData> plugins = client.getPluginMetaData();
        String serverId = client.getServerId();
        String version = client.getVersion();
        List<User> users = client.getUser(null);

        ServerInfo serverInfo = new ServerInfo();
        Map<String, String> components = new HashMap<String, String>();

        for (Iterator iterator = plugins.values().iterator(); iterator.hasNext();) {
            PluginMetaData pmd = (PluginMetaData) iterator.next();
            components.put(pmd.getId(), pmd.getPluginVersion());
        }

        components.put(MIRTH_GUID, version);
        serverInfo.setComponents(components);
        serverInfo.setServerId(serverId);

        Map<String, String> contacts = new HashMap<String, String>();

        for (Iterator iterator = users.iterator(); iterator.hasNext();) {
            User user = (User) iterator.next();
            contacts.put(user.getEmail(), user.getOrganization());
        }

        serverInfo.setContacts(contacts);
        submitServerInfo(serverInfo);

        List<String> ignore = getIgnoredComponentIds();
        List<UpdateInfo> updates = null;

        try {
            updates = getUpdatesFromFeed();

            for (Iterator iterator = updates.iterator(); iterator.hasNext();) {
                UpdateInfo updateInfo = (UpdateInfo) iterator.next();

                if (ignore.contains(updateInfo.getId())) {
                    updates.remove(updateInfo);
                }
            }
        } catch (Exception e) {
            throw new Exception("Could not retrieve update list from server.", e);
        }

        return updates;
    }
    
    public void setIgnoredComponentIds(List<String> ignoredComponentIds) throws Exception {
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

    private List<UpdateInfo> getUpdatesFromFeed() throws Exception {
        List<UpdateInfo> updates = new ArrayList<UpdateInfo>();

        for (SyndEntry entry : getSyndEntries()) {
            UpdateInfo updateInfo = new UpdateInfo();
            updateInfo.setId(entry.getTitle());
            updateInfo.setDescription(entry.getDescription().getValue());
            updateInfo.setUri(entry.getLink());
            
            if (entry.getTitle().equals(MIRTH_GUID)) {
                updateInfo.setType(Type.SERVER);
            } else {
                updateInfo.setType(Type.EXTENSION);
            }
        }

        return updates;
    }

    private List<SyndEntry> getSyndEntries() throws Exception {
        URL feedSource = new URL(PropertyLoader.getProperty(client.getServerProperties(), "update.url"));
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        return feed.getEntries();
    }

    private List<String> getIgnoredComponentIds() throws Exception {
        Map<String, String> userPreferences = client.getUserPreferences(requestUser);

        if (userPreferences == null) {
            throw new Exception("Could not load user preferences.");
        } else {
            String ignoredComponentIds = userPreferences.get(USER_PREF_IGNORED_IDS);

            if (ignoredComponentIds == null) {
                throw new Exception("Could not find user preference: " + USER_PREF_IGNORED_IDS);
            } else {
                return Arrays.asList(ignoredComponentIds.split(","));
            }
        }
    }

    private void submitServerInfo(ServerInfo serverInfo) throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod(PropertyLoader.getProperty(client.getServerProperties(), "update.url"));
        NameValuePair[] params = { new NameValuePair("identification", serializer.toXML(serverInfo)) };
        post.setRequestBody(params);

        try {
            int statusCode = httpClient.executeMethod(post);

            if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new Exception("Failed to connect to update server: " + post.getStatusLine());
            }
        } catch (Exception e) {
            throw new Exception("Failed to connect to update server.");
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }
}
