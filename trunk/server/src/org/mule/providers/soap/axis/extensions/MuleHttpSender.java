/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/extensions/MuleHttpSender.java,v 1.11 2005/10/27 15:04:25 holger Exp $
 * $Revision: 1.11 $
 * $Date: 2005/10/27 15:04:25 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.soap.axis.extensions;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.DefaultSocketFactory;
import org.apache.axis.components.net.SocketFactory;
import org.apache.axis.components.net.SocketFactoryFactory;
import org.apache.axis.encoding.Base64;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.soap.SOAP12Constants;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.transport.http.ChunkedInputStream;
import org.apache.axis.transport.http.ChunkedOutputStream;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.transport.http.SocketHolder;
import org.apache.axis.transport.http.SocketInputStream;
import org.apache.axis.utils.Messages;
import org.apache.axis.utils.TeeOutputStream;
import org.apache.commons.logging.Log;
import org.mule.util.Utility;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * <code>MuleHttpSender</code> is a rewrite of the Axis HttpSender.
 * Unfortunately, the Axis implementation is not extensible so this class is a
 * copy of it with modifications. The enhancements made are to allow for
 * asynchronous Http method calls which Mule initiates when the endpoint is
 * asynchronous.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.11 $
 */
public class MuleHttpSender extends BasicHandler
{
    protected static Log log = LogFactory.getLog(HTTPSender.class.getName());

    private static final String ACCEPT_HEADERS = HTTPConstants.HEADER_ACCEPT + // Limit
                                                                                // to
                                                                                // the
                                                                                // types
                                                                                // that
                                                                                // are
            // meaningful to us.
            ": " + HTTPConstants.HEADER_ACCEPT_APPL_SOAP + ", " + HTTPConstants.HEADER_ACCEPT_APPLICATION_DIME + ", "
            + HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED + ", " + HTTPConstants.HEADER_ACCEPT_TEXT_ALL + "\r\n"
            + HTTPConstants.HEADER_USER_AGENT + // Tell who we are.
            ": " + Messages.getMessage("axisUserAgent") + "\r\n";

    private static final String CACHE_HEADERS = HTTPConstants.HEADER_CACHE_CONTROL + // Stop
                                                                                        // caching
                                                                                        // proxies
                                                                                        // from
            // caching SOAP reqeuest.
            ": " + HTTPConstants.HEADER_CACHE_CONTROL_NOCACHE + "\r\n" + HTTPConstants.HEADER_PRAGMA + ": "
            + HTTPConstants.HEADER_CACHE_CONTROL_NOCACHE + "\r\n";

    private static final String CHUNKED_HEADER = HTTPConstants.HEADER_TRANSFER_ENCODING + ": "
            + HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED + "\r\n";

    private static final String HEADER_CONTENT_TYPE_LC = HTTPConstants.HEADER_CONTENT_TYPE.toLowerCase();

    private static final String HEADER_LOCATION_LC = HTTPConstants.HEADER_LOCATION.toLowerCase();

    private static final String HEADER_CONTENT_LOCATION_LC = HTTPConstants.HEADER_CONTENT_LOCATION.toLowerCase();

    private static final String HEADER_CONTENT_LENGTH_LC = HTTPConstants.HEADER_CONTENT_LENGTH.toLowerCase();

    private static final String HEADER_TRANSFER_ENCODING_LC = HTTPConstants.HEADER_TRANSFER_ENCODING.toLowerCase();

    /**
     * the url; used for error reporting
     */
    URL targetURL;

    /**
     * invoke creates a socket connection, sends the request SOAP message and
     * then reads the response SOAP message back from the SOAP server
     * 
     * @param msgContext the messsage context
     * 
     * @throws AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault
    {

        if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("enter00", "HTTPSender::invoke"));
        }
        try {
            Call call = (Call) msgContext.getProperty("call_object");
            String transURL = msgContext.getStrProp(MessageContext.TRANS_URL);
            String uri = transURL;
            if(call!=null && call.useSOAPAction()) {
                uri = call.getSOAPActionURI();
            }
            msgContext.setProperty("SOAPAction", uri);

            BooleanHolder useFullURL = new BooleanHolder(false);
            StringBuffer otherHeaders = new StringBuffer();
            targetURL = new URL(transURL);
            String host = targetURL.getHost();
            int port = targetURL.getPort();

            SocketHolder socketHolder = new SocketHolder(null);

            // Send the SOAP request to the server
            InputStream inp = writeToSocket(socketHolder,
                                            msgContext,
                                            targetURL,
                                            otherHeaders,
                                            host,
                                            port,
                                            msgContext.getTimeout(),
                                            useFullURL);

            if (msgContext.isClient() && call!=null) {
                if (Boolean.TRUE.equals(call.getProperty("axis.one.way")))
                    return;
            }

            // Read the response back from the server
            Hashtable headers = new Hashtable();
            inp = readHeadersFromSocket(socketHolder, msgContext, inp, headers);
            readFromSocket(socketHolder, msgContext, inp, headers);
        } catch (Exception e) {
            log.debug(e);
            throw AxisFault.makeFault(e);
        }
        if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("exit00", "HTTPDispatchHandler::invoke"));
        }
    }

    /**
     * Creates a socket connection to the SOAP server
     * 
     * @param protocol "http" for standard, "https" for ssl.
     * @param host host name
     * @param port port to connect to
     * @param otherHeaders buffer for storing additional headers that need to be
     *            sent
     * @param useFullURL flag to indicate if the complete URL has to be sent
     * 
     * @throws java.io.IOException
     */
    protected void getSocket(SocketHolder sockHolder,
                             MessageContext msgContext,
                             String protocol,
                             String host,
                             int port,
                             int timeout,
                             StringBuffer otherHeaders,
                             BooleanHolder useFullURL) throws Exception
    {
        Hashtable options = getOptions();
        if (timeout > 0) {
            if (options == null) {
                options = new Hashtable();
            }
            options.put(DefaultSocketFactory.CONNECT_TIMEOUT, Integer.toString(timeout));
        }
        SocketFactory factory = SocketFactoryFactory.getFactory(protocol, options);
        if (factory == null) {
            throw new IOException(Messages.getMessage("noSocketFactory", protocol));
        }
        // log.fatal("Axis client: connect on socket: " + host + ":" + port);
        Socket sock = null;
        try {
            sock = factory.create(host, port, otherHeaders, useFullURL);
        } catch (Exception e) {
            Thread.sleep(1000);
            try {
                sock = factory.create(host, port, otherHeaders, useFullURL);
            } catch (Exception e1) {
                log.fatal("Axis client Failed: connect on socket: " + host + ":" + port, e);
                throw e;
            }
        }
        if (timeout > 0) {
            sock.setSoTimeout(timeout);
        }
        sockHolder.setSocket(sock);
    }

    /**
     * Send the soap request message to the server
     * 
     * @param msgContext message context
     * @param tmpURL url to connect to
     * @param otherHeaders other headers if any
     * @param host host name
     * @param port port
     * @param useFullURL flag to indicate if the whole url needs to be sent
     * 
     * @throws IOException
     */
    private InputStream writeToSocket(SocketHolder sockHolder,
                                      MessageContext msgContext,
                                      URL tmpURL,
                                      StringBuffer otherHeaders,
                                      String host,
                                      int port,
                                      int timeout,
                                      BooleanHolder useFullURL) throws Exception
    {

        String userID = msgContext.getUsername();
        String passwd = msgContext.getPassword();

        // Get SOAPAction, default to ""
        String action = msgContext.useSOAPAction() ? msgContext.getSOAPActionURI() : "";

        if (action == null) {
            action = "";
        }

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
            StringBuffer tmpBuf = new StringBuffer();

            tmpBuf.append(userID).append(":").append((passwd == null) ? "" : passwd);
            otherHeaders.append(HTTPConstants.HEADER_AUTHORIZATION)
                        .append(": Basic ")
                        .append(Base64.encode(tmpBuf.toString().getBytes()))
                        .append("\r\n");
        }

        // don't forget the cookies!
        // mmm... cookies
        if (msgContext.getMaintainSession()) {
            String cookie = msgContext.getStrProp(HTTPConstants.HEADER_COOKIE);
            String cookie2 = msgContext.getStrProp(HTTPConstants.HEADER_COOKIE2);

            if (cookie != null) {
                otherHeaders.append(HTTPConstants.HEADER_COOKIE).append(": ").append(cookie).append("\r\n");
            }
            if (cookie2 != null) {
                otherHeaders.append(HTTPConstants.HEADER_COOKIE2).append(": ").append(cookie2).append("\r\n");
            }
        }

        StringBuffer header2 = new StringBuffer();

        String webMethod = null;
        boolean posting = true;

        Message reqMessage = msgContext.getRequestMessage();

        boolean http10 = true; // True if this is to use HTTP 1.0 / false HTTP
                                // 1.1
        boolean httpChunkStream = false; // Use HTTP chunking or not.
        boolean httpContinueExpected = false; // Under HTTP 1.1 if false you
                                                // *MAY* need to wait for a 100
                                                // rc,
        // if true the server MUST reply with 100 continue.
        String httpConnection = null;

        String httpver = msgContext.getStrProp(MessageContext.HTTP_TRANSPORT_VERSION);
        if (null == httpver) {
            httpver = HTTPConstants.HEADER_PROTOCOL_V10;
        }
        httpver = httpver.trim();
        if (httpver.equals(HTTPConstants.HEADER_PROTOCOL_V11)) {
            http10 = false;
        }

        // process user defined headers for information.
        Hashtable userHeaderTable = (Hashtable) msgContext.getProperty(HTTPConstants.REQUEST_HEADERS);

        if (userHeaderTable != null) {
            if (null == otherHeaders) {
                otherHeaders = new StringBuffer(1024);
            }

            for (java.util.Iterator e = userHeaderTable.entrySet().iterator(); e.hasNext();) {

                java.util.Map.Entry me = (java.util.Map.Entry) e.next();
                Object keyObj = me.getKey();
                if (null == keyObj)
                    continue;
                String key = keyObj.toString().trim();

                if (key.equalsIgnoreCase(HTTPConstants.HEADER_TRANSFER_ENCODING)) {
                    if (!http10) {
                        String val = me.getValue().toString();
                        if (null != val && val.trim().equalsIgnoreCase(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED))
                            httpChunkStream = true;
                    }
                } else if (key.equalsIgnoreCase(HTTPConstants.HEADER_CONNECTION)) {
                    if (!http10) {
                        String val = me.getValue().toString();
                        if (val.trim().equalsIgnoreCase(HTTPConstants.HEADER_CONNECTION_CLOSE))
                            httpConnection = HTTPConstants.HEADER_CONNECTION_CLOSE;
                    }
                    // HTTP 1.0 will always close.
                    // HTTP 1.1 will use persistent. //no need to specify
                } else {
                    if (!http10 && key.equalsIgnoreCase(HTTPConstants.HEADER_EXPECT)) {
                        String val = me.getValue().toString();
                        if (null != val && val.trim().equalsIgnoreCase(HTTPConstants.HEADER_EXPECT_100_Continue))
                            httpContinueExpected = true;
                    }

                    otherHeaders.append(key).append(": ").append(me.getValue()).append("\r\n");
                }
            }
        }

        if (!http10) {
            // Force close for now.
            // TODO HTTP/1.1
            httpConnection = HTTPConstants.HEADER_CONNECTION_CLOSE;
        }

        header2.append(" ");
        header2.append(http10 ? HTTPConstants.HEADER_PROTOCOL_10 : HTTPConstants.HEADER_PROTOCOL_11).append("\r\n");
        MimeHeaders mimeHeaders = reqMessage.getMimeHeaders();

        if (posting) {
            String contentType;
            if (mimeHeaders.getHeader(HTTPConstants.HEADER_CONTENT_TYPE) != null) {
                contentType = mimeHeaders.getHeader(HTTPConstants.HEADER_CONTENT_TYPE)[0];
            } else {
                contentType = reqMessage.getContentType(msgContext.getSOAPConstants());
            }
            header2.append(HTTPConstants.HEADER_CONTENT_TYPE).append(": ").append(contentType).append("\r\n");
        }

        header2.append(ACCEPT_HEADERS)
               .append(HTTPConstants.HEADER_HOST)
               // used for virtual connections
               .append(": ")
               .append(host)
               .append((port == -1) ? ("") : (":" + port))
               .append("\r\n")
               .append(CACHE_HEADERS)
               .append(HTTPConstants.HEADER_SOAP_ACTION)
               // The SOAP action.
               .append(": \"")
               .append(action)
               .append("\"\r\n");

        if (posting) {
            if (!httpChunkStream) {
                // Content length MUST be sent on HTTP 1.0 requests.
                header2.append(HTTPConstants.HEADER_CONTENT_LENGTH)
                       .append(": ")
                       .append(reqMessage.getContentLength())
                       .append("\r\n");
            } else {
                // Do http chunking.
                header2.append(CHUNKED_HEADER);
            }
        }

        // Transfer MIME headers of SOAPMessage to HTTP headers.
        if (mimeHeaders != null) {
            for (Iterator i = mimeHeaders.getAllHeaders(); i.hasNext();) {
                MimeHeader mimeHeader = (MimeHeader) i.next();
                String headerName = mimeHeader.getName();
                if (headerName.equals(HTTPConstants.HEADER_CONTENT_TYPE)
                        || headerName.equals(HTTPConstants.HEADER_SOAP_ACTION)) {
                    continue;
                }
                header2.append(mimeHeader.getName()).append(": ").append(mimeHeader.getValue()).append("\r\n");
            }
        }

        if (null != httpConnection) {
            header2.append(HTTPConstants.HEADER_CONNECTION);
            header2.append(": ");
            header2.append(httpConnection);
            header2.append("\r\n");
        }

        getSocket(sockHolder, msgContext, targetURL.getProtocol(), host, port, timeout, otherHeaders, useFullURL);

        if (null != otherHeaders) {
            // Add other headers to the end.
            // for pre java1.4 support, we have to turn the string buffer
            // argument into
            // a string before appending.
            header2.append(otherHeaders.toString());
        }

        header2.append("\r\n"); // The empty line to start the BODY.

        StringBuffer header = new StringBuffer();

        // If we're SOAP 1.2, allow the web method to be set from the
        // MessageContext.
        if (msgContext.getSOAPConstants() == SOAPConstants.SOAP12_CONSTANTS) {
            webMethod = msgContext.getStrProp(SOAP12Constants.PROP_WEBMETHOD);
        }
        if (webMethod == null) {
            webMethod = HTTPConstants.HEADER_POST;
        } else {
            posting = webMethod.equals(HTTPConstants.HEADER_POST);
        }

        header.append(webMethod).append(" ");
        if (useFullURL.value) {
            header.append(tmpURL.toExternalForm());
        } else {
            header.append((((tmpURL.getFile() == null) || tmpURL.getFile().equals("")) ? "/" : tmpURL.getFile()));
        }
        header.append(header2.toString());

        OutputStream out = sockHolder.getSocket().getOutputStream();

        if (!posting) {
            out.write(header.toString().getBytes(HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING));
            out.flush();
            return null;
        }

        InputStream inp = null;

        if (httpChunkStream || httpContinueExpected) {
            out.write(header.toString().getBytes(HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING));
        }

        if (httpContinueExpected) { // We need to get a reply from the server as
                                    // to whether
            // it wants us send anything more.
            out.flush();
            Hashtable cheaders = new Hashtable();
            inp = readHeadersFromSocket(sockHolder, msgContext, null, cheaders);
            int returnCode = -1;
            Integer Irc = (Integer) msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE);
            if (null != Irc) {
                returnCode = Irc.intValue();
            }
            if (100 == returnCode) { // got 100 we may continue.
                // Need todo a little msgContext house keeping....
                msgContext.removeProperty(HTTPConstants.MC_HTTP_STATUS_CODE);
                msgContext.removeProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE);
            } else { // If no 100 Continue then we must not send anything!
                String statusMessage = (String) msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE);

                AxisFault fault = new AxisFault("HTTP", "(" + returnCode + ")" + statusMessage, null, null);

                fault.setFaultDetailString(Messages.getMessage("return01", "" + returnCode, ""));
                throw fault;
            }
        }
        ByteArrayOutputStream baos = null;
        if (log.isDebugEnabled()) {
            log.debug(Messages.getMessage("xmlSent00"));
            log.debug("---------------------------------------------------");
            baos = new ByteArrayOutputStream();
        }
        if (httpChunkStream) {
            ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(out);
            out = new BufferedOutputStream(chunkedOutputStream, Constants.HTTP_TXR_BUFFER_SIZE);
            try {
                if (baos != null) {
                    out = new TeeOutputStream(out, baos);
                }
                reqMessage.writeTo(out);
            } catch (SOAPException e) {
                log.error(Messages.getMessage("exception00"), e);
            }
            out.flush();
            chunkedOutputStream.eos();
        } else {
            out = new BufferedOutputStream(out, Constants.HTTP_TXR_BUFFER_SIZE);
            try {
                if (!httpContinueExpected) {
                    out.write(header.toString().getBytes(HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING));
                }
                if (baos != null) {
                    out = new TeeOutputStream(out, baos);
                }
                reqMessage.writeTo(out);
            } catch (SOAPException e) {
                log.error(Messages.getMessage("exception00"), e);
            }
            // Flush ONLY once.
            out.flush();
        }
        if (log.isDebugEnabled()) {
            log.debug(header + new String(baos.toByteArray()));
        }

        return inp;
    }

    private InputStream readHeadersFromSocket(SocketHolder sockHolder,
                                              MessageContext msgContext,
                                              InputStream inp,
                                              Hashtable headers) throws IOException
    {
        byte b = 0;
        int len = 0;
        int colonIndex = -1;
        String name, value;
        int returnCode = 0;
        if (null == inp) {
            inp = new BufferedInputStream(sockHolder.getSocket().getInputStream());
        }

        if (headers == null) {
            headers = new Hashtable();
        }

        // Should help performance. Temporary fix only till its all stream
        // oriented.
        // Need to add logic for getting the version # and the return code
        // but that's for tomorrow!

        /* Logic to read HTTP response headers */
        boolean readTooMuch = false;

        for (ByteArrayOutputStream buf = new ByteArrayOutputStream(4097);;) {
            if (!readTooMuch) {
                b = (byte) inp.read();
            }
            if (b == -1) {
                break;
            }
            readTooMuch = false;
            if ((b != '\r') && (b != '\n')) {
                if ((b == ':') && (colonIndex == -1)) {
                    colonIndex = len;
                }
                len++;
                buf.write(b);
            } else if (b == '\r') {
                continue;
            } else { // b== '\n'
                if (len == 0) {
                    break;
                }
                b = (byte) inp.read();
                readTooMuch = true;

                // A space or tab at the begining of a line means the header
                // continues.
                if ((b == ' ') || (b == '\t')) {
                    continue;
                }
                buf.close();
                byte[] hdata = buf.toByteArray();
                buf.reset();
                if (colonIndex != -1) {
                    name = new String(hdata, 0, colonIndex, HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING);
                    value = new String(hdata,
                                       colonIndex + 1,
                                       len - 1 - colonIndex,
                                       HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING);
                    colonIndex = -1;
                } else {

                    name = new String(hdata, 0, len, HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING);
                    value = "";
                }
                if (log.isDebugEnabled()) {
                    log.debug(name + value);
                }
                if (msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE) == null) {

                    // Reader status code
                    int start = name.indexOf(' ') + 1;
                    String tmp = name.substring(start).trim();
                    int end = tmp.indexOf(' ');

                    if (end != -1) {
                        tmp = tmp.substring(0, end);
                    }
                    returnCode = Integer.parseInt(tmp);
                    msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_CODE, new Integer(returnCode));
                    msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE, name.substring(start + end + 1));
                } else {
                    headers.put(name.toLowerCase(), value);
                }
                len = 0;
            }
        }

        return inp;
    }

    /**
     * Reads the SOAP response back from the server
     * 
     * @param msgContext message context
     * 
     * @throws IOException
     */
    private InputStream readFromSocket(SocketHolder socketHolder,
                                       MessageContext msgContext,
                                       InputStream inp,
                                       Hashtable headers) throws IOException
    {
        Message outMsg = null;
        byte b;

        Integer rc = (Integer) msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE);
        int returnCode = 0;
        if (rc != null) {
            returnCode = rc.intValue();
        } else {
            // No return code?? Should have one by now.
        }

        /* All HTTP headers have been read. */
        String contentType = (String) headers.get(HEADER_CONTENT_TYPE_LC);

        contentType = (null == contentType) ? null : contentType.trim();

        String location = (String) headers.get(HEADER_LOCATION_LC);

        location = (null == location) ? null : location.trim();

        if ((returnCode > 199) && (returnCode < 300)) {
            if (returnCode == 202) {
                return inp;
            }
            // SOAP return is OK - so fall through
        } else if (msgContext.getSOAPConstants() == SOAPConstants.SOAP12_CONSTANTS) {
            // For now, if we're SOAP 1.2, fall through, since the range of
            // valid result codes is much greater
        } else if ((contentType != null) && !contentType.startsWith("text/html")
                && ((returnCode > 499) && (returnCode < 600))) {
            // SOAP Fault should be in here - so fall through
        } else if ((location != null) && ((returnCode == 302) || (returnCode == 307))) {
            // Temporary Redirect (HTTP: 302/307)
            // close old connection
            inp.close();
            socketHolder.getSocket().close();
            // remove former result and set new target url
            msgContext.removeProperty(HTTPConstants.MC_HTTP_STATUS_CODE);
            msgContext.setProperty(MessageContext.TRANS_URL, location);
            // next try
            invoke(msgContext);
            return inp;
        } else if (returnCode == 100) {
            msgContext.removeProperty(HTTPConstants.MC_HTTP_STATUS_CODE);
            msgContext.removeProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE);
            readHeadersFromSocket(socketHolder, msgContext, inp, headers);
            return readFromSocket(socketHolder, msgContext, inp, headers);
        } else {
            // Unknown return code - so wrap up the content into a
            // SOAP Fault.
            ByteArrayOutputStream buf = new ByteArrayOutputStream(4097);

            while (-1 != (b = (byte) inp.read())) {
                buf.write(b);
            }
            String statusMessage = msgContext.getStrProp(HTTPConstants.MC_HTTP_STATUS_MESSAGE);
            AxisFault fault = new AxisFault("HTTP", "(" + returnCode + ")" + statusMessage, null, null);

            fault.setFaultDetailString(Messages.getMessage("return01", "" + returnCode, buf.toString()));
            fault.addFaultDetail(Constants.QNAME_FAULTDETAIL_HTTPERRORCODE, Integer.toString(returnCode));
            throw fault;
        }

        String contentLocation = (String) headers.get(HEADER_CONTENT_LOCATION_LC);

        contentLocation = (null == contentLocation) ? null : contentLocation.trim();

        String contentLength = (String) headers.get(HEADER_CONTENT_LENGTH_LC);

        contentLength = (null == contentLength) ? null : contentLength.trim();

        String transferEncoding = (String) headers.get(HEADER_TRANSFER_ENCODING_LC);

        if (null != transferEncoding) {
            transferEncoding = transferEncoding.trim().toLowerCase();
            if (transferEncoding.equals(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED)) {
                inp = new ChunkedInputStream(inp);
            }
        }

        outMsg = new Message(new SocketInputStream(inp, socketHolder.getSocket()), false, contentType, contentLocation);
        // Transfer HTTP headers of HTTP message to MIME headers of SOAP message
        MimeHeaders mimeHeaders = outMsg.getMimeHeaders();
        for (Enumeration e = headers.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            mimeHeaders.addHeader(key, ((String) headers.get(key)).trim());
        }
        outMsg.setMessageType(Message.RESPONSE);
        msgContext.setResponseMessage(outMsg);
        if (log.isDebugEnabled()) {
            if (null == contentLength) {
                log.debug(Utility.CRLF + Messages.getMessage("no00", "Content-Length"));
            }
            log.debug(Utility.CRLF + Messages.getMessage("xmlRecd00"));
            log.debug("-----------------------------------------------");
            log.debug(outMsg.getSOAPEnvelope().toString());
        }

        // if we are maintaining session state,
        // handle cookies (if any)
        if (msgContext.getMaintainSession()) {
            handleCookie(HTTPConstants.HEADER_COOKIE, HTTPConstants.HEADER_SET_COOKIE, headers, msgContext);
            handleCookie(HTTPConstants.HEADER_COOKIE2, HTTPConstants.HEADER_SET_COOKIE2, headers, msgContext);
        }
        return inp;
    }

    /**
     * little helper function for cookies
     * 
     * @param cookieName
     * @param setCookieName
     * @param headers
     * @param msgContext
     */
    public void handleCookie(String cookieName, String setCookieName, Hashtable headers, MessageContext msgContext)
    {

        if (headers.containsKey(setCookieName.toLowerCase())) {
            String cookie = (String) headers.get(setCookieName.toLowerCase());
            cookie = cookie.trim();

            // chop after first ; a la Apache SOAP (see HTTPUtils.java there)
            int index = cookie.indexOf(';');

            if (index != -1) {
                cookie = cookie.substring(0, index);
            }
            msgContext.setProperty(cookieName, cookie);
        }
    }
}
