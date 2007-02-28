/*
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/jetty/JettyConnector.java,v 1.3 2005/11/01 17:43:30 rossmason Exp $
 * $Revision: 1.3 $
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
package org.mule.providers.http.jetty;

import org.mule.providers.AbstractServiceEnabledConnector;

/**
 * <code>ServletConnector</code> is a channel adapter between Mule and a
 * servlet engine.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.3 $
 */

public class JettyConnector extends AbstractServiceEnabledConnector
{
    public JettyConnector() {
        super();
        registerSupportedProtocol("http");
        registerSupportedProtocol("https");
        registerSupportedProtocol("rest");
    }

    public String getProtocol()
    {
        return "jetty";
    }

}
