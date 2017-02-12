/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.message.internal.Statuses;

import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.util.HttpUtil;
import com.mirth.connect.util.MirthSSLUtil;

public class ServerConnection implements Connector {

    public static final String EXECUTE_TYPE_PROPERTY = "executeType";
    public static final String OPERATION_PROPERTY = "operation";

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int IDLE_TIMEOUT = 300000;

    private Logger logger = Logger.getLogger(getClass());
    private Registry<ConnectionSocketFactory> socketFactoryRegistry;
    private PoolingHttpClientConnectionManager httpClientConnectionManager;
    private CookieStore cookieStore;
    private SocketConfig socketConfig;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private final Operation currentOp = new Operation(null, null, null, false);
    private HttpRequestBase syncRequestBase;
    private HttpRequestBase abortPendingRequestBase;
    private HttpClientContext abortPendingClientContext = null;
    private final AbortTask abortTask = new AbortTask();
    private ExecutorService abortExecutor = Executors.newSingleThreadExecutor();
    private IdleConnectionMonitor idleConnectionMonitor;
    private ConnectionKeepAliveStrategy keepAliveStrategy;

    public ServerConnection(int timeout, String[] httpsProtocols, String[] httpsCipherSuites) {
        this(timeout, httpsProtocols, httpsCipherSuites, false);
    }

    public ServerConnection(int timeout, String[] httpsProtocols, String[] httpsCipherSuites, boolean allowHTTP) {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        } catch (Exception e) {
            logger.error("Unable to build SSL context.", e);
        }

        String[] enabledProtocols = MirthSSLUtil.getEnabledHttpsProtocols(httpsProtocols);
        String[] enabledCipherSuites = MirthSSLUtil.getEnabledHttpsCipherSuites(httpsCipherSuites);
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, enabledProtocols, enabledCipherSuites, NoopHostnameVerifier.INSTANCE);
        RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder.<ConnectionSocketFactory> create().register("https", sslConnectionSocketFactory);
        if (allowHTTP) {
            builder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        }
        socketFactoryRegistry = builder.build();

        cookieStore = new BasicCookieStore();
        socketConfig = SocketConfig.custom().setSoTimeout(timeout).build();
        requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(timeout).build();
        keepAliveStrategy = new CustomKeepAliveStrategy();

        createClient();
    }

    @Override
    public ClientResponse apply(ClientRequest request) {
        Operation operation = (Operation) request.getConfiguration().getProperty(OPERATION_PROPERTY);
        if (operation == null) {
            throw new ProcessingException("No operation provided for request: " + request);
        }

        ExecuteType executeType = (ExecuteType) request.getConfiguration().getProperty(EXECUTE_TYPE_PROPERTY);
        if (executeType == null) {
            executeType = operation.getExecuteType();
        }

        if (logger.isDebugEnabled()) {
            StringBuilder debugMessage = new StringBuilder(operation.getDisplayName()).append('\n');
            debugMessage.append(request.getMethod()).append(' ').append(request.getUri());
            logger.debug(debugMessage.toString());
        }

        try {
            switch (executeType) {
                case SYNC:
                    return executeSync(request, operation);
                case ASYNC:
                    return executeAsync(request);
                case ABORT_PENDING:
                    return executeAbortPending(request);
            }
        } catch (ClientException e) {
            throw new ProcessingException(e);
        }

        return null;
    }

    @Override
    public Future<?> apply(ClientRequest request, AsyncConnectorCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return "Mirth Server Connection";
    }

    @Override
    public void close() {
        // Do nothing
    }

    private void createClient() {
        httpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        httpClientConnectionManager.setDefaultMaxPerRoute(5);
        httpClientConnectionManager.setDefaultSocketConfig(socketConfig);
        // MIRTH-3962: The stale connection settings has been deprecated, and this is recommended instead
        httpClientConnectionManager.setValidateAfterInactivity(5000);

        HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(httpClientConnectionManager).setDefaultCookieStore(cookieStore).setKeepAliveStrategy(keepAliveStrategy);
        HttpUtil.configureClientBuilder(clientBuilder);

        client = clientBuilder.build();

        idleConnectionMonitor = new IdleConnectionMonitor();
        idleConnectionMonitor.start();
    }

    public synchronized void shutdown() {
        idleConnectionMonitor.shutdown();

        // Shutdown the abort thread
        abortExecutor.shutdownNow();

        HttpClientUtils.closeQuietly(client);
    }

    public synchronized void restart() {
        shutdown();
        abortExecutor = Executors.newSingleThreadExecutor();
        createClient();
    }

    /**
     * Aborts the request if the currentOp is equal to the passed operation, or if the passed
     * operation is null
     * 
     * @param operation
     */
    public void abort(Collection<Operation> operations) {
        synchronized (currentOp) {
            if (operations.contains(currentOp)) {
                syncRequestBase.abort();
            }
        }
    }

    /**
     * Allows one request at a time.
     */
    private synchronized ClientResponse executeSync(ClientRequest request, Operation operation) throws ClientException {
        synchronized (currentOp) {
            currentOp.setName(operation.getName());
            currentOp.setDisplayName(operation.getDisplayName());
            currentOp.setAuditable(operation.isAuditable());
        }

        HttpRequestBase requestBase = null;
        CloseableHttpResponse response = null;
        boolean shouldClose = true;

        try {
            requestBase = setupRequestBase(request, ExecuteType.SYNC);
            response = client.execute(requestBase);
            ClientResponse responseContext = handleResponse(request, requestBase, response, true);
            if (responseContext.hasEntity()) {
                shouldClose = false;
            }
            return responseContext;
        } catch (Error e) {
            // If an error occurred we can't guarantee the state of the client, so close it
            HttpUtil.closeVeryQuietly(response);
            restart();
            throw e;
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                HttpUtil.closeVeryQuietly(response);
                restart();
            }

            if (requestBase != null && requestBase.isAborted()) {
                throw new RequestAbortedException(e);
            } else if (e instanceof ClientException) {
                throw (ClientException) e;
            }
            throw new ClientException(e);
        } finally {
            if (shouldClose) {
                HttpUtil.closeVeryQuietly(response);

                synchronized (currentOp) {
                    currentOp.setName(null);
                    currentOp.setDisplayName(null);
                    currentOp.setAuditable(false);
                }
            }
        }
    }

    /**
     * Allows multiple simultaneous requests.
     */
    private ClientResponse executeAsync(ClientRequest request) throws ClientException {
        HttpRequestBase requestBase = null;
        CloseableHttpResponse response = null;
        boolean shouldClose = true;

        try {
            requestBase = setupRequestBase(request, ExecuteType.ASYNC);
            response = client.execute(requestBase);
            ClientResponse responseContext = handleResponse(request, requestBase, response);
            if (responseContext.hasEntity()) {
                shouldClose = false;
            }
            return responseContext;
        } catch (Error e) {
            // If an error occurred we can't guarantee the state of the client, so close it
            HttpUtil.closeVeryQuietly(response);
            restart();
            throw e;
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                HttpUtil.closeVeryQuietly(response);
                restart();
            }

            if (requestBase != null && requestBase.isAborted()) {
                throw new RequestAbortedException(e);
            } else if (e instanceof ClientException) {
                throw (ClientException) e;
            }
            throw new ClientException(e);
        } finally {
            if (shouldClose) {
                HttpUtil.closeVeryQuietly(response);
            }
        }
    }

    /**
     * The requests sent through this channel will be aborted on the client side when a new request
     * arrives. Currently there is no guarantee of the order that pending requests will be sent.
     */
    private ClientResponse executeAbortPending(ClientRequest request) throws ClientException {
        // TODO: Make order sequential
        abortTask.incrementRequestsInQueue();

        synchronized (abortExecutor) {
            if (!abortExecutor.isShutdown() && !abortTask.isRunning()) {
                abortExecutor.execute(abortTask);
            }

            HttpRequestBase requestBase = null;
            CloseableHttpResponse response = null;
            boolean shouldClose = true;

            try {
                abortPendingClientContext = HttpClientContext.create();
                abortPendingClientContext.setRequestConfig(requestConfig);

                requestBase = setupRequestBase(request, ExecuteType.ABORT_PENDING);

                abortTask.setAbortAllowed(true);
                response = client.execute(requestBase, abortPendingClientContext);
                abortTask.setAbortAllowed(false);

                ClientResponse responseContext = handleResponse(request, requestBase, response);
                if (responseContext.hasEntity()) {
                    shouldClose = false;
                }
                return responseContext;
            } catch (Error e) {
                // If an error occurred we can't guarantee the state of the client, so close it
                HttpUtil.closeVeryQuietly(response);
                restart();
                throw e;
            } catch (Exception e) {
                if (e instanceof IllegalStateException) {
                    HttpUtil.closeVeryQuietly(response);
                    restart();
                }

                if (requestBase != null && requestBase.isAborted()) {
                    return new ClientResponse(Status.NO_CONTENT, request);
                } else if (e instanceof ClientException) {
                    throw (ClientException) e;
                }
                throw new ClientException(e);
            } finally {
                abortTask.decrementRequestsInQueue();
                if (shouldClose) {
                    HttpUtil.closeVeryQuietly(response);
                }
            }
        }
    }

    private HttpRequestBase createRequestBase(String method) {
        HttpRequestBase requestBase = null;

        if (StringUtils.equalsIgnoreCase(HttpGet.METHOD_NAME, method)) {
            requestBase = new HttpGet();
        } else if (StringUtils.equalsIgnoreCase(HttpPost.METHOD_NAME, method)) {
            requestBase = new HttpPost();
        } else if (StringUtils.equalsIgnoreCase(HttpPut.METHOD_NAME, method)) {
            requestBase = new HttpPut();
        } else if (StringUtils.equalsIgnoreCase(HttpDelete.METHOD_NAME, method)) {
            requestBase = new HttpDelete();
        } else if (StringUtils.equalsIgnoreCase(HttpOptions.METHOD_NAME, method)) {
            requestBase = new HttpOptions();
        } else if (StringUtils.equalsIgnoreCase(HttpPatch.METHOD_NAME, method)) {
            requestBase = new HttpPatch();
        }

        requestBase.setConfig(requestConfig);
        return requestBase;
    }

    private HttpRequestBase getRequestBase(ExecuteType executeType, String method) {
        HttpRequestBase requestBase = createRequestBase(method);

        if (executeType == ExecuteType.SYNC) {
            syncRequestBase = requestBase;
        } else if (executeType == ExecuteType.ABORT_PENDING) {
            abortPendingRequestBase = requestBase;
        }

        return requestBase;
    }

    private HttpRequestBase setupRequestBase(ClientRequest request, ExecuteType executeType) {
        HttpRequestBase requestBase = getRequestBase(executeType, request.getMethod());
        requestBase.setURI(request.getUri());

        for (Entry<String, List<String>> entry : request.getStringHeaders().entrySet()) {
            for (String value : entry.getValue()) {
                requestBase.addHeader(entry.getKey(), value);
            }
        }

        if (request.hasEntity() && requestBase instanceof HttpEntityEnclosingRequestBase) {
            final HttpEntityEnclosingRequestBase entityRequestBase = (HttpEntityEnclosingRequestBase) requestBase;
            entityRequestBase.setEntity(new ClientRequestEntity(request));
        }

        return requestBase;
    }

    private ClientResponse handleResponse(ClientRequest request, HttpRequestBase requestBase, CloseableHttpResponse response) throws IOException, ClientException {
        return handleResponse(request, requestBase, response, false);
    }

    private ClientResponse handleResponse(ClientRequest request, HttpRequestBase requestBase, CloseableHttpResponse response, boolean sync) throws IOException, ClientException {
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        ClientResponse responseContext = new MirthClientResponse(Statuses.from(statusCode), request);

        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<String, String>();
        for (Header header : response.getAllHeaders()) {
            headerMap.add(header.getName(), header.getValue());
        }
        responseContext.headers(headerMap);

        HttpEntity responseEntity = response.getEntity();
        if (responseEntity != null) {
            responseContext.setEntityStream(new EntityInputStreamWrapper(response, responseEntity.getContent(), sync));
        }

        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            if (responseContext.hasEntity()) {
                try {
                    Object entity = responseContext.readEntity(Object.class);
                    throw new UnauthorizedException(statusLine.toString(), entity);
                } catch (ProcessingException e) {
                }
            }
            throw new UnauthorizedException(statusLine.toString());
        } else if (statusCode == HttpStatus.SC_FORBIDDEN) {
            throw new ForbiddenException(statusLine.toString());
        }

        if (statusCode >= 400) {
            if (responseContext.hasEntity()) {
                try {
                    Object entity = responseContext.readEntity(Object.class);
                    if (entity instanceof Throwable) {
                        throw new ClientException("Method failed: " + statusLine, (Throwable) entity);
                    }
                } catch (ProcessingException e) {
                }
            }
            throw new ClientException("Method failed: " + statusLine);
        }

        return responseContext;
    }

    private class ClientRequestEntity extends AbstractHttpEntity {

        private ClientRequest request;

        public ClientRequestEntity(ClientRequest request) {
            this.request = request;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public long getContentLength() {
            return -1;
        }

        @Override
        public InputStream getContent() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeTo(final OutputStream outstream) throws IOException {
            request.setStreamProvider(new OutboundMessageContext.StreamProvider() {
                @Override
                public OutputStream getOutputStream(int contentLength) throws IOException {
                    return outstream;
                }
            });
            request.writeEntity();
        }

        @Override
        public boolean isStreaming() {
            return true;
        }
    }

    private class EntityInputStreamWrapper extends InputStream {

        private CloseableHttpResponse response;
        private InputStream delegate;
        private boolean sync;

        public EntityInputStreamWrapper(CloseableHttpResponse response, InputStream delegate, boolean sync) {
            this.response = response;
            this.delegate = delegate;
            this.sync = sync;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                HttpClientUtils.closeQuietly(response);

                if (sync) {
                    synchronized (currentOp) {
                        currentOp.setName(null);
                        currentOp.setDisplayName(null);
                        currentOp.setAuditable(false);
                    }
                }
            }
        }

        @Override
        public synchronized void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }
    }

    private class AbortTask implements Runnable {
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
                        if (requestsInQueue > 1 && abortAllowed && abortPendingClientContext.isRequestSent()) {
                            abortPendingRequestBase.abort();
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

    private class IdleConnectionMonitor extends Thread {

        private volatile boolean shutdown;

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        httpClientConnectionManager.closeExpiredConnections();
                        httpClientConnectionManager.closeIdleConnections(IDLE_TIMEOUT, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
            try {
                join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class CustomKeepAliveStrategy extends DefaultConnectionKeepAliveStrategy {

        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            long keepAlive = super.getKeepAliveDuration(response, context);

            if (keepAlive <= 0) {
                keepAlive = IDLE_TIMEOUT;
            }

            return keepAlive;
        }
    }
}