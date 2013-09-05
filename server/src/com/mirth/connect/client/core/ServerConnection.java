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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public final class ServerConnection {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpClient client;
    private String address;
    private IdleConnectionTimeoutThread idleConnectionTimeoutThread;
    private PostMethod post = null;
    private PostMethod channelPost = null;
    final private Operation currentOp = new Operation(null, null, false);
    final private AbortTask abortTask = new AbortTask();
    private ExecutorService abortExecutor = Executors.newSingleThreadExecutor();

    public ServerConnection(String address) {
        // Default timeout is infinite.
        this(address, 0);
    }

    public ServerConnection(String address, int timeout) {
        this.address = address;

        HttpClientParams httpClientParams = new HttpClientParams();
        HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        httpClientParams.setSoTimeout(timeout);
        httpConnectionManager.getParams().setConnectionTimeout(10 * 1000);
        httpConnectionManager.getParams().setSoTimeout(timeout);
        httpConnectionManager.getParams().setDefaultMaxConnectionsPerHost(5);

        client = new HttpClient(httpClientParams, httpConnectionManager);
        
        // MIRTH-1872
        HttpClientParams clientParams = new HttpClientParams();
        clientParams.setContentCharset("UTF-8");
        client.setParams(clientParams);
        
        try {
            Protocol mirthHttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8443);
            Protocol.registerProtocol("https", mirthHttps);
        } catch (Exception e) {
            logger.error("Unable to register HTTPS protocol.", e);
        }

        /*
         * Close connections that have been idle for more than 5 seconds, every
         * 5 seconds. This should help avoid stale connections.
         */
        idleConnectionTimeoutThread = new IdleConnectionTimeoutThread();
        idleConnectionTimeoutThread.addConnectionManager(httpConnectionManager);
        idleConnectionTimeoutThread.setTimeoutInterval(5000);
        idleConnectionTimeoutThread.setConnectionTimeout(5000);
        idleConnectionTimeoutThread.start();
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
        synchronized(currentOp) {
            if (params[0].getName().equals("op")) {
                Operation op = Operations.getOperation(params[0].getValue());
                currentOp.setName(op.getName());
                currentOp.setDisplayName(op.getDisplayName());
                currentOp.setAuditable(op.isAuditable());
            }
        }
        
        post = null;

        try {
            post = new PostMethod(address + servletName);
            post.addResponseFooter(new Header("Content-Encoding", "gzip"));
            post.addResponseFooter(new Header("Accept-Encoding", "gzip,deflate"));
            post.setRequestBody(params);

            int statusCode = client.executeMethod(post);

            if (statusCode == HttpStatus.SC_FORBIDDEN) {
                throw new InvalidLoginException(post.getStatusLine().toString());
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new UnauthorizedException(post.getStatusLine().toString());
            } else if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new ClientException("method failed: " + post.getStatusLine());
            }

            return IOUtils.toString(post.getResponseBodyAsStream(), post.getResponseCharSet()).trim();
        } catch (Exception e) {
            if (post.isAborted()) {
                throw new ClientException(new RequestAbortedException(e));
            } else {
                throw new ClientException(e);
            }
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
            
            synchronized(currentOp) {
                currentOp.setName(null);
                currentOp.setDisplayName(null);
                currentOp.setAuditable(false);
            }
        }
    }
    
    /**
     * Executes a POST method on a servlet with a set of parameters. The requests sent through this channel will be aborted on the client side
     * when a new request arrives. Currently there is no guarantee of the order that pending requests will be sent
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
        
        synchronized(abortExecutor) { 
            if (!abortTask.isRunning()) {
                abortExecutor.execute(abortTask);
            }
            
            try {
                channelPost = new PostMethod(address + servletName);
                channelPost.addResponseFooter(new Header("Content-Encoding", "gzip"));
                channelPost.addResponseFooter(new Header("Accept-Encoding", "gzip,deflate"));
                channelPost.setRequestBody(params);
    
                abortTask.setAbortAllowed(true);
                int statusCode = client.executeMethod(channelPost);
                abortTask.setAbortAllowed(false);
    
                if (statusCode == HttpStatus.SC_FORBIDDEN) {
                    throw new InvalidLoginException(channelPost.getStatusLine().toString());
                } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    throw new UnauthorizedException(channelPost.getStatusLine().toString());
                } else if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                    throw new ClientException("method failed: " + channelPost.getStatusLine());
                }
                
                return IOUtils.toString(channelPost.getResponseBodyAsStream(), channelPost.getResponseCharSet()).trim();
            } catch (Exception e) {
                if (channelPost.isAborted()) {
                    return null;
                } else {
                    throw new ClientException(e);
                }
            } finally {
                abortTask.decrementRequestsInQueue();
                
                if (channelPost != null) {
                    channelPost.releaseConnection();
                }
            }
        }
    }
    
    /**
     * Executes a POST method on a servlet with a set of parameters, but allows multiple simultaneous requests.
     * 
     * @param servletName
     *            The name of the servlet.
     * @param params
     *            NameValuePair parameters for the request.
     * @return
     * @throws ClientException
     */
    public String executePostMethodAsync(String servletName, NameValuePair[] params) throws ClientException {       
        PostMethod post = null;

        try {
            post = new PostMethod(address + servletName);
            post.addResponseFooter(new Header("Content-Encoding", "gzip"));
            post.addResponseFooter(new Header("Accept-Encoding", "gzip,deflate"));
            post.setRequestBody(params);

            int statusCode = client.executeMethod(post);

            if (statusCode == HttpStatus.SC_FORBIDDEN) {
                throw new InvalidLoginException(post.getStatusLine().toString());
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new UnauthorizedException(post.getStatusLine().toString());
            } else if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new ClientException("method failed: " + post.getStatusLine());
            }

            return IOUtils.toString(post.getResponseBodyAsStream(), post.getResponseCharSet()).trim();
        } catch (Exception e) {
            if (post.isAborted()) {
                throw new ClientException(new RequestAbortedException(e));
            } else {
                throw new ClientException(e);
            }
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    public synchronized String executeFileUpload(String servletName, NameValuePair[] params, File file) throws ClientException {
        synchronized(currentOp) {
            if (params[0].getName().equals("op")) {
                Operation op = Operations.getOperation(params[0].getValue());
                currentOp.setName(op.getName());
                currentOp.setDisplayName(op.getDisplayName());
                currentOp.setAuditable(op.isAuditable());
            }
        }
        
        post = null;

        try {
            post = new PostMethod(address + servletName);
            post.addResponseFooter(new Header("Content-Encoding", "gzip"));
            post.addResponseFooter(new Header("Accept-Encoding", "gzip,deflate"));

            // Create a multipart request from the parameters and the file.
            Part[] parts = new Part[params.length + 1];

            for (int i = 0; i < params.length; i++) {
                parts[i] = new StringPart(params[i].getName(), params[i].getValue());
            }

            parts[params.length] = new FilePart(file.getName(), file);
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

            int statusCode = client.executeMethod(post);

            if (statusCode == HttpStatus.SC_FORBIDDEN) {
                throw new InvalidLoginException(post.getStatusLine().toString());
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new UnauthorizedException(post.getStatusLine().toString());
            } else if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
                throw new ClientException("method failed: " + post.getStatusLine());
            }

            return IOUtils.toString(post.getResponseBodyAsStream(), post.getResponseCharSet()).trim();
        } catch (Exception e) {
            if (post.isAborted()) {
                throw new ClientException(new RequestAbortedException(e));
            } else {
                throw new ClientException(e);
            }
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
            
            synchronized(currentOp) {
                currentOp.setName(null);
                currentOp.setDisplayName(null);
                currentOp.setAuditable(false);
            }
        }
    }
    
    /**
     * Aborts the request if the currentOp is equal to the passed operation,
     * or if the passed operation is null
     * @param operation
     */
    public void abort(List<Operation> operations) {
        synchronized(currentOp) {
            for (Operation operation : operations) {
                if (currentOp.equals(operation)) {
                    post.abort();
                    return;
                }
            }
        }
    }

    public void shutdownTimeoutThread() {
        if (idleConnectionTimeoutThread != null) {
            idleConnectionTimeoutThread.shutdown();
        }
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
                        if (requestsInQueue > 1 && abortAllowed && channelPost.isRequestSent()) {
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
}
