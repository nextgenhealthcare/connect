/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

public class MessagePrunerException extends Exception {
    public MessagePrunerException(String message) {
        super(message);
    }

    public MessagePrunerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagePrunerException(Throwable cause) {
        super(cause);
    }
}
