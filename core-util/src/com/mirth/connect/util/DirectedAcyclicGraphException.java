/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

public class DirectedAcyclicGraphException extends Exception {

    public DirectedAcyclicGraphException() {}

    public DirectedAcyclicGraphException(String message, Throwable cause) {
        super(message, cause);
    }

    public DirectedAcyclicGraphException(String message) {
        super(message);
    }

    public DirectedAcyclicGraphException(Throwable cause) {
        super(cause);
    }
}