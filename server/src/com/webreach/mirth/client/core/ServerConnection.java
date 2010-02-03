/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;

import com.webreach.mirth.client.core.ssl.EasySSLProtocolSocketFactory;

public class ServerConnection {
	private HttpClient client;
	private String address;
	private IdleConnectionTimeoutThread idleConnectionTimeoutThread;

	public ServerConnection(String address) {
	    // default timeout is inifinite
	    this(address, 0);
	}
	
	public ServerConnection(String address, int timeout) {
        this.address = address;
        
        HttpClientParams httpClientParams = new HttpClientParams();
    	HttpConnectionManager httpConnectionManager = new SimpleHttpConnectionManager();
    	httpClientParams.setSoTimeout(timeout);
        httpConnectionManager.getParams().setConnectionTimeout(10 * 1000);
        httpConnectionManager.getParams().setSoTimeout(timeout);
        
        client = new HttpClient(httpClientParams, httpConnectionManager);
        Protocol mirthHttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8443);
        Protocol.registerProtocol("https", mirthHttps);
        
        // Close connections that have been idle for more than 5 seconds every 5 seconds.
        // This should help avoid stale connections.
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
	 *            An array of NameValuePair objects.
	 * @return
	 * @throws ClientException
	 */
	public synchronized String executePostMethod(String servletName, NameValuePair[] params) throws ClientException {
		PostMethod post = null;

		try {
			post = new PostMethod(address + servletName);
			post.addResponseFooter(new Header("Content-Encoding", "gzip"));
			post.addResponseFooter(new Header("Accept-Encoding", "gzip,deflate"));
			post.setRequestBody(params);

			int statusCode = client.executeMethod(post);

			if (statusCode == HttpStatus.SC_NOT_ACCEPTABLE) {
				throw new VersionMismatchException(post.getStatusLine().toString());
			} else if (statusCode == HttpStatus.SC_FORBIDDEN) {
				throw new InvalidLoginException(post.getStatusLine().toString());
			} else if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), post.getResponseCharSet()));

			StringBuffer result = new StringBuffer();
			String input = new String();
			
			while ((input = reader.readLine()) != null) {
				result.append(input);
				result.append('\n');
			}
			
			return result.toString().trim();
		} catch (Exception e) {
			throw new ClientException(e);
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}
	}
	public synchronized String executeFileUpload(String servletName, NameValuePair[] params, File file) throws ClientException {
		PostMethod post = null;

		try {
			post = new PostMethod(address + servletName);
			post.addResponseFooter(new Header("Content-Encoding", "gzip"));
			post.addResponseFooter(new Header("Accept-Encoding", "gzip,deflate"));
			//Create multipart segment
			Part[] parts = new Part[params.length + 1];
			for (int i = 0; i < params.length; i++){
				parts[i] = new StringPart(params[i].getName(), params[i].getValue());
			}
			parts[params.length] = new FilePart(file.getName(), file);
			
			post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
			
			int statusCode = client.executeMethod(post);

			if (statusCode == HttpStatus.SC_NOT_ACCEPTABLE) {
				throw new VersionMismatchException(post.getStatusLine().toString());
			} else if (statusCode == HttpStatus.SC_FORBIDDEN) {
				throw new InvalidLoginException(post.getStatusLine().toString());
			} else if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), post.getResponseCharSet()));

			StringBuffer result = new StringBuffer();
			String input = new String();
			
			while ((input = reader.readLine()) != null) {
				result.append(input);
				result.append('\n');
			}
			
			return result.toString().trim();
		} catch (Exception e) {
			throw new ClientException(e);
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}
	}
	
	public void shutdownTimeoutThread() {
	    if (idleConnectionTimeoutThread != null) {
	        idleConnectionTimeoutThread.shutdown();
	    }
	}
}
