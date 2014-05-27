/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;

public final class ServerConnection {
    private Logger logger = Logger.getLogger(this.getClass());
    private CloseableHttpClient client;
    private RequestConfig requestConfig;
    private String address;
    private HttpPost post = null;
    private HttpPost channelPost = null;
    private HttpClientContext channelPostContext = null;
    final private Operation currentOp = new Operation(null, null, false);
    final private AbortTask abortTask = new AbortTask();
    private ExecutorService abortExecutor = Executors.newSingleThreadExecutor();

    private static final Charset CONTENT_CHARSET = Charset.forName("UTF-8");
    private static final int CONNECT_TIMEOUT = 10000;

    public ServerConnection(String address) {
        // Default timeout is infinite.
        this(address, 0);
    }

    public ServerConnection(String address, int timeout) {
        this.address = address;

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        } catch (Exception e) {
            logger.error("Unable to build SSL context.", e);
        }
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("https", sslConnectionSocketFactory).build();

        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        httpClientConnectionManager.setDefaultMaxPerRoute(5);
        httpClientConnectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeout).build());

        client = HttpClients.custom().setConnectionManager(httpClientConnectionManager).build();
        requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setConnectionRequestTimeout(CONNECT_TIMEOUT).setSocketTimeout(timeout).build();
        post = getDefaultHttpPost();
        channelPost = getDefaultHttpPost();
    }

    /**
     * Executes a POST method on a servlet with a set of parameters.
     * 
     * @param servletName
     *            The name of the servlet.
     * @param params
     *            NameValuePair parameters for the request.
     * @return
     * @throws ClientException
     */
    public synchronized String executePostMethod(String servletName, NameValuePair[] params) throws ClientException {
        synchronized (currentOp) {
            if (params[0].getName().equals("op")) {
                Operation op = Operations.getOperation(params[0].getValue());
                currentOp.setName(op.getName());
                currentOp.setDisplayName(op.getDisplayName());
                currentOp.setAuditable(op.isAuditable());
            }
        }

        CloseableHttpResponse response = null;

        try {
            post.setURI(URI.create(address + servletName));
            post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), CONTENT_CHARSET));

            response = client.execute(post);
            return getResponsePayload(response);
        } catch (Exception e) {
            if (post.isAborted()) {
                throw new ClientException(new RequestAbortedException(e));
            } else {
                throw new ClientException(e);
            }
        } finally {
            HttpClientUtils.closeQuietly(response);
            post.reset();

            synchronized (currentOp) {
                currentOp.setName(null);
                currentOp.setDisplayName(null);
                currentOp.setAuditable(false);
            }
        }
    }

    /**
     * Executes a POST method on a servlet with a set of parameters. The requests sent through this
     * channel will be aborted on the client side when a new request arrives. Currently there is no
     * guarantee of the order that pending requests will be sent
     * 
     * @param servletName
     *            The name of the servlet.
     * @param params
     *            NameValuePair parameters for the request.
     * @return
     * @throws ClientException
     */
    public String executePostMethodAbortPending(String servletName, NameValuePair[] params) throws ClientException {
        //TODO make order sequential
        abortTask.incrementRequestsInQueue();

        synchronized (abortExecutor) {
            if (!abortExecutor.isShutdown() && !abortTask.isRunning()) {
                abortExecutor.execute(abortTask);
            }

            CloseableHttpResponse response = null;

            try {
                channelPostContext = HttpClientContext.create();
                channelPostContext.setRequestConfig(requestConfig);
                channelPost.setURI(URI.create(address + servletName));
                channelPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), CONTENT_CHARSET));

                abortTask.setAbortAllowed(true);
                response = client.execute(channelPost, channelPostContext);
                abortTask.setAbortAllowed(false);

                return getResponsePayload(response);
            } catch (Exception e) {
                if (channelPost.isAborted()) {
                    return null;
                } else {
                    throw new ClientException(e);
                }
            } finally {
                abortTask.decrementRequestsInQueue();
                HttpClientUtils.closeQuietly(response);
                channelPost.reset();
            }
        }
    }

    /**
     * Executes a POST method on a servlet with a set of parameters, but allows multiple
     * simultaneous requests.
     * 
     * @param servletName
     *            The name of the servlet.
     * @param params
     *            NameValuePair parameters for the request.
     * @return
     * @throws ClientException
     */
    public String executePostMethodAsync(String servletName, NameValuePair[] params) throws ClientException {
        HttpPost post = null;
        CloseableHttpResponse response = null;

        try {
            post = getDefaultHttpPost();
            post.setURI(URI.create(address + servletName));
            post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), CONTENT_CHARSET));

            response = client.execute(post);
            return getResponsePayload(response);
        } catch (Exception e) {
            if (post.isAborted()) {
                throw new ClientException(new RequestAbortedException(e));
            } else {
                throw new ClientException(e);
            }
        } finally {
            HttpClientUtils.closeQuietly(response);

            if (post != null) {
                post.reset();
            }
        }
    }

    public synchronized String executeFileUpload(String servletName, NameValuePair[] params, File file) throws ClientException {
        synchronized (currentOp) {
            if (params[0].getName().equals("op")) {
                Operation op = Operations.getOperation(params[0].getValue());
                currentOp.setName(op.getName());
                currentOp.setDisplayName(op.getDisplayName());
                currentOp.setAuditable(op.isAuditable());
            }
        }

        CloseableHttpResponse response = null;

        try {
            post.setURI(URI.create(address + servletName));

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            for (NameValuePair param : params) {
                multipartEntityBuilder.addTextBody(param.getName(), param.getValue());
            }
            multipartEntityBuilder.addPart(file.getName(), new FileBody(file));
            post.setEntity(multipartEntityBuilder.build());

            response = client.execute(post);
            return getResponsePayload(response);
        } catch (Exception e) {
            if (post.isAborted()) {
                throw new ClientException(new RequestAbortedException(e));
            } else {
                throw new ClientException(e);
            }
        } finally {
            HttpClientUtils.closeQuietly(response);
            post.reset();

            synchronized (currentOp) {
                currentOp.setName(null);
                currentOp.setDisplayName(null);
                currentOp.setAuditable(false);
            }
        }
    }

    /**
     * Aborts the request if the currentOp is equal to the passed operation, or if the passed
     * operation is null
     * 
     * @param operation
     */
    public void abort(List<Operation> operations) {
        synchronized (currentOp) {
            for (Operation operation : operations) {
                if (currentOp.equals(operation)) {
                    post.abort();
                    return;
                }
            }
        }
    }

    public void shutdown() {
        // Shutdown the abort thread
        abortExecutor.shutdownNow();

        HttpClientUtils.closeQuietly(client);
    }

    public class AbortTask implements Runnable {
        private final AtomicBoolean running = new AtomicBoolean(false);
        private int requestsInQueue = 0;
        private boolean abortAllowed = false;

        public synchronized void incrementRequestsInQueue() {
            requestsInQueue++;
        }

        public synchronized void decrementRequestsInQueue() {
            requestsInQueue--;
        }

        public synchronized void setAbortAllowed(boolean abortAllowed) {
            this.abortAllowed = abortAllowed;
        }

        public boolean isRunning() {
            return running.get();
        }

        @Override
        public void run() {
            try {
                running.set(true);
                while (true) {
                    synchronized (this) {
                        if (requestsInQueue == 0) {
                            return;
                        }
                        if (requestsInQueue > 1 && abortAllowed && channelPostContext.isRequestSent()) {
                            channelPost.abort();
                            abortAllowed = false;
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } finally {
                running.set(false);
            }
        }
    }

    private HttpPost getDefaultHttpPost() {
        HttpPost post = new HttpPost();
        post.setConfig(requestConfig);
        return post;
    }

    private String getResponsePayload(HttpResponse response) throws ClientException, IOException {
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        if (statusCode == HttpStatus.SC_FORBIDDEN) {
            throw new InvalidLoginException(statusLine.toString());
        } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new UnauthorizedException(statusLine.toString());
        } else if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
            throw new ClientException("method failed: " + statusLine);
        }

        HttpEntity responseEntity = response.getEntity();
        Charset responseCharset = null;
        try {
            responseCharset = ContentType.getOrDefault(responseEntity).getCharset();
        } catch (Exception e) {
            responseCharset = ContentType.TEXT_PLAIN.getCharset();
        }

        return IOUtils.toString(responseEntity.getContent(), responseCharset).trim();
    }
}
