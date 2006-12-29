/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/MuleSoapHeaders.java,v 1.4 2005/06/03 01:20:35 gnt Exp $
 * $Revision: 1.4 $
 * $Date: 2005/06/03 01:20:35 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.soap.axis;

import java.util.Iterator;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis.MessageContext;
import org.apache.axis.message.MessageElement;
import org.apache.axis.utils.XMLUtils;
import org.mule.config.MuleProperties;
import org.mule.umo.UMOEvent;

/**
 * <code>MuleSoapHeaders</code> is a helper class for extracting and writing
 * Mule header properties to s Soap message
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */
public class MuleSoapHeaders
{
    private String replyTo;
    private String correlationId;
    private String correlationGroup;
    private String correlationSequence;

    public static final String MULE_10_ACTOR = "http://www.muleumo.org/providers/soap/1.0";
    public static final String MULE_NAMESPACE = "mule";
    public static final String MULE_HEADER = "header";
    public static final String ENV_REQUEST_HEADERS = "MULE_REQUEST_HEADERS";

    /**
     * Extracts header properties from a Mule event
     * 
     * @param event
     */
    public MuleSoapHeaders(UMOEvent event)
    {
        setCorrelationId(event.getMessage().getCorrelationId());
        setCorrelationGroup(String.valueOf(event.getMessage().getCorrelationGroupSize()));
        setCorrelationSequence(String.valueOf(event.getMessage().getCorrelationSequence()));
        setReplyTo((String) event.getMessage().getReplyTo());
    }

    /**
     * Extracts Mule header properties from a Soap message
     * 
     * @param soapHeader
     * @throws SOAPException
     */
    public MuleSoapHeaders(SOAPHeader soapHeader) throws SOAPException
    {
        Iterator iter = soapHeader.examineHeaderElements(MULE_10_ACTOR);
        SOAPHeaderElement header;
        SOAPElement element;
        while (iter.hasNext()) {
            header = (SOAPHeaderElement) iter.next();
            Iterator iter2 = header.getChildElements();

            while (iter2.hasNext()) {
                element = (SOAPElement) iter2.next();
                if (MuleProperties.MULE_CORRELATION_ID_PROPERTY.equals(element.getElementName().getLocalName())) {
                    correlationId = getStringValue(element);
                } else if (MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY.equals(element.getElementName()
                                                                                             .getLocalName())) {
                    correlationGroup = getStringValue(element);
                } else if (MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY.equals(element.getElementName()
                                                                                           .getLocalName())) {
                    correlationSequence = getStringValue(element);
                } else if (MuleProperties.MULE_REPLY_TO_PROPERTY.equals(element.getElementName().getLocalName())) {
                    replyTo = getStringValue(element);
                } else {
                    throw new SOAPException("Unrecognised Mule Soap header: "
                            + element.getElementName().getQualifiedName());
                }
            }
        }
    }

    private String getStringValue(SOAPElement e)
    {
        String value = e.getValue();
        if (value == null && e.hasChildNodes()) {
            // see if the value is base64 ecoded
            value = e.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
            if (value != null) {
                value = new String(org.apache.axis.encoding.Base64.decode(value));
            }
        }
        return value;
    }

    /**
     * Sets the header properties as properties directly on the message context
     * 
     * @param context
     */
    public void setAsClientProperties(MessageContext context)
    {
        if (correlationId != null)
            context.setProperty(MuleProperties.MULE_CORRELATION_ID_PROPERTY, correlationId);
        if (correlationGroup != null)
            context.setProperty(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY, correlationGroup);
        if (correlationSequence != null)
            context.setProperty(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY, correlationSequence);

        if (replyTo != null)
            context.setProperty(MuleProperties.MULE_REPLY_TO_PROPERTY, replyTo);
    }

    /**
     * Writes the header properties to a Soap header
     * 
     * @param env
     * @throws SOAPException
     */
    public void addHeaders(SOAPEnvelope env) throws SOAPException
    {
        SOAPHeader header = env.getHeader();
        SOAPHeaderElement muleHeader;
        if (correlationId != null || replyTo != null) {
            if (header == null) {
                header = env.addHeader();
            }
            Name muleHeaderName = env.createName(MULE_HEADER, MULE_NAMESPACE, MULE_10_ACTOR);
            muleHeader = header.addHeaderElement(muleHeaderName);
            muleHeader.setActor(MULE_10_ACTOR);
        } else {
            return;
        }

        if (correlationId != null) {
            MessageElement e = (MessageElement) muleHeader.addChildElement(MuleProperties.MULE_CORRELATION_ID_PROPERTY,
                                                                           MULE_NAMESPACE);
            e.setObjectValue(correlationId);
            e = (MessageElement) muleHeader.addChildElement(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY,
                                                            MULE_NAMESPACE);
            e.setObjectValue(correlationGroup);
            e = (MessageElement) muleHeader.addChildElement(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY,
                                                            MULE_NAMESPACE);
            e.setObjectValue(correlationSequence);
        }
        if (replyTo != null) {
            MessageElement e = (MessageElement) muleHeader.addChildElement(MuleProperties.MULE_REPLY_TO_PROPERTY,
                                                                           MULE_NAMESPACE);
            String enc = XMLUtils.xmlEncodeString(replyTo);
            e.setObjectValue(enc);
        }
    }

    public String getReplyTo()
    {
        return replyTo;
    }

    public void setReplyTo(String replyTo)
    {
        this.replyTo = replyTo;
    }

    public String getCorrelationId()
    {
        return correlationId;
    }

    public void setCorrelationId(String correlationId)
    {
        this.correlationId = correlationId;
    }

    public String getCorrelationGroup()
    {
        return correlationGroup;
    }

    public void setCorrelationGroup(String correlationGroup)
    {
        this.correlationGroup = correlationGroup;
    }

    public String getCorrelationSequence()
    {
        return correlationSequence;
    }

    public void setCorrelationSequence(String correlationSequence)
    {
        this.correlationSequence = correlationSequence;
    }
}
