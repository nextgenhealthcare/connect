/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

public class StopException extends Exception {
    public StopException(String message) {
        super(message);
    }

    public StopException(Throwable cause) {
        super(cause);
    }

    public StopException(String message, Throwable cause) {
        super(message, cause);
    }
}
