/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

public class DatabaseReceiverException extends Exception {
    public DatabaseReceiverException(String message) {
        super(message);
    }

    public DatabaseReceiverException(Throwable cause) {
        super(cause);
    }

    public DatabaseReceiverException(String message, Throwable cause) {
        super(message, cause);
    }
}
