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

package com.webreach.mirth.server.mule.providers.http;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.mule.config.i18n.Message;
import org.mule.impl.MuleMessage;
import org.mule.impl.message.ExceptionPayload;
import org.mule.providers.http.HttpConstants;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.NullPayload;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.http.transformers.HttpClientMethodResponseToObject;
import org.mule.providers.http.transformers.ObjectToHttpClientMethodRequest;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.provider.ReceiveException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.transformer.UMOTransformer;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.MessageObjectController;

import com.webreach.mirth.server.mule.util.VMRouter;

import sun.misc.BASE64Encoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.BindException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * <code>HttpClientMessageDispatcher</code> dispatches Mule events over http.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.27 $
 */
public class HttpClientMessageDispatcher extends AbstractMessageDispatcher
{
    private HttpConnector connector;
    private HttpState state;
    private UMOTransformer receiveTransformer;
    private MessageObjectController messageObjectController = new MessageObjectController();
    public HttpClientMessageDispatcher(HttpConnector connector)
    {
        super(connector);
        this.connector = connector;
        receiveTransformer = new HttpClientMethodResponseToObject();

        state = new HttpState();
        if (connector.getProxyUsername() != null) {
            state.setProxyCredentials(new AuthScope( null, -1, null, null), new UsernamePasswordCredentials(connector.getProxyUsername(),
                                                                                  connector.getProxyPassword()));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.providers.AbstractConnectorSession#doDispatch(org.mule.umo.UMOEvent)
     */
    public void doDispatch(UMOEvent event) throws Exception
    {
        HttpMethod httpMethod = execute(event, true);
        if(httpMethod!=null) {
            httpMethod.releaseConnection();
            if(httpMethod.getStatusCode() >= 400 ) {
                logger.error(httpMethod.getResponseBodyAsString());
                throw new DispatchException(event.getMessage(), event.getEndpoint(),
                        new Exception("Http call returned a status of: " + httpMethod.getStatusCode() + " " + httpMethod.getStatusText()));
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.provider.UMOConnectorSession#getConnector()
     */
    public UMOConnector getConnector()
    {
        return connector;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.provider.UMOConnectorSession#getDelegateSession()
     */
    public Object getDelegateSession() throws UMOException
    {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.provider.UMOConnectorSession#receive(java.lang.String,
     *      org.mule.umo.UMOEvent)
     */
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception
    {
        if (endpointUri == null)
            return null;

        HttpMethod httpMethod = new GetMethod(endpointUri.getAddress());

        HttpConnection connection = null;
        try {
            connection = getConnection(endpointUri.getUri());
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
                throw new ReceiveException(new Message("http", 3, httpMethod.getStatusLine().toString()),
                                           endpointUri,
                                           timeout);
            }
        } catch (ReceiveException e) {
            throw e;
        } catch (Exception e) {
            throw new ReceiveException(endpointUri, timeout, e);
        } finally {
            if (httpMethod != null)
                httpMethod.releaseConnection();
        }
    }

    protected HttpMethod execute(UMOEvent event, boolean closeConnection) throws Exception
    {
    	TemplateValueReplacer replacer = new TemplateValueReplacer();
    	MessageObject messageObject = null;
        String method = (String) event.getProperty(HttpConnector.HTTP_METHOD_PROPERTY, HttpConstants.METHOD_POST);
        URI uri = event.getEndpoint().getEndpointURI().getUri();
        HttpMethod httpMethod = null;
        Object body = event.getTransformedMessage();
        if (body instanceof MessageObject){
        	messageObject = (MessageObject) body;
        	Map requestVariables = connector.getRequestVariables();
        	if (connector.getMethod().equals("post")){
        		//We add all the paramerters from the connector to the post
        		PostMethod postMethod = new PostMethod(uri.toString());
            	for (Iterator iter = requestVariables.keySet().iterator(); iter.hasNext();) {
    				String key = (String) iter.next();
    				postMethod.addParameter(key, replacer.replaceValues((String)requestVariables.get(key), messageObject, "http"));
    			}
        		httpMethod = postMethod;
        	}else if (connector.getMethod().equals("get")){
        		//We need to add all the parameters to the get request
        		String url = uri.toString();
        		StringBuilder urlBuilder = new StringBuilder();
        		urlBuilder.append(url);
        		if (url.indexOf('?') > -1){
        			urlBuilder.append("&");
        		}else{
        			urlBuilder.append("?");
        		}
        		for (Iterator iter = requestVariables.keySet().iterator(); iter.hasNext();) {
    				String key = (String) iter.next();
    				urlBuilder.append(key);
    				urlBuilder.append("=");
    				urlBuilder.append(replacer.replaceValues((String)requestVariables.get(key), messageObject, "http"));
    				if (iter.hasNext()){
    					urlBuilder.append("&");
    				}
        		}
        		httpMethod = new GetMethod(urlBuilder.toString());

        	}else{
        		throw new Exception("Invalid HTTP Method: " + connector.getMethod());
        	}
        	

        
        } else if (body instanceof HttpMethod) {
            httpMethod = (HttpMethod) body;
        } else if ("GET".equalsIgnoreCase(method) || body instanceof NullPayload) {
            httpMethod = new GetMethod(uri.toString());
        } else {
            PostMethod postMethod = new PostMethod(uri.toString());

            if (body instanceof String) {
                ObjectToHttpClientMethodRequest trans = new ObjectToHttpClientMethodRequest();
                httpMethod = (HttpMethod) trans.transform(body.toString());
            } else if (body instanceof HttpMethod) {
                httpMethod = (HttpMethod) body;
            } else {
                byte[] buffer = event.getTransformedMessageAsBytes();
                //todo MULE20 Encoding
                postMethod.setRequestEntity(new ByteArrayRequestEntity(buffer));
                httpMethod = postMethod;
            }

        }
        HttpConnection connection = null;
        try {
            connection = getConnection(uri);
            connection.open();
            if (connection.isProxied() && connection.isSecure()) {
                httpMethod = new ConnectMethod();
            }
            httpMethod.setDoAuthentication(true);

            if (event.getCredentials() != null) {
                // Add User Creds
                StringBuffer header = new StringBuffer();
                header.append("Basic ");
                String creds = event.getCredentials().getUsername() + ":" + new String(event.getCredentials().getPassword());
                header.append(new BASE64Encoder().encode(creds.getBytes()));
                httpMethod.addRequestHeader(HttpConstants.HEADER_AUTHORIZATION, header.toString());
            }

            try {
                httpMethod.execute(state, connection);
                
                
            } catch (BindException e) {
                //retry
                Thread.sleep(100);
                httpMethod.execute(state, connection);
            } catch(HttpException e) {
                logger.error(e, e);
				if (messageObject != null) {
					// NACK
					messageObject.setStatus(MessageObject.Status.ERROR);
					messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + e.getMessage());
					messageObjectController.updateMessage(messageObject);
				}
            }
            
            if (messageObject != null) {
				messageObject.setStatus(MessageObject.Status.SENT);
				messageObjectController.updateMessage(messageObject);
			}
            return httpMethod;
        } catch (Exception e) {
            if (httpMethod != null)
                httpMethod.releaseConnection();
                connection.close();
    			if (messageObject != null) {
					messageObject.setStatus(MessageObject.Status.ERROR);
					messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + e.getMessage());
					messageObjectController.updateMessage(messageObject);
				}
            throw new DispatchException(event.getMessage(), event.getEndpoint(), e);
        } finally {
            if(connection!=null && closeConnection) {
                connection.close();
            }
            //if (bis != null){
           // 	bis.close();
           // }
        }
    }
    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.provider.UMOConnector#send(org.mule.umo.UMOEvent)
     */
    public UMOMessage doSend(UMOEvent event) throws Exception
    {
        HttpMethod httpMethod = execute(event, false);
        Object data = event.getMessage();
        MessageObject messageObject = null;
        if (data instanceof MessageObject){
        	messageObject = (MessageObject)event.getMessage();
        }
        try {
            Properties h = new Properties();
            Header[] headers = httpMethod.getRequestHeaders();
            for (int i = 0; i < headers.length; i++) {
                h.setProperty(headers[i].getName(), headers[i].getValue());
            }
            String status = String.valueOf(httpMethod.getStatusCode());

            h.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, status);
            logger.debug("Http response is: " + status);
            ExceptionPayload ep = null;
            if(httpMethod.getStatusCode() >= 400 ) {
                logger.error(httpMethod.getResponseBodyAsString());
                String exceptionText = "Http call returned a status of: " + httpMethod.getStatusCode() + " " + httpMethod.getStatusText();
                ep = new ExceptionPayload(new DispatchException(event.getMessage(), event.getEndpoint(),
                        new Exception(exceptionText)));
    			if (messageObject != null) {
					messageObject.setStatus(MessageObject.Status.ERROR);
					messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + exceptionText);
					messageObjectController.updateMessage(messageObject);
				}
            }
            UMOMessage m = null;
            // text or binary content?
            if(httpMethod.getResponseHeader(HttpConstants.HEADER_CONTENT_TYPE).getValue().startsWith("text/")) {
                m = new MuleMessage(httpMethod.getResponseBodyAsString(), h);
            } else {
                m = new MuleMessage(httpMethod.getResponseBody(), h);
            }
//          handle reply to
            if (connector.getReplyChannelId() != null){
                VMRouter router = new VMRouter();
                router.routeMessageByChannelId(connector.getReplyChannelId(), m.getPayload(), true);
            }
            
            m.setExceptionPayload(ep);
            
            return m;
        } catch (Exception e) {
            throw new DispatchException(event.getMessage(), event.getEndpoint(), e);
        } finally {
            if (httpMethod != null)
                httpMethod.releaseConnection();
        }
    }

    protected HttpConnection getConnection(URI uri) throws URISyntaxException
    {
        HttpConnection connection = null;

        Protocol protocol = Protocol.getProtocol(connector.getProtocol().toLowerCase());

        String host = uri.getHost();
        int port = uri.getPort();

        connection = new HttpConnection(host, port, protocol);
        connection.setProxyHost(connector.getProxyHostname());
        connection.setProxyPort(connector.getProxyPort());
        return connection;
    }

    public void doDispose()
    {
        state = null;
    }
}
