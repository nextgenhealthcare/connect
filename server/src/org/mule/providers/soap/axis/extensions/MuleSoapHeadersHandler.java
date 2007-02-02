/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/extensions/MuleSoapHeadersHandler.java,v 1.5 2005/06/23 08:01:31 gnt Exp $
 * $Revision: 1.5 $
 * $Date: 2005/06/23 08:01:31 $
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

import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.MuleProperties;
import org.mule.providers.soap.axis.MuleSoapHeaders;
import org.mule.umo.UMOEvent;

/**
 * <code>MuleSoapHeadersHandler</code> is an Axis handler that can read and
 * write Mule header properties to a Soap message
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */
public class MuleSoapHeadersHandler extends BasicHandler
{
    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(MuleSoapHeadersHandler.class);

    public void invoke(MessageContext msgContext) throws AxisFault
    {
        boolean setMustUnderstand = msgContext.isPropertyTrue("MULE_HEADER_MUST_UNDERSTAND");

        try {
            if (msgContext.isClient()) {
                if (!msgContext.getPastPivot()) {
                    processClientRequest(msgContext, setMustUnderstand);
                    if (logger.isDebugEnabled()) {
                        logger.debug("After Client Request, Message is:\n"
                                + msgContext.getRequestMessage().getSOAPPartAsString());
                    }
                } else {
                    processClientResponse(msgContext);
                    if (logger.isDebugEnabled()) {
                        logger.debug("After Client Response, Message is:\n"
                                + msgContext.getRequestMessage().getSOAPPartAsString());
                    }
                }
            } else {
                if (!msgContext.getPastPivot()) {
                    processServerRequest(msgContext);
                    if (logger.isDebugEnabled()) {
                        logger.debug("After Server Request, Message is:\n"
                                + msgContext.getRequestMessage().getSOAPPartAsString());
                    }
                } else {
                    processServerResponse(msgContext, setMustUnderstand);
                    if (logger.isDebugEnabled()) {
                        logger.debug("After Server Response, Message is:\n"
                                + msgContext.getRequestMessage().getSOAPPartAsString());
                    }
                }
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Method processClientRequest
     * 
     * @param msgContext
     */
    protected synchronized void processClientRequest(MessageContext msgContext, boolean setMustUnderstand)
            throws Exception
    {

        SOAPMessageContext soapMsgContext = msgContext;
        SOAPMessage msg = soapMsgContext.getMessage();
        if (msg == null) {
            return;
        }
        UMOEvent event = (UMOEvent) msgContext.getProperty(MuleProperties.MULE_EVENT_PROPERTY);

        if (event == null) {
            return;
        } else {
            synchronized (msgContext) {
                MuleSoapHeaders headers = new MuleSoapHeaders(event);
                headers.addHeaders(msgContext.getMessage().getSOAPPart().getEnvelope());
            }
        }
    }

    /**
     * Method processClientResponse
     * 
     * @param msgContext
     */
    protected void processClientResponse(MessageContext msgContext) throws Exception
    {
        SOAPMessageContext soapMsgContext = msgContext;
        SOAPMessage msg = soapMsgContext.getMessage();
        if (msg == null) {
            return;
        }
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        MuleSoapHeaders headers = new MuleSoapHeaders(env.getHeader());
        headers.setAsClientProperties(msgContext);
    }

    /**
     * Method processServerRequest
     * 
     * @param msgContext
     * @throws Exception
     */
    protected void processServerRequest(MessageContext msgContext) throws Exception
    {
        SOAPMessageContext soapMsgContext = msgContext;
        SOAPMessage msg = soapMsgContext.getMessage();
        if (msg == null) {
            return;
        }
        MuleSoapHeaders headers = new MuleSoapHeaders(msg.getSOAPPart().getEnvelope().getHeader());
        msgContext.setProperty(MuleSoapHeaders.ENV_REQUEST_HEADERS, headers);
    }

    /**
     * Method processServerResponse
     * 
     * @param msgContext
     */
    protected void processServerResponse(MessageContext msgContext, boolean setMustUnderstand) throws Exception
    {
        SOAPMessageContext soapMsgContext = msgContext;
        SOAPMessage msg = soapMsgContext.getMessage();
        if (msg == null) {
            return;
        }
        MuleSoapHeaders headers = (MuleSoapHeaders) msgContext.getProperty(MuleSoapHeaders.ENV_REQUEST_HEADERS);

        if (headers == null) {
            return;
        } else {
            headers.addHeaders(msgContext.getMessage().getSOAPPart().getEnvelope());
        }
    }

}
