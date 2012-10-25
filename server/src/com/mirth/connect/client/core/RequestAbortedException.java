/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

public class RequestAbortedException extends Exception {
    public RequestAbortedException(Throwable cause) {
        super(cause);
    }
    
    public RequestAbortedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestAbortedException(String message) {
        super(message);
    }
}
