/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/glue/GlueConnector.java,v 1.10 2005/11/01 17:43:30 rossmason Exp $
 * $Revision: 1.10 $
 * $Date: 2005/11/01 17:43:30 $
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

import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.provider.UMOMessageReceiver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <code>GlueConnector</code> instanciates a Glue soap server and allows beans
 * to be dynamically exposed a swebservices simply by registering with the
 * connector
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.10 $
 */

public class GlueConnector extends AbstractServiceEnabledConnector
{
    private List serverEndpoints = new ArrayList();
    private Map context;

    public GlueConnector() {
        super();
        registerSupportedProtocol("http");
    }

    public String getProtocol()
    {
        return "glue";
    }

    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception
    {
        boolean createServer = shouldCreateServer(endpoint.getEndpointURI().getAddress());

        UMOMessageReceiver receiver = serviceDescriptor.createMessageReceiver(this,
                                                                              component,
                                                                              endpoint,
                                                                              new Object[] { Boolean.valueOf(createServer) });

        if (createServer) {
            serverEndpoints.add(endpoint.getEndpointURI().getAddress());
        }
        return receiver;
    }

    protected Object getReceiverKey(UMOComponent component, UMOEndpoint endpoint)
    {
        return endpoint.getEndpointURI().getAddress() + "/" + component.getDescriptor().getName();
    }
    
    private boolean shouldCreateServer(String endpoint) throws URISyntaxException
    {
        URI uri = new URI(endpoint);
        String ep = uri.getScheme() + "://" + uri.getHost();
        if (uri.getPort() != -1)
            ep += ":" + uri.getPort();

        for (Iterator iterator = serverEndpoints.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            if (s.startsWith(ep)) {
                return false;
            }
        }
        return true;
    }

    public Map getContext()
    {
        return context;
    }

    public void setContext(Map context)
    {
        this.context = context;
    }

    public boolean supportsProtocol(String protocol) {
        return super.supportsProtocol(protocol) || protocol.toLowerCase().equals("glue:http");
    }
}
