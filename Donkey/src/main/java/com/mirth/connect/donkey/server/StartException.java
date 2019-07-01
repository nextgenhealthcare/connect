/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

public class StartException extends Exception {
    public StartException(String message) {
        super(message);
    }

    public StartException(Throwable cause) {
        super(cause);
    }

    public StartException(String message, Throwable cause) {
        super(message, cause);
    }
}
