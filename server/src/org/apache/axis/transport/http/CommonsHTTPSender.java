/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.net.CommonsHTTPClientProperties;
import org.apache.axis.components.net.CommonsHTTPClientPropertiesFactory;
import org.apache.axis.components.net.TransportClientProperties;
import org.apache.axis.components.net.TransportClientPropertiesFactory;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.soap.SOAP12Constants;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.Messages;
import org.apache.axis.utils.NetworkUtils;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;

/**
 * This class uses Jakarta Commons's HttpClient to call a SOAP server.
 *
 * @author Davanum Srinivas (dims@yahoo.com)
 * History: By Chandra Talluri
 * Modifications done for maintaining sessions. Cookies needed to be set on
 * HttpState not on MessageContext, since ttpMethodBase overwrites the cookies 
 * from HttpState. Also we need to setCookiePolicy on HttpState to 
 * CookiePolicy.COMPATIBILITY else it is defaulting to RFC2109Spec and adding 
 * Version information to it and tomcat server not recognizing it
 */
public class CommonsHTTPSender extends BasicHandler {
    
    /** Field log           */
    protected static Log log =
        LogFactory.getLog(CommonsHTTPSender.class.getName());

    private static final String CHUNKED_PROP = "axis.chunked";

    protected static HttpConnectionManager defaultConnectionManager;
    protected static CommonsHTTPClientProperties defaultClientProperties;
    
    protected HttpConnectionManager connectionManager;
    protected CommonsHTTPClientProperties clientProperties;

    public CommonsHTTPSender() {
        initialize();
    }

    protected void initialize() {
        initializeDefaultConnectionManager();
        
        this.clientProperties = defaultClientProperties;
        this.connectionManager = defaultConnectionManager;
    }

    protected static synchronized void initializeDefaultConnectionManager() {
        if (defaultConnectionManager != null) {
            // defults already initialized
            return;
        }

        MultiThreadedHttpConnectionManager cm = 
            new MultiThreadedHttpConnectionManager();

        defaultClientProperties = CommonsHTTPClientPropertiesFactory.create();

        cm.getParams().setDefaultMaxConnectionsPerHost(
                     defaultClientProperties.getMaximumConnectionsPerHost());
        cm.getParams().setMaxTotalConnections(
                     defaultClientProperties.getMaximumTotalConnections());

        // If defined, set the default timeouts
        // Can be overridden by the MessageContext
        if(defaultClientProperties.getDefaultConnectionTimeout()>0) {
            cm.getParams().setConnectionTimeout(
                     defaultClientProperties.getDefaultConnectionTimeout());
        }
        if(defaultClientProperties.getDefaultSoTimeout()>0) {
            cm.getParams().setSoTimeout(
                     defaultClientProperties.getDefaultSoTimeout());
        }
        
        defaultConnectionManager = cm;
    }
    
    /**
     * invoke creates a socket connection, sends the request SOAP message and then
     * reads the response SOAP message back from the SOAP server
     *
     * @param msgContext the messsage context
     *
     * @throws AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        HttpMethodBase method = null;
        if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("enter00",
                                          "CommonsHTTPSender::invoke"));
        }
        try {
            URL targetURL =
                new URL(msgContext.getStrProp(MessageContext.TRANS_URL));
            
            // no need to retain these, as the cookies/credentials are
            // stored in the message context across multiple requests.
            // the underlying connection manager, however, is retained
            // so sockets get recycled when possible.
            HttpClient httpClient = new HttpClient(this.connectionManager);
            // the timeout value for allocation of connections from the pool
            httpClient.getParams().setConnectionManagerTimeout(this.clientProperties.getConnectionPoolTimeout());

            HostConfiguration hostConfiguration = 
                getHostConfiguration(httpClient, msgContext, targetURL);
            
            boolean posting = true;
            
            // If we're SOAP 1.2, allow the web method to be set from the
            // MessageContext.
            if (msgContext.getSOAPConstants() == SOAPConstants.SOAP12_CONSTANTS) {
                String webMethod = msgContext.getStrProp(SOAP12Constants.PROP_WEBMETHOD);
                if (webMethod != null) {
                    posting = webMethod.equals(HTTPConstants.HEADER_POST);
                }
            }

            if (posting) {
                Message reqMessage = msgContext.getRequestMessage();
                method = new PostMethod(targetURL.toString());

                // set false as default, addContetInfo can overwrite
                method.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE,
                                                       false);

                addContextInfo(method, httpClient, msgContext, targetURL);
                
                boolean httpChunkStream = 
                    method.getParams().getBooleanParameter(CHUNKED_PROP, true);
                
                MessageRequestEntity requestEntity = null;
                if (msgContext.isPropertyTrue(HTTPConstants.MC_GZIP_REQUEST)) {
                	requestEntity = new GzipMessageRequestEntity(method, reqMessage, httpChunkStream);
                } else {
                	requestEntity = new MessageRequestEntity(method, reqMessage, httpChunkStream);
                }
                ((PostMethod)method).setRequestEntity(requestEntity);
            } else {
                method = new GetMethod(targetURL.toString());
                addContextInfo(method, httpClient, msgContext, targetURL);
            }

            String httpVersion = 
                msgContext.getStrProp(MessageContext.HTTP_TRANSPORT_VERSION);
            if (httpVersion != null) {
                if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_V10)) {
                    method.getParams().setVersion(HttpVersion.HTTP_1_0);
                }
                // assume 1.1
            }
            
            // don't forget the cookies!
            // Cookies need to be set on HttpState, since HttpMethodBase 
            // overwrites the cookies from HttpState
            if (msgContext.getMaintainSession()) {
                HttpState state = httpClient.getState();
                method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
                String host = hostConfiguration.getHost();
                String path = targetURL.getPath();
                boolean secure = hostConfiguration.getProtocol().isSecure();
                fillHeaders(msgContext, state, HTTPConstants.HEADER_COOKIE, host, path, secure);
                fillHeaders(msgContext, state, HTTPConstants.HEADER_COOKIE2, host, path, secure);
                httpClient.setState(state);
            }

            int returnCode = httpClient.executeMethod(hostConfiguration, method, null);
            String statusMessage = method.getStatusText();

            msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_CODE,
                                   new Integer(returnCode));
            msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE,
                                   statusMessage);

            String contentType = 
                getHeader(method, HTTPConstants.HEADER_CONTENT_TYPE);
            String contentLocation = 
                getHeader(method, HTTPConstants.HEADER_CONTENT_LOCATION);
            String contentLength = 
                getHeader(method, HTTPConstants.HEADER_CONTENT_LENGTH);

            if ((returnCode > 199) && (returnCode < 300)) {
                
                // SOAP return is OK - so fall through
            } else if (msgContext.getSOAPConstants() ==
                       SOAPConstants.SOAP12_CONSTANTS) {
                // For now, if we're SOAP 1.2, fall through, since the range of
                // valid result codes is much greater
            } else if ((contentType != null) && !contentType.startsWith("text/html")
                       && ((returnCode > 499) && (returnCode < 600))) {
                
                // SOAP Fault should be in here - so fall through
            } else {
                AxisFault fault = new AxisFault("HTTP",
                                                "(" + returnCode + ")"
                                                + statusMessage, null,
                                                null);
                
                try {
                    fault.setFaultDetailString(
                         Messages.getMessage("return01",
                                             "" + returnCode,
                                             method.getResponseBodyAsString()));
                    fault.addFaultDetail(Constants.QNAME_FAULTDETAIL_HTTPERRORCODE,
                                         Integer.toString(returnCode));
                    throw fault;
                } finally {
                    method.releaseConnection(); // release connection back to pool.
                }
            }
            
            if (contentLength != null) {
                long length = Long.parseLong(contentLength);
                
                if (length == 0) {
                    method.releaseConnection();
                    return;
                }
            }

            // wrap the response body stream so that close() also releases 
            // the connection back to the pool.
            InputStream releaseConnectionOnCloseStream = 
                createConnectionReleasingInputStream(method);

            Header contentEncoding = 
            	method.getResponseHeader(HTTPConstants.HEADER_CONTENT_ENCODING);
            if (contentEncoding != null) {
            	if (contentEncoding.getValue().
            			equalsIgnoreCase(HTTPConstants.COMPRESSION_GZIP)) {
            		releaseConnectionOnCloseStream = 
            			new GZIPInputStream(releaseConnectionOnCloseStream);
            	} else {
                    AxisFault fault = new AxisFault("HTTP",
                            "unsupported content-encoding of '" 
                    		+ contentEncoding.getValue()
                            + "' found", null, null);
                    throw fault;
            	}
            		
            }
            Message outMsg = new Message(releaseConnectionOnCloseStream,
                                         false, contentType, contentLocation);
            // Transfer HTTP headers of HTTP message to MIME headers of SOAP message
            Header[] responseHeaders = method.getResponseHeaders();
            MimeHeaders responseMimeHeaders = outMsg.getMimeHeaders();
            for (int i = 0; i < responseHeaders.length; i++) {
                Header responseHeader = responseHeaders[i];
                responseMimeHeaders.addHeader(responseHeader.getName(), 
                                              responseHeader.getValue());
            }
            outMsg.setMessageType(Message.RESPONSE);
            msgContext.setResponseMessage(outMsg);
            if (log.isDebugEnabled()) {
                if (null == contentLength) {
                    log.debug("\n"
                    + Messages.getMessage("no00", "Content-Length"));
                }
                log.debug("\n" + Messages.getMessage("xmlRecd00"));
                log.debug("-----------------------------------------------");
                log.debug(outMsg.getSOAPPartAsString());
            }
            
            // if we are maintaining session state,
            // handle cookies (if any)
            if (msgContext.getMaintainSession()) {
                Header[] headers = method.getResponseHeaders();

                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].getName().equalsIgnoreCase(HTTPConstants.HEADER_SET_COOKIE)) {
                        handleCookie(HTTPConstants.HEADER_COOKIE, headers[i].getValue(), msgContext);
                    } else if (headers[i].getName().equalsIgnoreCase(HTTPConstants.HEADER_SET_COOKIE2)) {
                        handleCookie(HTTPConstants.HEADER_COOKIE2, headers[i].getValue(), msgContext);
                    }
                }
            }
            
        } catch (Exception e) {
            log.debug(e);
            throw AxisFault.makeFault(e);
        }
        
        if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("exit00",
                                          "CommonsHTTPSender::invoke"));
        }
    }

    /**
     * little helper function for cookies. fills up the message context with
     * a string or an array of strings (if there are more than one Set-Cookie)
     *
     * @param cookieName
     * @param setCookieName
     * @param cookie
     * @param msgContext
     */
    public void handleCookie(String cookieName, String cookie,
            MessageContext msgContext) {
        
        cookie = cleanupCookie(cookie);
        int keyIndex = cookie.indexOf("=");
        String key = (keyIndex != -1) ? cookie.substring(0, keyIndex) : cookie;
        
        ArrayList cookies = new ArrayList();
        Object oldCookies = msgContext.getProperty(cookieName);
        boolean alreadyExist = false;
        if(oldCookies != null) {
            if(oldCookies instanceof String[]) {
                String[] oldCookiesArray = (String[])oldCookies;
                for(int i = 0; i < oldCookiesArray.length; i++) {
                    String anOldCookie = oldCookiesArray[i];
                    if (key != null && anOldCookie.indexOf(key) == 0) { // same cookie key
                        anOldCookie = cookie;             // update to new one
                        alreadyExist = true;
                    }
                    cookies.add(anOldCookie);
                }
            } else {
				String oldCookie = (String)oldCookies;
                if (key != null && oldCookie.indexOf(key) == 0) { // same cookie key
					oldCookie = cookie;             // update to new one
                    alreadyExist = true;
                }
                cookies.add(oldCookie);
            }
        }
        
        if (!alreadyExist) {
            cookies.add(cookie);
        }
        
        if(cookies.size()==1) {
            msgContext.setProperty(cookieName, cookies.get(0));
        } else if (cookies.size() > 1) {
            msgContext.setProperty(cookieName, cookies.toArray(new String[cookies.size()]));
        }
    }
    
    /**
     * Add cookies from message context
     *
     * @param msgContext
     * @param state
     * @param header
     * @param host
     * @param path
     * @param secure
     */
    private void fillHeaders(MessageContext msgContext, HttpState state, String header, String host, String path, boolean secure) {
        Object ck1 = msgContext.getProperty(header);
        if (ck1 != null) {
            if (ck1 instanceof String[]) {
                String [] cookies = (String[]) ck1;
                for (int i = 0; i < cookies.length; i++) {
                    addCookie(state, cookies[i], host, path, secure);
                }
            } else {
                addCookie(state, (String) ck1, host, path, secure);
            }
        }
    }

    /**
     * add cookie to state
     * @param state
     * @param cookie
     */
    private void addCookie(HttpState state, String cookie,String host, String path, boolean secure) {
        int index = cookie.indexOf('=');
        state.addCookie(new Cookie(host, cookie.substring(0, index),
                cookie.substring(index + 1), path,
                null, secure));
    }

    /**
     * cleanup the cookie value.
     *
     * @param cookie initial cookie value
     *
     * @return a cleaned up cookie value.
     */
    private String cleanupCookie(String cookie) {
        cookie = cookie.trim();
        // chop after first ; a la Apache SOAP (see HTTPUtils.java there)
        int index = cookie.indexOf(';');
        if (index != -1) {
            cookie = cookie.substring(0, index);
        }
        return cookie;
    }
    
    protected HostConfiguration getHostConfiguration(HttpClient client, 
                                                     MessageContext context,
                                                     URL targetURL) {
        TransportClientProperties tcp = 
            TransportClientPropertiesFactory.create(targetURL.getProtocol()); // http or https
        int port = targetURL.getPort();
        boolean hostInNonProxyList =
            isHostInNonProxyList(targetURL.getHost(), tcp.getNonProxyHosts());
        
        HostConfiguration config = new HostConfiguration();
        
        if (port == -1) {
        	if(targetURL.getProtocol().equalsIgnoreCase("https")) {
        		port = 443;		// default port for https being 443
        	} else { // it must be http
        		port = 80;		// default port for http being 80
        	}
        }
        
        if(hostInNonProxyList){
            config.setHost(targetURL.getHost(), port, targetURL.getProtocol());
        } else {
            if (tcp.getProxyHost().length() == 0 ||
                tcp.getProxyPort().length() == 0) {
                config.setHost(targetURL.getHost(), port, targetURL.getProtocol());
            } else {
                if (tcp.getProxyUser().length() != 0) {
                    Credentials proxyCred = 
                        new UsernamePasswordCredentials(tcp.getProxyUser(),
                                                        tcp.getProxyPassword());
                    // if the username is in the form "user\domain" 
                    // then use NTCredentials instead.
                    int domainIndex = tcp.getProxyUser().indexOf("\\");
                    if (domainIndex > 0) {
                        String domain = tcp.getProxyUser().substring(0, domainIndex);
                        if (tcp.getProxyUser().length() > domainIndex + 1) {
                            String user = tcp.getProxyUser().substring(domainIndex + 1);
                            proxyCred = new NTCredentials(user,
                                            tcp.getProxyPassword(),
                                            tcp.getProxyHost(), domain);
                        }
                    }
                    client.getState().setProxyCredentials(AuthScope.ANY, proxyCred);
                }
                int proxyPort = new Integer(tcp.getProxyPort()).intValue();
                config.setProxy(tcp.getProxyHost(), proxyPort);
            }
        }
        return config;
    }
    
    /**
     * Extracts info from message context.
     *
     * @param method Post method
     * @param httpClient The client used for posting
     * @param msgContext the message context
     * @param tmpURL the url to post to.
     *
     * @throws Exception
     */
    private void addContextInfo(HttpMethodBase method, 
                                HttpClient httpClient, 
                                MessageContext msgContext, 
                                URL tmpURL)
        throws Exception {
        
        // optionally set a timeout for the request
        if (msgContext.getTimeout() != 0) {
            /* ISSUE: these are not the same, but MessageContext has only one
                      definition of timeout */
            // SO_TIMEOUT -- timeout for blocking reads
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(msgContext.getTimeout());
            // timeout for initial connection
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(msgContext.getTimeout());
        }
        
        // Get SOAPAction, default to ""
        String action = msgContext.useSOAPAction()
            ? msgContext.getSOAPActionURI()
            : "";
        
        if (action == null) {
            action = "";
        }

        Message msg = msgContext.getRequestMessage();
        if (msg != null){
            method.setRequestHeader(new Header(HTTPConstants.HEADER_CONTENT_TYPE,
                                               msg.getContentType(msgContext.getSOAPConstants())));
        }
        method.setRequestHeader(new Header(HTTPConstants.HEADER_SOAP_ACTION, 
                                           "\"" + action + "\""));
        method.setRequestHeader(new Header(HTTPConstants.HEADER_USER_AGENT, Messages.getMessage("axisUserAgent")));
        String userID = msgContext.getUsername();
        String passwd = msgContext.getPassword();
        
        // if UserID is not part of the context, but is in the URL, use
        // the one in the URL.
        if ((userID == null) && (tmpURL.getUserInfo() != null)) {
            String info = tmpURL.getUserInfo();
            int sep = info.indexOf(':');
            
            if ((sep >= 0) && (sep + 1 < info.length())) {
                userID = info.substring(0, sep);
                passwd = info.substring(sep + 1);
            } else {
                userID = info;
            }
        }
        if (userID != null) {
            Credentials proxyCred =
                new UsernamePasswordCredentials(userID,
                                                passwd);
            // if the username is in the form "user\domain"
            // then use NTCredentials instead.
            int domainIndex = userID.indexOf("\\");
            if (domainIndex > 0) {
                String domain = userID.substring(0, domainIndex);
                if (userID.length() > domainIndex + 1) {
                    String user = userID.substring(domainIndex + 1);
                    proxyCred = new NTCredentials(user,
                                    passwd,
                                    NetworkUtils.getLocalHostname(), domain);
                }
            }
            httpClient.getState().setCredentials(AuthScope.ANY, proxyCred);
        }
        
        // add compression headers if needed
        if (msgContext.isPropertyTrue(HTTPConstants.MC_ACCEPT_GZIP)) {
        	method.addRequestHeader(HTTPConstants.HEADER_ACCEPT_ENCODING, 
        			HTTPConstants.COMPRESSION_GZIP);
        }
        if (msgContext.isPropertyTrue(HTTPConstants.MC_GZIP_REQUEST)) {
        	method.addRequestHeader(HTTPConstants.HEADER_CONTENT_ENCODING, 
        			HTTPConstants.COMPRESSION_GZIP);
        }
        
        // Transfer MIME headers of SOAPMessage to HTTP headers. 
        MimeHeaders mimeHeaders = msg.getMimeHeaders();
        if (mimeHeaders != null) {
            for (Iterator i = mimeHeaders.getAllHeaders(); i.hasNext(); ) {
                MimeHeader mimeHeader = (MimeHeader) i.next();
                //HEADER_CONTENT_TYPE and HEADER_SOAP_ACTION are already set.
                //Let's not duplicate them.
                String headerName = mimeHeader.getName();
                if (headerName.equals(HTTPConstants.HEADER_CONTENT_TYPE)
                        || headerName.equals(HTTPConstants.HEADER_SOAP_ACTION)) {
                        continue;
                }
                method.addRequestHeader(mimeHeader.getName(), 
                                        mimeHeader.getValue());
            }
        }

        // process user defined headers for information.
        Hashtable userHeaderTable =
            (Hashtable) msgContext.getProperty(HTTPConstants.REQUEST_HEADERS);
        
        if (userHeaderTable != null) {
            for (Iterator e = userHeaderTable.entrySet().iterator();
                 e.hasNext();) {
                Map.Entry me = (Map.Entry) e.next();
                Object keyObj = me.getKey();
                
                if (null == keyObj) {
                    continue;
                }
                String key = keyObj.toString().trim();
                String value = me.getValue().toString().trim();
                
                if (key.equalsIgnoreCase(HTTPConstants.HEADER_EXPECT) &&
                    value.equalsIgnoreCase(HTTPConstants.HEADER_EXPECT_100_Continue)) {
                    method.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE,
                                                           true);
                } else if (key.equalsIgnoreCase(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED)) {
                    String val = me.getValue().toString();
                    if (null != val)  {
                        method.getParams().setBooleanParameter(CHUNKED_PROP, 
                                                               JavaUtils.isTrue(val));
                    }
                } else {
                    method.addRequestHeader(key, value);
                }
            }
        }
    }
    
    /**
     * Check if the specified host is in the list of non proxy hosts.
     *
     * @param host host name
     * @param nonProxyHosts string containing the list of non proxy hosts
     *
     * @return true/false
     */
    protected boolean isHostInNonProxyList(String host, String nonProxyHosts) {
        
        if ((nonProxyHosts == null) || (host == null)) {
            return false;
        }
        
        /*
         * The http.nonProxyHosts system property is a list enclosed in
         * double quotes with items separated by a vertical bar.
         */
        StringTokenizer tokenizer = new StringTokenizer(nonProxyHosts, "|\"");
        
        while (tokenizer.hasMoreTokens()) {
            String pattern = tokenizer.nextToken();
            
            if (log.isDebugEnabled()) {
                log.debug(Messages.getMessage("match00",
                new String[]{"HTTPSender",
                host,
                pattern}));
            }
            if (match(pattern, host, false)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Matches a string against a pattern. The pattern contains two special
     * characters:
     * '*' which means zero or more characters,
     *
     * @param pattern the (non-null) pattern to match against
     * @param str     the (non-null) string that must be matched against the
     *                pattern
     * @param isCaseSensitive
     *
     * @return <code>true</code> when the string matches against the pattern,
     *         <code>false</code> otherwise.
     */
    protected static boolean match(String pattern, String str,
                                   boolean isCaseSensitive) {
        
        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;
        boolean containsStar = false;
        
        for (int i = 0; i < patArr.length; i++) {
            if (patArr[i] == '*') {
                containsStar = true;
                break;
            }
        }
        if (!containsStar) {
            
            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false;        // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (isCaseSensitive && (ch != strArr[i])) {
                    return false;    // Character mismatch
                }
                if (!isCaseSensitive
                && (Character.toUpperCase(ch)
                != Character.toUpperCase(strArr[i]))) {
                    return false;    // Character mismatch
                }
            }
            return true;             // String matches against pattern
        }
        if (patIdxEnd == 0) {
            return true;    // Pattern contains only '*', which matches anything
        }
        
        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*'
        && (strIdxStart <= strIdxEnd)) {
            if (isCaseSensitive && (ch != strArr[strIdxStart])) {
                return false;    // Character mismatch
            }
            if (!isCaseSensitive
            && (Character.toUpperCase(ch)
            != Character.toUpperCase(strArr[strIdxStart]))) {
                return false;    // Character mismatch
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }
        
        // Process characters after last star
        while ((ch = patArr[patIdxEnd]) != '*' && (strIdxStart <= strIdxEnd)) {
            if (isCaseSensitive && (ch != strArr[strIdxEnd])) {
                return false;    // Character mismatch
            }
            if (!isCaseSensitive
            && (Character.toUpperCase(ch)
            != Character.toUpperCase(strArr[strIdxEnd]))) {
                return false;    // Character mismatch
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }
        
        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while ((patIdxStart != patIdxEnd) && (strIdxStart <= strIdxEnd)) {
            int patIdxTmp = -1;
            
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            
            strLoop:
                for (int i = 0; i <= strLength - patLength; i++) {
                    for (int j = 0; j < patLength; j++) {
                        ch = patArr[patIdxStart + j + 1];
                        if (isCaseSensitive
                        && (ch != strArr[strIdxStart + i + j])) {
                            continue strLoop;
                        }
                        if (!isCaseSensitive && (Character
                        .toUpperCase(ch) != Character
                        .toUpperCase(strArr[strIdxStart + i + j]))) {
                            continue strLoop;
                        }
                    }
                    foundIdx = strIdxStart + i;
                    break;
                }
                if (foundIdx == -1) {
                    return false;
                }
                patIdxStart = patIdxTmp;
                strIdxStart = foundIdx + patLength;
        }
        
        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
    }

    private static String getHeader(HttpMethodBase method, String headerName) {
        Header header = method.getResponseHeader(headerName);
        return (header == null) ? null : header.getValue().trim();
    }

    private InputStream createConnectionReleasingInputStream(final HttpMethodBase method) throws IOException {
        return new FilterInputStream(method.getResponseBodyAsStream()) {
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        method.releaseConnection();
                    }
                }
            };
    }

    private static class MessageRequestEntity implements RequestEntity {
        
        private HttpMethodBase method;
        private Message message;
        boolean httpChunkStream = true; //Use HTTP chunking or not.

        public MessageRequestEntity(HttpMethodBase method, Message message) {
            this.message = message;
            this.method = method;
        }

        public MessageRequestEntity(HttpMethodBase method, Message message, boolean httpChunkStream) {
            this.message = message;
            this.method = method;
            this.httpChunkStream = httpChunkStream;
        }

        public boolean isRepeatable() {
            return true;
        }

        public void writeRequest(OutputStream out) throws IOException {
            try {
                this.message.writeTo(out);
            } catch (SOAPException e) {
                throw new IOException(e.getMessage());
            }
        }

        protected boolean isContentLengthNeeded() {
        	return this.method.getParams().getVersion() == HttpVersion.HTTP_1_0 || !httpChunkStream;
        }
        
        public long getContentLength() {
            if (isContentLengthNeeded()) {
                try {
                    return message.getContentLength();
                } catch (Exception e) {
                }
            } 
            return -1; /* -1 for chunked */
        }

        public String getContentType() {
            return null; // a separate header is added
        }
        
    }
    
    private static class GzipMessageRequestEntity extends MessageRequestEntity {

    	public GzipMessageRequestEntity(HttpMethodBase method, Message message) {
    		super(method, message);
        }

        public GzipMessageRequestEntity(HttpMethodBase method, Message message, boolean httpChunkStream) {
        	super(method, message, httpChunkStream);
        }
        
        public void writeRequest(OutputStream out) throws IOException {
        	if (cachedStream != null) {
        		cachedStream.writeTo(out);
        	} else {
        		GZIPOutputStream gzStream = new GZIPOutputStream(out);
        		super.writeRequest(gzStream);
        		gzStream.finish();
        	}
        }
        
        public long getContentLength() {
        	if(isContentLengthNeeded()) {
        		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        		try {
        			writeRequest(baos);
        			cachedStream = baos;
        			return baos.size();
        		} catch (IOException e) {
        			// fall through to doing chunked.
        		}
        	}
        	return -1; // do chunked 
        }
        
        private ByteArrayOutputStream cachedStream;
    }
}



