/* 
 * $Header: /home/projects/mule/scm/mule/providers/ftp/src/java/org/mule/providers/ftp/FtpMessageDispatcherFactory.java,v 1.2 2005/06/03 01:20:33 gnt Exp $
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

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public class FtpMessageDispatcherFactory implements UMOMessageDispatcherFactory
{

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnectorSessionFactory#create(org.mule.umo.provider.UMOConnector)
     */
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException
    {
        return new FtpMessageDispatcher((FtpConnector) connector);
    }

}
