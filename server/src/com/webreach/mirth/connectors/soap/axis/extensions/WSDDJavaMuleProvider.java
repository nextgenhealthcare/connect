/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/extensions/WSDDJavaMuleProvider.java,v 1.3 2005/06/03 01:20:35 gnt Exp $
 * $Revision: 1.3 $
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
package com.webreach.mirth.connectors.soap.axis.extensions;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.deployment.wsdd.WSDDConstants;
import org.apache.axis.deployment.wsdd.WSDDProvider;
import org.apache.axis.deployment.wsdd.WSDDService;

import com.webreach.mirth.connectors.soap.axis.AxisConnector;

/**
 * <code>WSDDJavaMuleProvider</code> is a factory class for creating the
 * MuleProvider
 * 
 * @see MuleProvider
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.3 $
 */
public class WSDDJavaMuleProvider extends WSDDProvider
{
    private AxisConnector connector;

    public WSDDJavaMuleProvider(AxisConnector connector)
    {
        this.connector = connector;
    }

    /**
     * Factory method for creating an <code>MuleProvider</code>.
     * 
     * @param wsddService a <code>WSDDService</code> value
     * @param engineConfiguration an <code>EngineConfiguration</code> value
     * @return a <code>Handler</code> value
     * @exception Exception if an error occurs
     */
    public org.apache.axis.Handler newProviderInstance(WSDDService wsddService, EngineConfiguration engineConfiguration)
            throws Exception
    {
        return new MuleProvider(connector);
    }

    /**
     * @return String
     * @see org.apache.axis.deployment.wsdd.WSDDProvider#getName()
     */
    public String getName()
    {
        return WSDDConstants.PROVIDER_RPC;
    }
}
