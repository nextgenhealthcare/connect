/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/AxisMessageAdapter.java,v 1.6 2005/07/25 02:07:17 rossmason Exp $
 * $Revision: 1.6 $
 * $Date: 2005/07/25 02:07:17 $
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

import java.util.Iterator;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.mule.config.i18n.Message;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.providers.soap.axis.MuleSoapHeaders;
import org.mule.transformers.simple.SerializableToByteArray;
import org.mule.umo.MessagingException;
import org.mule.umo.transformer.UMOTransformer;

/**
 * <code>AxisMessageAdapter</code>
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.6 $
 */
public class AxisMessageAdapter extends AbstractMessageAdapter
{
    private Object payload;
    private SOAPMessage message;
    private UMOTransformer trans = new SerializableToByteArray();

    public AxisMessageAdapter(Object message) throws MessagingException
    {
        this.payload = message;
        try {
            MessageContext ctx = MessageContext.getCurrentContext();

            if (ctx != null) {
                MuleSoapHeaders header = new MuleSoapHeaders(ctx.getMessage().getSOAPPart().getEnvelope().getHeader());

                if (header.getReplyTo() != null && !"".equals(header.getReplyTo())) {
                    setReplyTo(header.getReplyTo());
                }

                if (header.getCorrelationGroup() != null && !"".equals(header.getCorrelationGroup())
                        && !"-1".equals(header.getCorrelationGroup())) {
                    setCorrelationGroupSize(Integer.parseInt(header.getCorrelationGroup()));
                }
                if (header.getCorrelationSequence() != null && !"".equals(header.getCorrelationSequence())
                        && !"-1".equals(header.getCorrelationSequence())) {
                    setCorrelationSequence(Integer.parseInt(header.getCorrelationSequence()));
                }
                if (header.getCorrelationId() != null && !"".equals(header.getCorrelationId())) {
                    setCorrelationId(header.getCorrelationId());
                }

                this.message = ctx.getMessage();
                int x=1;
                try {
                    for(Iterator i = this.message.getAttachments();i.hasNext();x++) {
                        addAttachment(String.valueOf(x), ((AttachmentPart)i.next()).getActivationDataHandler());
                    }
                } catch (Exception e) {
                    //this will not happen
                    logger.fatal("Failed to read attachments", e);
                }
            } else {

            }
        } catch (SOAPException e) {
            throw new MessagingException(new Message("soap", 5), message, e);
        }
    }

    /**
     * Converts the payload implementation into a String representation
     * 
     * @return String representation of the payload payload
     * @throws Exception Implementation may throw an endpoint specific exception
     */
    public String getPayloadAsString() throws Exception
    {
        return new String(getPayloadAsBytes());
    }

    /**
     * Converts the payload implementation into a String representation
     * 
     * @return String representation of the payload
     * @throws Exception Implemetation may throw an endpoint specific exception
     */
    public byte[] getPayloadAsBytes() throws Exception
    {
        return (byte[]) trans.transform(payload);
    }

    /**
     * @return the current payload
     */
    public Object getPayload()
    {
        return payload;
    }

    public SOAPMessage getSoapMessage() {
        return message;
    }

    public void addAttachment(String name, DataHandler dataHandler) throws Exception {
        message.addAttachmentPart(new AttachmentPart(dataHandler));
        super.addAttachment(name, dataHandler);
    }

    public void removeAttachment(String name) throws Exception {
        if("all".equalsIgnoreCase(name)) {
            message.removeAllAttachments();
            attachments.clear();
        } else {
            throw new SOAPException(new Message("soap", 6).toString());
        }
    }
}
