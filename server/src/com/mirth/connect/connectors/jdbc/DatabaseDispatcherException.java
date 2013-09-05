/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

public class DatabaseDispatcherException extends Exception {
    public DatabaseDispatcherException(String message) {
        super(message);
    }

    public DatabaseDispatcherException(Throwable cause) {
        super(cause);
    }

    public DatabaseDispatcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
