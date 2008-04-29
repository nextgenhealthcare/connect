/* 
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/JmsMessageDispatcherFactory.java,v 1.5 2005/06/03 01:20:34 gnt Exp $
 * $Revision: 1.5 $
 * $Date: 2005/06/03 01:20:34 $
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
package com.webreach.mirth.connectors.jms;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */
public class JmsMessageDispatcherFactory implements UMOMessageDispatcherFactory
{
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException
    {
        return new JmsMessageDispatcher((JmsConnector) connector);
    }
}
