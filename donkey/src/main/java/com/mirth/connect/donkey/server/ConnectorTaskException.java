/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

public class ConnectorTaskException extends Exception {
    public ConnectorTaskException(Throwable cause) {
        super(cause);
    }

    public ConnectorTaskException(String message) {
        super(message);
    }

    public ConnectorTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
