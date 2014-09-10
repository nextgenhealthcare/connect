package com.mirth.connect.client.ui.util;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
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
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.notification.Notification;

public class NotificationUtil {
    private final static String URL_CONNECT_SERVER = "http://connect.mirthcorp.com";
    private final static String URL_NOTIFICATION_SERVLET = "/NotificationServlet";
    private final static int TIMEOUT = 10000;
    private static String NOTIFICATION_GET = "getNotifications";
    private static String NOTIFICATION_COUNT_GET = "getNotificationCount";

    public static List<Notification> getNotifications() {
        CloseableHttpClient client = null;
        HttpPost post = new HttpPost();
        CloseableHttpResponse response = null;

        ObjectMapper mapper = new ObjectMapper();
        String extensionVersions = null;
        List<Notification> allNotifications = new ArrayList<Notification>();

        try {
            extensionVersions = mapper.writeValueAsString(LoadedExtensions.getInstance().getExtensionVersions());
            NameValuePair[] params = { new BasicNameValuePair("op", NOTIFICATION_GET),
                    new BasicNameValuePair("serverVersion", PlatformUI.SERVER_VERSION),
                    new BasicNameValuePair("extensionVersions", extensionVersions) };
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
            PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, "Failed to retrieve notifications. Please try again later.");
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(client);
        }

        return allNotifications;
    }

    public static int getNotificationCount(Set<Integer> archivedNotifications) {
        CloseableHttpClient client = null;
        HttpPost post = new HttpPost();
        CloseableHttpResponse response = null;

        ObjectMapper mapper = new ObjectMapper();
        String extensionVersions = null;
        int notificationCount = 0;

        try {
            extensionVersions = mapper.writeValueAsString(LoadedExtensions.getInstance().getExtensionVersions());
            NameValuePair[] params = { new BasicNameValuePair("op", NOTIFICATION_COUNT_GET),
                    new BasicNameValuePair("serverVersion", PlatformUI.SERVER_VERSION),
                    new BasicNameValuePair("extensionVersions", extensionVersions) };
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

                List<Integer> notificationIds = mapper.readValue(IOUtils.toString(responseEntity.getContent(), responseCharset).trim(), new TypeReference<List<Integer>>() {});
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
}
