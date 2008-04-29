/* 
 * $Header: /home/projects/mule/scm/mule/providers/ftp/src/java/org/mule/providers/ftp/FtpMessageAdapter.java,v 1.2 2005/06/03 01:20:33 gnt Exp $
 * $Revision: 1.2 $
 * $Date: 2005/06/03 01:20:33 $
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
package com.webreach.mirth.connectors.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public class FtpMessageAdapter extends AbstractMessageAdapter
{

    private byte[] message;

    public FtpMessageAdapter(Object message) throws MessagingException
    {
        if (message instanceof byte[])
            this.message = (byte[]) message;
        else if (message instanceof FTPFile)
        	this.message = ((FTPFile)message).getName().getBytes();
        
        else
            throw new MessageTypeNotSupportedException(message, getClass());
    }

    public String getPayloadAsString() throws Exception
    {
        return new String(message);
    }

    public byte[] getPayloadAsBytes() throws Exception
    {
        return message;
    }

    public Object getPayload()
    {
        return message;
    }

}
