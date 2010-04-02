/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.email;

/**
 * Creates a Secure Imap connection
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.1 $
 */
public class ImapsConnector extends Pop3sConnector {

    public static final String DEFAULT_IMAPS_PORT = "993";

    public ImapsConnector() {
        setPort(DEFAULT_IMAPS_PORT);
    }

    public String getProtocol() {
        return "imaps";
    }
}
