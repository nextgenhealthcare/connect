/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.uima;

import org.apache.log4j.Logger;

import com.mirth.connect.connectors.ConnectorService;

public class UimaConnectorService implements ConnectorService {
    private Logger logger = Logger.getLogger(this.getClass());
    
    public Object invoke(String method, Object object, String sessionId) throws Exception {
        logger.debug("invoking method: " + method);
        return null;
    }
}