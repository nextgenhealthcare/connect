/*
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/HttpMessageAdapter.java,v 1.15 2005/10/06 15:49:26 rossmason Exp $
 * $Revision: 1.15 $
 * $Date: 2005/10/06 15:49:26 $
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

package org.mule.providers.http;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.transformers.simple.SerializableToByteArray;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOMessage;
import org.mule.umo.provider.MessageTypeNotSupportedException;
import org.mule.umo.transformer.TransformerException;
import org.mule.umo.transformer.UMOTransformer;

import java.io.InputStream;
import java.util.Map;

/**
 * <code>HttpMessageAdapter</code> Wraps an incoming Http Request making the
 * payload and heads available a standard message adapter
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.15 $
 */
public class HttpMessageAdapter extends AbstractMessageAdapter {
    private Object message = null;
    private UMOTransformer trans = new SerializableToByteArray();

    private boolean http11 = true;

    public HttpMessageAdapter(Object message) throws MessagingException {
        if (message instanceof Object[]) {
            this.message = ((Object[]) message)[0];
            if (((Object[]) message).length > 1) {
                properties = (Map) ((Object[]) message)[1];
            }
        } else if (message instanceof byte[]) {
            this.message = (byte[]) message;
        } else {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
        String temp = (String) properties.get(HttpConnector.HTTP_VERSION_PROPERTY);
        if (HttpConstants.HTTP10.equalsIgnoreCase(temp)) {
            http11 = false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.providers.UMOMessageAdapter#getPayload()
     */
    public Object getPayload() {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.providers.UMOMessageAdapter#getPayloadAsBytes()
     */
    public byte[] getPayloadAsBytes() throws Exception
    {
        if(message instanceof byte[]) {
            return (byte[]) message;
        } else if (message instanceof String) {
            return message.toString().getBytes();
        } else {
            return (byte[]) trans.transform(message);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.providers.UMOMessageAdapter#getPayloadAsString()
     */
    public String getPayloadAsString() throws Exception {
        if(message instanceof byte[]) {
            return new String((byte[])message);
        } else {
            return message.toString();
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see org.mule.providers.UMOMessageAdapter#getProperty(java.lang.Object)
    */
    public Object getProperty(Object key) {
        if (HttpConstants.HEADER_KEEP_ALIVE.equals(key) || HttpConstants.HEADER_CONNECTION.equals(key)) {
            if (!http11) {
                String connection = (String) super.getProperty(HttpConstants.HEADER_CONNECTION);
                if (connection != null && connection.equalsIgnoreCase("close")) {
                    return "false";
                } else {
                    return "true";
                }
            } else {
                return (super.getProperty(HttpConstants.HEADER_CONNECTION) != null ? "true" : "false");
            }
        } else {
            return super.getProperty(key);
        }
    }
}
