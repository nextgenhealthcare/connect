/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/AxisServiceComponent.java,v 1.12 2005/09/21 15:27:45 rsears Exp $
 * $Revision: 1.12 $
 * $Date: 2005/09/21 15:27:45 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.soap.axis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.ConfigurationException;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.i18n.Messages;
import org.apache.axis.security.servlet.ServletSecurityProvider;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.ServletEndpointContextImpl;
import org.apache.axis.utils.Admin;
import org.apache.axis.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.mule.MuleManager;
import org.mule.impl.MuleMessage;
import org.mule.impl.RequestContext;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.WriterMessageAdapter;
import org.mule.providers.http.HttpConnector;
import org.mule.providers.http.HttpConstants;
import org.mule.providers.soap.axis.extensions.MuleConfigProvider;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.Callable;
import org.mule.umo.lifecycle.Initialisable;
import org.mule.umo.lifecycle.InitialisationException;
import org.w3c.dom.Document;

/**
 * <code>AxisServiceComponent</code> is a Mule component implementation of the
 * Axis servlet. This component supports all the features of the Axis servlet
 * except -
 * <ol>
 * <li>Jws class services are not supported as they don't add any value to the
 * Mule model</li>
 * <li>Currently there is no HttpSession support. This will be fixed when
 * Session support is added to the Http Connector</li>
 * </ol>
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.12 $
 */

public class AxisServiceComponent implements Initialisable, Callable
{
    /**
     * logger used by this class
     */
    protected static transient Log logger = org.apache.commons.logging.LogFactory.getLog(AxisServiceComponent.class);

    private static Log tlog = LogFactory.getLog("org.apache.axis.TIME");
    private static Log exceptionLog = LogFactory.getLog("org.apache.axis.EXCEPTIONS");
    public static final String INIT_PROPERTY_TRANSPORT_NAME = "transport.name";
    public static final String INIT_PROPERTY_USE_SECURITY = "use-servlet-security";
    public static final String INIT_PROPERTY_ENABLE_LIST = "axis.enableListQuery";
    public static final String DEFAULT_AXIS_HOME = "/axisHome";
    private String transportName = "http";
    private ServletSecurityProvider securityProvider;
    private boolean enableList;
    private String homeDir;
    private AxisServer axisServer;

    public AxisServiceComponent()
    {
        securityProvider = null;
        enableList = true;
    }

    /**
     * Passes the context to the listener
     * 
     * @param context the context ot process
     * @return Object this object can be anything. When the
     *         <code>UMOLifecycleAdapter</code> for the component receives
     *         this object it will first see if the Object is an
     *         <code>UMOEvent</code> if not and the Object is not null a new
     *         context will be created using the returned object as the payload.
     *         This new context will then get published to the configured
     *         outbound endpoint if-
     *         <ol>
     *         <li>One has been configured for the UMO.</li>
     *         <li>the <code>setStopFurtherProcessing(true)</code> wasn't
     *         called on the previous context.</li>
     *         </ol>
     * @throws Exception if the context fails to process properly. If exceptions
     *             aren't handled by the implementation they will be handled by
     *             the exceptionListener associated with the component
     */
    public Object onCall(UMOEventContext context) throws Exception
    {
        String method = (String) context.getProperty(HttpConnector.HTTP_METHOD_PROPERTY, "POST");
        WriterMessageAdapter response = new WriterMessageAdapter(new StringWriter());
        
        if ("GET".equals(method.toUpperCase())) {
            doGet(context, response);
        } else {
            doPost(context, response);
        }
        response.getWriter().close();
        return new MuleMessage(response);
    }

    public void initialise() throws InitialisationException
    {

    }

    public void doGet(UMOEventContext context, WriterMessageAdapter response) throws UMOException, IOException
    {
        try {
//            UMOEndpointURI endpointUri = context.getEndpointURI();
            String uri = "soap:" + context.getEndpointURI().toString();
//            int i = uri.indexOf("?");
//            if(i > -1) {
//                uri = uri.substring(0, i);
//            }
            //update stats
            uri += context.getMessageAsString();
            UMOEndpointURI endpointUri = new MuleEndpointURI(uri);
            AxisEngine engine = getAxisServer();
            String pathInfo = endpointUri.getPath();
            // String realpath =
            // servletContext.getRealPath(request.getServletPath());
            boolean wsdlRequested = false;
            boolean listRequested = false;

            if (endpointUri.getAddress().endsWith(".jws")) {
                throw new AxisFault("Jws not supported by the Mule Axis service");
            }

            String queryString = endpointUri.getQuery();
            if (queryString != null) {
                if (queryString.equalsIgnoreCase("wsdl")) {
                    wsdlRequested = true;
                } else {
                    if (queryString.equalsIgnoreCase("list")) {
                        listRequested = true;
                    }
                }
            }
            boolean hasNoPath = pathInfo == null || pathInfo.equals("") || pathInfo.equals("/");
            if (!wsdlRequested && !listRequested && hasNoPath) {
                reportAvailableServices(context, response);
            } else {
                // if(realpath != null)
                MessageContext msgContext = new MessageContext(engine);
                populateMessageContext(msgContext, context, endpointUri);

                msgContext.setProperty("transport.url", endpointUri.toString().replaceFirst("soap:", "http:"));
                if (wsdlRequested)
                    processWsdlRequest(msgContext, response);
                else if (listRequested)
                    processListRequest(response);
                else if (true /* hasParameters */) {
                    processMethodRequest(msgContext, context, response, endpointUri);
                } else {
                    String serviceName = (String) msgContext.getProperty("serviceName");
                    if (pathInfo.startsWith("/"))
                        serviceName = pathInfo.substring(1);
                    else
                        serviceName = pathInfo;
                    SOAPService s = engine.getService(serviceName);
                    if (s == null) {
                        reportCantGetAxisService(context, response);
                    } else {
                        reportServiceInfo(response, s, serviceName);
                    }
                }
            }
        } catch (AxisFault fault) {
            reportTroubleInGet(fault, response);
        } catch (Exception e) {
            reportTroubleInGet(e, response);
        }
    }

    private void reportTroubleInGet(Exception exception, WriterMessageAdapter response)
    {
        response.setProperty(HttpConstants.HEADER_CONTENT_TYPE, "text/html");
        response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "500");
        response.write("<h2>" + Messages.getMessage("error00") + "</h2>");
        response.write("<p>" + Messages.getMessage("somethingWrong00") + "</p>");
        if (exception instanceof AxisFault) {
            AxisFault fault = (AxisFault) exception;
            processAxisFault(fault);
            writeFault(response, fault);
        } else {
            logException(exception);
            response.write("<pre>Exception - " + exception + "<br>");
            response.write("</pre>");
        }
    }

    protected void processAxisFault(AxisFault fault)
    {
        org.w3c.dom.Element runtimeException = fault.lookupFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
        if (runtimeException != null) {
            exceptionLog.info(Messages.getMessage("axisFault00"), fault);
            fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
        } else if (exceptionLog.isDebugEnabled()) {
            exceptionLog.debug(Messages.getMessage("axisFault00"), fault);
        }

    }

    protected void logException(Exception e)
    {
        exceptionLog.info(Messages.getMessage("exception00"), e);
    }

    private void writeFault(WriterMessageAdapter response, AxisFault axisFault)
    {
        String localizedMessage = XMLUtils.xmlEncodeString(axisFault.getLocalizedMessage());
        response.write("<pre>Fault - " + localizedMessage + "<br>");
        response.write(axisFault.dumpToString());
        response.write("</pre>");
    }

    protected void processMethodRequest(MessageContext msgContext,
                                        UMOEventContext context,
                                        WriterMessageAdapter response,
                                        UMOEndpointURI endpointUri) throws AxisFault
    {
        Properties params = endpointUri.getUserParams();

        String method = (String) params.remove("method");
        if (method == null) {
            method = endpointUri.getPath().substring(endpointUri.getPath().lastIndexOf("/") + 1);
        }
        StringBuffer args = new StringBuffer();

        Map.Entry entry;
        for (Iterator iterator = params.entrySet().iterator(); iterator.hasNext();) {
            entry = (Map.Entry) iterator.next();
            args.append("<").append(entry.getKey()).append(">");
            args.append(entry.getValue());
            args.append("</").append(entry.getKey()).append(">");
        }

        if (method == null) {
            response.setProperty(HttpConstants.HEADER_CONTENT_TYPE, "text/html");
            response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "400");
            response.write("<h2>" + Messages.getMessage("error00") + ":  " + Messages.getMessage("invokeGet00")
                    + "</h2>");
            response.write("<p>" + Messages.getMessage("noMethod01") + "</p>");
        } else {
            invokeEndpointFromGet(msgContext, response, method, args.toString());
        }
    }

    protected void processWsdlRequest(MessageContext msgContext, WriterMessageAdapter response) throws AxisFault
    {
        AxisEngine engine = getAxisServer();
        try {
            engine.generateWSDL(msgContext);
            Document doc = (Document) msgContext.getProperty("WSDL");
            if (doc != null) {
                response.setProperty(HttpConstants.HEADER_CONTENT_TYPE, "text/xml");
                XMLUtils.DocumentToWriter(doc, response.getWriter());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("processWsdlRequest: failed to create WSDL");
                }
                reportNoWSDL(response, "noWSDL02", null);
            }
        } catch (AxisFault axisFault) {
            if (axisFault.getFaultCode().equals(Constants.QNAME_NO_SERVICE_FAULT_CODE)) {
                processAxisFault(axisFault);
                response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "404");
                reportNoWSDL(response, "noWSDL01", axisFault);
            } else {
                throw axisFault;
            }
        }
    }

    protected void invokeEndpointFromGet(MessageContext msgContext,
                                         WriterMessageAdapter response,
                                         String method,
                                         String args) throws AxisFault
    {
        String body = "<" + method + ">" + args + "</" + method + ">";
        String msgtxt = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body>"
                + body + "</SOAP-ENV:Body>" + "</SOAP-ENV:Envelope>";
        Message responseMsg = null;
        try {
            ByteArrayInputStream istream = new ByteArrayInputStream(msgtxt.getBytes("ISO-8859-1"));
            AxisEngine engine = getAxisServer();
            Message msg = new Message(msgtxt, false);
            msgContext.setRequestMessage(msg);
            engine.invoke(msgContext);
            responseMsg = msgContext.getResponseMessage();
            response.setProperty(HTTPConstants.HEADER_CACHE_CONTROL, "no-cache");
            response.setProperty(HTTPConstants.HEADER_PRAGMA, "no-cache");
            response.setProperty(HTTPConstants.HEADER_CONTENT_TYPE, "text/xml");
            if (responseMsg == null)
                throw new Exception(Messages.getMessage("noResponse01"));
        } catch (AxisFault fault) {
            processAxisFault(fault);
            configureResponseFromAxisFault(response, fault);
            if (responseMsg == null)
                responseMsg = new Message(fault);
        } catch (Exception e) {
            response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "500");

            responseMsg = convertExceptionToAxisFault(e, responseMsg);
        }
        response.setProperty(HTTPConstants.HEADER_CONTENT_TYPE, "text/xml");
        response.write(responseMsg.getSOAPPartAsString());
    }

    protected void reportServiceInfo(WriterMessageAdapter response, SOAPService service, String serviceName)
    {
        response.setProperty(HttpConstants.HEADER_CONTENT_TYPE, "text/html");
        response.write("<h1>" + service.getName() + "</h1>");
        response.write("<p>" + Messages.getMessage("axisService00") + "</p>");
        response.write("<i>" + Messages.getMessage("perhaps00") + "</i>");
    }

    protected void processListRequest(WriterMessageAdapter response) throws AxisFault
    {
        AxisEngine engine = getAxisServer();
        if (enableList) {
            Document doc = Admin.listConfig(engine);
            if (doc != null) {
                response.setProperty(HttpConstants.HEADER_CONTENT_TYPE, "text/xml");
                XMLUtils.DocumentToWriter(doc, response.getWriter());
            } else {
                response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "404");
                response.setProperty(HTTPConstants.HEADER_CONTENT_TYPE, "text/html");
                response.write("<h2>" + Messages.getMessage("error00") + "</h2>");
                response.write("<p>" + Messages.getMessage("noDeploy00") + "</p>");
            }
        } else {
            response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "403");
            response.setProperty(HTTPConstants.HEADER_CONTENT_TYPE, "text/html");
            response.write("<h2>" + Messages.getMessage("error00") + "</h2>");
            response.write("<p><i>?list</i> " + Messages.getMessage("disabled00") + "</p>");
        }
    }

    protected void reportNoWSDL(WriterMessageAdapter response, String moreDetailCode, AxisFault axisFault)
    {
        response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "404");
        response.setProperty(HTTPConstants.HEADER_CONTENT_TYPE, "text/html");
        response.write("<h2>" + Messages.getMessage("error00") + "</h2>");
        response.write("<p>" + Messages.getMessage("noWSDL00") + "</p>");
        if (moreDetailCode != null)
            response.write("<p>" + Messages.getMessage(moreDetailCode) + "</p>");

    }

    protected void reportAvailableServices(UMOEventContext context, WriterMessageAdapter response)
            throws ConfigurationException, AxisFault
    {
        AxisEngine engine = getAxisServer();
        response.setProperty(HttpConstants.HEADER_CONTENT_TYPE, "text/html");
        response.write("<h2>And now... Some Services</h2>");
        String version = MuleManager.getConfiguration().getProductVersion();
        if (version == null)
            version = "Version Not Set";
        response.write("<h5>(Mule - " + version + ")</h5>");
        Iterator i;

        try {
            response.write("<table width=\"400\"><tr><th>Mule Component Services</th><th>Axis Services</th></tr><tr><td width=\"200\" valign=\"top\">");
            i = engine.getConfig().getDeployedServices();
            listServices(i, response);
            response.write("</td><td width=\"200\" valign=\"top\">");
            i = ((MuleConfigProvider) engine.getConfig()).getAxisDeployedServices();
            listServices(i, response);
            response.write("</td></tr></table>");
        } catch (ConfigurationException configException) {
            if (configException.getContainedException() instanceof AxisFault)
                throw (AxisFault) configException.getContainedException();
            else
                throw configException;
        }

    }

    private void listServices(Iterator i, WriterMessageAdapter response)
    {
        response.write("<ul>");
        while (i.hasNext()) {
            ServiceDesc sd = (ServiceDesc) i.next();
            StringBuffer sb = new StringBuffer();
            sb.append("<li>");
            String name = sd.getName();
            sb.append(name);
            sb.append(" <a href=\"");
            if (sd.getEndpointURL() != null) {
                sb.append(sd.getEndpointURL());
                if (!sd.getEndpointURL().endsWith("/"))
                    sb.append("/");
            }
            sb.append(name);
            sb.append("?wsdl\"><i>(wsdl)</i></a></li>");
            response.write(sb.toString());
            if (sd.getDocumentation() != null) {
                response.write("<ul><h6>" + sd.getDocumentation() + "</h6></ul>");
            }
            ArrayList operations = sd.getOperations();
            if (!operations.isEmpty()) {
                response.write("<ul>");
                OperationDesc desc;
                for (Iterator it = operations.iterator(); it.hasNext();) {
                    desc = (OperationDesc) it.next();
                    response.write("<li>" + desc.getName());
                }
                response.write("</ul>");
            }
        }
        response.write("</ul>");
    }

    protected void reportCantGetAxisService(UMOEventContext context, WriterMessageAdapter response)
    {
        response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "404");
        response.setProperty(HttpConstants.HEADER_CONTENT_TYPE, "text/html");
        response.write("<h2>" + Messages.getMessage("error00") + "</h2>");
        response.write("<p>" + Messages.getMessage("noService06") + "</p>");
    }

    public void doPost(UMOEventContext context, WriterMessageAdapter response) throws ServletException, IOException
    {
        long t0 = 0L;
        long t1 = 0L;
        long t2 = 0L;
        long t3 = 0L;
        long t4 = 0L;

        String soapAction = null;
        AxisEngine engine = getAxisServer();
        if (engine == null) {
            ServletException se = new ServletException(Messages.getMessage("noEngine00"));
            logger.debug("No Engine!", se);
            throw se;
        }
        MessageContext msgContext = new MessageContext(engine);
        if (logger.isDebugEnabled()) {
            logger.debug("Enter: doPost()");
        }
        if (tlog.isDebugEnabled()) {
            t0 = System.currentTimeMillis();
        }
        Message responseMsg = null;
        String contentType = null;
        try {
            UMOEndpointURI endpointUri = getEndpoint(context);
            populateMessageContext(msgContext, context, endpointUri);
            if (securityProvider != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("securityProvider:" + securityProvider);
                }
                msgContext.setProperty("securityProvider", securityProvider);
            }

            Object request = context.getTransformedMessage();
            Message requestMsg = new Message((request instanceof File) ? new FileInputStream((File) request) :
                                             request,
                                             false,
                                             (String) context.getProperty(HTTPConstants.HEADER_CONTENT_TYPE),
                                             (String) context.getProperty(HTTPConstants.HEADER_CONTENT_LOCATION));

            if (logger.isDebugEnabled()) {
                logger.debug("Request Message:" + requestMsg);
            }
            msgContext.setRequestMessage(requestMsg);
            msgContext.setProperty("transport.url", endpointUri.toString());

            soapAction = getSoapAction(context);
            if (soapAction != null) {
                msgContext.setUseSOAPAction(true);
                msgContext.setSOAPActionURI(soapAction);
            }
            // todo session support
            // msgContext.setSession(new AxisHttpSession(req));
            if (tlog.isDebugEnabled()) {
                t1 = System.currentTimeMillis();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Invoking Axis Engine.");
            }
            engine.invoke(msgContext);
            if (logger.isDebugEnabled()) {
                logger.debug("Return from Axis Engine.");
            }
            if (tlog.isDebugEnabled()) {
                t2 = System.currentTimeMillis();
            }
            if (RequestContext.getExceptionPayload() instanceof Exception) {
                throw (Exception) RequestContext.getExceptionPayload().getException();
            }
            // remove temporary file used for soap message with attachment
            if (request instanceof File) {
            	((File) request).delete();
            }
            response.setProperty(HTTPConstants.HEADER_CONTENT_TYPE, "text/xml");
            responseMsg = msgContext.getResponseMessage();

            if (responseMsg == null)
                throw new Exception(Messages.getMessage("noResponse01"));
            responseMsg.setProperty(HTTPConstants.HEADER_CONTENT_TYPE, "text/xml");
            
        } catch (AxisFault fault) {
            processAxisFault(fault);
            configureResponseFromAxisFault(response, fault);
            responseMsg = msgContext.getResponseMessage();
            if (responseMsg == null)
                responseMsg = new Message(fault);
        } catch (Exception e) {
            responseMsg = msgContext.getResponseMessage();
            response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "500");
            responseMsg = convertExceptionToAxisFault(e, responseMsg);
        }

        contentType = "text/xml";//responseMsg.getContentType(msgContext.getSOAPConstants());
        if (tlog.isDebugEnabled())
            t3 = System.currentTimeMillis();
        if (responseMsg != null)
            sendResponse((String) context.getProperty(HttpConnector.HTTP_STATUS_PROPERTY),
                         contentType,
                         response,
                         responseMsg);
        if (logger.isDebugEnabled()) {
            logger.debug("Response sent.");
            logger.debug("Exit: doPost()");
        }
        if (tlog.isDebugEnabled()) {
            t4 = System.currentTimeMillis();
            tlog.debug("axisServlet.doPost: " + soapAction + " pre=" + (t1 - t0) + " invoke=" + (t2 - t1) + " post="
                    + (t3 - t2) + " send=" + (t4 - t3) + " " + msgContext.getTargetService() + "."
                    + (msgContext.getOperation() != null ? msgContext.getOperation().getName() : ""));
        }
    }

    private UMOEndpointURI getEndpoint(UMOEventContext context) throws MalformedEndpointException
    {
        String endpoint = context.getEndpointURI().getAddress();
        String request = (String) context.getProperty(HttpConnector.HTTP_REQUEST_PROPERTY);
        if (request != null) {
            int i = endpoint.indexOf("/", endpoint.indexOf("://") + 3);
            if (i > -1) {
                endpoint = endpoint.substring(0, i);
            }
            endpoint += request;
            return new MuleEndpointURI(endpoint);
        }
        return context.getEndpointURI();
    }

    private void configureResponseFromAxisFault(WriterMessageAdapter response, AxisFault fault)
    {
        int status = getHttpResponseStatus(fault);
        if (status == 401)
            response.setProperty(HttpConstants.HEADER_WWW_AUTHENTICATE, "Basic realm=\"AXIS\"");
        response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, String.valueOf(status));
    }

    private Message convertExceptionToAxisFault(Exception exception, Message responseMsg)
    {
        logException(exception);
        if (responseMsg == null) {
            AxisFault fault = AxisFault.makeFault(exception);
            processAxisFault(fault);
            responseMsg = new Message(fault);
        }
        return responseMsg;
    }

    protected int getHttpResponseStatus(AxisFault af)
    {
        return af.getFaultCode().getLocalPart().startsWith("Server.Unauth") ? 401 : '\u01F4';
    }

    private void sendResponse(String clientVersion,
                              String contentType,
                              WriterMessageAdapter response,
                              Message responseMsg) throws AxisFault, IOException
    {
        if (responseMsg == null) {
            response.setProperty(HttpConnector.HTTP_STATUS_PROPERTY, "204");
            if (logger.isDebugEnabled()) {
                logger.debug("NO AXIS MESSAGE TO RETURN!");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Returned Content-Type:" + contentType);
            }
            try {
                response.setProperty(HttpConstants.HEADER_CONTENT_TYPE, contentType);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                responseMsg.writeTo(baos);
                response.write(baos.toString());
            } catch (SOAPException e) {
                logException(e);
            }
        }
        // if(!res.isCommitted())
        // res.flushBuffer();
    }

    private void populateMessageContext(MessageContext msgContext, UMOEventContext context, UMOEndpointURI endpointUri)
            throws AxisFault, ConfigurationException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("MessageContext:" + msgContext);
            logger.debug("HEADER_CONTENT_TYPE:" + context.getProperty("Content-Type"));
            logger.debug("HEADER_CONTENT_LOCATION:" + context.getProperty("Content-Location"));
            logger.debug("Constants.MC_HOME_DIR:" + String.valueOf(getHomeDir()));
            logger.debug("Constants.MC_RELATIVE_PATH:" + endpointUri.getPath());
            // logger.debug("HTTPConstants.MC_HTTP_SERVLETLOCATION:" +
            // String.valueOf(getWebInfPath()));
            // logger.debug("HTTPConstants.MC_HTTP_SERVLETPATHINFO:" +
            // req.getPathInfo());
            logger.debug("HTTPConstants.HEADER_AUTHORIZATION:" + context.getProperty("Authorization"));
            logger.debug("Constants.MC_REMOTE_ADDR:" + endpointUri.getHost());
            // logger.debug("configPath:" + String.valueOf(getWebInfPath()));
        }
        msgContext.setTransportName(transportName);
        msgContext.setProperty("home.dir", getHomeDir());
        msgContext.setProperty("path", endpointUri.getPath());
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLET, this);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETLOCATION, endpointUri.getPath());
        // determine service name
        String serviceName = getServiceName(context, endpointUri);
        // Validate Service path against request path
        SOAPService service = msgContext.getAxisEngine().getConfig().getService(new QName(serviceName.substring(1)));

        // Component Name is set by Mule so if its null we can skip this check
        if (service.getOption(AxisConnector.SERVICE_PROPERTY_COMPONENT_NAME) != null) {
            String servicePath = (String) service.getOption("servicePath");
            if("".equals(endpointUri.getPath())) {
                if(!("/" + endpointUri.getAddress()).startsWith(servicePath + serviceName)) {
                    throw new AxisFault("Failed to find service: " + "/" + endpointUri.getAddress());                    
                }
            } else if (!endpointUri.getPath().startsWith(servicePath + serviceName)) {
                throw new AxisFault("Failed to find service: " + endpointUri.getPath());
            }
        }

        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETPATHINFO, serviceName);
        msgContext.setProperty("serviceName", serviceName);

        msgContext.setProperty("Authorization", context.getProperty("Authorization"));
        msgContext.setProperty("remoteaddr", endpointUri.getHost());
        ServletEndpointContextImpl sec = new ServletEndpointContextImpl();
        msgContext.setProperty("servletEndpointContext", sec);
        // String realpath =
        // getServletConfig().getServletContext().getRealPath(req.getServletPath());
        // if(realpath != null)
        // msgContext.setProperty("realpath", realpath);
        // msgContext.setProperty("configPath", getWebInfPath());
    }

    private String getSoapAction(UMOEventContext context) throws AxisFault
    {
        String soapAction = (String) context.getProperty("SOAPAction");		    
		/*ast: SOAPAction is send in an HTTP HEADER, and this property shold be trated as case-sensitive, wich is not the way is done using getProerty
			so, we need to check all the properties searching for a good header
		*/		
        if (soapAction == null) {
			Iterator it=context.getProperties().keySet().iterator();
			while((it.hasNext()&&(soapAction == null)) ){
				String p=(String) it.next();
				if ((p!=null) && (p.equalsIgnoreCase("SOAPAction"))) soapAction=(String) context.getProperty(p);
			}
		}
		if (logger.isDebugEnabled()) {
            logger.debug("HEADER_SOAP_ACTION:" + soapAction);
        }
		//System.out.println("---------------> Soap ACTION:  ("+soapAction+")");
		if (soapAction == null) {
            AxisFault af = new AxisFault("Client.NoSOAPAction",
                                         Messages.getMessage("noHeader00", "SOAPAction"),
                                         null,
                                         null);
            exceptionLog.error(Messages.getMessage("genFault00"), af);
            throw af;
        }
        if (soapAction.length() == 0) {
            soapAction = context.getEndpointURI().getAddress();
        }
        return soapAction;
    }

    protected String getServiceName(UMOEventContext context, UMOEndpointURI endpointUri) throws AxisFault
    {
        String serviceName = endpointUri.getPath();
        if (serviceName == null || serviceName.length() == 0) {
            serviceName = getSoapAction(context);
            serviceName = serviceName.replaceAll("\"", "");
            int i = serviceName.indexOf("/", serviceName.indexOf("//"));
            if(i<-1) {
                serviceName = serviceName.substring(i + 2);
            }

        }
        // int i = serviceName.lastIndexOf("/");
        // if (i > -1) serviceName = serviceName.substring(0, i);

        int i = serviceName.lastIndexOf("/");
        if (i > -1) {
            serviceName = serviceName.substring(i);
        }
        i = serviceName.lastIndexOf("?");
        if (i > -1) {
            serviceName = serviceName.substring(0, i);
        }
        return serviceName;
    }

    protected String getProtocolVersion(HttpServletRequest req)
    {
        String ret = HTTPConstants.HEADER_PROTOCOL_V10;
        String prot = req.getProtocol();
        if (prot != null) {
            int sindex = prot.indexOf('/');
            if (-1 != sindex) {
                String ver = prot.substring(sindex + 1);
                if (HTTPConstants.HEADER_PROTOCOL_V11.equals(ver.trim()))
                    ret = HTTPConstants.HEADER_PROTOCOL_V11;
            }
        }
        return ret;
    }

    public String getTransportName()
    {
        return transportName;
    }

    public void setTransportName(String transportName)
    {
        this.transportName = transportName;
    }

    public boolean isEnableList()
    {
        return enableList;
    }

    public void setEnableList(boolean enableList)
    {
        this.enableList = enableList;
    }

    public String getHomeDir()
    {
        if (homeDir == null) {
            homeDir = MuleManager.getConfiguration().getWorkingDirectory() + DEFAULT_AXIS_HOME;
        }
        return homeDir;
    }

    public void setHomeDir(String homeDir)
    {
        this.homeDir = homeDir;
    }

    public AxisServer getAxisServer()
    {
        return axisServer;
    }

    public void setAxisServer(AxisServer axisServer)
    {
        this.axisServer = axisServer;
    }
}
