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
import java.util.Properties;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.User;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.UsageUtil;

public class UpdateClient {
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private final static String URL_UPDATE_SERVER = "http://connect.mirthcorp.com";
    private final static String URL_REGISTRATION = "/RegistrationServlet";

    private final static int TIMEOUT = 10000;

    private Client client;
    private User requestUser;

    public UpdateClient(Client client, User requestUser) {
        this.client = client;
        this.requestUser = requestUser;
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
        post.setURI(URI.create(URL_UPDATE_SERVER + URL_REGISTRATION));
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
}
