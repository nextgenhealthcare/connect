/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.client.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;

import com.webreach.mirth.client.core.ssl.EasySSLProtocolSocketFactory;

public class ServerConnection {
	private HttpClient client;
	private String address;

	public ServerConnection(String address) {
		this.address = address;
		client = new HttpClient();
		Protocol mirthHttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8443);
		Protocol.registerProtocol("https", mirthHttps);
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
			post.setRequestBody(params);

			int statusCode = client.executeMethod(post);

			if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));

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
			post.releaseConnection();
		}
	}

}
