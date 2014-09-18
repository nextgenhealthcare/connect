/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirth.connect.model.User;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.notification.Notification;

public class ConnectServiceUtil {
    private final static String URL_CONNECT_SERVER = "https://connect.mirthcorp.com";
    private final static String URL_REGISTRATION_SERVLET = "/RegistrationServlet";
    private final static String URL_USAGE_SERVLET = "/UsageStatisticsServlet";
    private final static String URL_NOTIFICATION_SERVLET = "/NotificationServlet";
    private static String NOTIFICATION_GET = "getNotifications";
    private static String NOTIFICATION_COUNT_GET = "getNotificationCount";
    private final static int TIMEOUT = 10000;
    public final static Integer MILLIS_PER_DAY = 86400000;

    public static void registerUser(String serverId, String mirthVersion, User user) throws ClientException {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        NameValuePair[] params = { new BasicNameValuePair("serverId", serverId),
                new BasicNameValuePair("version", mirthVersion),
                new BasicNameValuePair("user", ObjectXMLSerializer.getInstance().serialize(user)) };

        HttpPost post = new HttpPost();
        post.setURI(URI.create(URL_CONNECT_SERVER + URL_REGISTRATION_SERVLET));
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

    public static List<Notification> getNotifications(String serverId, String mirthVersion, Map<String, String> extensionVersions) throws Exception {
        CloseableHttpClient client = null;
        HttpPost post = new HttpPost();
        CloseableHttpResponse response = null;

        List<Notification> allNotifications = new ArrayList<Notification>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            String extensionVersionsJson = mapper.writeValueAsString(extensionVersions);
            NameValuePair[] params = { new BasicNameValuePair("op", NOTIFICATION_GET),
                    new BasicNameValuePair("serverId", serverId),
                    new BasicNameValuePair("version", mirthVersion),
                    new BasicNameValuePair("extensionVersions", extensionVersionsJson) };
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();

            post.setURI(URI.create(URL_CONNECT_SERVER + URL_NOTIFICATION_SERVLET));
            post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), Charset.forName("UTF-8")));

            HttpClientContext postContext = HttpClientContext.create();
            postContext.setRequestConfig(requestConfig);
            client = HttpClients.createDefault();
            response = client.execute(post, postContext);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if ((statusCode == HttpStatus.SC_OK)) {
                HttpEntity responseEntity = response.getEntity();
                Charset responseCharset = null;
                try {
                    responseCharset = ContentType.getOrDefault(responseEntity).getCharset();
                } catch (Exception e) {
                    responseCharset = ContentType.TEXT_PLAIN.getCharset();
                }

                String responseContent = IOUtils.toString(responseEntity.getContent(), responseCharset).trim();
                JsonNode rootNode = mapper.readTree(responseContent);

                for (JsonNode childNode : rootNode) {
                    Notification notification = new Notification();
                    notification.setId(childNode.get("id").asInt());
                    notification.setName(childNode.get("name").asText());
                    notification.setDate(childNode.get("date").asText());
                    notification.setContent(childNode.get("content").asText());
                    allNotifications.add(notification);
                }
            } else {
                throw new ClientException("Status code: " + statusCode);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(client);
        }

        return allNotifications;
    }

    public static int getNotificationCount(String serverId, String mirthVersion, Map<String, String> extensionVersions, Set<Integer> archivedNotifications) {
        CloseableHttpClient client = null;
        HttpPost post = new HttpPost();
        CloseableHttpResponse response = null;

        int notificationCount = 0;

        try {
            ObjectMapper mapper = new ObjectMapper();
            String extensionVersionsJson = mapper.writeValueAsString(extensionVersions);
            NameValuePair[] params = { new BasicNameValuePair("op", NOTIFICATION_COUNT_GET),
                    new BasicNameValuePair("serverId", serverId),
                    new BasicNameValuePair("version", mirthVersion),
                    new BasicNameValuePair("extensionVersions", extensionVersionsJson) };
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();

            post.setURI(URI.create(URL_CONNECT_SERVER + URL_NOTIFICATION_SERVLET));
            post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), Charset.forName("UTF-8")));

            HttpClientContext postContext = HttpClientContext.create();
            postContext.setRequestConfig(requestConfig);
            client = HttpClients.createDefault();
            response = client.execute(post, postContext);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if ((statusCode == HttpStatus.SC_OK)) {
                HttpEntity responseEntity = response.getEntity();
                Charset responseCharset = null;
                try {
                    responseCharset = ContentType.getOrDefault(responseEntity).getCharset();
                } catch (Exception e) {
                    responseCharset = ContentType.TEXT_PLAIN.getCharset();
                }

                List<Integer> notificationIds = mapper.readValue(IOUtils.toString(responseEntity.getContent(), responseCharset).trim(), new TypeReference<List<Integer>>() {
                });
                for (int id : notificationIds) {
                    if (!archivedNotifications.contains(id)) {
                        notificationCount++;
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(client);
        }
        return notificationCount;
    }

    public static boolean sendStatistics(String serverId, String mirthVersion, boolean server, String data) {
        if (data == null) {
            return false;
        }

        boolean isSent = false;

        CloseableHttpClient client = null;
        HttpPost post = new HttpPost();
        CloseableHttpResponse response = null;
        NameValuePair[] params = { new BasicNameValuePair("serverId", serverId),
                new BasicNameValuePair("version", mirthVersion),
                new BasicNameValuePair("server", Boolean.toString(server)),
                new BasicNameValuePair("data", data) };
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();

        post.setURI(URI.create(URL_CONNECT_SERVER + URL_USAGE_SERVLET));
        post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), Charset.forName("UTF-8")));

        try {
            HttpClientContext postContext = HttpClientContext.create();
            postContext.setRequestConfig(requestConfig);
            client = HttpClients.createDefault();
            response = client.execute(post, postContext);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if ((statusCode == HttpStatus.SC_OK)) {
                isSent = true;
            }
        } catch (Exception e) {
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(client);
        }
        return isSent;
    }
}
