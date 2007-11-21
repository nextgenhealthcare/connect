/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/JdbcMessageAdapter.java,v 1.3 2005/06/03 01:20:34 gnt Exp $
 * $Revision: 1.3 $
 * $Date: 2005/06/03 01:20:34 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.js;

import org.mule.providers.AbstractMessageAdapter;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.3 $
 */
public class JavaScriptMessageAdapter extends AbstractMessageAdapter
{

    private String message;

    public JavaScriptMessageAdapter(Object obj)
    {
        this.message = (String) obj;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOMessageAdapter#getPayloadAsString()
     */
    public String getPayloadAsString() throws Exception
    {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOMessageAdapter#getPayloadAsBytes()
     */
    public byte[] getPayloadAsBytes() throws Exception
    {
        return message.getBytes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOMessageAdapter#getPayload()
     */
    public Object getPayload()
    {
        return message;
    }

}
