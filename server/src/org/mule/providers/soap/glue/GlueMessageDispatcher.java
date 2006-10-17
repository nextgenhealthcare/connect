/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/glue/GlueMessageDispatcher.java,v 1.7 2005/09/22 16:23:24 rossmason Exp $
 * $Revision: 1.7 $
 * $Date: 2005/09/22 16:23:24 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.soap.glue;

import electric.glue.context.ThreadContext;
import electric.proxy.IProxy;
import electric.registry.Registry;
import org.mule.config.MuleProperties;
import org.mule.impl.MuleMessage;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.AbstractConnector;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.provider.ReceiveException;

import java.util.Map;

/**
 * <code>GlueMessageDispatcher</code> will make web services calls using the
 * Glue inoking mechanism.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.7 $
 */

public class GlueMessageDispatcher extends AbstractMessageDispatcher
{
    public GlueMessageDispatcher(AbstractConnector connector)
    {
        super(connector);
    }

    public void doDispatch(UMOEvent event) throws Exception
    {
        doSend(event);
    }

    public UMOMessage doSend(UMOEvent event) throws Exception
    {
        UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();
        // if(!endpoint.startsWith("glue:")) {
        // endpoint = "glue:" + endpoint;
        // }
        String method = (String) endpointUri.getParams().remove("method");
        setContext(event);
        IProxy proxy = null;
        String bindAddress = endpointUri.getAddress();
        if (bindAddress.indexOf(".wsdl") == -1) {
            bindAddress = bindAddress.replaceAll("/" + method, ".wsdl/" + method);
        }
        int i = bindAddress.indexOf("?");
        if(i > -1) {
            bindAddress = bindAddress.substring(0,i);
        }
        proxy = Registry.bind(bindAddress);

        Object payload = event.getTransformedMessage();
        Object[] args;
        if (payload instanceof Object[]) {
            args = (Object[]) payload;
        } else {
            args = new Object[] { payload };
        }
        if (event.getMessage().getReplyTo() != null) {
            ThreadContext.setProperty(MuleProperties.MULE_REPLY_TO_PROPERTY, event.getMessage().getReplyTo());
        }
        if (event.getMessage().getCorrelationId() != null) {
            ThreadContext.setProperty(MuleProperties.MULE_CORRELATION_ID_PROPERTY, event.getMessage()
                                                                                        .getCorrelationId());
        }
        try {
            Object result = proxy.invoke(method, args);
            if (result == null) {
                return null;
            } else {
                return new MuleMessage(result);
            }
        } catch (Throwable t) {
            throw new DispatchException(event.getMessage(), event.getEndpoint(), t);
        }
    }

    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception
    {
        UMOEndpointURI ep = new MuleEndpointURI(endpointUri);
        Map params = ep.getParams();
        String method = (String) params.remove("method");

        String bindAddress = ep.getAddress();
        int i = bindAddress.indexOf("?");
        if(i > -1) {
            bindAddress = bindAddress.substring(0,i);
        }
        IProxy proxy = Registry.bind(bindAddress);
        try {
            Object result = proxy.invoke(method, params.values().toArray());
            return new MuleMessage(result);
        } catch (Throwable t) {
            throw new ReceiveException(endpointUri, timeout, t);
        }
    }

    public Object getDelegateSession() throws UMOException
    {
        return null;
    }

    public void doDispose()
    {
    }

    protected String getMethod(String endpoint) throws MalformedEndpointException
    {
        int i = endpoint.lastIndexOf("/");
        String method = endpoint.substring(i + 1);
        if (method.indexOf(".wsdl") != -1) {
            throw new MalformedEndpointException("Soap url must contain method to invoke as a param [method=X] or as the last path element");
        } else {
            return method;
            // endpointUri = endpointUri.substring(0, endpointUri.length() -
            // (method.length() + 1));
        }
    }

    protected void setContext(UMOEvent event)
    {
        Object replyTo = event.getMessage().getReplyTo();
        if (replyTo != null) {
            ThreadContext.setProperty(MuleProperties.MULE_REPLY_TO_PROPERTY, replyTo);
        }

        String correlationId = event.getMessage().getCorrelationId();
        if (replyTo != null) {
            ThreadContext.setProperty(MuleProperties.MULE_CORRELATION_ID_PROPERTY, correlationId);
        }

        int value = event.getMessage().getCorrelationSequence();
        if (value > 0)
            ThreadContext.setProperty(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY, String.valueOf(value));

        value = event.getMessage().getCorrelationGroupSize();
        if (value > 0)
            ThreadContext.setProperty(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY, String.valueOf(value));
    }
}
