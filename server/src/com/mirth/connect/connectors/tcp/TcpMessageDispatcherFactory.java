/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

/**
 * <code>TcpMessageDispatcherFactory</code> TODO
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.3 $
 */

public class TcpMessageDispatcherFactory implements UMOMessageDispatcherFactory {
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException {
        return new TcpMessageDispatcher((TcpConnector) connector);
    }
}
