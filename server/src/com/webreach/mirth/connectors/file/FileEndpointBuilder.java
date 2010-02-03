/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.webreach.mirth.connectors.file;

import java.net.URI;
import java.util.Properties;

import org.mule.impl.endpoint.AbstractEndpointBuilder;
import org.mule.umo.endpoint.MalformedEndpointException;

/**
 * <code>FileEndpointBuilder</code> File uris need some special processing
 * because the uri path can be any length, and the default resolver relies on a
 * particular path format
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */

public class FileEndpointBuilder extends AbstractEndpointBuilder {
    protected void setEndpoint(URI uri, Properties props) throws MalformedEndpointException {
        address = uri.getSchemeSpecificPart();
        if (address.startsWith("//")) {
            address = address.substring(2);
        }
        int i = address.indexOf("?");
        if (i > -1) {
            address = address.substring(0, i);
        }

    }
}
