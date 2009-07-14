/*
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/transformers/UMOMessageToHttpResponse.java,v 1.6 2005/10/25 19:47:52 holger Exp $
 * $Revision: 1.6 $
 * $Date: 2005/10/25 19:47:52 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.soap.axis.transport.http.transformers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.mule.MuleManager;
import org.mule.config.MuleProperties;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.NullPayload;
import org.mule.providers.http.HttpConnector;
import org.mule.providers.http.HttpConstants;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.Utility;

/**
 * <code>UMOMessageToHttpResponse</code> converts a UMOMEssage into an Http
 * response.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.6 $
 */

public class UMOMessageToHttpResponse extends AbstractEventAwareTransformer
{
    private SimpleDateFormat format = null;
    private String server = null;

    public UMOMessageToHttpResponse()
    {
        registerSourceType(Object.class);
        setReturnClass(Object.class);

        format = new SimpleDateFormat(HttpConstants.DATE_FORMAT);

        //When running with the source code, Meta information is not set
        //so product name and version are not available, hence we hard code
        if(MuleManager.getConfiguration().getProductName()==null) {
            server = "Mule/SNAPSHOT";
        } else {
            server = MuleManager.getConfiguration().getProductName() + "/"
                + MuleManager.getConfiguration().getProductVersion();
        }
    }

    public Object transform(Object src, UMOEventContext context) throws TransformerException
    {

        //Note this transformer excepts Null as we must always return a result from the Http
        //connector if a response transformer is present
        if(src instanceof NullPayload) src = Utility.EMPTY_STRING;
        int status = context.getIntProperty(HttpConnector.HTTP_STATUS_PROPERTY, HttpConstants.SC_OK);
        String version = (String) context.getProperty(HttpConnector.HTTP_VERSION_PROPERTY, HttpConstants.HTTP11);
        String date = format.format(new Date());
        byte[] response = null;
        //TODO: Make this dynamic 1.6.1
        boolean closeConnection = true;
        
        String contentType = (String) context.getProperty(HttpConstants.HEADER_CONTENT_TYPE, HttpConnector.DEFAULT_CONTENT_TYPE);

        if (src instanceof byte[]) {
            response = (byte[]) src;
        } else if (contentType.startsWith("text/")) {
            response = src.toString().getBytes();
        } else {
            try {
                response = Utility.objectToByteArray(src);
            } catch (IOException e) {
                throw new TransformerException(new Message(Messages.TRANSFORM_FAILED_FROM_X_TO_X, "Object", "byte[]"),
                                               this,
                                               e);
            }
        }

        StringBuffer httpMessage = new StringBuffer(512);

        httpMessage.append(version).append(" ");
        httpMessage.append(status).append(HttpConstants.CRLF);
        httpMessage.append(HttpConstants.HEADER_DATE);
        httpMessage.append(": ").append(date).append(HttpConstants.CRLF);
        httpMessage.append(HttpConstants.HEADER_SERVER);
        httpMessage.append(": ").append(server).append(HttpConstants.CRLF);
        if (closeConnection){
        	httpMessage.append(HttpConstants.HEADER_CONNECTION);
        	httpMessage.append(": close").append(HttpConstants.CRLF);
        }
        if (context.getProperty(HttpConstants.HEADER_EXPIRES) == null) {
            httpMessage.append(HttpConstants.HEADER_EXPIRES);
            httpMessage.append(": ").append(date).append(HttpConstants.CRLF);
        }

        httpMessage.append(HttpConstants.HEADER_CONTENT_TYPE);
        if (contentType == null) {
            httpMessage.append(": ").append(HttpConstants.DEFAULT_CONTENT_TYPE).append(HttpConstants.CRLF);
        } else {
            httpMessage.append(": ").append(contentType).append(HttpConstants.CRLF);
        }

        httpMessage.append(HttpConstants.HEADER_CONTENT_LENGTH);
        httpMessage.append(": ").append(response.length).append(HttpConstants.CRLF);

        String headerName;
        String value;
        for (Iterator iterator = HttpConstants.RESPONSE_HEADER_NAMES.values().iterator(); iterator.hasNext();) {
            headerName = (String) iterator.next();
            value = context.getStringProperty(headerName);
            if (value != null) {
                httpMessage.append(headerName).append(": ").append(value);
                httpMessage.append(HttpConstants.CRLF);
            }
        }
        // Custom responseHeaderNames
        Map customHeaders = (Map) context.getProperty(HttpConnector.HTTP_CUSTOM_HEADERS_MAP_PROPERTY);
        if (customHeaders != null) {
            Map.Entry entry;
            for (Iterator iterator = customHeaders.entrySet().iterator(); iterator.hasNext();) {
                entry = (Map.Entry) iterator.next();
                httpMessage.append(entry.getKey()).append(": ").append(entry.getValue());
                httpMessage.append(HttpConstants.CRLF);
            }
        }

        // Mule properties
        UMOMessage m = context.getMessage();
        String user = (String) m.getProperty(MuleProperties.MULE_USER_PROPERTY);
        if (user != null) {
            httpMessage.append("X-" + MuleProperties.MULE_USER_PROPERTY).append(": ").append(user);
            httpMessage.append(HttpConstants.CRLF);
        }
        if (m.getCorrelationId() != null) {
            httpMessage.append("X-" + MuleProperties.MULE_CORRELATION_ID_PROPERTY)
                       .append(": ")
                       .append(m.getCorrelationId());
            httpMessage.append(HttpConstants.CRLF);
            httpMessage.append("X-" + MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY)
                       .append(": ")
                       .append(m.getCorrelationGroupSize());
            httpMessage.append(HttpConstants.CRLF);
            httpMessage.append("X-" + MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY)
                       .append(": ")
                       .append(m.getCorrelationSequence());
            httpMessage.append(HttpConstants.CRLF);
        }
        if (m.getReplyTo() != null) {
            httpMessage.append("X-" + MuleProperties.MULE_REPLY_TO_PROPERTY)
            			.append(": ")
            			.append(m.getReplyTo().toString());
            httpMessage.append(HttpConstants.CRLF);
        }

        // End header
        httpMessage.append(HttpConstants.CRLF);
        byte [] headerPayload=httpMessage.toString().getBytes();
        byte[] resultPayload = new byte[headerPayload.length + response.length];
        System.arraycopy(headerPayload, 0, resultPayload, 0, headerPayload.length);
        System.arraycopy(response, 0, resultPayload, headerPayload.length, response.length);

        if(contentType.startsWith("text/")) {
            return new String(resultPayload);
        } else {
           return resultPayload;
        }
    }

    public boolean isAcceptNull() {
        return true;
    }
}
