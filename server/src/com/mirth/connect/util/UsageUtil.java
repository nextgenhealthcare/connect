package com.mirth.connect.util;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;

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

public class UsageUtil {
    private final static String URL_UPDATE_SERVER = "http://connect.mirthcorp.com";
    private final static String URL_USAGE_SERVLET = "/UsageServlet";
    public final static Integer MILLIS_PER_DAY = 86400000;
    private final static int TIMEOUT = 10000;
    
    public static boolean sendStatistics(String version, String data) {
        if (data == null) {
            return false;
        }
        
        boolean isSent = false;
        
        CloseableHttpClient client = null;
        HttpPost post = new HttpPost();;
        CloseableHttpResponse response = null;
        NameValuePair[] params = { new BasicNameValuePair("version", version), new BasicNameValuePair("data", data) };
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();
        
        post.setURI(URI.create(URL_UPDATE_SERVER + URL_USAGE_SERVLET));
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
