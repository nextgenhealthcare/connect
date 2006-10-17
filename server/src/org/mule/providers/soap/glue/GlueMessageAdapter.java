/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/glue/GlueMessageAdapter.java,v 1.4 2005/06/03 01:20:35 gnt Exp $
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
package org.mule.providers.soap.glue;

import org.mule.config.MuleProperties;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.transformers.simple.SerializableToByteArray;
import org.mule.umo.transformer.UMOTransformer;
import org.mule.util.IteratorAdapter;

import electric.glue.context.ThreadContext;
import electric.service.IService;

/**
 * <code>GlueMessageAdapter</code> wraps a Glue soap request
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */
public class GlueMessageAdapter extends AbstractMessageAdapter
{
    private Object message;
    private UMOTransformer trans = new SerializableToByteArray();

    public GlueMessageAdapter(Object message)
    {
        if (message instanceof GlueMessageHolder) {
            GlueMessageHolder holder = (GlueMessageHolder) message;
            this.message = holder.getMessage();
            IteratorAdapter iter = new IteratorAdapter(holder.getService().getContext().getPropertyNames());
            String key;
            while (iter.hasNext()) {
                key = iter.next().toString();
                setProperty(key, holder.getService().getContext().getProperty(key));
            }
        } else {
            this.message = message;
        }
        String value = (String) ThreadContext.removeProperty(MuleProperties.MULE_REPLY_TO_PROPERTY);
        if (value != null)
            setReplyTo(value);
        value = (String) ThreadContext.removeProperty(MuleProperties.MULE_CORRELATION_ID_PROPERTY);
        if (value != null)
            setCorrelationId(value);

        value = (String) ThreadContext.removeProperty(MuleProperties.MULE_CORRELATION_SEQUENCE_PROPERTY);
        if (value != null && !"-1".equals(value))
            setCorrelationSequence(Integer.parseInt(value));
        value = (String) ThreadContext.removeProperty(MuleProperties.MULE_CORRELATION_GROUP_SIZE_PROPERTY);
        if (value != null && !"-1".equals(value))
            setCorrelationGroupSize(Integer.parseInt(value));
    }

    /**
     * Converts the message implementation into a String representation
     * 
     * @return String representation of the message payload
     * @throws Exception Implementation may throw an endpoint specific exception
     */
    public String getPayloadAsString() throws Exception
    {
        return new String(getPayloadAsBytes());
    }

    /**
     * Converts the message implementation into a String representation
     * 
     * @return String representation of the message
     * @throws Exception Implemetation may throw an endpoint specific exception
     */
    public byte[] getPayloadAsBytes() throws Exception
    {
        return (byte[]) trans.transform(message);
    }

    /**
     * @return the current message
     */
    public Object getPayload()
    {
        return message;
    }

    public static class GlueMessageHolder
    {
        private Object message;
        private IService service;

        public GlueMessageHolder(Object message, IService service)
        {
            this.message = message;
            this.service = service;
        }

        public Object getMessage()
        {
            return message;
        }

        public IService getService()
        {
            return service;
        }
    }
}
