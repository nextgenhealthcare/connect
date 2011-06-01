/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

public class JavaScriptMessageDispatcherFactory implements UMOMessageDispatcherFactory {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mule.umo.provider.UMOMessageDispatcherFactory#create(org.mule.umo
     * .provider.UMOConnector)
     */
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException {
        return new JavaScriptMessageDispatcher((JavaScriptConnector) connector);
    }

}
