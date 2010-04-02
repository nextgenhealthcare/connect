/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.net.URI;
import java.util.Properties;

import org.mule.impl.endpoint.AbstractEndpointBuilder;
import org.mule.umo.endpoint.MalformedEndpointException;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.3 $
 */
public class JdbcEndpointBuilder extends AbstractEndpointBuilder {
    protected void setEndpoint(URI uri, Properties props) throws MalformedEndpointException {
        if (uri.getHost() != null && !"localhost".equals(uri.getHost())) {
            endpointName = uri.getHost();
        }
        int i = uri.getPath().indexOf("/", 1);
        if (i > 0) {
            endpointName = uri.getPath().substring(1, i);
            address = uri.getPath().substring(i + 1);
        } else if (uri.getPath() != null && uri.getPath().length() != 0) {
            address = uri.getPath().substring(1);
        } else {
            address = uri.getAuthority();
        }
    }
}
