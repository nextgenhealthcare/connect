/*
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/HttpClientMessageDispatcher.java,v 1.27 2005/11/12 09:04:17 rossmason Exp $
 * $Revision: 1.27 $
 * $Date: 2005/11/12 09:04:17 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */

package com.webreach.mirth.connectors.http;

import java.net.BindException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.ConnectMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.mule.config.i18n.Message;
import org.mule.impl.MuleMessage;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.impl.message.ExceptionPayload;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.QueueEnabledMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.http.HttpConstants;
import org.mule.providers.http.transformers.HttpClientMethodResponseToObject;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.provider.ReceiveException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.transformer.UMOTransformer;

import sun.misc.BASE64Encoder;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.QueuedMessage;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.util.VMRouter;

/**
 * <p>
 * <code>HttpClientMessageDispatcher</code> dispatches Mule events over http.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.27 $
 */
public class HttpClientMessageDispatcher extends AbstractMessageDispatcher implements QueueEnabledMessageDispatcher {
	private HttpConnector connector;
	private HttpState state;
	private UMOTransformer receiveTransformer;
	private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
	private AlertController alertController = ControllerFactory.getFactory().createAlertController();
	private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	private ConnectorType connectorType = ConnectorType.SENDER;
	private final String PAYLOAD_KEY = "$payload";

	public HttpClientMessageDispatcher(HttpConnector connector) {
		super(connector);
		this.connector = connector;
		receiveTransformer = new HttpClientMethodResponseToObject();

		state = new HttpState();
		if (connector.getProxyUsername() != null) {
			state.setProxyCredentials(new AuthScope(null, -1, null, null), new UsernamePasswordCredentials(connector.getProxyUsername(), connector.getProxyPassword()));
		}
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.AbstractConnectorSession#doDispatch(org.mule.umo.UMOEvent)
	 */
	public void doDispatch(UMOEvent event) throws Exception {
		doSend(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#getConnector()
	 */
	public UMOConnector getConnector() {
		return connector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#getDelegateSession()
	 */
	public Object getDelegateSession() throws UMOException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#receive(java.lang.String,
	 *      org.mule.umo.UMOEvent)
	 */
	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		monitoringController.updateStatus(connector, connectorType, Event.BUSY);
		if (endpointUri == null)
			return null;

		HttpMethod httpMethod = new GetMethod(endpointUri.getAddress());

		HttpConnection connection = null;
		try {
			connection = getConnection(endpointUri);
			connection.open();
			if (connection.isProxied() && connection.isSecure()) {
				httpMethod = new ConnectMethod();
			}
			httpMethod.setDoAuthentication(true);
			if (endpointUri.getUserInfo() != null) {
				// Add User Creds
				StringBuffer header = new StringBuffer();
				header.append("Basic ");
				header.append(new BASE64Encoder().encode(endpointUri.getUserInfo().getBytes()));
				httpMethod.addRequestHeader(HttpConstants.HEADER_AUTHORIZATION, header.toString());
			}

			httpMethod.execute(state, connection);

			if (httpMethod.getStatusCode() == HttpStatus.SC_OK) {
				return (UMOMessage) receiveTransformer.transform(httpMethod);
			} else {
				throw new ReceiveException(new Message("http", 3, httpMethod.getStatusLine().toString()), endpointUri, timeout);
			}
		} catch (ReceiveException e) {
			throw e;
		} catch (Exception e) {
			throw new ReceiveException(endpointUri, timeout, e);
		} finally {
			if (httpMethod != null)
				httpMethod.releaseConnection();
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}
	}

	protected HttpMethod execute(UMOEndpointURI endpointURI, boolean closeConnection, MessageObject messageObject) throws Exception {
		MuleEndpointURI uri = new MuleEndpointURI(replacer.replaceURLValues(endpointURI.toString(), messageObject));
		HttpMethod httpMethod = null;
		Map requestVariables = connector.getRequestVariables();
		
		if (connector.getMethod().equals("post")) {
			// We add all the paramerters from the connector to the post
			PostMethod postMethod = new PostMethod(uri.toString());
			if (requestVariables != null && requestVariables.size() > 0) {
				for (Iterator iter = requestVariables.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					// one of our variables can be $payload (or current
					// payload_key)
					// set our request entiity to this if set
					if (key.equals(PAYLOAD_KEY)) {
						postMethod.setRequestEntity(new StringRequestEntity(replacer.replaceValues((String) requestVariables.get(key), messageObject)));
					} else {
						postMethod.addParameter(key, replacer.replaceValues((String) requestVariables.get(key), messageObject));
					}
				}
			}
			httpMethod = postMethod;
		} else if (connector.getMethod().equals("get")) {
			// We need to add all the parameters to the get request
			String url = uri.toString();
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(url);
			if (url.indexOf('?') > -1) {
				urlBuilder.append("&");
			} else {
				urlBuilder.append("?");
			}
			if (requestVariables != null && requestVariables.size() > 0) {
				for (Iterator iter = requestVariables.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					urlBuilder.append(key);
					urlBuilder.append("=");
					urlBuilder.append(replacer.replaceValues((String) requestVariables.get(key), messageObject));
					if (iter.hasNext()) {
						urlBuilder.append("&");
					}
				}
			}
			httpMethod = new GetMethod(urlBuilder.toString());
		} else if (connector.getMethod().equals("put")) {
		    PutMethod putMethod = new PutMethod(replacer.replaceValues(uri.toString(), messageObject));
		    putMethod.setRequestEntity(new ByteArrayRequestEntity(requestVariables.get(PAYLOAD_KEY).toString().getBytes()));
		    httpMethod = putMethod;
		} else if (connector.getMethod().equals("delete")) {
		    httpMethod = new DeleteMethod(replacer.replaceValues(uri.toString(), messageObject));
		} else {
			throw new Exception("Invalid HTTP Method: " + connector.getMethod());
		}

		// ignore cookies
	    httpMethod.getParams().setCookiePolicy(org.apache.commons.httpclient.cookie.CookiePolicy.IGNORE_COOKIES);

		/*
		 * this is not used with Mirth, however it will be helpful if we add
		 * binary data one day else if (body instanceof HttpMethod) { httpMethod =
		 * (HttpMethod) body; } else if ("GET".equalsIgnoreCase(method) || body
		 * instanceof NullPayload) { httpMethod = new GetMethod(uri.toString()); }
		 * else { PostMethod postMethod = new PostMethod(uri.toString());
		 * 
		 * if (body instanceof String) { ObjectToHttpClientMethodRequest trans =
		 * new ObjectToHttpClientMethodRequest(); httpMethod = (HttpMethod)
		 * trans.transform(body.toString()); } else if (body instanceof
		 * HttpMethod) { httpMethod = (HttpMethod) body; } else { byte[] buffer =
		 * event.getTransformedMessageAsBytes(); //todo MULE20 Encoding
		 * postMethod.setRequestEntity(new ByteArrayRequestEntity(buffer));
		 * httpMethod = postMethod; }
		 */

		HttpConnection connection = null;
		try {
			connection = getConnection(uri);
			connection.open();
			if (connection.isProxied() && connection.isSecure()) {
				httpMethod = new ConnectMethod();
			}
			httpMethod.setDoAuthentication(true);
			if (uri.getUserInfo() != null) {
				// Add User Creds
				StringBuffer header = new StringBuffer();
				header.append("Basic ");
				String creds = uri.getUsername() + ":" + uri.getPassword();
				header.append(new BASE64Encoder().encode(creds.getBytes()));
				httpMethod.addRequestHeader(HttpConstants.HEADER_AUTHORIZATION, header.toString());
			}
			// add headers
			Map headerVariables = connector.getHeaderVariables();
			if (headerVariables != null && headerVariables.size() > 0) {
				for (Iterator iter = headerVariables.keySet().iterator(); iter.hasNext();) {
					String key = (String) iter.next();
					Header httpHeader = httpMethod.getRequestHeader(key);
					// TODO: Verify this in testing
					if (httpHeader != null) {
						httpMethod.removeRequestHeader(httpHeader);
					}
					httpMethod.addRequestHeader(key, replacer.replaceValues((String) headerVariables.get(key), messageObject));
				}
			}
			try {
				httpMethod.execute(state, connection);
			} catch (BindException e) {
				// retry
				Thread.sleep(100);
				httpMethod.execute(state, connection);
			} catch (HttpException e) {
				logger.error(e, e);
				messageObjectController.setError(messageObject, Constants.ERROR_404, "HTTP Error", e);
			}
			return httpMethod;
		} catch (Exception e) {
			if (httpMethod != null)
				httpMethod.releaseConnection();
			connection.close();
			throw e;
		} finally {
			if (connection != null && closeConnection) {
				connection.close();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnector#send(org.mule.umo.UMOEvent)
	 */
	public UMOMessage doSend(UMOEvent event) throws Exception {
		monitoringController.updateStatus(connector, connectorType, Event.BUSY);
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);

		if (messageObject == null) {
			return null;
		}

		if (connector.isUsePersistentQueues()) {
			connector.putMessageInQueue(event.getEndpoint().getEndpointURI(), messageObject);
			return event.getMessage();
		} else {
			HttpMethod httpMethod = null;
			try {
				send(event.getEndpoint().getEndpointURI(), httpMethod, messageObject);

				if (httpMethod == null)
					return null;

				return event.getMessage();
			} catch (Exception e) {
				alertController.sendAlerts(((HttpConnector) connector).getChannelId(), Constants.ERROR_404, null, e);
				messageObjectController.setError(messageObject, Constants.ERROR_404, "HTTP Error", e);
				throw new DispatchException(event.getMessage(), event.getEndpoint(), e);
			} finally {
				if (httpMethod != null)
					httpMethod.releaseConnection();
				monitoringController.updateStatus(connector, connectorType, Event.DONE);
			}
		}
	}

	public void send(UMOEndpointURI endpointUri, HttpMethod httpMethod, MessageObject messageObject) throws Exception {
		httpMethod = execute(endpointUri, false, messageObject);

		if (httpMethod == null) {
			return;
		}

		Properties h = new Properties();
		Header[] headers = httpMethod.getRequestHeaders();
		for (int i = 0; i < headers.length; i++) {
			h.setProperty(headers[i].getName(), headers[i].getValue());
		}
		String status = String.valueOf(httpMethod.getStatusCode());

		h.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, status);
		logger.debug("Http response is: " + status);
		ExceptionPayload ep = null;

		String fullResponseHeader = "";

		if (!connector.isExcludeHeaders()) {
			fullResponseHeader = (httpMethod.getStatusLine() == null) ? "" : httpMethod.getStatusLine().toString() + "\r\n";
			org.apache.commons.httpclient.Header[] responseHeaders = httpMethod.getResponseHeaders();
			for (int i = 0; i < responseHeaders.length; i++) {
				fullResponseHeader += responseHeaders[i].toString();
			}
		}

		String fullResponse = fullResponseHeader + httpMethod.getResponseBodyAsString();
		logger.debug("Full response from HTTP:\r\n" + fullResponse);

		if (httpMethod.getStatusCode() >= 400) {
			logger.error("HTTP Error message. Full Response: \r\n" + fullResponse);
			String exceptionText = "Http call returned a status of: " + httpMethod.getStatusCode() + " " + httpMethod.getStatusText();
			ep = new ExceptionPayload(new Exception(exceptionText));
			messageObjectController.setError(messageObject, Constants.ERROR_404, "HTTP Error", ep.getException());
		}
		UMOMessage m = null;
		// text or binary content?
		if (httpMethod.getResponseHeader(HttpConstants.HEADER_CONTENT_TYPE) != null && httpMethod.getResponseHeader(HttpConstants.HEADER_CONTENT_TYPE).getValue().startsWith("text/")) {
			m = new MuleMessage(fullResponseHeader + httpMethod.getResponseBodyAsString(), h);
		} else {
			byte[] headerBytes = fullResponseHeader.getBytes();
			byte[] bodyBytes = httpMethod.getResponseBody();
			byte[] fullResponseBytes = new byte[headerBytes.length + bodyBytes.length];
			System.arraycopy(headerBytes, 0, fullResponseBytes, 0, headerBytes.length);
			System.arraycopy(bodyBytes, 0, fullResponseBytes, headerBytes.length, bodyBytes.length);
			m = new MuleMessage(fullResponseBytes, h);
		}

		// update the message status to sent
		if (ep == null) {
			// if we didn't have an exception
			messageObjectController.setSuccess(messageObject, m.getPayloadAsString());
		}
		// handle reply to
		if (connector.getReplyChannelId() != null && !connector.getReplyChannelId().equals("sink")) {
			VMRouter router = new VMRouter();
			router.routeMessageByChannelId(connector.getReplyChannelId(), m.getPayload(), true, !connector.isUsePersistentQueues());
		}

		m.setExceptionPayload(ep);
	}

	public boolean sendPayload(QueuedMessage thePayload) throws Exception {
		boolean result = true;

		try {
			HttpMethod httpMethod = null;
			send(thePayload.getEndpointUri(), httpMethod, thePayload.getMessageObject());
		} catch (SocketException e) {
			messageObjectController.setError(thePayload.getMessageObject(), Constants.ERROR_404, "Connection refused", e);
			throw e;
		} catch (Exception e) {
			messageObjectController.setError(thePayload.getMessageObject(), Constants.ERROR_404, e.getMessage(), e);
			alertController.sendAlerts(thePayload.getMessageObject().getChannelId(), Constants.ERROR_404, e.getMessage(), e);
		}

		return result;
	}

	protected HttpConnection getConnection(UMOEndpointURI uri) throws URISyntaxException {
		HttpConnection connection = null;

		Protocol protocol = Protocol.getProtocol(connector.getProtocol().toLowerCase());

		String host = uri.getHost();
		int port = uri.getPort();

		connection = new HttpConnection(host, port, protocol);
		connection.setProxyHost(connector.getProxyHostname());
		connection.setProxyPort(connector.getProxyPort());
		return connection;
	}

	public void doDispose() {
		state = null;
	}
}
