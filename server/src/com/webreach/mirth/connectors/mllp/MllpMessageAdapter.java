/*
 * $Header: /home/projects/mule/scm/mule/providers/tcp/src/java/org/mule/providers/tcp/TcpMessageAdapter.java,v 1.4 2005/06/03 01:20:35 gnt Exp $
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
package com.webreach.mirth.connectors.mllp;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

/**
 * <code>TcpMessageAdapter</code> TODO
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */

public class MllpMessageAdapter extends AbstractMessageAdapter
{
   //ast: an HL7 message is really a String
    private String message;

    public MllpMessageAdapter(Object message) throws MessagingException
    {
        if (message instanceof byte[]) {
            this.message = new String((byte[])message);
        }else if (message instanceof String){
            this.message = (String) message;
        }else{
            throw new MessageTypeNotSupportedException(message, getClass());
        }
    }

    public String getPayloadAsString() throws Exception
    {
        return message;
    }

    public byte[] getPayloadAsBytes() throws Exception
    {
         return message.getBytes();
    }
    
    //ast: select the encoding in which we whant the message
    public byte[] getPayloadAsBytes(String encoding) throws Exception
    {
        byte []r;
        try{
            r=message.getBytes(encoding);
        }catch(Exception e){
            logger.error("The charset "+encoding+" can't be used in this JVM " +e);
            r=message.getBytes();
        }
        return r;
    }
    
    public Object getPayload()
    {
        return message;
    }
}
