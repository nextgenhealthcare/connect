/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

/**
 * @author Ross Mason
 *         <p/>
 *         //TODO document
 */
public class SmtpMessageDispatcherFactory implements UMOMessageDispatcherFactory {
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mule.umo.provider.UMOConnectorSessionFactory#create(org.mule.umo.
     * provider.UMOConnector)
     */
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException {
        return new SmtpMessageDispatcher((SmtpConnector) connector);
    }
}
