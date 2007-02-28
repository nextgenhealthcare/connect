/*
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/servlet/ServletMessageReceiver.java,v 1.1 2005/07/19 18:55:55 rossmason Exp $
 * $Revision: 1.1 $
 * $Date: 2005/07/19 18:55:55 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.http.servlet;

import org.mule.providers.AbstractMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

/**
 * <code>ServletMessageReceiver</code> is a receiver that is invoked from a
 * Servlet when an event is received.
 * 
 * There is a one-to-one mapping between a ServletMessageReceiver and a servlet
 * in the serving webapp.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.1 $
 */

public class ServletMessageReceiver extends AbstractMessageReceiver
{
    public ServletMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint)
            throws InitialisationException
    {
        super(connector, component, endpoint);
    }

    public void doConnect() throws Exception
    {

    }

    public void doDisconnect() throws Exception
    {

    }
}
